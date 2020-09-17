package co.uk.maksmozolewski;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import co.uk.maksmozolewski.gen.CodeGenerator;
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
		
    public static final List<FunDecl> stlib = new LinkedList<FunDecl>(Arrays.asList(
        new FunDecl(BaseType.VOID,
            "print_c",
            new LinkedList<VarDecl>(Arrays.asList(
            new VarDecl(BaseType.CHAR,"c"))),
            new Block(new LinkedList<VarDecl>(),new LinkedList<Stmt>())),
        new FunDecl(BaseType.VOID, 
            "print_s", 
            new LinkedList<VarDecl>(Arrays.asList(
                new VarDecl(new PointerType(BaseType.CHAR), "s"))), 
            new Block(new LinkedList<VarDecl>(),new LinkedList<Stmt>())
        ),
        new FunDecl(BaseType.VOID,
            "print_i",
            new LinkedList<VarDecl>(Arrays.asList(
                new VarDecl(BaseType.INT,"i"))
            ),
            new Block(new LinkedList<VarDecl>(),new LinkedList<Stmt>())
        ),
        new FunDecl(BaseType.CHAR,
            "read_c",
            new LinkedList<VarDecl>(),
            new Block(new LinkedList<VarDecl>(),new LinkedList<Stmt>())),
        new FunDecl(BaseType.INT,
            "read_i",
            new LinkedList<VarDecl>(),
            new Block(new LinkedList<VarDecl>(),new LinkedList<Stmt>())),
        new FunDecl(new PointerType(BaseType.VOID),
            "mcmalloc",
            new LinkedList<VarDecl>(Arrays.asList(
                new VarDecl(BaseType.INT, "size")
                
            )),
            new Block(new LinkedList<VarDecl>(),new LinkedList<Stmt>()))
    ));

    private static void usage() {
        System.out.println("Usage: java " + Main.class.getSimpleName() + " inputfile outputfile");
        System.exit(-1);
    }

    public static void main(String[] args) throws IOException {

        if (args.length != 2 )
            usage();

        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);

        Scanner scanner;
        try {
            scanner = new Scanner(inputFile);
        } catch (FileNotFoundException e) {
            System.err.println("File "+inputFile.toString()+" does not exist.");
            System.exit(FILE_NOT_FOUND);
            return;
        }
        System.out.println("Compiling...");
        Tokeniser tokeniser = new Tokeniser(scanner);
        Parser parser = new Parser(tokeniser);
        Program program = parser.parse();

        // write tree to outfile.astdot
        
        // analyze code
        SemanticAnalyzer semAnalyser = new SemanticAnalyzer();
        int semErrCount = semAnalyser.analyze(program);

        // generate code if success
        if(semErrCount == 0){
            CodeGenerator codeGen = new CodeGenerator(outputFile);
            codeGen.emitProgram(program);
        }

        System.out.println("Done with: " + (tokeniser.getErrorCount() + parser.getErrorCount() + semErrCount) +" errors.");     
    
        
        // draw ast
        File astFile = new File(args[1] + ".astdump");
        
        PrintWriter writer = new PrintWriter(astFile);

        ASTDotPrinter dotPrinter = new ASTDotPrinter(writer);

        try {
            program.accept(dotPrinter);
        } catch (Exception e) {
            System.err.println("DotPrinter encountered an exception, continuing.");
        }

        writer.close();
        // draw tree to outfile.ast.png
        Runtime rt = Runtime.getRuntime();
        String dotDrawCommand = "dot -Tpng "+ args[1] + ".astdump -o " + args[1] + ".ast.png";
        Process dotProcess = rt.exec(dotDrawCommand);
        
        BufferedReader stdInput = new BufferedReader(new 
                InputStreamReader(dotProcess.getInputStream()));
   
        BufferedReader stdError = new BufferedReader(new 
                InputStreamReader(dotProcess.getErrorStream()));
        
        // Read the output from the command
        System.out.println("Drawing AST tree");

     }
}