package co.uk.maksmozolewski.ast;

public class Return extends Stmt{

    public FunDecl fd; // to be filled in by name analyser
    public final Expr stmt;


    public Return(Expr stmt) {
        this.stmt = stmt;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitReturn(this);
    }
    
}
