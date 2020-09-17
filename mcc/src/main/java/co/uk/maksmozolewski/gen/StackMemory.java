package co.uk.maksmozolewski.gen;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import co.uk.maksmozolewski.ast.FunDecl;
import co.uk.maksmozolewski.ast.StrLiteral;
import co.uk.maksmozolewski.ast.VarDecl;
import co.uk.maksmozolewski.ast.VarExpr;
import co.uk.maksmozolewski.gen.Memory;
import co.uk.maksmozolewski.gen.MipsWriter.WriteTarget;

public class StackMemory extends BaseMemory {

    private Memory outerMemory;
    private MipsWriter writer;
    private RegisterAllocator registerAllocator;

    private Map<VarDecl, Integer> framePointerWordOffsets;
    /** the sizes of the given identifiers in memory in words */
    private Map<VarDecl, Integer> identifierInStackWordSizes;

    /** equivalent to the var decl frame offsets */
    private Map<String, Integer> framePointerWordOffsetsRegisters;

    private int stackSizeWords;

    private int framePointerWordOffset;

    public StackMemory(Memory m, MipsWriter w, RegisterAllocator r, int framePointerWordOffset) {
        writer = w;
        outerMemory = m;
        registerAllocator = r;

        framePointerWordOffsets = new HashMap<VarDecl, Integer>();
        framePointerWordOffsetsRegisters = new HashMap<String, Integer>();
        identifierInStackWordSizes = new HashMap<VarDecl, Integer>();
        this.framePointerWordOffset = framePointerWordOffset;
    }

    @Override
    public void putStringConstant(StrLiteral var) {
        // all string constants are stored in the data segment, pass it upwards untill
        // it reaches a global memory (i.e. data segment)
        outerMemory.putStringConstant(var);

    }

    @Override
    public Register retrieveStringConstant(StrLiteral var) {
        return outerMemory.retrieveStringConstant(var);
    }

    /** stores variable at its declared place in the stack, throws error if not declared */
    @Override
    public void putVariable(StorageDirectory d, VarDecl var, Register value, Register wordOffset) {

        if (d == StorageDirectory.DATA_SECTION)
            outerMemory.putVariable(d, var, value, wordOffset); // frame pointer offset is irrelevant here
        else {
            if (framePointerWordOffsets.containsKey(var)) {
                String label = storeOrGetVarLabel(var);
                
                // calculate address and write to it

                Register addressOfVar = getVariableAddress(var, wordOffset);
                if(var.varType.sizeOfType() == 1){
                    writer.writeSb(value, addressOfVar, 0, "store byte value in variable: " + label);
                }else {
                    writer.writeSw(value,addressOfVar, 0, "store value in variable: " + label);
                }

                registerAllocator.freeRegister(addressOfVar);

            } else {
                // the frame pointer was shifted down by the previous stacks size from its point
                // of view, so we "revert that"
                outerMemory.putVariable(d, var, value, wordOffset);
            }

        }
    }

    /**  */
    @Override
    public void declareVariable(StorageDirectory d, VarDecl var, int words) {
        if (d == StorageDirectory.DATA_SECTION) {
            outerMemory.declareVariable(d, var, words);
        } else {
            if(containsVariable(var))
                throw new MemoryAlreadyDeclaredError();

            // now $sp is pointing to free location
            // store $fp word offset for this identifier (stack size + 1) since we're one
            // word ahead of the actual data
            framePointerWordOffsets.put(var, stackSizeWords);
            identifierInStackWordSizes.put(var, words);

            String label = storeOrGetVarLabel(var);

            // now expand the stack by the size ($sp will point to the next free word after
            // our data)

            writer.writeAddI(Register.sp, Register.sp, words * -4, "add space for declared variable: " + label);



            stackSizeWords += words;
        }

    }

    /** retrieves address of the given variable, throws error if not declared */
    @Override
    public Register retrieveVariableAddress(VarDecl var, Register offsetWords) {
        // if we cannot find identifier in this stack, look outside with $fp corrected
        if (framePointerWordOffsets.containsKey(var)) {
            return getVariableAddress(var, offsetWords);
        } else {
            return outerMemory.retrieveVariableAddress(var, offsetWords);
        }
    }

