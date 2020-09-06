package co.uk.maksmozolewski.ast;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


public class ASTPrinter implements ASTVisitor<Void> {

    private PrintWriter writer;

    public ASTPrinter(PrintWriter writer) {
            this.writer = writer;
    }

    /**
     * utility for writing trees which only consist of ASTNodes themselves
     * @param rootName
     * @param subtrees
     */
    private void writeNodes(ASTNode...subtrees){
        String sep = "";
        for (ASTNode astNode : subtrees) {
            if(astNode != null){
                writer.write(sep);
                astNode.accept(this);
                if(sep == "")sep = ",";
            }
        }
    }

    /**
     * utility for writing trees which consist of lists of ASTNodes themselves
     * @param rootName
     * @param subtrees
     */
    private void writeNodesLists(List<ASTNode> ...subtrees){
        String sep = "";
        for(List<ASTNode> subtree : subtrees){
            writer.write(sep);
            writeNodes(subtree.toArray(new ASTNode[subtree.size()]));
            sep = ",";
        }
    }

    @Override
    public Void visitBlock(Block b) {
        writer.print("Block(");
        List<ASTNode> nodes = new LinkedList<>(b.varDecls);
        nodes.addAll(b.stmnts); 
        writeNodesLists(nodes);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitFunDecl(FunDecl fd) {
        writer.print("FunDecl(");
        fd.funType.accept(this);

        writer.print(","+fd.name+",");
        for (VarDecl vd : fd.params) {
            vd.accept(this);
            writer.print(",");
        }
        
        fd.block.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitProgram(Program p) {
        writer.print("Program(");

        List<ASTNode> nodes = new LinkedList<ASTNode>(p.structTypeDecls);
        nodes.addAll(p.varDecls);
        nodes.addAll(p.funDecls);
        writeNodesLists(nodes);
        writer.print(")");
	    writer.flush();
        return null;
    }

    @Override
    public Void visitVarDecl(VarDecl vd){
        writer.print("VarDecl(");
        vd.varType.accept(this);
        writer.print(","+vd.varName);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitVarExpr(VarExpr v) {
        writer.print("VarExpr(");
        writer.print(v.name);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitBaseType(BaseType bt) {
        writer.print("BaseType(");
        writer.print(bt.toString());
        writer.print(")");
        return null;
    }

    @Override
    public Void visitStructTypeDecl(StructTypeDecl st) {
        writer.print("StructType(");
        writer.print(st.structType);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitPointerType(PointerType pt) {
        writer.print("PointerType(");
        pt.pointedToType.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitStructType(StructType st) {
        writer.print("StructType(");
        writer.print(st.structType);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitArrayType(ArrayType at) {

        writer.print("ArrayType(");
        at.innerType.accept(this);
        writer.print(',');
        writer.print(at.size);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitIntLiteral(IntLiteral il) {
        writer.print("IntLiteral(");
        writer.print(il.val);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitStrLiteral(StrLiteral sl) {
        writer.print("StrLiteral(");
        writer.print(sl.val);
        writer.print(")");        
        return null;
    }

    @Override
    public Void visitChrLiteral(ChrLiteral cl) {
        writer.print("ChrLiteral(");
        writer.print(cl.val);
        writer.print(")");        
        return null;
    }

    @Override
    public Void visitFunCallExpr(FunCallExpr fce){
        writer.print("FunCallExpr(");
        writer.print(fce.funName);
        for (Expr arg : fce.args) {
            writer.print(",");
            arg.accept(this);
        }
        writer.print(")");
        return null;
    }

    @Override
    public Void visitBinOp(BinOp bo) {
        writer.print("BinOp(");
        writeNodes(bo.lhs,bo.op,bo.rhs);
        writer.print(")");

        return null;
    }

    @Override
    public Void visitOp(Op o) {
        writer.write("Op(");
        writer.write(o.toString());
        writer.write(')');
        return null;
    }

    @Override
    public Void visitArrayAccessExpr(ArrayAccessExpr aae) {
        writer.write("ArrayAccessExpr(");
        writeNodes(aae.array,aae.idx);
        writer.write(")");
        return null;
    }

    @Override
    public Void visitFieldAccessExpr(FieldAccessExpr fae) {
        writer.write("FieldAccessExpr(");
        writeNodes(fae.structure);
        writer.write(",");
        writer.write(fae.fieldName);
        writer.write(")");
        return null;
    }

    @Override
    public Void visitValueAtExpr(ValueAtExpr vae) {
        writer.write("ValueAtExpr(");
        writeNodes(vae.ptr);
        writer.write(")");
        
        return null;
    }

    @Override
    public Void visitSizeOfExpr(SizeOfExpr sizeOfExpr) {
        writer.write("SizeOfExpr(");
        writeNodes(sizeOfExpr.val);
        writer.write(")");
        return null;
    }

    @Override
    public Void visitTypecastExpr(TypecastExpr typecastExpr) {
        writer.write("TypecastExpr(");
        writeNodes(typecastExpr.newType,typecastExpr.castedExpr);
        writer.write(")");
        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmt exprStmt) {
        writer.write("ExprStmt(");
        writeNodes(exprStmt.expr);
        writer.write(")");
        return null;
    }

    @Override
    public Void visitWhile(While w) {
        writer.write("While(");
        writeNodes(w.condition,w.stmt);
        writer.write(")");
        return null;
    }

    @Override
    public Void visitIf(If i) {
        writer.write("If(");
        writeNodes(i.condition,i.ifStmt,i.elseStmt);
        writer.write(")");
        return null;
    }

    @Override
    public Void visitAssign(Assign a) {
        writer.write("Assign(");
        writeNodes(a.lhs,a.rhs);
        writer.write(")");
        return null;
    }

    @Override
    public Void visitReturn(Return r) {
        writer.write("Return(");
        writeNodes(r.stmt);
        writer.write(")");
        return null;
    }
    
}
