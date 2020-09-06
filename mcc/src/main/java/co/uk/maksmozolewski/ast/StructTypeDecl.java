package co.uk.maksmozolewski.ast;

import java.util.List;

public class StructTypeDecl implements ASTNode {

    /** the name of the type of struct declared */
    String structType;

    /** the variables declared within the struct  */
    List<VarDecl> varDecls;

    public StructTypeDecl(String structType, List<VarDecl> varDecls){
        this.structType = structType;
        this.varDecls = varDecls;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStructTypeDecl(this);
    }

}
