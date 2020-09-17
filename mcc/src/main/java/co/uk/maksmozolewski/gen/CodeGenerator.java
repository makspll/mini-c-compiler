package co.uk.maksmozolewski.gen;

import co.uk.maksmozolewski.Main;
import co.uk.maksmozolewski.ast.*;
import co.uk.maksmozolewski.gen.MipsWriter.WriteTarget;
import co.uk.maksmozolewski.gen.MipsWriter.Directive;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;



public class CodeGenerator extends BaseASTVisitor<Register> {


    private MipsWriter writer;
    private RegisterAllocator registerAllocator;
    private Memory currMemory;
    private DataSegmentMemory globalMemory;

    /** used to avoid needing a second visitor pass */
    private boolean inFunDecl = false;
    private FunDecl currFunDecl = null;

    /** for array access  */
    private boolean inAssignLhsFirstLevel = false;

    private int uniqueNum = 0;

    private int getUniqueNum(){
        return uniqueNum++;
    }

    public CodeGenerator(File f) throws IOException {
        writer = new MipsWriter( new FileWriter(f));
        registerAllocator = new RegisterAllocator(writer);
    }





    public void emitProgram(Program program) throws IOException {

        writer.writeDirective(WriteTarget.DATA,Directive.DATA);
        writer.writeDirective(WriteTarget.TEXT,Directive.TEXT);

        visitProgram(program);
        
        writer.writeProgram();
    }

    @Override
    public Register visitProgram(Program p) {
        globalMemory = new DataSegmentMemory(writer,registerAllocator);
        currMemory = globalMemory;

        writer.writeMove(Register.fp, Register.sp, "initialize frame pointer");
        writer.writeB("main", "entry point");
        visitAll(p.varDecls,p.funDecls);

        emitStdlib();
        return null;
    }

    @Override
    public Register visitVarDecl(VarDecl vd) {
        writer.writeCommentNl(WriteTarget.TEXT, "VAR DECL");

        int compactSize = vd.varType.sizeOfType();
        int paddedSize = (int)Math.ceil((float)compactSize / 4);
        if(currMemory.isGlobalMemory()){
            currMemory.declareVariable(StorageDirectory.DATA_SECTION, vd, paddedSize);
        } else {
            currMemory.declareVariable(StorageDirectory.STACK, vd, paddedSize);
        }


        return null;
    }

    @Override
    public Register visitFunDecl(FunDecl p) {
        emitBeginStdFunc(p);
        emitBodyStdFunc(p);
        emitEndStdFunc(p);

        return null;

    }

    @Override
    public Register visitFunCallExpr(FunCallExpr fce) {

        int idx = 0;
        // the call declares parameters on the stack
        //
        // we need to store callee saved registers


        // store frame pointer value just before pushing fp and sp
        writer.writeCommentNl(WriteTarget.TEXT,"FUNCALL");
        writer.writeCommentNl(WriteTarget.TEXT,"STORE SP");

        Memory oldMemory = currMemory;
        currMemory = new StackMemory(oldMemory, writer, registerAllocator,oldMemory.getStackWordSizeSoFar());
        String labelSP = "$sp" + getUniqueNum();
        StoreStackPointer(labelSP);
        writer.writeCommentNl(WriteTarget.TEXT,"ARGUMENTS PUSH TO STACK");
        for (Expr val : fce.args) {
            // we can safely assume we're not going to be calling functions in the global scope in mini-c
            VarDecl correspondingDecl = fce.funDecl.params.get(idx);
            
            // declare (in the scope of the declaration)
            // if it already exists this means that we're actually recursing!
            // we have to "re-declare" it manually
            
            if(currMemory.containsVariable(correspondingDecl)){     
                Register register = val.accept(this);
                writer.writeSw(register, Register.sp, 0, "re-declare variable");
                int extraStackExpansionWords = ((int)Math.ceil((float)correspondingDecl.varType.sizeOfType() / 4f));
                currMemory.expandStack(extraStackExpansionWords);
                registerAllocator.freeRegister(register);
            } else {
                currMemory.declareVariable(StorageDirectory.STACK,correspondingDecl , 1);
                // fill value from parameter passed
                Register register = val.accept(this);
                currMemory.putVariable(StorageDirectory.STACK, correspondingDecl, register,null);
            
                registerAllocator.freeRegister(register);
            }
            idx++;
        }
        writer.writeCommentNl(WriteTarget.TEXT,"CALL");

        // perform jump
        writer.writeJal(fce.funName, "call");

        writer.writeCommentNl(WriteTarget.TEXT,"RETRIEVE REGS");


        Register retReg = registerAllocator.getRegister();
        writer.writeMove(retReg, Register.v0, "move register to permament");

        currMemory.retrieveRegister(labelSP, Register.sp);
        currMemory = oldMemory;

        return retReg;
    }

