package co.uk.maksmozolewski.ASTPrinterTests.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.uk.maksmozolewski.CompilerTest;
import co.uk.maksmozolewski.ast.ASTNode;
import co.uk.maksmozolewski.ast.ASTPrinter;
import co.uk.maksmozolewski.ast.ArrayAccessExpr;
import co.uk.maksmozolewski.ast.ArrayType;
import co.uk.maksmozolewski.ast.Assign;
import co.uk.maksmozolewski.ast.BaseType;
import co.uk.maksmozolewski.ast.BinOp;
import co.uk.maksmozolewski.ast.Block;
import co.uk.maksmozolewski.ast.ChrLiteral;
import co.uk.maksmozolewski.ast.Expr;
import co.uk.maksmozolewski.ast.ExprStmt;
import co.uk.maksmozolewski.ast.FieldAccessExpr;
import co.uk.maksmozolewski.ast.FunCallExpr;
import co.uk.maksmozolewski.ast.If;
import co.uk.maksmozolewski.ast.IntLiteral;
import co.uk.maksmozolewski.ast.Op;
import co.uk.maksmozolewski.ast.PointerType;
import co.uk.maksmozolewski.ast.Return;
import co.uk.maksmozolewski.ast.SizeOfExpr;
import co.uk.maksmozolewski.ast.Stmt;
import co.uk.maksmozolewski.ast.StrLiteral;
import co.uk.maksmozolewski.ast.StructType;
import co.uk.maksmozolewski.ast.TypecastExpr;
import co.uk.maksmozolewski.ast.ValueAtExpr;
import co.uk.maksmozolewski.ast.VarDecl;
import co.uk.maksmozolewski.ast.VarExpr;
import co.uk.maksmozolewski.ast.While;


public class TestPrint extends CompilerTest {



    @Test
    public void testPrintBaseType(){
        assertASTPrint(BaseType.INT,"BaseType(INT)");
    }

    @Test
    public void testPrintStructType(){
        assertASTPrint(new StructType("struboi"), "StructType(struboi)");
    }


    @Test
    public void testPrintArrayType(){
        assertASTPrint(new ArrayType(BaseType.INT,2), "ArrayType(BaseType(INT),2)");
    }

    @Test
    public void testPrintPointerType(){
        assertASTPrint(new PointerType(BaseType.CHAR), "PointerType(BaseType(CHAR))");
    }
    
    @Test
    public void testPrintVarDecl(){
        assertASTPrint(new VarDecl(BaseType.VOID,"varman"), "VarDecl(BaseType(VOID),varman)");
    }

    @Test
    public void testPrintVarExp(){
        assertASTPrint(new VarExpr("varman"), "VarExpr(varman)");
    }

    @Test
    public void testPrintStrLiteral(){
        assertASTPrint(new StrLiteral("varman"), "StrLiteral(varman)");
    }
    @Test
    public void testPrintChrLiteral(){
        assertASTPrint(new ChrLiteral('a'), "ChrLiteral(a)");
    }
    @Test
    public void testPrintIntLiteral(){
        assertASTPrint(new IntLiteral(5), "IntLiteral(5)");
    }

    @Test
    public void testFunCallExpr(){
        assertASTPrint(new FunCallExpr("func",new ArrayList<Expr>(Arrays.asList(
            new StrLiteral("hello"),
            new StrLiteral("boie"),
            new StrLiteral("boyle")))),
            "FunCallExpr(func,StrLiteral(hello),StrLiteral(boie),StrLiteral(boyle))");
    }

    @Test
    public void testBinOp(){
        assertASTPrint(new BinOp(new IntLiteral(1), Op.ADD, 
                            new BinOp(new IntLiteral(2),Op.MUL,new IntLiteral(4))
                            ),"BinOp(IntLiteral(1),Op(ADD),BinOp(IntLiteral(2),Op(MUL),IntLiteral(4)))" 
        );
    }

    @Test
    public void testPrintArrayAccessExpr(){
        assertASTPrint(new ArrayAccessExpr(new IntLiteral(2),new IntLiteral(5)), "ArrayAccessExpr(IntLiteral(2),IntLiteral(5))");
    }

    @Test
    public void testPrintFieldAccessExpr(){
        assertASTPrint(new FieldAccessExpr(new StrLiteral("somestruc"), "field"),
            "FieldAccessExpr(StrLiteral(somestruc),field)");
    }

    @Test
    public void testPrintValueAtExpr(){
        assertASTPrint(new ValueAtExpr(new StrLiteral("somestruc")),
            "ValueAtExpr(StrLiteral(somestruc))");
    }

    @Test
    public void testPrintSizeOfExpr(){
        assertASTPrint(new SizeOfExpr(BaseType.CHAR),
            "SizeOfExpr(BaseType(CHAR))");
    }


    @Test
    public void testPrintTypecastExpr(){
        assertASTPrint(new TypecastExpr(BaseType.CHAR,new IntLiteral(2)),
            "TypecastExpr(BaseType(CHAR),IntLiteral(2))");
    }

    @Test
    public void testPrintExprStmt(){
        assertASTPrint(new ExprStmt(new VarExpr("asd")),
            "ExprStmt(VarExpr(asd))");
    }

    
    @Test
    public void testPrintBlock(){

        assertASTPrint(new Block(
            new LinkedList<VarDecl>(Arrays.asList( 
                new VarDecl(BaseType.CHAR, "asd"),
                new VarDecl(BaseType.INT, "bsd")
                )),
            new LinkedList<Stmt>(Arrays.asList(
                new ExprStmt(new VarExpr("hello"))
            ))),
            "Block(VarDecl(BaseType(CHAR),asd),VarDecl(BaseType(INT),bsd),ExprStmt(VarExpr(hello)))");
    }

    @Test
    public void testPrintWhile(){
        assertASTPrint(new While(new VarExpr("asd"), new ExprStmt(new VarExpr("basdd"))),
            "While(VarExpr(asd),ExprStmt(VarExpr(basdd)))");
    }

    @Test
    public void testPrintIf(){
        assertASTPrint(new If(new VarExpr("asd"), new ExprStmt(new VarExpr("basdd")), new ExprStmt(new VarExpr("asd"))),
            "If(VarExpr(asd),ExprStmt(VarExpr(basdd)),ExprStmt(VarExpr(asd)))");
    }

    @Test
    public void testPrintAssign(){
        assertASTPrint(new Assign(new VarExpr("asd"),new VarExpr("basd")),
            "Assign(VarExpr(asd),VarExpr(basd))");
    }

        
    @Test
    public void testPrintReturn(){
        assertASTPrint(new Return(new VarExpr("basd")),
            "Return(VarExpr(basd))");
    }

}
