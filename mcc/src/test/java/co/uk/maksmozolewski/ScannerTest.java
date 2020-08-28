package co.uk.maksmozolewski;

import co.uk.maksmozolewski.lexer.Scanner;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

public class ScannerTest{

    private Scanner testScanner;
    private String testFile = new String(
        "Hello World\nLine 2\nFurther text"
    );

    @BeforeEach
    public void setup(@TempDir Path tempDir) throws FileNotFoundException,IOException {
        // create temp file for testing
        Path testFileDir = tempDir.resolve("testFile");
        Files.writeString(testFileDir,testFile);
        testScanner = new Scanner(testFileDir.toFile());
    }

    @Test
    public void testNext()
    {
        char currentChar;
        int line = 1;
        int col = 0;
        try {
            for(int i = 0; i < testFile.length(); i++){

                if( i == testFile.length()){
                    assertThrows(EOFException.class, ()->{testScanner.next();});
                } else {

                    currentChar = testScanner.next();
                    char rightChar = testFile.charAt(i);

                    if(rightChar == '\n'){
                        line ++;
                        col = 0;
                    } else {
                        col ++;
                    }

                    assertEquals(rightChar,currentChar);
                    assertEquals(line,testScanner.getLine());
                    assertEquals(col, testScanner.getColumn());
                }
                
            }
           
        } catch (IOException e) {
            fail("Exception was thrown");
        }
    }

    @Test
    public void testPeekAndNext(){
        char peekedChar;
        try {
            for(int i = 0; i < testFile.length(); i++){

                if( i == testFile.length()){
                    assertThrows(EOFException.class, ()->{testScanner.next();});
                } else {
                    peekedChar = testScanner.peek();
                    char rightChar = testFile.charAt(i);
                    assertEquals(rightChar,peekedChar);
                    char nextChar = testScanner.next();
                    assertEquals(peekedChar, nextChar);
                }
                
            }
           
        } catch (IOException e) {
            fail("Exception was thrown");
        }
    }

    @Test
    public void testClose(){
        assertThrows(IOException.class, ()->{
            testScanner.close();
            testScanner.next();
        });
    }
}