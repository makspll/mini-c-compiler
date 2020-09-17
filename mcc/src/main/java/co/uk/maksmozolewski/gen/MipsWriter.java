package co.uk.maksmozolewski.gen;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Used to write a mips file. Allows writing to the data and text section in
 * parallel, uses a buffer to store write calls whicih is unfolded at final write.
 */
public class MipsWriter {

    public enum WriteTarget{
        TEXT,
        DATA,

    }
    
    public enum Directive{
        ALIGN {public String toString(){return ".align";};},
        ASCIIZ{public String toString(){return ".asciiz";};},
        BYTE{public String toString(){return ".byte";};},
        DATA{public String toString(){return ".data";};},
        SPACE{public String toString(){return ".space";};},
        TEXT{public String toString(){return ".text";};},
        WORD{public String toString(){return ".word";};},
    }

    private final FileWriter writer;

    private final StringBuilder dataSection;
    private final StringBuilder textSection;

    private int idntLvlDtaSection = 0;
    private int idntLvlTxtSection = 0;

    public MipsWriter(final FileWriter w) {
        writer = w;

        dataSection = new StringBuilder();
        textSection = new StringBuilder();
    }

    /**
     * after writing to the buffer call this to "release" the program
     * 
     * @throws IOException
     */
    public void writeProgram() throws IOException {
        writer.write(dataSection.toString());
        writer.write(textSection.toString());

        writer.flush();
        writer.close();
    }

    private void writeTo(final WriteTarget t,final String s){
        if(t == WriteTarget.TEXT){
            textSection.append("\t".repeat(idntLvlTxtSection));

            textSection.append(s);
        } else {
            dataSection.append("\t".repeat(idntLvlDtaSection));

            dataSection.append(s);
        }
    }

    private void writeToNoIdnt(final WriteTarget t,final String s){
        if(t == WriteTarget.TEXT){
            textSection.append(s);
        } else {
            dataSection.append(s);
        }
    }

    private void writeToNoIdnt(final WriteTarget t,final char s){
        if(t == WriteTarget.TEXT){
            textSection.append(s);
        } else {
            dataSection.append(s);
        }
    }

    private void writeTo(final WriteTarget t,final char s){
        writeTo(t, "" + s);
    }

    private void changeIdnt(final WriteTarget t, int diff){
        if(t == WriteTarget.TEXT){
            idntLvlTxtSection += diff;
        } else {
            idntLvlDtaSection += diff;
        }
    }

    private void setIdnt(final WriteTarget t, int val){
        if(t == WriteTarget.TEXT){
            idntLvlTxtSection = val;
        } else {
            idntLvlDtaSection = val;
        }
    }

    private int getIdnt(final WriteTarget t){
        if(t == WriteTarget.TEXT){
            return idntLvlTxtSection;
        } else {
            return idntLvlDtaSection;
        }
    }

    public void writeNewline(WriteTarget t){
        writeToNoIdnt(t, '\n');
        setIdnt(t, 2);
    }

    public void writeIndent(WriteTarget t){
        writeToNoIdnt(t,'\t');
    }

    public void writeDirective(WriteTarget t,Directive d){
        boolean idnted = d == Directive.DATA || d == Directive.TEXT;

        if(idnted) setIdnt(t, 0);
        writeTo(t, d.toString() + " ");
        if(idnted) {
            writeNewline(t);
            setIdnt(t, 2);
        } else {
            setIdnt(t, 0);
        }
        
    }
    
    public void writeQuotedString(WriteTarget t, String s){
        writeToNoIdnt(t, '\"'+s+"\"");
        writeNewline(t);

    }

    public void writeInt(WriteTarget t, int i){
        writeToNoIdnt(t,""+ i + ' ');
        writeNewline(t);
    }

    public void writeComment(WriteTarget t, String comment){
        writeToNoIdnt(t,'\t');
        writeToNoIdnt(t, "# " + comment);
    }

