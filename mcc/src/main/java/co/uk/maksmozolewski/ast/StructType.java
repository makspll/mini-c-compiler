package co.uk.maksmozolewski.ast;

public class StructType implements Type{

    public String structTypeIdentifier;
    public StructTypeDecl dec; // to be filled in by the name analyser

    public StructType(String structType){
        this.structTypeIdentifier = structType;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStructType(this);
    }

    @Override
    public boolean isStructTypeType() {
        return true;
    }

    @Override
    public boolean isArrayType() {
        return false;
    }

    @Override
    public boolean isPointerType() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof StructType)) {
            return false;
        }
        StructType structType = (StructType) o;
        return structType.structTypeIdentifier.equals(structTypeIdentifier);
    }

    @Override
    public int hashCode() {
        return structTypeIdentifier.hashCode() + (dec.hashCode() * Integer.MAX_VALUE);
    }

    @Override
    public int sizeOfType() {
        // we treat each field as a word size apart from arrays, which we pad (but ArrayType.sizeof() will give you the padded size anyway)
        int size = 0;
        for (VarDecl var : dec.varDecls) {
            size += var.varType.sizeOfType();
        }
        return size;
    }

    


}
