package co.uk.maksmozolewski.ast;

public class StrLiteral extends Expr{
    public String val;

    public StrLiteral(String val){
        this.val = val;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStrLiteral(this);
    }
}