package co.uk.maksmozolewski.sem;

import co.uk.maksmozolewski.ast.*;

public class TypeCheckVisitor extends BaseSemanticVisitor<Type> {



	@Override
	public Type visitProgram(Program p) {
		visitAll(p.structTypeDecls,p.varDecls,p.funDecls);

		return null;
	}

	@Override
	public Type visitStructTypeDecl(StructTypeDecl st) {
		st.structType.accept(this);
		visitAll(st.varDecls);
		return null;
	}

	@Override
	public Type visitVarDecl(VarDecl vd) {
		if(vd.varType == BaseType.VOID){
			error("Variable cannot be declared as VOID: void "+ vd.varName + ".");
		}

		return null;
	}

	@Override
	public Type visitFunDecl(FunDecl p) {
		p.funType.accept(this);
		visitAll(p.params);
		p.block.accept(this);
		return null;
	}

	@Override
	public Type visitBaseType(BaseType bt) {
		return bt;
	}

	@Override
	public Type visitBlock(Block b) {
		visitAll(b.stmnts,b.varDecls);
		return null;
	}


	@Override
	public Type visitVarExpr(VarExpr v) {
		assert v.vd != null; 
		// the variable is the same type as its declaration 
		return v.type = v.vd.varType;
	}

	@Override
	public Type visitPointerType(PointerType pt) {
		pt.pointedToType.accept(this);
		return null;
	}

	@Override
	public Type visitStructType(StructType st) {
		return null;
	}

	@Override
	public Type visitArrayType(ArrayType at) {
		at.innerType.accept(this);
		return null;
	}

	@Override
	public Type visitIntLiteral(IntLiteral il) {
		il.type = BaseType.INT;
		return il.type;
	}

	@Override
	public Type visitStrLiteral(StrLiteral sl) {
		// strings are char arrays with an implicit null terminator at the end
		return sl.type = new ArrayType(BaseType.CHAR,sl.val.length() + 1);
	}

	@Override
	public Type visitChrLiteral(ChrLiteral cl) {
		return cl.type = BaseType.CHAR;
	}

	@Override
	public Type visitFunCallExpr(FunCallExpr fce) {
		assert fce.funDecl != null;

		// check the function call arguments match the function definitions
		if(fce.args.size() != fce.funDecl.params.size()){
			error("Function: " + fce.funName + " requires " + fce.funDecl.params.size() + " parameters, but was called with " + fce.args.size());
			// cannot keep checking
			return null;
		}

		boolean matchedAllArgs = true;
		for (int i = 0; i < fce.args.size(); i++) {
			Expr arg = fce.args.get(i);

			Type argType = arg.accept(this);
			Type argDeclType = fce.funDecl.params.get(i).accept(this);

			if(!argType.equals(argDeclType)){
				error("Type mismatch, argument no." + i + "is of type " + argType + " and needs to be of type " + argDeclType);
				// keep going to check all parameters
				matchedAllArgs = false;
			}
		}

		if(matchedAllArgs){
			return fce.type = fce.funDecl.funType;
		} else {
			return null;
		}

	}

	@Override
	public Type visitBinOp(BinOp bo) {
		// for most operators the two operands just need to be the same
		switch (bo.op){
			case ADD:
			case AND:
			case DIV:
			case OR:
			case SUB:
			case GE:
			case GT:
			case LE:
			case LT:
			case MOD:
			case MUL:
				// check the two operands have the same type
				Type lhs = bo.lhs.accept(this);
				Type rhs = bo.rhs.accept(this);
				if(!lhs.equals(rhs)){
					error("Type mismatch in binary operator: "+ bo.op + ". The left hand side was of type:" +lhs + ", while the right hand side was of type: " + rhs);
					return null;
				} else {
					return bo.type = lhs; // either rhs or lhs doesn't matter
				}

			case NE:
			case EQ:
				// just check that neither of the operands are struct type/array type or void
				lhs = bo.lhs.accept(this);
				rhs = bo.rhs.accept(this);
				if((lhs.isArrayType() || lhs.isStructTypeType() || lhs == BaseType.VOID) || 
					(rhs.isArrayType() || rhs.isStructTypeType() || rhs ==BaseType.VOID))
				{
					error("Type mismatch, the operator: "  +  bo.op + " cannot be performed on the types:" + lhs + " and " + rhs);
					return null;
				} else {
					// result is an int (1 or 0 represent true or false)
					return bo.type = BaseType.INT;
				}

			default:
				// shouldn't happen
				assert false;
				return null;
		}
	}

	@Override
	public Type visitOp(Op o) {
		return null;
	}

