package co.uk.maksmozolewski.gen;

import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class RegisterAllocator {
    /*
     * Simple register allocator.
     */
    private MipsWriter writer;
    public RegisterAllocator(MipsWriter wr){
        freeRegs.addAll(Register.tmpRegs);
        writer = wr;
    }
    
    // contains all the free temporary registers
    private Stack<Register> freeRegs = new Stack<Register>();

    private class RegisterAllocationError extends Error {
    }
    
    /** returns clean register */
    public Register getRegister() {
        try {
            Register reg = freeRegs.pop();
            writer.writeAddI(reg, Register.zero, 0, "clean register");
            return reg;
        } catch (EmptyStackException ese) {
            throw new RegisterAllocationError(); // no more free registers, bad luck!
        }
    }

    public void freeRegister(Register reg) {
        if(Register.tmpRegs.contains(reg)){
            freeRegs.push(reg);
        }
    }

    public List<Register> getUsedRegisters(){
        List<Register> out = new LinkedList<Register>();
        for (Register r : Register.tmpRegs) {
            if(freeRegs.contains(r)){
                // if used 
                out.add(r);
            }
        }

        return out;
    }

    public void resetRegisters(){
        freeRegs = new Stack<Register>();
        freeRegs.addAll(Register.tmpRegs);
    }
}
