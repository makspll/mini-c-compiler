package co.uk.maksmozolewski.ast;

public class ValueAtExpr extends Expr{


    public final Expr ptr;

    public ValueAtExpr(Expr ptr) {
        this.ptr = ptr;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitValueAtExpr(this);
    }
    
}