	@Override
	public Type visitArrayAccessExpr(ArrayAccessExpr aae) {
		// expr needs to be an array or pointer and the idx needs to be an int
		Type accessed = aae.array.accept(this);
		if(accessed.isArrayType() || accessed.isPointerType()){
			Type idx = aae.idx.accept(this);

			if(idx != BaseType.INT){
				error("Type mismatch, the index of an array needs to be an integer," + aae.idx);
				return null;
			}

			if(accessed.isArrayType()){
				return aae.type = ((ArrayType)accessed).innerType;
			} else{
				return aae.type = ((PointerType)accessed).pointedToType;
			}
			
		} else {
			error("Type mismatch," + aae.array + " needs to be an array or a pointer");
			return null;
		}
	}

	@Override
	public Type visitFieldAccessExpr(FieldAccessExpr fae) {
		// left hand side needs to be a struct reference
		Type lhs = fae.structure.accept(this);
		if(lhs.isStructTypeType()){
			// the struct needs to have the accessed field
			StructTypeDecl sd  = ((StructType)lhs).dec;
			for (VarDecl vd : sd.varDecls) {
				if(vd.varName.equals(fae.fieldName)){
					return fae.type = vd.varType;
				}
			}
			error("Struct:" + sd.structType.structTypeIdentifier + " does not contain a definition for the field: " + fae.fieldName);
			return null;
		}else {
			error("Type mismatch, expected a struct type but got " + lhs + ", as the lhs to the field access expression");
			return null;
		}

	}

	@Override
	public Type visitValueAtExpr(ValueAtExpr vae) {
		Type ptr = vae.ptr.accept(this);
		if(ptr.isPointerType()){
			return vae.type = ((PointerType)ptr).pointedToType;
		} else {
			error("Type mismatch, expected a pointer type but got: " + ptr + " as the lhs to the value at expression");
			return null;
		}
	}

	@Override
	public Type visitSizeOfExpr(SizeOfExpr sizeOfExpr) {
		sizeOfExpr.val.accept(this);
		return sizeOfExpr.type = BaseType.INT;
	}

	@Override
	public Type visitTypecastExpr(TypecastExpr typecastExpr) {

		typecastExpr.newType.accept(this);
		
		// only certain type changes are allowed
		// char to int

		Type expType = typecastExpr.castedExpr.accept(this);
		if(expType == BaseType.CHAR && typecastExpr.newType == BaseType.INT){
			return typecastExpr.type = typecastExpr.newType;
		// array to ptr1
		} else if (expType.isArrayType() && typecastExpr.newType.isPointerType()) {
			return typecastExpr.type = new PointerType(((ArrayType)expType).innerType);
		// ptr to ptr
		} else if (expType.isPointerType() && typecastExpr.newType.isPointerType()){
			return typecastExpr.type = new PointerType(((PointerType)typecastExpr.newType).pointedToType);
		} else if (expType.equals(typecastExpr.newType)){
			return expType;
		} else {
			error("Illegal typecast from: " + expType + " to " + typecastExpr.newType);
			return null;
		}
	}

	@Override
	public Type visitExprStmt(ExprStmt exprStmt) {
		exprStmt.expr.accept(this);
		return null;
	}

	@Override
	public Type visitWhile(While w) {
		// check condition is an integer
		Type condition = w.condition.accept(this);
		if(condition != BaseType.INT){
			error("Expected integer type in the condition of while loop but got: " + condition);
			return null;
		}

		w.stmt.accept(this);
		return null;
	}

	@Override
	public Type visitIf(If i) {
		// check condition is an integer
		Type condition = i.condition.accept(this);
		if(condition != BaseType.INT){
			error("Expected integer type in the condition of if but got: " + condition);
			return null;
		}

		i.ifStmt.accept(this);
		if(i.elseStmt != null){
			i.elseStmt.accept(this);
		}

		return null;	
	}

	@Override
	public Type visitAssign(Assign a) {
		// check lhs is not void or an array
		Type lhs = a.lhs.accept(this);
		Type rhs = a.rhs.accept(this);

		// null means an error below, pass it on
		if(lhs == null || rhs == null) return null;

		if(!(a.lhs instanceof VarExpr || 
			a.lhs instanceof ValueAtExpr || 
			a.lhs instanceof FieldAccessExpr || 
			a.lhs instanceof ArrayAccessExpr)){
			error("Type mismatch, left hand side of assignment has to be a variable, struct field, array element or pointer, but is:" + lhs);
		}

		if(lhs != BaseType.VOID && !rhs.isArrayType()){
			if(!lhs.equals(rhs)){
				error("Type mismatch, left hand side of assignment:"+ lhs + "doesn't match the right side:" + rhs);
			}
			return null;
		} else {
			error("Type mismatch, left hand side of an assignment cannot be of type: " + lhs);
			return null;
		}
	}

	@Override
	public Type visitReturn(Return r) {
		// check the function return type matches the function type
		Type returnType = r.stmt == null ? BaseType.VOID :r.stmt.accept(this);
		if(r.fd.funType != returnType){
			// this is different to C but cw asks for it
			error("Type mismatch, return type:" + returnType + " does not match the function return type");
		}
		
		return null;
	}



}
