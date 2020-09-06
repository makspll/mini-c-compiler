package co.uk.maksmozolewski.ast;

public class Assign extends Stmt{

    public final Expr lhs;
    public final Expr rhs;

    public Assign(Expr lhs, Expr rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitAssign(this);
    }
    
}
