package co.uk.maksmozolewski.gen;

import co.uk.maksmozolewski.ast.FunDecl;
import co.uk.maksmozolewski.ast.StrLiteral;
import co.uk.maksmozolewski.ast.VarDecl;
import co.uk.maksmozolewski.ast.VarExpr;

public interface Memory {
    void putStringConstant(StrLiteral var);
    Register retrieveStringConstant(StrLiteral var);

    void declareVariable(StorageDirectory d, VarDecl var, int words);
    void putVariable(StorageDirectory d, VarDecl var,Register value, Register wordOffset);
    Register retrieveVariableAddress(VarDecl var,Register offsetWords);
    boolean containsVariable(VarDecl var);

    void putRegister(Register dumped, String name);
    void retrieveRegister(String name, Register retrieveAddress);

    /** retrieves the function argument from above the frame pointer, to be used from a function declaration */
    Register retrieveFunctionArgumentAddress(FunDecl d, VarDecl arg);
    /** returns the size of the stack above and including the memory */
    int getStackWordSizeSoFar();
    void expandStack(int words);
    void shrinkStack(int words);

    boolean isGlobalMemory();
}