    /** the action of putRegister actually changes the Stack pointer so we need special treatment in storing $sp itself */
    public void StoreStackPointer(String name){
        Register temp = Register.tempSP;
        writer.writeMove(temp, Register.sp, "temporary store");
        currMemory.putRegister(temp, name);
        registerAllocator.freeRegister(temp);
    }

    @Override
    public Register visitBlock(Block b) {
        visitAll(b.varDecls,b.stmnts);
        return null;
    }
    @Override
    public Register visitWhile(While w) {

        writer.writeCommentNl(WriteTarget.TEXT,"WHILE");

        String whileExitLabel = "$while_branch_exit_" + getUniqueNum(); 
        String whileConditionLabel = "$while_branch_condition_" + getUniqueNum(); 


        writer.writeLabelInlineNl(WriteTarget.TEXT, whileConditionLabel);
        writer.writeCommentNl(WriteTarget.TEXT,"CONDITION CHECK");

        // condition is in outer scope
        Register condition = w.condition.accept(this);
        
        writer.writeBeq(condition,0, whileExitLabel, "condition check, jump to exit");
        
        Memory oldMemory = currMemory;
        currMemory = new StackMemory(oldMemory, writer, registerAllocator,currMemory.getStackWordSizeSoFar());
        writer.writeCommentNl(WriteTarget.TEXT,"BODY");

        String stackPointerLabel = "$sp" + getUniqueNum();
        StoreStackPointer(stackPointerLabel);


        // stmt is in inner
        w.stmt.accept(this);

        currMemory.retrieveRegister(stackPointerLabel, Register.sp);
        currMemory = oldMemory;

        writer.writeB(whileConditionLabel, "jump to condition check");

        writer.writeLabel(WriteTarget.TEXT, whileExitLabel);

        return null;
    }

    @Override
    public Register visitIf(If i) {

        String ifExitLabel = "$if_branch_exit_" + getUniqueNum(); 
        String ifElseLabel = "$if_branch_else_" + getUniqueNum(); 
        writer.writeCommentNl(WriteTarget.TEXT,"IF");
        writer.writeCommentNl(WriteTarget.TEXT,"CONDITION");
        // condition is in outer scope
        Register condition = i.condition.accept(this);

        // on true fallthrough otherwise jump out
        if(i.elseStmt != null){
            writer.writeBeq(condition, 0,ifElseLabel, "if stmt condition, jump to else");
        } else {
            writer.writeBeq(condition, 0, ifExitLabel, "if stmt condition, jump to exit");
        }

        Memory oldMemory = currMemory;
        currMemory = new StackMemory(oldMemory, writer, registerAllocator,oldMemory.getStackWordSizeSoFar());
        writer.writeCommentNl(WriteTarget.TEXT,"IF BODY");

        String stackPointerlabel = "$sp" + getUniqueNum();
        StoreStackPointer(stackPointerlabel);
        // stmt is in inner
        i.ifStmt.accept(this);
        currMemory.retrieveRegister(stackPointerlabel,Register.sp);
        // else stmt is in another inner
        
        if(i.elseStmt != null){
            writer.writeB(ifExitLabel, "leave if");

            writer.writeCommentNl(WriteTarget.TEXT,"ELSE BODY");
            writer.writeLabel(WriteTarget.TEXT, ifElseLabel);
            currMemory = new StackMemory(oldMemory,writer,registerAllocator,oldMemory.getStackWordSizeSoFar());
            String stackPointerlabelElse = "$sp" + getUniqueNum();
            
            StoreStackPointer(stackPointerlabelElse);
            i.elseStmt.accept(this);
            currMemory.retrieveRegister(stackPointerlabelElse, Register.sp);

            writer.writeLabel(WriteTarget.TEXT, ifExitLabel);

        } else {
            writer.writeLabel(WriteTarget.TEXT, ifExitLabel);

        }


        currMemory = oldMemory;
        return null;
    }

