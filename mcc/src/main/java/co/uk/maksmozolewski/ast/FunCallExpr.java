package co.uk.maksmozolewski.ast;

import java.util.List;

public class FunCallExpr extends Expr {

    public String funName;
    public List<Expr> args;

    public FunDecl funDecl; // to be filled by name analysis;

    public FunCallExpr(String funName, List<Expr> args){
        this.funName = funName;
        this.args = args;
    }


    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitFunCallExpr(this);
    }

    
}
