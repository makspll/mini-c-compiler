package co.uk.maksmozolewski.gen;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import co.uk.maksmozolewski.ast.ASTVisitor;
import co.uk.maksmozolewski.ast.ArrayAccessExpr;
import co.uk.maksmozolewski.ast.ArrayType;
import co.uk.maksmozolewski.ast.Assign;
import co.uk.maksmozolewski.ast.BaseType;
import co.uk.maksmozolewski.ast.BinOp;
import co.uk.maksmozolewski.ast.Block;
import co.uk.maksmozolewski.ast.ChrLiteral;
import co.uk.maksmozolewski.ast.ExprStmt;
import co.uk.maksmozolewski.ast.FieldAccessExpr;
import co.uk.maksmozolewski.ast.FunCallExpr;
import co.uk.maksmozolewski.ast.FunDecl;
import co.uk.maksmozolewski.ast.If;
import co.uk.maksmozolewski.ast.IntLiteral;
import co.uk.maksmozolewski.ast.Op;
import co.uk.maksmozolewski.ast.PointerType;
import co.uk.maksmozolewski.ast.Program;
import co.uk.maksmozolewski.ast.Return;
import co.uk.maksmozolewski.ast.SizeOfExpr;
import co.uk.maksmozolewski.ast.Stmt;
import co.uk.maksmozolewski.ast.StrLiteral;
import co.uk.maksmozolewski.ast.StructType;
import co.uk.maksmozolewski.ast.StructTypeDecl;
import co.uk.maksmozolewski.ast.TypecastExpr;
import co.uk.maksmozolewski.ast.ValueAtExpr;
import co.uk.maksmozolewski.ast.VarDecl;
import co.uk.maksmozolewski.ast.VarExpr;
import co.uk.maksmozolewski.ast.While;

/** returns a list of return statements under the node which accepts the visitor in their natural order
 * . Also assigns the return.fd value for each return stmt found in each fundecl
 */
public class ReturnMatcher implements ASTVisitor<List<Return>> {

    private FunDecl currFunDecl; 
    @Override
    public List<Return> visitStructTypeDecl(StructTypeDecl st) {
        return new LinkedList<Return>();
    }

    @Override
    public List<Return> visitFunDecl(FunDecl p) {
        currFunDecl = p;
        List<Return> out = p.block.accept(this);
        currFunDecl = null;
        return out;
    }

    @Override
    public List<Return> visitVarDecl(VarDecl vd) {
        return new LinkedList<Return>();

    }

    @Override
    public List<Return> visitBlock(Block b) {
        List<Return> out = new LinkedList<Return>();

        for (Stmt s : b.stmnts) {
            out.addAll(s.accept(this));            
        }
        return out;
    }

    @Override
    public List<Return> visitProgram(Program p) {
        List<Return> out = new LinkedList<Return>();

        for (FunDecl f : p.funDecls) {
            out.addAll(f.accept(this));
        }

        return out;
    }

    @Override
    public List<Return> visitExprStmt(ExprStmt exprStmt) {
        return exprStmt.expr.accept(this);
    }

    @Override
    public List<Return> visitWhile(While w) {
        return w.stmt.accept(this);
    }

    @Override
    public List<Return> visitIf(If i) {
        List<Return> out = new LinkedList<Return>();
        out.addAll(i.ifStmt.accept(this));
        if(i.elseStmt!=null){
            out.addAll(i.elseStmt.accept(this));
        }
        return out;
    }

    @Override
    public List<Return> visitAssign(Assign a) {
        return new LinkedList<Return>();
    }

    @Override
    public List<Return> visitReturn(Return r) {
        r.fd = currFunDecl;
        return new LinkedList<Return>(Arrays.asList(r));
    }

    @Override
    public List<Return> visitVarExpr(VarExpr v) {
        return new LinkedList<Return>();
    }

    @Override
    public List<Return> visitFunCallExpr(FunCallExpr fce) {
        return new LinkedList<Return>();
    }

    @Override
    public List<Return> visitArrayAccessExpr(ArrayAccessExpr aae) {
        return new LinkedList<Return>();
    }

    @Override
    public List<Return> visitFieldAccessExpr(FieldAccessExpr fae) {
        return new LinkedList<Return>();
    }

    @Override
    public List<Return> visitValueAtExpr(ValueAtExpr vae) {
        return new LinkedList<Return>();
    }

    @Override
    public List<Return> visitSizeOfExpr(SizeOfExpr sizeOfExpr) {
        return new LinkedList<Return>();
    }

    @Override
    public List<Return> visitTypecastExpr(TypecastExpr typecastExpr) {
        return new LinkedList<Return>();
    }

    @Override
    public List<Return> visitIntLiteral(IntLiteral il) {
        return new LinkedList<Return>();
    }

    @Override
    public List<Return> visitStrLiteral(StrLiteral sl) {
        return new LinkedList<Return>();
    }

    @Override
    public List<Return> visitChrLiteral(ChrLiteral cl) {
        return new LinkedList<Return>();
    }

    @Override
    public List<Return> visitBinOp(BinOp bo) {
        List<Return> out =  new LinkedList<Return>();
        out.addAll(bo.lhs.accept(this));
        out.addAll(bo.rhs.accept(this));

        return out;
    }

    @Override
    public List<Return> visitOp(Op o) {
        return new LinkedList<Return>();
    }

    @Override
    public List<Return> visitBaseType(BaseType bt) {
        return new LinkedList<Return>();
    }

    @Override
    public List<Return> visitPointerType(PointerType pt) {
        return new LinkedList<Return>();
    }

    @Override
    public List<Return> visitStructType(StructType st) {
        return new LinkedList<Return>();
    }

    @Override
    public List<Return> visitArrayType(ArrayType at) {
        return new LinkedList<Return>();
    }

    
}