    @Override
    public Register visitReturn(Return r) {
        if(r.exp != null){
            // place return in v0
            Register exp = r.exp.accept(this);
            writer.writeMove(Register.v0, exp, "move return val");
            registerAllocator.freeRegister(exp);
        } 

        //TODO: optimise this
        for(int i = 0; i < Register.tmpRegs.size(); i ++){
            String label = Register.tmpRegs.get(i).toString();
            currMemory.retrieveRegister(label,Register.tmpRegs.get(i));
        }

        currMemory.retrieveRegister("$spF",Register.sp);
        currMemory.retrieveRegister("$raF",Register.ra);
        currMemory.retrieveRegister("$fpF",Register.fp);
        
        // jump to return
        writer.writeJr(Register.ra, "return to caller");

        return null;
    }

    // expr

    @Override
    public Register visitExprStmt(ExprStmt exprStmt) {
        exprStmt.expr.accept(this);
        return null;
    }
    @Override
    public Register visitVarExpr(VarExpr v) {
        writer.writeCommentNl(WriteTarget.TEXT, "VAREXPR");
        Register addr;

        if(inFunDecl){
            // first try to find a matching parameter above the $fp then look in the current scope
            addr = currMemory.retrieveFunctionArgumentAddress(currFunDecl, v.vd);
            if(addr == null){
                // then just look through the scope
                addr = currMemory.retrieveVariableAddress(v.vd, null);
            } 
        } else {
            addr = currMemory.retrieveVariableAddress(v.vd,null);
        }


        // arrays and structs are always evaluated by reference unless they're dereferenced
        if(!(v.type.isArrayType() || v.type.isStructTypeType())){
            // other types are only referenced by address on the first level LHS of assignments
            if(!inAssignLhsFirstLevel){
                if(v.type.sizeOfType() == 1){
                    writer.writeLb(addr, addr, 0, "load variable expression byte value");
                } else {
                    writer.writeLw(addr, addr, 0, "load variable expression value");
                }
            }
        } 


        return addr;
    }

    
    @Override
    public Register visitBinOp(BinOp bo) {

        Register lhs = bo.lhs.accept(this);
        Register rhs = null;
        // store result in lhs always
        switch(bo.op){
            case ADD:
                rhs = bo.rhs.accept(this);

                // check if one side is a ptr, in that case the numeric operator needs to be multiplied by size of ptr inside type
                boolean lhsIsPointer = bo.lhs.type.isPointerType();
                boolean rhsIsPointer = bo.rhs.type.isPointerType(); 
                if(lhsIsPointer || rhsIsPointer){
                    if(lhsIsPointer && !rhsIsPointer){
                        if(((PointerType)(bo.lhs.type)).pointedToType.sizeOfType() != 1){
                            writer.writeSll(rhs, rhs, 2, "multiply int to match ptr inner type size");
                        }
                    } if(rhsIsPointer && !lhsIsPointer){
                        if(((PointerType)(bo.rhs.type)).pointedToType.sizeOfType() != 1){
                            writer.writeSll(lhs, lhs, 2, "multiply int to match ptr inner type size");
                        }
                    }
                }
                


                writer.writeAdd(lhs, lhs, rhs, "operator +");
                break;
            case SUB:
                rhs = bo.rhs.accept(this);
                writer.writeSub(lhs, lhs, rhs, "operator -");
                break;
            case AND:
                // allow short-circuting
                // fall through on true, skip on false
                String trueAndFallthrough =  "$and_branch_true_" + getUniqueNum();
                String falseAndFallthrough =  "$and_branch_false_" + getUniqueNum();
                String andExit =  "$and_branch_exit_" + getUniqueNum();


                writer.writeBeq(lhs, Register.zero, falseAndFallthrough, "operator && lhs, false jump");
                rhs = bo.rhs.accept(this);
                writer.writeBeq(rhs, Register.zero, falseAndFallthrough, "rhs, false jump");

                writer.writeLabel(WriteTarget.TEXT, trueAndFallthrough);
                writer.writeAddI(lhs, Register.zero, 1, "true outcome");
                writer.writeB(andExit, "exit and");

                writer.writeLabel(WriteTarget.TEXT, falseAndFallthrough);
                writer.writeAddI(lhs, Register.zero, 0, "false outcome");


                writer.writeLabel(WriteTarget.TEXT, andExit);
                break;

            case DIV:
                rhs = bo.rhs.accept(this);
                writer.writeDiv(lhs,rhs,"operator /");
                writer.writeMflo(lhs, "load div quotient");
                break;

            case EQ:
                rhs = bo.rhs.accept(this);
                writer.writeSeq(lhs, lhs, rhs, "operator ==");
                break;

            case GE:
                rhs = bo.rhs.accept(this);
                writer.writeSge(lhs, lhs,rhs, "operator >=");
                break;

            case GT:
                rhs = bo.rhs.accept(this);
                writer.writeSgt(lhs, lhs, rhs, "operator >");
                break;

            case LE:
                rhs = bo.rhs.accept(this);
                writer.writeSle(lhs, lhs, rhs, "operator <=");
                break;

            case LT:
                rhs = bo.rhs.accept(this);
                writer.writeSlt(lhs, lhs, rhs, "operator <");
                break;

            case MOD:
                rhs = bo.rhs.accept(this);
                writer.writeDiv(lhs,rhs,"operator %");
                writer.writeMfhi(lhs, "load div remainder");
                break;

            case MUL:
                rhs = bo.rhs.accept(this);
                writer.writeMult(lhs, rhs, "operator *");
                writer.writeMflo(lhs, "load lower 32 bit result");
                break;

            case NE:
                rhs = bo.rhs.accept(this);
                writer.writeSne(lhs, lhs, rhs, "operator != ");
                break;

            case OR:
                // allow short-circuting
                // fall through on true, skip on false
                String trueOrFallthrough =  "$or_branch_true_" + getUniqueNum();
                String falseOrFallthrough =  "$or_branch_false_" + getUniqueNum();
                String exitOr =  "$or_branch_exit_" + getUniqueNum();

                writer.writeBge(lhs, 1, trueOrFallthrough, "operator ||, true jump lhs" );
                rhs = bo.rhs.accept(this);
                writer.writeBge(rhs, 1, trueOrFallthrough, "true jump rhs");

                writer.writeLabel(WriteTarget.TEXT,falseOrFallthrough);
                writer.writeAddI(lhs, Register.zero, 0, "false outcome");
                writer.writeB(exitOr, "exit ||");

                writer.writeLabel(WriteTarget.TEXT,trueOrFallthrough);
                writer.writeAddI(lhs, Register.zero, 1, "true outcome");

                writer.writeLabel(WriteTarget.TEXT, exitOr);
                break;

            default:
                // should not happen
                assert false;
                break;
        }
        
        if(rhs != null){
            registerAllocator.freeRegister(rhs);
        }

        return lhs;
    }

