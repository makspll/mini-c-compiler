package co.uk.maksmozolewski.sem;

import co.uk.maksmozolewski.ast.FunDecl;

public class FuncSymbol extends Symbol{
    public FunDecl fd;
    public FuncSymbol(FunDecl fd) {
        super(fd.name);
        this.fd = fd;
    }

    @Override
    public boolean isVar() {
        return false;
    }

    @Override
    public boolean isFunc() {
        return true;
    }

    @Override
    public boolean isStruct() {
        return false;
    }
    
}
