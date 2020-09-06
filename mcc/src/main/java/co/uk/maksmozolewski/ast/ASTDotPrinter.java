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
            writer.write(parentID);
            writer.write("->");
            writer.write(childID);
            writer.write(";\n");
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
        String nodeID = writeNodeLabel("StructType", st.structType);
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
        String nodeID = writeNodeLabel("StructTypeDecl",st.structType);
        
        List<String> nodes = new LinkedList<String>();
        for (VarDecl vd : st.varDecls) {
            nodes.add(vd.accept(this));
        }
        writeChildrenGraphs(nodeID, nodes.toArray(new String[nodes.size()]));

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

        String[] nodes = new String[b.varDecls.size() + b.stmnts.size()];
        for (int i = 0; i < b.varDecls.size(); i++) {
            nodes[i] = b.varDecls.get(i).accept(this);
        }

        for (int i = b.varDecls.size(); i < b.stmnts.size(); i++) {
            nodes[i] = b.stmnts.get(i).accept(this);
        }

        writeChildrenGraphs(nodeID,nodes);
        return nodeID;

    }

    @Override
    public String visitProgram(Program p) {
        writer.write("digraph program{\n");
        String nodeID = writeNodeLabel("Program", null);

        String[] nodes = new String[p.structTypeDecls.size() + p.varDecls.size() + p.funDecls.size()];
        for (int i = 0; i < p.structTypeDecls.size(); i++) {
            nodes[i] = p.structTypeDecls.get(i).accept(this);    
        }

        for (int i = p.structTypeDecls.size(); i < p.varDecls.size(); i++) {
            nodes[i] = p.varDecls.get(i).accept(this);    
        }

        for (int i = p.varDecls.size(); i < p.funDecls.size(); i++) {
            nodes[i] = p.funDecls.get(i).accept(this);    
        }

        writeChildrenGraphs(nodeID, nodes);

        writer.write("}");
        writer.flush();
        return nodeID;
    }

    @Override
    public String visitVarExpr(VarExpr v) {
        String nodeID = writeNodeLabel("VarExpr", v.name);
        
        writeChildrenGraphs(nodeID, v.vd.accept(this));

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

        if(r.stmt != null){
            writeChildrenGraphs(nodeID, r.stmt.accept(this));
        } 

        return nodeID;
    }

  
    
}

