package co.uk.maksmozolewski.ast;

import co.uk.maksmozolewski.ast.ASTNode;

public enum Op implements ASTNode{
    ADD,SUB,MUL,DIV,MOD,GT,LT,GE,LE,NE,EQ,OR,AND;

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitOp(this);
    }

}
