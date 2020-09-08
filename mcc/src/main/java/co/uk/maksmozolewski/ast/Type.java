package co.uk.maksmozolewski.ast;

public interface Type extends ASTNode {

    public <T> T accept(ASTVisitor<T> v);
    public boolean isStructTypeType();
    public boolean isArrayType();
	public boolean isPointerType();
}