    @Override
    public Register visitOp(Op o) {
        return null;
    }

    @Override
    public Register visitArrayAccessExpr(ArrayAccessExpr aae) {
        writer.writeCommentNl(WriteTarget.TEXT, "ARRAY ACCESS");

        writer.writeCommentNl(WriteTarget.TEXT, "BASE ADDRESS");
        Register accessAddr = aae.array.accept(this);

        // any further values inside will be evaluated by value 
        boolean wasInAssignLhsFirstLevel = inAssignLhsFirstLevel;
        inAssignLhsFirstLevel = false;
        writer.writeCommentNl(WriteTarget.TEXT, "IDX");
        Register arrayIdx = aae.idx.accept(this);
        inAssignLhsFirstLevel = wasInAssignLhsFirstLevel;

        // we either perform byte access or word access depending on size of inner type
        boolean isPtr = aae.array.type instanceof PointerType;
        Type innerType = isPtr ?
            ((PointerType)aae.array.type).pointedToType:
            ((ArrayType)aae.array.type).innerType;

        int innerSize = innerType.sizeOfType();
        boolean bytewiseAccess = innerSize != 4;
        if(!bytewiseAccess){
            writer.writeSll(arrayIdx, arrayIdx, 2, "multiply idx by 4");
        } 

        writer.writeCommentNl(WriteTarget.TEXT, "CALCULATE ADDRESS");

        if(isPtr && inAssignLhsFirstLevel){
            // ptr on lhs evaluates to the address of the ptr on the stack
            writer.writeLw(accessAddr, accessAddr, 0, "load pointer address value");
        }

        writer.writeAdd(accessAddr, arrayIdx, accessAddr, "find address of value");

        if(!inAssignLhsFirstLevel){
            if(bytewiseAccess){

                writer.writeLb(accessAddr, accessAddr, 0, "load array element byte value");
                
            } else {
                writer.writeLw(accessAddr, accessAddr, 0, "load array element value");
                
            }
        }

        registerAllocator.freeRegister(arrayIdx);
        return accessAddr;
    }

