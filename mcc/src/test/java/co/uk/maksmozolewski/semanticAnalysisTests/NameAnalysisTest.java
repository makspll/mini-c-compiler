package co.uk.maksmozolewski.semanticAnalysisTests;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import co.uk.maksmozolewski.CompilerTest;

public class NameAnalysisTest extends CompilerTest {

    @Test
    public void testDuplicateVar() throws FileNotFoundException, IOException {
        assertProgramNameAnalysisErrorCount("int h;int h;", 1);
    }

    @Test
    public void testShadowingNoError() throws FileNotFoundException, IOException {
        assertProgramNameAnalysisErrorCount("int h; int main(){int h}", 0);
    }

    @Test
    public void testDifferentBlocksDuplicateVarNoError() throws FileNotFoundException, IOException {
        assertProgramNameAnalysisErrorCount("int main(){ if(){int h} if(){int h}}", 0);
    }

    @Test
    public void testUndeclaredVarError() throws FileNotFoundException, IOException {
        assertProgramNameAnalysisErrorCount("int main(){h = 2;}", 1);
    }

    @Test
    public void testDeclarationOfOtherTypeError() throws FileNotFoundException, IOException {
        assertProgramNameAnalysisErrorCount("struct hemlo{}; int hemlo;", 1);
    }

    @Test
    public void testDeclarationOfOtherTypeError2() throws FileNotFoundException, IOException {
        assertProgramNameAnalysisErrorCount("int hemlo; int hemlo(){}", 1);
    }

    @Test
    public void testDeclarationDuplicateBlockScope() throws FileNotFoundException, IOException {
        assertProgramNameAnalysisErrorCount("int main(){int a; int a;}", 1);
    }

    @Test
    public void testDeclaredVariableUsageNoError() throws FileNotFoundException, IOException {
        assertProgramNameAnalysisErrorCount("int h; int main(){h = 2;}", 0);
    }

    @Test
    public void testDeclaredVariableUsageNoError2() throws FileNotFoundException, IOException {
        assertProgramNameAnalysisErrorCount("int main(){int h; h = h + h;}", 0);
    }


    // void print_s(char* s);
    // void print_i(int i);
    // void print_c(char c);
    // char read_c();
    // int read_i();
    // void* mcmalloc(int size);
    @Test
    public void testStandardLibraryFuncsNoError() throws FileNotFoundException, IOException {
        assertProgramNameAnalysisErrorCount("char* s; int i; char c; int size; int main(){print_s(s);print_i(i);print_c(c);read_c();read_i();mcmalloc(size);}", 0);
    }

    @Test
    public void testStandardLibraryFuncsShadowingNoError() throws FileNotFoundException, IOException {
        assertProgramNameAnalysisErrorCount("void print_s(){} void print_i(){} void print_c(){} void read_c(){} void read_i(){} void mcmalloc(){}", 0);
    }


    @Test
    public void testRecursionDefinition() throws FileNotFoundException, IOException {
        assertProgramNameAnalysisErrorCount("int rec(int a){if(a == 0){return 0;} else { return rec(a - 1);}}int main(){print_i(rec(10));}", 0);
    }
}
