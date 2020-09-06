package co.uk.maksmozolewski.sem;

import java.util.List;

import co.uk.maksmozolewski.ast.*;

public class NameAnalysisVisitor extends BaseSemanticVisitor<Void> {
	int errCount;
	Scope currentScope;

	public void visitAll(List<? extends ASTNode>...subtrees){
		for (List<? extends ASTNode> list : subtrees) {
			for (ASTNode node : list) {
				node.accept(this);
			}
		}
	}

	public void Error(String msg){
		System.out.println("Name Analysis Error:" + msg);
		errCount++;
	}

	@Override
	public Void visitBaseType(BaseType bt) {
		// To be completed...
		return null;
	}

	@Override
	public Void visitStructTypeDecl(StructTypeDecl sts) {
		// To be completed...
		return null;
	}

	@Override
	public Void visitBlock(Block b) {
		// To be completed...
		return null;
	}

	@Override
	public Void visitFunDecl(FunDecl p) {
		// To be completed...
		return null;
	}


	@Override
	public Void visitProgram(Program p) {
		currentScope = new FileScope();
		visitAll(p.structTypeDecls,p.varDecls,p.funDecls);
		return null;
	}

	@Override
	public Void visitVarDecl(VarDecl vd) {
		// To be completed...
		return null;
	}

	@Override
	public Void visitVarExpr(VarExpr v) {
		// To be completed...
		return null;
	}

	@Override
	public Void visitPointerType(PointerType pt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitStructType(StructType st) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitArrayType(ArrayType at) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitIntLiteral(IntLiteral il) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitStrLiteral(StrLiteral sl) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitChrLiteral(ChrLiteral cl) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitFunCallExpr(FunCallExpr fce) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitBinOp(BinOp bo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitOp(Op o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitArrayAccessExpr(ArrayAccessExpr aae) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitFieldAccessExpr(FieldAccessExpr fae) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitValueAtExpr(ValueAtExpr vae) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitSizeOfExpr(SizeOfExpr sizeOfExpr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitTypecastExpr(TypecastExpr typecastExpr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitExprStmt(ExprStmt exprStmt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitWhile(While w) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitIf(If i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitAssign(Assign a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitReturn(Return r) {
		// TODO Auto-generated method stub
		return null;
	}


}
