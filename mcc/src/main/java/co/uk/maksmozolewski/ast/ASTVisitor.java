package co.uk.maksmozolewski.ast;


public interface ASTVisitor<T> {
    // types
    public T visitBaseType(BaseType bt);
    public T visitPointerType(PointerType pt);
    public T visitStructType(StructType st);
    public T visitArrayType(ArrayType at);

    // decls
    public T visitStructTypeDecl(StructTypeDecl st);
    public T visitFunDecl(FunDecl p);
    public T visitVarDecl(VarDecl vd);
    
    // structures
    public T visitBlock(Block b);
    public T visitProgram(Program p);

    // Expression
    public T visitVarExpr(VarExpr v);
    public T visitIntLiteral(IntLiteral il);
    public T visitStrLiteral(StrLiteral sl);
    public T visitChrLiteral(ChrLiteral cl);
    public T visitFunCallExpr(FunCallExpr fce);
    public T visitBinOp(BinOp bo);
    public T visitOp(Op o);
    public T visitArrayAccessExpr(ArrayAccessExpr aae);
    public T visitFieldAccessExpr(FieldAccessExpr fae);
	public T visitValueAtExpr(ValueAtExpr vae);
	public T visitSizeOfExpr(SizeOfExpr sizeOfExpr);
    public T visitTypecastExpr(TypecastExpr typecastExpr);
    
    // Stmts
	public T visitExprStmt(ExprStmt exprStmt);
	public T visitWhile(While w);
	public T visitIf(If i);
	public T visitAssign(Assign a);
	public T visitReturn(Return r);
}
