package co.uk.maksmozolewski.ast;

public class PointerType implements Type{

    public Type pointedToType;
    
    public PointerType(Type pointedToType){
        this.pointedToType = pointedToType;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitPointerType(this);
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
        return true;
    }
    

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof PointerType)) {
            return false;
        }
        PointerType pointerType = (PointerType) o;

        return pointerType.pointedToType.equals(pointedToType);
    }

    @Override
    public int hashCode() {
        return pointedToType.hashCode();
    }


}

