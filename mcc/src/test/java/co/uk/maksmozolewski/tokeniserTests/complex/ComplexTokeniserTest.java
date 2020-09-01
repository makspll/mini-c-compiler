package co.uk.maksmozolewski.tokeniserTests.complex;

import co.uk.maksmozolewski.tokeniserTests.TokeniserTest;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import co.uk.maksmozolewski.lexer.Token;
import co.uk.maksmozolewski.lexer.Token.TokenClass;

public class ComplexTokeniserTest extends TokeniserTest {
    
        
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
}