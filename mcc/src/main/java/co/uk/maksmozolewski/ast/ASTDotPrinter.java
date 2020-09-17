package co.uk.maksmozolewski.ast;


import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


public class ASTDotPrinter implements ASTVisitor<String> {

    private PrintWriter writer;
    private int counter;

    public ASTDotPrinter(PrintWriter writer) {
            this.writer = writer;
    }

    private String writeNodeLabel(String label,String val){
        int id = counter++;
        writer.write("\t");
        writer.write(label);
        writer.write("" + id);
        writer.write(" [label=\"" + label+ (val == null ? "" : "("+val+")")+ "\"];\n");
        return(label+ ("" +id));
    }

    private void writeChildrenGraphs(String parentID,String...childrenIDS){
        for (String childID : childrenIDS) {
            writer.write('\t');
            writer.write(parentID == null ? "null" : parentID);
            writer.write("->");
            writer.write(childID == null ?"null" :childID);
            writer.write(";\n");
        }
    }

    private void writeSubtrees(String parentID,List<? extends ASTNode>...lists){
        
        for (List<? extends ASTNode> list : lists) {
            String[] nodeIds = new String[list.size()];
            for (int i = 0; i < nodeIds.length; i++) {
                nodeIds[i] =  list.get(i) == null ?"null" : list.get(i).accept(this);
            }
            writeChildrenGraphs(parentID, nodeIds);
        }
    }

    @Override
    public String visitBaseType(BaseType bt) {
        return writeNodeLabel("BaseType",bt.toString());
    }

    @Override
    public String visitPointerType(PointerType pt) {
        String nodeID = writeNodeLabel("PointerType",null);
        writeChildrenGraphs(nodeID, pt.pointedToType.accept(this));
        return nodeID;
        
    }

    @Override
    public String visitStructType(StructType st) {
        String nodeID = writeNodeLabel("StructType", st.structTypeIdentifier);
        return nodeID;
        
    }

    @Override
    public String visitArrayType(ArrayType at) {
        String nodeID = writeNodeLabel("ArrayType", "" + at.size);
        
        writeChildrenGraphs(nodeID, at.innerType.accept(this));
        
        return nodeID;

    }

    @Override
    public String visitStructTypeDecl(StructTypeDecl st) {
        String nodeID = writeNodeLabel("StructTypeDecl",null);
        
        writeChildrenGraphs(nodeID, st.structType.accept(this));
        writeSubtrees(nodeID,st.varDecls);

        return nodeID;
    }

    @Override
    public String visitFunDecl(FunDecl p) {
       String nodeID = writeNodeLabel("FunDecl", p.name);

       String[] nodes = new String[p.params.size() + 2];

       nodes[0] = p.funType.accept(this);
       for (int i = 1; i < p.params.size() + 1; i++) {
           nodes[i] = p.params.get(i).accept(this);
       }
       nodes[nodes.length - 1] = p.block.accept(this);

       writeChildrenGraphs(nodeID, nodes);

       return nodeID;
    }

    @Override
    public String visitVarDecl(VarDecl vd) {
        String nodeID = writeNodeLabel("VarDecl", vd.varName);
        writeChildrenGraphs(nodeID, vd.varType.accept(this));
        

        return nodeID;
    }

    @Override
    public String visitBlock(Block b) {
        String nodeID = writeNodeLabel("Block", null);

        writeSubtrees(nodeID, b.varDecls,b.stmnts);
        return nodeID;

    }



    @Override
    public String visitProgram(Program p) {
        writer.write("digraph program{\n");
        String nodeID = writeNodeLabel("Program", null);
        writeSubtrees(nodeID, p.structTypeDecls,p.varDecls,p.funDecls);
        writer.write("}");
        writer.flush();
        return nodeID;
    }

    @Override
    public String visitVarExpr(VarExpr v) {
        String nodeID = writeNodeLabel("VarExpr", v.name);
        
        return nodeID;
    }

