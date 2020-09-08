package co.uk.maksmozolewski.sem;

import co.uk.maksmozolewski.ast.StructTypeDecl;

public class StructTypeSymbol extends Symbol{
    public StructTypeDecl std;

    public StructTypeSymbol(StructTypeDecl dtd) {
        super(dtd.structType.structTypeIdentifier);
        std = dtd;
    }

    @Override
    public boolean isVar() {
        return false;
    }

    @Override
    public boolean isFunc() {
        return false;
    }

    @Override
    public boolean isStruct() {
        return true;
    }
    
}
