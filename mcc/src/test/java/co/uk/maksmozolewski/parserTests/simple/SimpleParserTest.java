package co.uk.maksmozolewski.parserTests.simple;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import co.uk.maksmozolewski.CompilerTest;

public class SimpleParserTest extends CompilerTest {

    @Test
    public void testIncludes() throws FileNotFoundException, IOException {
        setupParser("#include \"hello\" \n #include \"helloagain\"");
        testParser.parse();
        assertNoParserErrors();
    }

    @Test
    public void testIncludesError() throws FileNotFoundException, IOException {
        setupParser("#include 2");

        testParser.parse();
        assertParserErrorsCount(1);
    }

    @Test
    public void testStructDefs() throws FileNotFoundException, IOException {
        setupParser("struct hello { int asd; }; struct hello { int asd; char asd[2]; void asd; struct hello;};");
        testParser.parse();
        assertNoParserErrors();
    }

    @Test
    public void testStructDefsError() throws FileNotFoundException, IOException {
        setupParser("struct hello { }; struct hello { int asd; char asd[]; void asd; struct hello;;");
        testParser.parse();
        assertParserErrorsCount(3);
    }

    @Test
    public void testVarDecls() throws FileNotFoundException, IOException {
        setupParser("int hello; char hello; void hello; struct hello; int hello[2]; char hello[2]; void hello[2]; struct hello[2];");
        testParser.parse();
        assertNoParserErrors();
    }

    @Test
    public void testVarDeclsError() throws FileNotFoundException, IOException {
        setupParser("int hello; char hello; void hello; struct hello; int hello[]; char hello[]; void hello[]; struct hello[];");
        testParser.parse();
        assertParserErrorsCount(4);
    }
    @Test
    public void testFunDeclsOne() throws FileNotFoundException, IOException {
        setupParser("char createsumfin(){void hello;mamma=2+2;}");
        testParser.parse();
        assertNoParserErrors();
    }

    @Test
    public void testFunDeclsMany() throws FileNotFoundException, IOException {
        setupParser("int createSomething(){}char createsumfin(){void hello;mamma=2+2;}");
        testParser.parse();
        assertNoParserErrors();
    }

    @Test
    public void testIf() throws FileNotFoundException, IOException {
        setupParser("void main(){if(asd == 2){return 0;}}");
        testParser.parse();
        assertNoParserErrors();
    }

    @Test
    public void testIfElse() throws FileNotFoundException, IOException {
        setupParser("void main(){if(asd == 2){return 0;}else {return 1;}}");
        testParser.parse();
        assertNoParserErrors();
    }

    @Test
    public void testWhile() throws FileNotFoundException, IOException {
        setupParser("void main(){while(asd == 2){return 0;}}");
        testParser.parse();
        assertNoParserErrors();
    }

    @Test
    public void testVariousVariables() throws FileNotFoundException, IOException {
        setupParser("int mammamia[2]; char* mamma[2];  void main(){void* hello[2]; struct gleb[2];}");
        testParser.parse();
        assertNoParserErrors();
    }
}
