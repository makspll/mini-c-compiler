package co.uk.maksmozolewski.parserTests.complex;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import co.uk.maksmozolewski.CompilerTest;

public class ComplexParserTest extends CompilerTest {
    
    @Test
    public void testHelloWorld() throws FileNotFoundException, IOException {
        setupParser(
            "#include \"asd\"" +"\n"+
            "int main(){" + "\n"+
            "   /* my first program in mini-C */" + "\n" +
            "   printf(\"Hello, World! \\n\");" + "\n" +
            "   return 0;" + "\n}"
        );

        testParser.parse();
        assertNoParserErrors();
    }

    @Test
    public void testLongExpressions() throws FileNotFoundException, IOException {
        setupParser(
            "#include \"asd\"" +"\n"+
            "int main(){" + "\n"+
            "   hello = (2 + 2) * 4 % 5 / 2 - 1 + func();" + "\n"+
            "   return 0;" + "\n}"
        );
    }

}
