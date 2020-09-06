package co.uk.maksmozolewski.ast;

public class BinOp extends Expr{

    Expr lhs;
    Op op;
    Expr rhs;

    public BinOp(Expr lhs, Op op, Expr rhs){
        this.lhs = lhs;
        this.op = op;
        this.rhs = rhs;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitBinOp(this);
    }
    
}
