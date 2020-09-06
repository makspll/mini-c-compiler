package co.uk.maksmozolewski.ast;

public class ChrLiteral extends Expr{
    char val;

    public ChrLiteral(char val){
        this.val = val;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitChrLiteral(this);
    }
}