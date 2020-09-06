package co.uk.maksmozolewski.ast;

import java.util.List;

public class FunDecl implements ASTNode {
    public final Type funType;
    public final String name;
    public final List<VarDecl> params;
    public final Block block;

    public FunDecl(Type type, String name, List<VarDecl> params, Block block) {
	    this.funType = type;
	    this.name = name;
	    this.params = params;
	    this.block = block;
    }

    public <T> T accept(ASTVisitor<T> v) {
	return v.visitFunDecl(this);
    }
}
