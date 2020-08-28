package co.uk.maksmozolewski;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Scanner {
    /** the file reader */
    private BufferedReader input;

    /** the peeked character if peeked and -1 otherwise */
    private int peekedVal = -1;

    /** current line in the file */
    private int line = 1;

    /** current column in the file */
    private int column = 0;

    public Scanner(File sourceFile) throws FileNotFoundException {
        input = new BufferedReader(new FileReader(sourceFile));
    }

    /**
     * Returns the next character in the file
     * @return
     */
    public char next() throws IOException{
        Boolean havePeeked = peekedVal != -1;
        
        char nextChar;
        if (havePeeked){
            nextChar = (char)peekedVal;
            peekedVal = -1;
        } else {
            int nextVal = input.read();
            if(nextVal == -1){
                throw new EOFException();
            }
            nextChar = (char)nextVal;
        }

        return nextChar;
    }

    /**
     * Peeks the next character without advancing the scanner
     * @return next character
     */
    public char peek() throws IOException{
        
        Boolean havePeeked = peekedVal != -1;
        
        char peekedChar;
        if (havePeeked){
            peekedChar = (char) peekedVal;
        } else {
            int readVal = input.read();
            if(readVal == -1){
                throw new EOFException();
            }
            peekedChar = (char) readVal;
            peekedVal = readVal;
        }

        return peekedChar;
    }

    /**
     * Returns the current line within the file of the scanner starting at 1.
     * @return Current Line
     */
    public int getLine(){
        return line;
    }

    /**
     * Returns the current column within the file of the scanner starting at 0.
     * @return Current Column
     */
    public int getColumn(){
        return column;
    }

    /**
     * Closes the scanner.
     * @throws IOException
     */
    public void close() throws IOException{
        input.close();
    }
}