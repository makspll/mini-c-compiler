package co.uk.maksmozolewski.ast;

public interface Type extends ASTNode {

    public <T> T accept(ASTVisitor<T> v);
    
}
