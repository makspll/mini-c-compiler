package co.uk.maksmozolewski.ast;

public class IntLiteral extends Expr{
    public int val;

    public IntLiteral(int val){
        this.val = val;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        // TODO Auto-generated method stub
        return v.visitIntLiteral(this);
    }
}
