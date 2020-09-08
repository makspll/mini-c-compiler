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

    @Override
    public boolean isStructTypeType() {
        return false;
    }

    @Override
    public boolean isArrayType() {
        return true;
    }

    @Override
    public boolean isPointerType() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ArrayType)) {
            return false;
        }
        ArrayType arrayType = (ArrayType) o;
        return arrayType.innerType.equals(innerType) && arrayType.size == size;
    }

    @Override
    public int hashCode() {
        return innerType.hashCode() + (size * Integer.MAX_VALUE);
    }

    
}
