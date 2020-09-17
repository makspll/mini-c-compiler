package co.uk.maksmozolewski.ast;

public class Return extends Stmt{

    public FunDecl fd; // to be filled in by name analyser
    public final Expr exp;


    public Return(Expr stmt) {
        this.exp = stmt;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitReturn(this);
    }
    
}
