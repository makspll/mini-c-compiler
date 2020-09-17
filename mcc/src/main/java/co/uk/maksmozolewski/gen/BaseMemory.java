package co.uk.maksmozolewski.gen;

import java.util.HashMap;
import java.util.Map;

import co.uk.maksmozolewski.ast.StrLiteral;
import co.uk.maksmozolewski.ast.VarDecl;

public abstract class BaseMemory implements Memory {
    protected int count = 0;

    protected Map<StrLiteral, String> labelsStrLiterals = new HashMap<StrLiteral,String>();
    protected Map<VarDecl, String> labelsVars = new HashMap<VarDecl,String>();
    protected Map<String, String> labelsRegs = new HashMap<String,String>();

    public class MemoryAlreadyDeclaredError extends Error {
        private static final long serialVersionUID = 1L;
    }

    public class InvalidMemoryDeclarationError extends Error {
        private static final long serialVersionUID = 1L;
    }

    public class MemoryNotDeclaredError extends Error {
        private static final long serialVersionUID = 1L;
    }

    public class InvalidMemoryAccess extends Error {
        private static final long serialVersionUID = 1L;
    }

    /** gives unique name */
    protected String storeOrGetStrLitIdentifier(StrLiteral lit){
        if(labelsStrLiterals.containsKey(lit)){
            return labelsStrLiterals.get(lit);
        } else {
            String label = "$_str_lit_" + count++;
            labelsStrLiterals.put(lit, label);
            return label;

        }
    }

    /** gives unique var label */
    protected String storeOrGetVarLabel(VarDecl varDecl){
        if(labelsVars.containsKey(varDecl)){
            return labelsVars.get(varDecl);
        } else {
            String label = "$_var_" + varDecl.varName + "_" + count++;
            labelsVars.put(varDecl,label);
            return label;
        }
    }

    protected String storeOrGetRegisterLabel(String registerName){
        if(labelsRegs.containsKey(registerName)){
            return labelsRegs.get(registerName);
        }
        else {
            String label ="$_reg_dump_" + registerName.toString();
            labelsRegs.put(registerName,label);
            return label;

        }
    }


}
