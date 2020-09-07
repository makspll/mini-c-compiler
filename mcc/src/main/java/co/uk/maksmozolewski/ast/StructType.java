package co.uk.maksmozolewski.ast;

public class StructType implements Type{

    public String structType;
    
    public StructType(String structType){
        this.structType = structType;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStructType(this);
    }

}
