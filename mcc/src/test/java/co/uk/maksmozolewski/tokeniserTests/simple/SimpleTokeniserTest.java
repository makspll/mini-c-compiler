package co.uk.maksmozolewski.tokeniserTests.simple;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import co.uk.maksmozolewski.lexer.Scanner;
import co.uk.maksmozolewski.lexer.Token;
import co.uk.maksmozolewski.lexer.Tokeniser;
import co.uk.maksmozolewski.lexer.Token.TokenClass;
import co.uk.maksmozolewski.CompilerTest;

public class SimpleTokeniserTest extends CompilerTest {
    
    private Scanner testScanner;
    private String testFile = new String(
        "Hello World\nLine 2\nFurther text"
    );


    @Test
    public void testSTRING_LITERAL() throws FileNotFoundException,IOException {
        String data = "\"i\'m a string,420, \t,\b \n \r \f \0 \"";
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.STRING_LITERAL,data,1,0),
            new Token(TokenClass.EOF,null,2,8)
        }, data);

    }

    @Test
    public void testINT_LITERAL() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.INT_LITERAL, "1234567890",1,0),
            new Token(TokenClass.EOF,null, 1,10)
        }, "1234567890");
    }

    @Test
    public void testCHAR_LITERAL() throws FileNotFoundException, IOException{
        String data = "\'c\'";
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.CHAR_LITERAL,data, 1, 0),
            new Token(TokenClass.EOF,null,1,3)
        }, data);
    }

    @Test
    public void testIDENTIFIER() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.IDENTIFIER,"new_var_1234567_im_var",1,0),
            new Token(TokenClass.EOF,null,1,22)
        }, "new_var_1234567_im_var");
    }

    @Test
    public void testINT() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.INT,null,1,0),
            new Token(TokenClass.EOF,null,1,3)
        }, "int");
    }


    @Test
    public void testVOID() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.VOID,null,1,0),
            new Token(TokenClass.EOF,null,1,4)
        }, "void");
    }

    @Test
    public void testCHAR() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.CHAR,null,1,0),
            new Token(TokenClass.EOF,null,1,4)
        }, "char");
    }
    @Test
    public void testIF() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.IF,null,1,0),
            new Token(TokenClass.EOF,null,1,2)
        }, "if");
    }
    @Test
    public void testELSE() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.ELSE,null,1,0),
            new Token(TokenClass.EOF,null,1,4)
        }, "else");
    }
    @Test
    public void testWHILE() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.WHILE,null,1,0),
            new Token(TokenClass.EOF,null,1,5)
        }, "while");
    }

    @Test
    public void testRETURN() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.RETURN,null,1,0),
            new Token(TokenClass.EOF,null,1,6)
        }, "return");
    }
    @Test
    public void testSTRUCT() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.STRUCT,null,1,0),
            new Token(TokenClass.EOF,null,1,6)
        }, "struct");
    }
    @Test
    public void testSIZEOF() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.SIZEOF,null,1,0),
            new Token(TokenClass.EOF,null,1,6)
        }, "sizeof");
    }

    @Test
    public void testAND() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.AND,null,1,0),
            new Token(TokenClass.EOF,null,1,2)
        }, "&&");
    }

    @Test
    public void testOR() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.OR,null,1,0),
            new Token(TokenClass.EOF,null,1,2)
        }, "||");
    }


    @Test
    public void testEQ() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.EQ,null,1,0),
            new Token(TokenClass.EOF,null,1,2)
        }, "==");
    }


    @Test
    public void testNE() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.NE,null,1,0),
            new Token(TokenClass.EOF,null,1,2)
        }, "!=");
    }


    @Test
    public void testLT() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.LT,null,1,0),
            new Token(TokenClass.EOF,null,1,1)
        }, "<");
    }


    @Test
    public void testGT() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.GT,null,1,0),
            new Token(TokenClass.EOF,null,1,1)
        }, ">");
    }


    @Test
    public void testLE() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.LE,null,1,0),
            new Token(TokenClass.EOF,null,1,2)
        }, "<=");
    }


    @Test
    public void testGE() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.GE,null,1,0),
            new Token(TokenClass.EOF,null,1,2)
        }, ">=");
    }


    @Test
    public void testASSIGN() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.ASSIGN,null,1,0),
            new Token(TokenClass.EOF,null,1,1)
        }, "=");
    }


    @Test
    public void testPLUS() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.PLUS,null,1,0),
            new Token(TokenClass.EOF,null,1,1)
        }, "+");
    }


    @Test
    public void testMINUS() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.MINUS,null,1,0),
            new Token(TokenClass.EOF,null,1,1)
        }, "-");
    }

    @Test
    public void testASTERIX() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.ASTERIX,null,1,0),
            new Token(TokenClass.EOF,null,1,1)
        }, "*");
    }


    @Test
    public void testDIV() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.DIV,null,1,0),
            new Token(TokenClass.EOF,null,1,1)
        }, "/");
    }


    @Test
    public void testREM() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.REM,null,1,0),
            new Token(TokenClass.EOF,null,1,1)
        }, "%");
    }


    @Test
    public void testDOT() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.DOT,null,1,0),
            new Token(TokenClass.EOF,null,1,1)
        }, ".");
    }

    @Test
    public void testLBRA() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.LBRA,null,1,0),
            new Token(TokenClass.EOF,null,1,1)
        }, "{");
    }

    @Test
    public void testRBRA() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.RBRA,null,1,0),
            new Token(TokenClass.EOF,null,1,1)
        }, "}");
    }

    @Test
    public void testLPAR() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.LPAR,null,1,0),
            new Token(TokenClass.EOF,null,1,1)
        }, "(");
    }


    @Test
    public void testRPAR() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.RPAR,null,1,0),
            new Token(TokenClass.EOF,null,1,1)
        }, ")");
    }


    @Test
    public void testLSBR() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.LSBR,null,1,0),
            new Token(TokenClass.EOF,null,1,1)
        }, "[");
    }


    @Test
    public void testRSBR() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.RSBR,null,1,0),
            new Token(TokenClass.EOF,null,1,1)
        }, "]");
    }


    @Test
    public void testSC() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.SC,null,1,0),
            new Token(TokenClass.EOF,null,1,1)
        }, ";");
    }

    @Test
    public void testCOMMA() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.COMMA,null,1,0),
            new Token(TokenClass.EOF,null,1,1)
        }, ",");
    }

    @Test
    public void testINCLUDE() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.INCLUDE,null,1,0),
            new Token(TokenClass.EOF,null,1,8)
        }, "#include");
    }

    @Test
    public void testOneLineComment() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.EOF,null,1,38)
        }, "// asdasdasda sdasdas dasd asd asd asd");
    }

    @Test
    public void testMultiLineComment() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.EOF,null,2,20)
        }, "/* asdasdasdasd \n asdqwijeiqjiajsd */");
    }

}