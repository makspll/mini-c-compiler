package co.uk.maksmozolewski.ast;

public class ArrayAccessExpr extends Expr{

    final Expr array;
    final Expr idx;

    public ArrayAccessExpr(Expr array, Expr idx) {
        this.array = array;
        this.idx = idx;
    }

    
    @Override
    public <T> T accept(ASTVisitor<T> v) {
        // TODO Auto-generated method stub
        return v.visitArrayAccessExpr(this);
    }
    
}
