package co.uk.maksmozolewski.ast;

public class VarDecl implements ASTNode {
    public final Type varType;
    public final String varName;

    public VarDecl(Type type, String varName) {
	    this.varType = type;
	    this.varName = varName;
    }

     public <T> T accept(ASTVisitor<T> v) {
	return v.visitVarDecl(this);
    }
}