    @Override
    public Register visitFieldAccessExpr(FieldAccessExpr fae) {
        assert fae.structure instanceof VarExpr;

        writer.writeCommentNl(WriteTarget.TEXT, "STRUCT ACCESS");

        VarExpr struct = (VarExpr)fae.structure;
        
        Register address = fae.structure.accept(this);
        // find the corresponding field
        // count offset as you go
        int wordOffset = 0;
        VarDecl correspondingVarDecl = null;
        boolean bytewiseAccess = false;
        boolean foundDecl = false;
        for ( VarDecl varDecl: ((StructType)struct.vd.varType).dec.varDecls) {
            if(varDecl.varName.equals(fae.fieldName)){
                foundDecl = true;
                correspondingVarDecl = varDecl;
                bytewiseAccess = varDecl.varType.sizeOfType() == 1;
                break;
            }
            wordOffset += varDecl.varType.sizeOfType() / 4;
        }

        if(!foundDecl)
            // should not reach here
            throw new Error("Should not happen, field does not exist");
        
        writer.writeAddI(address, address, (wordOffset * 4), "find field address");



        if(!inAssignLhsFirstLevel){
            if(bytewiseAccess){
                writer.writeLb(address, address, 0, "load field value");
            } else {
                writer.writeLw(address, address, 0, "load field value");
            }
        }

        return address;
    }

    @Override
    public Register visitValueAtExpr(ValueAtExpr vae) {
        Register address = vae.ptr.accept(this);

        if(!inAssignLhsFirstLevel){
            // pointers on rhs of assignment evaluate to the address they contain
            writer.writeLw(address, address, 0, "dereference pointer value");

        } else {
            // pointers on lhs of assignment evaluate to the address of the pointer
            writer.writeLw(address, address, 0, "load heap address from pointer");
        }

        return address;
    }

    @Override
    public Register visitSizeOfExpr(SizeOfExpr sizeOfExpr) {
        Register out = registerAllocator.getRegister();
        writer.writeAddI(out, Register.zero, sizeOfExpr.val.sizeOfType(), "size of evaluation");
        return out;
    }

    @Override
    public Register visitTypecastExpr(TypecastExpr typecastExpr) {
        return typecastExpr.castedExpr.accept(this);
    }

