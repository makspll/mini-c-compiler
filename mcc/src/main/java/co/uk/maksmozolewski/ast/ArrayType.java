package co.uk.maksmozolewski.ast;

import co.uk.maksmozolewski.ast.Type;

public class ArrayType implements Type{

    public Type innerType;
    public int size;

    public ArrayType(Type innerType,int size){
        this.innerType = innerType;
        this.size = size;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitArrayType(this);
    }
    
}