    public void writeCommentNl(WriteTarget t, String comment){
        setIdnt(WriteTarget.TEXT, 2);
        writeTo(t, "# " + comment);
        writeNewline(t);
    }


    public void writeLabel(WriteTarget t, String labelName){
        setIdnt(t, 1);
        writeTo(t, labelName + ": ");
        setIdnt(t, 0);
    }


    public void writeLabelNl(WriteTarget t, String labelName){
        setIdnt(t, 1);
        writeTo(t, labelName + ": ");
        writeNewline(t);
    }

    public void writeLabelInlineNl(WriteTarget t, String labelName){
        setIdnt(t, 2);
        writeTo(t, labelName + ": ");
        writeNewline(t);
    }



    ///// these assume writing to text segment

    /** add */
    public void writeAdd(Register rd, Register rs, Register rt, String comment){
        writeTo(WriteTarget.TEXT, "add " + rd + ',' + rs + ',' + rt);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** add unsigned */
    public void writeAddU(Register rd, Register rs, Register rt, String comment){
        writeTo(WriteTarget.TEXT, "addu " + rd + ',' + rs + ',' + rt);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** add immediate */
    public void writeAddI(Register rt,Register rs, int imm, String comment){
        writeTo(WriteTarget.TEXT, "addi " + rt + ',' + rs + ',' + imm);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** add unsigned immediate */
    public void writeAddIU(Register rt,Register rs, int imm, String comment){
        writeTo(WriteTarget.TEXT, "addiu " + rt + ',' + rs + ',' + imm);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** bitwise and */
    public void writeAnd(Register rd, Register rs, Register rt, String comment){
        writeTo(WriteTarget.TEXT, "and " + rd + ',' + rs + ',' + rt);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** bitwise and immediate*/
    public void writeAndI(Register rt, Register rs, int imm, String comment){
        writeTo(WriteTarget.TEXT, "andi " + rt + ',' + rs + ',' + imm);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** branch if equal */
    public void writeBeq(Register rt, Register rs, String label, String comment){
        writeTo(WriteTarget.TEXT, "beq " + rt + ',' + rs + ',' + label);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** branch if greater or equal to zero */
    public void writeBgez(Register rt, String label, String comment){
        writeTo(WriteTarget.TEXT, "bgez " + rt + ',' + label);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** branch if greater than to zero */
    public void writeBgtz(Register rt, String label, String comment){
        writeTo(WriteTarget.TEXT, "bgtz " + rt  + ',' + label);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** branch if less than zero */
    public void writeBltz(Register rt, String label, String comment){
        writeTo(WriteTarget.TEXT, "bltz " + rt + ',' + label);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** branch if less than or equal to zero */
    public void writeBlez(Register rt, String label, String comment){
        writeTo(WriteTarget.TEXT, "blez " + rt + ',' + label);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** branch if not equal */
    public void writeBne(Register rt, Register rs, String label, String comment){
        writeTo(WriteTarget.TEXT, "bne " + rt + ',' + rs + ',' + label);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** division with overflow, divide then set LO to quotient and HI to remainder */
    public void writeDiv(Register rt, Register rs, String comment){
        writeTo(WriteTarget.TEXT, "div " + rt + ',' + rs);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** division unsigned with overflow, divide then set LO to quotient and HI to remainder */
    public void writeDivU(Register rt, Register rs, String comment){
        writeTo(WriteTarget.TEXT, "divu " + rt + ',' + rs);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** jump to label */
    public void writeJ(String target, String comment){
        writeTo(WriteTarget.TEXT, "j " + target);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** jump to label and link (set $ra to PC)*/
    public void writeJal(String target, String comment){
        writeTo(WriteTarget.TEXT, "jal " + target);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** jump to address in t1 and link (set $ra to PC)*/
    public void writeJalr(Register rt, String comment){
        writeTo(WriteTarget.TEXT, "jalr " + rt);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** jump to register */
    public void writeJr(Register rt, String comment){
        writeTo(WriteTarget.TEXT, "jr " + rt);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    
    /** load byte (sign-extend) into lower 8 bits */
    public void writeLb(Register rt, Register rs,int offset, String comment){
        writeTo(WriteTarget.TEXT, "lb " + rt + ',' + offset + '(' + rs + ')');
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** load byte (zero-extend) into lower 8 bits */
    public void writeLbU(Register rt, Register rs,int offset, String comment){
        writeTo(WriteTarget.TEXT, "lbu " + rt + ',' + offset + '(' + rs + ')');
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }



    /** load word (sign-extend) into target */
    public void writeLw(Register rt, Register rs,int offset, String comment){
        writeTo(WriteTarget.TEXT, "lw " + rt + ',' + offset + '(' + rs + ')');
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** load byte (zero-extend) into target */
    public void writeLwU(Register rt, Register rs,int offset, String comment){
        writeTo(WriteTarget.TEXT, "lwu " + rt + ',' + offset + '(' + rs + ')');
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** move from hi */
    public void writeMfhi(Register rt, String comment){
        writeTo(WriteTarget.TEXT, "mfhi " + rt);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** move from lo */
    public void writeMflo(Register rt, String comment){
        writeTo(WriteTarget.TEXT, "mflo " + rt);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** multiplication  (set HI to higher 32 bits, LO to lower 32 bits)*/
    public void writeMult(Register rt, Register rs, String comment){
        writeTo(WriteTarget.TEXT, "mult " + rt + ',' + rs);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** multiplication unsigned (set HI to higher 32 bits, LO to lower 32 bits)*/
    public void writeMultU(Register rt, Register rs, String comment){
        writeTo(WriteTarget.TEXT, "multu " + rt + ',' + rs);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** no op */
    public void writeNop(String comment){
        writeTo(WriteTarget.TEXT, "nop ");
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }


    /** bitwise nor */
    public void writeNor(Register rd, Register rs, Register rt, String comment){
        writeTo(WriteTarget.TEXT, "nor " + rd + ',' + rs + ',' + rt);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** bitwise or */
    public void writeOr(Register rd, Register rs, Register rt, String comment){
        writeTo(WriteTarget.TEXT, "or " + rd + ',' + rs + ',' + rt);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** bitwise or immediate*/
    public void writeOrI(Register rt, Register rs,int imm, String comment){
        writeTo(WriteTarget.TEXT, "ori " + rt + ',' + rs + ',' + imm);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** store byte */
    public void writeSb(Register rt, Register rs,int offset, String comment){
        writeTo(WriteTarget.TEXT, "sb " + rt + ',' + offset + '(' + rs + ')');
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** shift left logical  */
    public void writeSll(Register rt, Register rs, int imm, String comment){
        writeTo(WriteTarget.TEXT, "sll " + rt + ',' + rs + ',' + imm);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** set rd to 1 if rs less than rt and 0 otherwise */
    public void writeSlt(Register rd, Register rs, Register rt, String comment){
        writeTo(WriteTarget.TEXT, "slt " + rd + ',' + rs + ',' + rt);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** set rd to 1 if rs less than rt and 0 otherwise using unsigned comparison*/
    public void writeSltU(Register rd, Register rs, Register rt, String comment){
        writeTo(WriteTarget.TEXT, "sltu " + rd + ',' + rs + ',' + rt);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** set rt to 1 if rs less than imm and 0 otherwise */
    public void writeSltI(Register rt, Register rs,int imm, String comment){
        writeTo(WriteTarget.TEXT, "slti " + rt + ',' + rs + ',' + imm);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }  

    /** set rt to 1 if rs less than imm and 0 otherwise using unsigned comparison*/
    public void writeSltIU(Register rt, Register rs,int imm, String comment){
        writeTo(WriteTarget.TEXT, "sltiu " + rt + ',' + rs + ',' + imm);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }  

    /** shift right arithmetic (sign extended) */
    public void writeSra(Register rt, Register rs, int imm, String comment){
        writeTo(WriteTarget.TEXT, "sra " + rt + ',' + rs + ',' + imm);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** shift right logical */
    public void writeSrl(Register rd, Register rs, Register rt, String comment){
        writeTo(WriteTarget.TEXT, "srl " + rd + ',' + rs + ',' + rt);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** substract */
    public void writeSub(Register rd, Register rs, Register rt, String comment){
        writeTo(WriteTarget.TEXT, "sub " + rd + ',' + rs + ',' + rt);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** substract unsigned*/
    public void writeSubU(Register rd, Register rs, Register rt, String comment){
        writeTo(WriteTarget.TEXT, "subu " + rd + ',' + rs + ',' + rt);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** store word */
    public void writeSw(Register rt, Register rs,int offset, String comment){
        writeTo(WriteTarget.TEXT, "sw " + rt + ',' + offset + '(' + rs + ')');
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** syscall */
    public void writeSyscall(String comment){
        writeTo(WriteTarget.TEXT, "syscall ");
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** xor logical  */
    public void writeXor(Register rd, Register rt, Register rs, String comment){
        writeTo(WriteTarget.TEXT, "xor " + rd + ',' + rt + ',' + rs);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** xor logical immediate  */
    public void writeXorI(Register rd, Register rt, Register rs, String comment){
        writeTo(WriteTarget.TEXT, "xori " + rd + ',' + rt + ',' + rs);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** store immediate in upper 2 bytes of rt */
    public void writeLui(Register rt, int imm, String comment){
        writeTo(WriteTarget.TEXT, "lui " + rt + ',' + imm);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    
    }




    // PSEUDO INSTRUCTIONS

    public void writeLa(Register rt, String label,String comment){
        writeTo(WriteTarget.TEXT, "la " + rt + ',' + label);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    public void writeMove(Register rt, Register rs,String comment){
        writeTo(WriteTarget.TEXT, "move " + rt + ',' + rs);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** set t1 to 1 if rest is equal or to 0 otherwise */
    public void writeSeq(Register rd, Register rt, Register rs, String comment){
        writeTo(WriteTarget.TEXT, "seq " + rd + ',' + rt + ',' + rs);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
      
    }

    /** set t1 to 1 if rest is greater equal or to 0 otherwise */
    public void writeSge(Register rd, Register rt, Register rs, String comment){
        writeTo(WriteTarget.TEXT, "sge " + rd + ',' + rt + ',' + rs);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** set t1 to 1 if rest is greater than 0 otherwise */
    public void writeSgt(Register rd, Register rt, Register rs, String comment){
        writeTo(WriteTarget.TEXT, "sgt " + rd + ',' + rt + ',' + rs);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** set t1 to 1 if rest is less than or equal or to 0 otherwise */
    public void writeSle(Register rd, Register rt, Register rs, String comment){
        writeTo(WriteTarget.TEXT, "sle " + rd + ',' + rt + ',' + rs);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** set t1 to 1 if rest is less than or equal or to 0 otherwise */
    public void writeSne(Register rd, Register rt, Register rs, String comment){
        writeTo(WriteTarget.TEXT, "sne " + rd + ',' + rt + ',' + rs);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }
    
    /** branch if greater or equal  */
    public void writeBge(Register rd, int imm,String label, String comment){
        writeTo(WriteTarget.TEXT, "bge "  + rd + ',' + imm + ',' + label);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** branch if less than  */
    public void writeBlt(Register rd, int imm,String label, String comment){
        writeTo(WriteTarget.TEXT, "blt "  + rd + ',' + imm + ',' + label);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }

    /** branch equal  */
    public void writeBeq(Register rd, int imm,String label, String comment){
        writeTo(WriteTarget.TEXT, "beq "  + rd + ',' + imm + ',' + label);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }
    /** unconditional branch */
    public void writeB(String label, String comment){
        writeTo(WriteTarget.TEXT, "b " + label);
        writeComment(WriteTarget.TEXT,comment);
        writeNewline(WriteTarget.TEXT);
    }
}
