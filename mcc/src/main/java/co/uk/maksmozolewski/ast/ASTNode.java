package co.uk.maksmozolewski.ast;

public interface ASTNode {
    public <T> T accept(ASTVisitor<T> v);
}
