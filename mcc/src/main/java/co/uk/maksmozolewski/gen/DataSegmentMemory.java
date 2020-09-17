package co.uk.maksmozolewski.gen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import co.uk.maksmozolewski.ast.Expr;
import co.uk.maksmozolewski.ast.FunDecl;
import co.uk.maksmozolewski.ast.StrLiteral;
import co.uk.maksmozolewski.ast.VarDecl;
import co.uk.maksmozolewski.ast.VarExpr;
import co.uk.maksmozolewski.gen.MipsWriter.Directive;
import co.uk.maksmozolewski.gen.MipsWriter.WriteTarget;

public class DataSegmentMemory extends BaseMemory {
    MipsWriter writer;
    RegisterAllocator registerAllocator;


    public DataSegmentMemory(MipsWriter w, RegisterAllocator r) {
        writer = w;
        registerAllocator = r;

    }

    @Override
    public void putStringConstant(StrLiteral var) {
        String label = storeOrGetStrLitIdentifier(var);

        writer.writeLabel(WriteTarget.DATA, label);
        writer.writeDirective(WriteTarget.DATA, Directive.ASCIIZ);
        writer.writeQuotedString(WriteTarget.DATA, var.val);

    }

    @Override
    public Register retrieveStringConstant(StrLiteral var) {
        String label = storeOrGetStrLitIdentifier(var);

        Register address = registerAllocator.getRegister();
        writer.writeLa(address, label, "load str literal location: " + label);
        return address;
    }

    @Override
    public void putVariable(StorageDirectory d, VarDecl var, Register value, Register wordOffset) {
        if (d == StorageDirectory.STACK)
            throw new InvalidMemoryDeclarationError();
        if (!labelsVars.containsKey(var))
            throw new MemoryNotDeclaredError();

        String label = labelsVars.get(var);

        // calculate wordOffset address
        Register addressOfVar = registerAllocator.getRegister();
        writer.writeLa(addressOfVar, label, "load variable address: " + label);

        if (wordOffset != null) {
            writer.writeSra(wordOffset, wordOffset, 2, "multiply offset by 4");
            writer.writeAdd(addressOfVar, addressOfVar, wordOffset, "add offset to calculate word aligned address");
        }

        writer.writeSw(value, addressOfVar, 0, "store value in variable at offset");
        registerAllocator.freeRegister(addressOfVar);
    }

    @Override
    public void declareVariable(StorageDirectory d, VarDecl var, int words) {
        if (d == StorageDirectory.STACK)
            throw new IllegalArgumentException();

        if (labelsVars.containsKey(var))
            throw new MemoryAlreadyDeclaredError();

        // generate unique label for every unique var decl
        String label = storeOrGetVarLabel(var);

        // write declaration
        writer.writeLabel(WriteTarget.DATA, label);
        writer.writeDirective(WriteTarget.DATA, Directive.WORD);
        writer.writeInt(WriteTarget.DATA, words);
    }

    @Override
    public int getStackWordSizeSoFar() {
        // we don't use the stack here
        return 0;
    }

    @Override
    public Register retrieveVariableAddress(VarDecl var, Register offset) {
        if (!labelsVars.containsKey(var))
            throw new MemoryNotDeclaredError();

        writer.writeNewline(WriteTarget.TEXT);
        
        Register addressRegister = registerAllocator.getRegister();

        String label = storeOrGetVarLabel(var);

        writer.writeLa(addressRegister, label, " load variable address: " + label);
        
        if(offset != null){
            writer.writeSra(offset, offset, 2, "multiply offset by 4 to get word offset");
            writer.writeAdd(addressRegister, addressRegister, offset, "calculate offset address");
            registerAllocator.freeRegister(offset);
        }

        writer.writeNewline(WriteTarget.TEXT);

        return addressRegister;
    }

    @Override
    public boolean isGlobalMemory() {
        return true;
    }

    /** dumps register value on the stack or updates it */
    @Override
    public void putRegister(Register dumped, String registerName) {
        throw new InvalidMemoryAccess();
    }

    /** recovers register value from the stack */
    @Override
    public void retrieveRegister(String dumped,Register targetRegister) {
        throw new MemoryNotDeclaredError();
    }

    @Override
    public Register retrieveFunctionArgumentAddress(FunDecl d, VarDecl arg) {
        // stack doesnt exist here
        throw new MemoryNotDeclaredError();
    }

    @Override
    public boolean containsVariable(VarDecl var) {
        return labelsVars.containsKey(var);
    }

    @Override
    public void expandStack(int words) {
        throw new InvalidMemoryAccess();

    }

    @Override
    public void shrinkStack(int words) {
        throw new InvalidMemoryAccess();

    }
}