    @Override
    public String visitIntLiteral(IntLiteral il) {
        String nodeID = writeNodeLabel("IntLiteral", ""+il.val);
        return nodeID;
    }

    @Override
    public String visitStrLiteral(StrLiteral sl) {
        String nodeID = writeNodeLabel("StrLiteral", sl.val);

        return nodeID;
    }

    @Override
    public String visitChrLiteral(ChrLiteral cl) {
        String nodeID = writeNodeLabel("ChrLiteral", ""+cl.val);

        return nodeID;
    }

    @Override
    public String visitFunCallExpr(FunCallExpr fce) {
        String nodeID = writeNodeLabel("FunCallExpr", fce.funName);

        writeChildrenGraphs(nodeID, fce.args.toArray(new String[fce.args.size()]));

        return nodeID;
    }

    @Override
    public String visitBinOp(BinOp bo) {
        String nodeID = writeNodeLabel("BinOp",null);

        writeChildrenGraphs(nodeID, bo.lhs.accept(this), bo.op.accept(this), bo.rhs.accept(this));

        return nodeID;
    }

    @Override
    public String visitOp(Op o) {
        String nodeID = writeNodeLabel("Op", o.toString());

        return nodeID;
    }

    @Override
    public String visitArrayAccessExpr(ArrayAccessExpr aae) {
        String nodeID = writeNodeLabel("ArrayAccessExpr",null);

        writeChildrenGraphs(aae.array.accept(this), aae.idx.accept(this));

        return nodeID;
    }

    @Override
    public String visitFieldAccessExpr(FieldAccessExpr fae) {
        String nodeID = writeNodeLabel("FieldAccessExpr", fae.fieldName);

        writeChildrenGraphs(nodeID, fae.structure.accept(this));

        return nodeID;
    }

    @Override
    public String visitValueAtExpr(ValueAtExpr vae) {
        String nodeID = writeNodeLabel("ValueAtExpr",null);

        writeChildrenGraphs(nodeID, vae.ptr.accept(this));

        return nodeID;
    }

    @Override
    public String visitSizeOfExpr(SizeOfExpr sizeOfExpr) {
        String nodeID = writeNodeLabel("SizeOfExpr",null);

        writeChildrenGraphs(nodeID, sizeOfExpr.val.accept(this));

        return nodeID;
    }

    @Override
    public String visitTypecastExpr(TypecastExpr typecastExpr) {
        String nodeID = writeNodeLabel("TypecastExpr",null);

        writeChildrenGraphs(nodeID, typecastExpr.newType.accept(this),typecastExpr.castedExpr.accept(this));

        return nodeID;
    }

    @Override
    public String visitExprStmt(ExprStmt exprStmt) {
        String nodeID = writeNodeLabel("ExprStmt",null);
        writeChildrenGraphs(nodeID, exprStmt.expr.accept(this));

        return nodeID;
    }

    @Override
    public String visitWhile(While w) {
        String nodeID = writeNodeLabel("While",null);

        writeChildrenGraphs(nodeID, w.condition.accept(this),w.stmt.accept(this));

        return nodeID;
    }

    @Override
    public String visitIf(If i) {
        String nodeID = writeNodeLabel("If",null);

        if(i.elseStmt != null){
            writeChildrenGraphs(nodeID, i.condition.accept(this),i.ifStmt.accept(this),i.elseStmt.accept(this));
        }{
            writeChildrenGraphs(nodeID, i.condition.accept(this),i.ifStmt.accept(this));
        }

        return nodeID;
    }

    @Override
    public String visitAssign(Assign a) {
        String nodeID = writeNodeLabel("Assign",null);

        writeChildrenGraphs(nodeID, a.lhs.accept(this),a.rhs.accept(this));

        return nodeID;
    }

    @Override
    public String visitReturn(Return r) {
        String nodeID = writeNodeLabel("Return",null);

        if(r.exp != null){
            writeChildrenGraphs(nodeID, r.exp.accept(this));
        } 

        return nodeID;
    }

  
    
}