    /** stores the value of the given register in the stack. */
    @Override
    public void putRegister(Register dumped, String registerName) {

        // give it a unique label for readability
        String label = storeOrGetRegisterLabel(registerName);

        // either update or initialize it on stack
        if (!framePointerWordOffsetsRegisters.containsKey(registerName)) {
            // remember frame pointer
            framePointerWordOffsetsRegisters.put(registerName, stackSizeWords);
            // expand stack
            writer.writeAddI(Register.sp, Register.sp, -4, "add space for dumped register: " + label);

            stackSizeWords += 1;
        }

        // TODO: don't use a random register for holding garbage ;) 
        Register address = Register.tempVal2;
        getRegisterAddress(registerName,address);
        writer.writeSw(dumped,address, 0, "update value of register: " + label);


    }

    /** retrieves given register from current stack, throws error if not stored beforehand */
    @Override
    public void retrieveRegister(String dumpedName, Register targetRegister) {

        if(!labelsRegs.containsKey(dumpedName)){
            outerMemory.retrieveRegister(dumpedName, targetRegister);
            return;
        }

        String label = storeOrGetRegisterLabel(dumpedName);

        // use target register as storage for the address too
        Register address = Register.tempVal1;
        getRegisterAddress(dumpedName,address);
        writer.writeLw(targetRegister,address , 0, "restore register value: " + label);
        registerAllocator.freeRegister(address);
    }

    /** restores register variable from stack, throws error if register not stored earlier */
    private void getRegisterAddress(String registerName, Register targetRegister) {

        if(!labelsRegs.containsKey(registerName))
            throw new MemoryNotDeclaredError();

        String label = storeOrGetRegisterLabel(registerName);

        int offsetFromFP = framePointerWordOffsetsRegisters.get(registerName);

        writer.writeAddI(targetRegister, Register.fp, (offsetFromFP * -4) - (framePointerWordOffset * 4), "find register in stack:" + label);
    }

    @Override
    public boolean isGlobalMemory() {
        return false;
    }

    @Override
    public int getStackWordSizeSoFar() {
        return stackSizeWords + outerMemory.getStackWordSizeSoFar();
    }

    /** retrieves address of variable, throws error if not declared earlier */
    private Register getVariableAddress(VarDecl var, Register offsetWords) {
        // get offset from FP
        
        if(!labelsVars.containsKey(var))
            throw new MemoryNotDeclaredError();

        String label = storeOrGetVarLabel(var);

        
        int offsetFromFP = framePointerWordOffsets.get(var);
        // work out address of variable
        Register addressOfVar = registerAllocator.getRegister();


        writer.writeAddI(addressOfVar, Register.fp, (offsetFromFP * -4) - (framePointerWordOffset * 4), "find variable address in stack: " + label);
        if (offsetWords != null) {
            writer.writeAdd(addressOfVar, offsetWords, addressOfVar, "calculate address at offset");
        }
        return addressOfVar;

    }



    /** returns the register containing value of given argument corresponding to the funDecl if it exists or null otherwise */
    @Override
    public Register retrieveFunctionArgumentAddress(FunDecl d, VarDecl argRetrieved) {
        // we look at the frame pointer and the declarations (which might not exist) we only use relative distances to access args
        int idx = 0;
        for (VarDecl argDeclared : d.params) {
            if(argDeclared.varName.equals(argRetrieved.varName)){


                // find expected offset from fp
                // all args are one word, so just count the idx
                // last argument will be at $fp + 1W , second last at $fp + 2W etc..
                // this works since idx starts at zero and $fp is on the last argument
                String label = storeOrGetVarLabel(argRetrieved);

                int wordOffset = (d.params.size() - idx); // - 1 since 
                Register address = registerAllocator.getRegister();
                writer.writeAddI(address, Register.fp, (wordOffset * 4) , "find address of argument: " + label);


                return address;
            }
            idx++;
        }

        // no argument found
        return null;
    }

    @Override
    public boolean containsVariable(VarDecl var) {
        return labelsVars.containsKey(var) || outerMemory.containsVariable(var);
    }

    @Override
    public void expandStack(int words) {
        writer.writeAddI(Register.sp, Register.sp, -words * 4, "expand stack");
        stackSizeWords+= words ;

    }

    @Override
    public void shrinkStack(int words) {
        writer.writeAddI(Register.sp, Register.sp, words * 4, "expand stack");
        stackSizeWords-= words ;

    }

}
