package co.uk.maksmozolewski;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import co.uk.maksmozolewski.gen.CodeGenerator;
import co.uk.maksmozolewski.lexer.Scanner;
import co.uk.maksmozolewski.lexer.Token;
import co.uk.maksmozolewski.lexer.Tokeniser;
import co.uk.maksmozolewski.lexer.Token.TokenClass;
import co.uk.maksmozolewski.parser.Parser;

/**
 * The Main file implies an interface for the subsequent components, e.g. * The
 * Tokeniser must have a constructor which accepts a Scanner, moreover Tokeniser
 * must provide a public method getErrorCount which returns the total number of
 * lexing errors.
 */
public class Main {
    private static final int FILE_NOT_FOUND = 2;
    private static final int MODE_FAIL = 254;
    private static final int LEXER_FAIL = 250;
    private static final int PARSER_FAIL = 245;
    private static final int SEM_FAIL = 240;
    private static final int PASS = 0;

    private enum Mode {
        LEXER, PARSER, AST, SEMANTICANALYSIS, GEN
    }

    private static void usage() {
        System.out.println("Usage: java " + Main.class.getSimpleName() + " inputfile outputfile");
        System.exit(-1);
    }

    public static void main(String[] args) throws IOException {

        if (args.length != 2)
            usage();

        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);

        Scanner scanner;
        try {
            scanner = new Scanner(inputFile);
        } catch (FileNotFoundException e) {
            System.out.println("File "+inputFile.toString()+" does not exist.");
            System.exit(FILE_NOT_FOUND);
            return;
        }

        Tokeniser tokeniser = new Tokeniser(scanner);

        ArrayList<Token> tokens = new ArrayList<Token>();

        for(Token t = tokeniser.nextToken(); t.tokenClass != TokenClass.EOF; t = tokeniser.nextToken()){
            tokens.add(t);
        }


        FileWriter tokenizerOutFileWriter = new FileWriter(outputFile);
        for (Token token : tokens) {
            tokenizerOutFileWriter.write(token.toString() + " ");
        }

        tokenizerOutFileWriter.close();
        
        System.out.println("Done with: " + tokeniser.getErrorCount() + " errors.");     
    }
}