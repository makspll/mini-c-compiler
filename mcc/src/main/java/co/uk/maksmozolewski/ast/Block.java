package co.uk.maksmozolewski.ast;

import java.util.List;

public class Block extends Stmt {

    public final List<VarDecl> varDecls;
    public final List<Stmt> stmnts;

    public Block(List<VarDecl> varDecls, List<Stmt> stmnts) {
        this.varDecls = varDecls;
        this.stmnts = stmnts;
    }

    public <T> T accept(ASTVisitor<T> v) {
	    return v.visitBlock(this);
    }
}
