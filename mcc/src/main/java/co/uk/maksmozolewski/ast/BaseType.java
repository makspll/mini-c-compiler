package co.uk.maksmozolewski.ast;

public enum BaseType implements Type {
    INT, CHAR, VOID;
    
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitBaseType(this);
    }

    @Override
    public boolean isStructTypeType() {
        return false;
    }

    @Override
    public boolean isArrayType() {
        return false;
    }

    @Override
    public boolean isPointerType() {
        return false;
    }

    public int sizeOfType(){
        if (this == BaseType.CHAR){
            return 1;
        } else {
            // void size of is undefined behaviour so let's just give it a nice wee 4
            return 4;
        }
    }


}
