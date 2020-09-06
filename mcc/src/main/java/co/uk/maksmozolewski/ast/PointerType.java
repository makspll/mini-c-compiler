package co.uk.maksmozolewski.ast;

public class PointerType implements Type{

    Type pointedToType;
    
    public PointerType(Type pointedToType){
        this.pointedToType = pointedToType;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitPointerType(this);
    }
    
}

