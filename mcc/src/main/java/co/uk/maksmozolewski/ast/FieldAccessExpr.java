package co.uk.maksmozolewski.ast;

public class FieldAccessExpr extends Expr {


    public final Expr structure;
    public final String fieldName;

    public FieldAccessExpr(Expr structure, String fieldName) {
        this.structure = structure;
        this.fieldName = fieldName;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitFieldAccessExpr(this);
    }
    
}
