package co.uk.maksmozolewski;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import co.uk.maksmozolewski.ast.ASTNode;
import co.uk.maksmozolewski.ast.ASTPrinter;
import co.uk.maksmozolewski.ast.Program;
import co.uk.maksmozolewski.lexer.Scanner;
import co.uk.maksmozolewski.lexer.Token;
import co.uk.maksmozolewski.lexer.Tokeniser;
import co.uk.maksmozolewski.parser.Parser;

public class CompilerTest {

    protected Tokeniser testTokeniser;
    protected Scanner testScanner;
    protected Parser testParser;

    protected ASTPrinter astPrinter;
    protected Path tempFile;

    protected PrintWriter testWriter;
    protected ByteArrayOutputStream stream;
    protected ASTPrinter printer;

    @BeforeEach
    public void setup(@TempDir Path tempDir) {
        // create temp file for testing
        tempFile = tempDir.resolve("testFile");
        stream = new ByteArrayOutputStream();
        testWriter = new PrintWriter(stream);
        printer = new ASTPrinter(testWriter);

    }

    protected void assertStreamContentEquals(String content){
        testWriter.flush();
        testWriter.close();
        assertEquals(content, stream.toString(),"Stream does not contain expected output");
    }

    protected void assertASTPrint(ASTNode n, String out){
        n.accept(printer);
        assertStreamContentEquals(out);
    }
    
    protected void setupTokenizer(String fileContent) throws FileNotFoundException,IOException {
        Files.writeString(tempFile,fileContent);
        testScanner = new Scanner(tempFile.toFile());
        testTokeniser = new Tokeniser(testScanner);
    }

    protected void setupParser(String fileContent) throws FileNotFoundException, IOException {
        setupTokenizer(fileContent);
        testParser = new Parser(testTokeniser);
    }

    protected void assertProgramAST(String code,String out) throws FileNotFoundException, IOException {
        setupParser(code);
        assertASTPrint(testParser.parse(),out);
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

    protected void assertNoParserErrors(){
        assertEquals(0,testParser.getErrorCount(),"Expected 0 errors");
    }

    protected void assertParserErrorsCount(int count){
        assertEquals(count, testParser.getErrorCount(),"Expected "+ count + " errors");
    }


}