    @Override
    public Register visitAssign(Assign a) {
        writer.writeCommentNl(WriteTarget.TEXT, "ASSIGN");
        writer.writeCommentNl(WriteTarget.TEXT, "LHS");

        inAssignLhsFirstLevel = true;
        Register lhs = a.lhs.accept(this);
        inAssignLhsFirstLevel = false;
        writer.writeCommentNl(WriteTarget.TEXT, "RHS");

        Register rhs = a.rhs.accept(this);
        writer.writeCommentNl(WriteTarget.TEXT, "=");

        int sizeOfTypeLeft = a.lhs.type.sizeOfType();
        boolean bytewiseAccess = sizeOfTypeLeft == 1;
        if(bytewiseAccess){
            writer.writeSb(rhs, lhs, 0, "assign byte value");
        } else {
            writer.writeSw(rhs, lhs, 0, "assign value");

        }

        return null;
    }

    @Override
    public Register visitIntLiteral(IntLiteral il) {
        Register val = registerAllocator.getRegister();
        writer.writeOrI(val, val, il.val, "load 4 byte integer");
        return val;
    }

    @Override
    public Register visitStrLiteral(StrLiteral sl) {
        currMemory.putStringConstant(sl);
        return currMemory.retrieveStringConstant(sl);
    }

    @Override
    public Register visitChrLiteral(ChrLiteral cl) {
        Register val = registerAllocator.getRegister();
        writer.writeAddI(val, Register.zero, cl.val, "load char");
        return val;
    }

    // types 

    @Override
    public Register visitBaseType(BaseType bt) {
        return null;
    }

    @Override
    public Register visitPointerType(PointerType pt) {
        return null;
    }

    @Override
    public Register visitStructType(StructType st) {
        return null;
    }

    @Override
    public Register visitArrayType(ArrayType at) {
        return null;
    }

    @Override
    public Register visitStructTypeDecl(StructTypeDecl st) {
        return null;
    }

    

    private void emitBeginStdFunc(FunDecl d){

        // we don't nest into the old memory, but start a new stack frame
        currMemory = new StackMemory(globalMemory, writer, registerAllocator,0);
        // this will mean that variables inside will also check the outer scope (just above the frame pointer)
        // for variable declaration



        inFunDecl = true;
        currFunDecl = d;

        // write label
        writer.writeLabelNl(WriteTarget.TEXT, d.name);

        writer.writeCommentNl(WriteTarget.TEXT,"FUNDECL");
        writer.writeCommentNl(WriteTarget.TEXT,"PRESERVE REGISTERS");
        Register framePointerBeforeSPFP = Register.tempFP;
        Register stackPointerBeforeSPFP = Register.tempSP;
        writer.writeAddI(stackPointerBeforeSPFP, Register.sp, 0, "hold stack pointer");
        writer.writeAddI(framePointerBeforeSPFP, Register.fp, 0, "hold frame pointer");

        // bring up frame pointer
        writer.writeMove(Register.fp, Register.sp, "bring back frame pointer");
        

        // store all temporary registers (this is were major optimisations could be inserted)
        // TODO : optimise this
        for (Register tempReg : Register.tmpRegs) {
            String label = tempReg.toString();
            currMemory.putRegister(tempReg, label);
        }



        // save frame pointer and stack pointer
        currMemory.putRegister(stackPointerBeforeSPFP,"$spF");
        currMemory.putRegister(Register.ra, "$raF");
        currMemory.putRegister(framePointerBeforeSPFP,"$fpF");
        registerAllocator.freeRegister(stackPointerBeforeSPFP);
        registerAllocator.freeRegister(framePointerBeforeSPFP);


        // since we back them up, we can use all the registers again
        registerAllocator.resetRegisters();


    }

    private void emitBodyStdFunc(FunDecl d){
        writer.writeCommentNl(WriteTarget.TEXT, "FUNC BODY");

        d.block.accept(this);

    }

