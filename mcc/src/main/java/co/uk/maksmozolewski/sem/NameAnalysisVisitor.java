package co.uk.maksmozolewski.sem;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import co.uk.maksmozolewski.Main;
import co.uk.maksmozolewski.ast.*;

public class NameAnalysisVisitor extends BaseSemanticVisitor<Void> {
	int errCount;
	Scope currentScope;

	@Override
	public Void visitBaseType(BaseType bt) {

		return null;
	}


	/**
	 * creates new scope from current scope, sets current scope to it and after executing given function sets the scope back to the original
	 * @param func
	 * @return the new scope
	 */
	public Scope executeInNewScope(Runnable func){
		Scope oldScope = currentScope;
		Scope newScope = new BlockScope(oldScope);
		currentScope = newScope;
		func.run();
		currentScope = oldScope;
		return newScope;
	}

	


	@Override
	public Void visitProgram(Program p) {
		GlobalScope gs = new GlobalScope();
		currentScope = gs;



        // supply library functions:
        // void print_s(char* s);
        // void print_i(int i);
        // void print_c(char c);
        // char read_c();
        // int read_i();
        // void* mcmalloc(int size);
    
		visitAll(Main.stlib);
		currentScope = new FileScope(gs);

	
		visitAll(p.structTypeDecls,p.varDecls,p.funDecls);
		return null;
	}
	@Override
	public Void visitStructTypeDecl(StructTypeDecl sts) {
		// check struct identifier is unique in the block
		// we still stay in the global scope untill we reach the structs varDecls

		Symbol s = currentScope.lookup(sts.structType.structTypeIdentifier);
		if(s == null){
			// what we expect
			currentScope.put(new StructTypeSymbol(sts));
		} else {
			// check that it doesn't exist in the current scope
			Symbol s2 = currentScope.lookupCurrent(sts.structType.structTypeIdentifier);
			if(s2 == null){
				// good to go with shadowing
				currentScope.put(new StructTypeSymbol(sts));
			} else {
				error("Cannot declare struct type, Identifier " + sts.structType.structTypeIdentifier + " is already defined.");
			}
		} 

		executeInNewScope(()->{
			visitAll(sts.varDecls);
		});
		
		return null;
	}

	@Override
	public Void visitFunDecl(FunDecl p) {

		// check func is not declared already
		Symbol s = currentScope.lookup(p.name);
		if(s == null){
			currentScope.put(new FuncSymbol(p));
		} else {
			// check that it doesn't exist in the current scope
			Symbol s2 = currentScope.lookupCurrent(p.name);
			if(s2 == null){
				// good to go with shadowing
				currentScope.put(new FuncSymbol(p));
			} else {
				error("Cannot declare function, Identifier " + p.name + " is already defined.");
			}
		}

		executeInNewScope(()->{
			p.funType.accept(this);
			visitAll(p.params);
			p.block.accept(this);
		});

		// we look for return statement and assign its fd
		// that's if one exists
		//TODO: make sure we recurse down the tree to find all the returns!
		for (Stmt stmt : p.block.stmnts) {
			if(stmt instanceof Return){
				((Return)stmt).fd = p;
			}
		}


		return null;
	}

	@Override
	public Void visitVarDecl(VarDecl vd) {
		// check var is not declared already
		// possibly shadow that declaration if it's in another scope

		// first check all good is with the type (say it's a struct, its type needs to have been declared)
		vd.varType.accept(this);

		Symbol s = currentScope.lookup(vd.varName);
		if(s == null){
			currentScope.put(new VarSymbol(vd));
		}else {

			// check that it doesn't exist in the current scope
			Symbol s2 = currentScope.lookupCurrent(vd.varName);
			if(s2 == null){
				// good to go with shadowing
				currentScope.put(new VarSymbol(vd));
			} else {
				error("Cannot declare variable, Identifier " + vd.varName + " is already defined.");
			}
		}

		return null;
	}

