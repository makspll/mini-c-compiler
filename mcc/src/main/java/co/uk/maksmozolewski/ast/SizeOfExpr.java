package co.uk.maksmozolewski.ast;

public class SizeOfExpr extends Expr{


    public final Type val;

    public SizeOfExpr(Type val) {
        this.val = val;
    }
        
    @Override
    public <T> T accept(ASTVisitor<T> v) {
        
        return v.visitSizeOfExpr(this);
    }

    
}