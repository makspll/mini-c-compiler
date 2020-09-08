package co.uk.maksmozolewski;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import co.uk.maksmozolewski.ast.ASTDotPrinter;
import co.uk.maksmozolewski.ast.BaseType;
import co.uk.maksmozolewski.ast.Block;
import co.uk.maksmozolewski.ast.FunDecl;
import co.uk.maksmozolewski.ast.PointerType;
import co.uk.maksmozolewski.ast.Program;
import co.uk.maksmozolewski.ast.Stmt;
import co.uk.maksmozolewski.ast.VarDecl;
import co.uk.maksmozolewski.lexer.Scanner;
import co.uk.maksmozolewski.lexer.Token;
import co.uk.maksmozolewski.lexer.Tokeniser;
import co.uk.maksmozolewski.lexer.Token.TokenClass;
import co.uk.maksmozolewski.parser.Parser;
import co.uk.maksmozolewski.sem.SemanticAnalyzer;

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
        Parser parser = new Parser(tokeniser);
        Program ast = parser.parse();

        PrintWriter writer = new PrintWriter(outputFile);
        ASTDotPrinter dotPrinter = new ASTDotPrinter(writer);

        try {
            ast.accept(dotPrinter);
        } catch (Exception e) {
            
        }

        writer.close();
        

        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec("dot -Tpng out.txt -o program.png");
        
        SemanticAnalyzer semAnalyser = new SemanticAnalyzer();
        int semErrCount = semAnalyser.analyze(ast);
        System.out.println("Done with: " + (tokeniser.getErrorCount() + parser.getErrorCount() + semErrCount) +" errors.");     
    }
}