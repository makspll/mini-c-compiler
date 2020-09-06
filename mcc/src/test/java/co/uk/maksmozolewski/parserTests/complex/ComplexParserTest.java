package co.uk.maksmozolewski.parserTests.complex;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import co.uk.maksmozolewski.CompilerTest;
import co.uk.maksmozolewski.ast.BinOp;
import co.uk.maksmozolewski.ast.Program;

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

        Program program = testParser.parse();
        assertNoParserErrors();
        assertASTPrint(program, 
        "Program(FunDecl(BaseType(INT),main,Block(ExprStmt(FunCallExpr(printf,StrLiteral(Hello, World! \n))),Return(IntLiteral(0)))))");
        
    }


    @Test
    public void testLongExpressions() throws FileNotFoundException, IOException {
        setupParser(
            "#include \"asd\"" +"\n"+
            "int main(){" + "\n"+
            "   hello = (2 + 2) * 4 % 5 / 2 - 1 + func();" + "\n"+
            "   return 0;" + "\n}"
        );
        Program program = testParser.parse();
        assertNoParserErrors();
        assertASTPrint(program, 
        "Program(FunDecl(BaseType(INT),main,Block(ExprStmt(FunCallExpr(printf,StrLiteral(Hello, World! \n))),Return(IntLiteral(0)))))");
    }

}