	@Override
	public Void visitVarExpr(VarExpr v) {
		// check the var is declared, and that the declared symbol is a variable
		Symbol s = currentScope.lookup(v.name);
		if(s == null){
			error("Variable was not declared: " + v.name);
		} else if (!(s.isVar())){
			error("Variable declaration not found: " + v.name + ".");
		} else {
			// set the declaration
			v.vd = ((VarSymbol)s).vd;
		}

		return null;
	}

	@Override
	public Void visitBlock(Block b) {

		visitAll(b.varDecls,b.stmnts);

		return null;
	}

	@Override
	public Void visitPointerType(PointerType pt) {
		pt.pointedToType.accept(this);
		return null;
	}

	@Override
	public Void visitStructType(StructType st) {
		// check struct type declaration exists;
		Symbol s = currentScope.lookup(st.structTypeIdentifier);
		if(s == null){
			error("Struct type was not declared: " + s.name + ".");
		} else if  (!s.isStruct()){
			error("Struct type was not declared: " + s.name + ", The same identifier is used in another variable or function declaration.");
		} else {
			st.dec = ((StructTypeSymbol)s).std;
		}
		
		return null;
	}

	@Override
	public Void visitArrayType(ArrayType at) {
		at.innerType.accept(this);
		return null;
	}

	@Override
	public Void visitIntLiteral(IntLiteral il) {
		return null;
	}

	@Override
	public Void visitStrLiteral(StrLiteral sl) {
		return null;
	}

	@Override
	public Void visitChrLiteral(ChrLiteral cl) {
		return null;
	}

	@Override
	public Void visitFunCallExpr(FunCallExpr fce) {
		// check function is declared
		Symbol s = currentScope.lookup(fce.funName);
		if(s == null){
			error("Function was not declared: " + fce.funName + ".");
		} else if (!s.isFunc()){
			error("Function was not declared: " + fce.funName + ", Another Struct or Variable declared with the same identifier.");
		} else {
			fce.funDecl = ((FuncSymbol)s).fd;
		}
		
		executeInNewScope(()->{
			visitAll(fce.args);
		});

		return null;
	}

	@Override
	public Void visitBinOp(BinOp bo) {
		bo.lhs.accept(this);
		bo.op.accept(this);
		bo.rhs.accept(this);

		return null;
	}

	@Override
	public Void visitOp(Op o) {
		return null;
	}

	@Override
	public Void visitArrayAccessExpr(ArrayAccessExpr aae) {
		aae.array.accept(this);
		aae.idx.accept(this);
		return null;
	}

	@Override
	public Void visitFieldAccessExpr(FieldAccessExpr fae) {
		fae.structure.accept(this);
		return null;
	}

	@Override
	public Void visitValueAtExpr(ValueAtExpr vae) {
		vae.ptr.accept(this);
		return null;
	}

	@Override
	public Void visitSizeOfExpr(SizeOfExpr sizeOfExpr) {
		sizeOfExpr.val.accept(this);
		return null;
	}

	@Override
	public Void visitTypecastExpr(TypecastExpr typecastExpr) {
		typecastExpr.newType.accept(this);
		typecastExpr.castedExpr.accept(this);
		return null;
	}

	@Override
	public Void visitExprStmt(ExprStmt exprStmt) {
		exprStmt.expr.accept(this);
		return null;
	}

	@Override
	public Void visitWhile(While w) {
		// condition is in the outer scope
		w.condition.accept(this);
		// stmt is in inner scope
		executeInNewScope(()->{
			w.stmt.accept(this);
		});

		return null;
	}

	@Override
	public Void visitIf(If i) {
		
		// condition is in outer scope
		i.condition.accept(this);
		// rest is in brace scopes
		executeInNewScope(()->{
			i.ifStmt.accept(this);

		});

		// else
		if(i.elseStmt != null)
			executeInNewScope(()->{
				i.elseStmt.accept(this);
			});

		return null;
	}

	@Override
	public Void visitAssign(Assign a) {
		a.lhs.accept(this);
		a.rhs.accept(this);
		return null;
	}

	@Override
	public Void visitReturn(Return r) {
		if(r.exp != null)
			r.exp.accept(this);
		return null;
	}


}
