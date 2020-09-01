package co.uk.maksmozolewski.tokeniserTests;

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

public class TokeniserTest {

    protected Tokeniser testTokeniser;
    protected Scanner testScanner;
    protected Path tempFile;

    @BeforeEach
    public void setup(@TempDir Path tempDir) {
        // create temp file for testing
        tempFile = tempDir.resolve("testFile");
    }

    protected void setupTokenizer(String fileContent) throws FileNotFoundException,IOException {
        Files.writeString(tempFile,fileContent);
        testScanner = new Scanner(tempFile.toFile());
        testTokeniser = new Tokeniser(testScanner);
    }

    protected void assertTokenEquals(String message,Token expected, Token result){
        assertAll( message,
            ()->{assertEquals(expected.data, result.data,"Token data did not match");},
            ()->{assertEquals(expected.position, result.position,"Token position did not match");},
            ()->{assertEquals(expected.tokenClass, result.tokenClass,"Token class did not match");}
        );
    }


    protected void assertTokenizerOutput(Token[] expectedOutput,String input) throws FileNotFoundException, IOException {
        setupTokenizer(input);
        ArrayList<Token> currOutput = new ArrayList<Token>();
        for (Token token : expectedOutput) {
            currOutput.add(token);
            assertTokenEquals("Tokens did not match at: " + token.position.getLine() + ":" + token.position.getColumn(),token, testTokeniser.nextToken());
        }

        assertFalse(currOutput.size() < expectedOutput.length,"output is too short, not enough tokens." );
        assertFalse(currOutput.size() > expectedOutput.length,"output is too long, too many tokens." );

    }



}