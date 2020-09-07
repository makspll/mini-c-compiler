package co.uk.maksmozolewski.tokeniserTests.complex;

import co.uk.maksmozolewski.CompilerTest;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import co.uk.maksmozolewski.lexer.Token;
import co.uk.maksmozolewski.lexer.Token.TokenClass;

public class ComplexTokeniserTest extends CompilerTest {
    
        
    @Test
    public void testMultipleTokensEasy() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.INCLUDE,null,1,0),
            new Token(TokenClass.STRING_LITERAL,"\"hello\"",1,9),
            new Token(TokenClass.WHILE,null,2,0),
            new Token(TokenClass.LPAR,null,2,5),
            new Token(TokenClass.IDENTIFIER,"true",2,6),
            new Token(TokenClass.RPAR,null,2,10),
            new Token(TokenClass.LBRA,null,2,11),
            new Token(TokenClass.RETURN,null,2,12),
            new Token(TokenClass.INT_LITERAL,"1",2,19),
            new Token(TokenClass.RBRA,null,2,20),
            new Token(TokenClass.EOF,null,2,21)
        }, "#include \"hello\"\nwhile(true){return 1}");
    }

    @Test
    public void testExpressionTokens() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.VOID,null,1,0),
            new Token(TokenClass.IDENTIFIER,"hello",1,5),
            new Token(TokenClass.SC,null,1,10),
            new Token(TokenClass.IDENTIFIER,"mamma",1,11),
            new Token(TokenClass.ASSIGN,null,1,16),
            new Token(TokenClass.INT_LITERAL,"2",1,17),
            new Token(TokenClass.PLUS,null,1,18),
            new Token(TokenClass.INT_LITERAL,"2",1,19),
            new Token(TokenClass.EOF,null,1,20)
        },"void hello;mamma=2+2"); 
    }

    @Test
    public void testHelloWorldTokens() throws FileNotFoundException, IOException {

        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.INCLUDE,null,1,0),
            new Token(TokenClass.STRING_LITERAL,"\"asd\"",1,9),
            new Token(TokenClass.INT,null,2,0),
            new Token(TokenClass.IDENTIFIER,"main",2,4),
            new Token(TokenClass.LPAR,null,2,8),
            new Token(TokenClass.RPAR,null,2,9),
            new Token(TokenClass.LBRA,null,2,10),
            new Token(TokenClass.IDENTIFIER,"printf",4,3),
            new Token(TokenClass.LPAR,null,4,9),
            new Token(TokenClass.STRING_LITERAL,"\"Hello, World! \n\"",4,10),
            new Token(TokenClass.RPAR,null,4,28),
            new Token(TokenClass.SC,null,4,29),
            new Token(TokenClass.RETURN,null,5,3),
            new Token(TokenClass.INT_LITERAL,"0",5,10),
            new Token(TokenClass.SC,null,5,11),
            new Token(TokenClass.RBRA,6,0),
            new Token(TokenClass.EOF,6,1),

        }, 
            "#include \"asd\"" +"\n"+
            "int main(){" + "\n"+
            "   /* my first program in mini-C */" + "\n" +
            "   printf(\"Hello, World! \\n\");" + "\n" +
            "   return 0;" + "\n}"
        );
    }

    @Test
    public void testIdentifiers() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
            new Token(TokenClass.INT,null,1,0),
            new Token(TokenClass.IDENTIFIER,"i",1,4),
            new Token(TokenClass.SC,null,1,5)
        }, "int i;");
    }

}