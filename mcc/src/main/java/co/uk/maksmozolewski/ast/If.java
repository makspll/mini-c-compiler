package co.uk.maksmozolewski.ast;

public class If extends Stmt{

    public final Expr condition;
    public final Stmt ifStmt;
    public final Stmt elseStmt;


    public If(Expr condition, Stmt ifStmt, Stmt elseStmt) {
        this.condition = condition;
        this.ifStmt = ifStmt;
        this.elseStmt = elseStmt;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitIf(this);
    }
    
}