    private void emitEndStdFunc(FunDecl d){
        writer.writeCommentNl(WriteTarget.TEXT, "RESTORE SAVED REGISTERS");
 
        //TODO: optimise this
        for(int i = 0; i < Register.tmpRegs.size(); i ++){
            String label = Register.tmpRegs.get(i).toString();
            currMemory.retrieveRegister(label,Register.tmpRegs.get(i));
        }
        currMemory.retrieveRegister("$spF",Register.sp);
        currMemory.retrieveRegister("$raF",Register.ra);
        currMemory.retrieveRegister("$fpF",Register.fp);
        
        // if the funciton is called main and missing return we return an implicit 0
        if(d.name.equals("main")){
            writer.writeAddI(Register.v0, Register.zero, 10, "end program");
            writer.writeSyscall("end");
        } else {

            // jump to return
            writer.writeJr(Register.ra, "return to caller");
            
        }
        currMemory = globalMemory;
        inFunDecl = false;
    }

    /** used to emit stdlib functions, emits normal function 
     * but runs the body before processing the rest of 
     * the function as normal with the injected stmts at the end*/
    private void emitFuncInjectBody(FunDecl d,Runnable body, List<Stmt> injectedStmtsAfterBody){
        emitBeginStdFunc(d);
        body.run();
        d.block.stmnts.addAll(injectedStmtsAfterBody);
        emitBodyStdFunc(d);
        emitEndStdFunc(d);
    }

    private void emitStdlib(){
        writer.writeNewline(WriteTarget.TEXT);
        writer.writeComment(WriteTarget.TEXT, "//////////// STDLIB ///////////");
        writer.writeNewline(WriteTarget.TEXT);
        writer.writeNewline(WriteTarget.TEXT);

        // void print_c(char c);
        FunDecl print_c = Main.stlib.get(0);
        emitFuncInjectBody(print_c, ()->{
            writer.writeAddI(Register.v0, Register.zero, 11, "code for print char");
            Register character = currMemory.retrieveFunctionArgumentAddress(print_c, print_c.params.get(0));
            writer.writeLw(Register.paramRegs[0],character, 0,"load char value");
            writer.writeSyscall("print char");
        }, new LinkedList<Stmt>());

        // void print_s(char* s);
        FunDecl print_s = Main.stlib.get(1);
        emitFuncInjectBody(print_s, ()->{
            writer.writeAddI(Register.v0, Register.zero, 4, "code for print str");
            Register string = currMemory.retrieveFunctionArgumentAddress(print_s, print_s.params.get(0));
            writer.writeLw(Register.paramRegs[0],string, 0,"load string value");
            writer.writeSyscall("print str");
        }, new LinkedList<Stmt>());

        // void print_i(int i);

        FunDecl print_i = Main.stlib.get(2);
        emitFuncInjectBody(print_i,()->{
            writer.writeAddI(Register.v0,Register.zero,1, "code for print int");
            Register integer = currMemory.retrieveFunctionArgumentAddress(print_i,print_i.params.get(0));
            writer.writeLw(Register.paramRegs[0],integer, 0,"load integer value");
            writer.writeSyscall("print int");
        }, new LinkedList<Stmt>());
        // char read_c();
        FunDecl read_c = Main.stlib.get(3);
        emitFuncInjectBody(read_c, ()->{
            writer.writeAddI(Register.v0, Register.zero, 12, "code for read char");
            writer.writeSyscall("read char");
            
        }, new LinkedList<Stmt>());
        // int read_i();
        FunDecl read_i = Main.stlib.get(4);
        emitFuncInjectBody(read_i, ()->{
            writer.writeAddI(Register.v0,Register.zero,5, "code for integer read");
            writer.writeSyscall("read int");
        }, new LinkedList<Stmt>());
        // void* mcmalloc(int size);
        FunDecl mcmalloc = Main.stlib.get(5);
        emitFuncInjectBody(mcmalloc, ()->{
            writer.writeAddI(Register.v0,Register.zero,9, "code for heap alloc");
            Register bytes = currMemory.retrieveFunctionArgumentAddress(mcmalloc,mcmalloc.params.get(0));
            writer.writeLw(Register.paramRegs[0], bytes, 0, "load argument bytes");
            writer.writeSyscall("get memory");
        }, new LinkedList<Stmt>());
    }

}
