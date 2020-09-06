package co.uk.maksmozolewski.ast;

public class TypecastExpr extends Expr{

    public final Type newType;
    public final Expr castedExpr;


    public TypecastExpr(Type newType, Expr castedExpr) {
        this.newType = newType;
        this.castedExpr = castedExpr;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitTypecastExpr(this);
    }
    
}
