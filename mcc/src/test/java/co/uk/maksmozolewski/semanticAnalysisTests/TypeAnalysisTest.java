package co.uk.maksmozolewski.semanticAnalysisTests;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import co.uk.maksmozolewski.CompilerTest;

public class TypeAnalysisTest extends CompilerTest {
    @Test
    public void testIllegalCastError() throws FileNotFoundException, IOException {
        assertProgramTypeAnalysisErrorCount("int main(){int h;h = (void)2;}",1 );
    }

    @Test 
    public void testCastNoError() throws FileNotFoundException, IOException {
        assertProgramTypeAnalysisErrorCount("int main(){int* h; int a[2]; h = (int*)a;}", 0);
    }

    @Test
    public void testCastNoError2() throws FileNotFoundException, IOException {
        assertProgramTypeAnalysisErrorCount("int main(){int* h; char* j; j = (char*)h;}",0);
    }

    @Test
    public void testWrongTypesBinaryOpsError() throws FileNotFoundException, IOException {
        assertProgramTypeAnalysisErrorCount("int main(){int h; char j; h = j + h;}",1);
    }

    @Test
    public void testIllegalTypeEqError() throws FileNotFoundException, IOException {
        assertProgramTypeAnalysisErrorCount("struct hell{}; int main(){struct hell h; char j; h = h == j;}",1);
    }
    @Test
    public void testIllegalTypeEqError2() throws FileNotFoundException, IOException {
        assertProgramTypeAnalysisErrorCount("struct hell{}; int main(){int h[2]; char j; h = h != j;}",1);
    }

    @Test
    public void testIllegalTypeEqError3() throws FileNotFoundException, IOException {
        assertProgramTypeAnalysisErrorCount("void func(){} int main() {char j; j = func() != j;}",1);
    }

    @Test public void testIllegalVarTypeError() throws FileNotFoundException, IOException {
        assertProgramTypeAnalysisErrorCount("void h;", 1);
    }

    @Test public void testIllegalAssignmentLhsError() throws FileNotFoundException, IOException {
        assertProgramTypeAnalysisErrorCount("int main(){int i; int* j; i+2=2;}", 1);
    }

    @Test public void testIllegalAssignmentLhsError2() throws FileNotFoundException, IOException {
        assertProgramTypeAnalysisErrorCount("int main(){int i; int* j; sizeof(int)=2;}", 1);
    }


    @Test public void testIllegalAssignmentLhsError3() throws FileNotFoundException, IOException {
        assertProgramTypeAnalysisErrorCount("int main(){int i; int* j; (int*)j=2;}", 2);
    }

    @Test public void testValidAssignmentLhsNoError3() throws FileNotFoundException, IOException {
        assertProgramTypeAnalysisErrorCount("struct hello{int h;}; int main(){struct hello a; int b; a.h = a.h;}",0);
    }
    
    @Test public void testValidAssignmentNoError() throws FileNotFoundException, IOException {
        assertProgramTypeAnalysisErrorCount("int main(){int i; int* j; *j=2;}",0);
    }


    @Test public void testValidAssignmentNoError2() throws FileNotFoundException, IOException {
        assertProgramTypeAnalysisErrorCount("int main(){int i; int* j; j[2]=2;}",0);
    }

    @Test public void testStructFieldTypeNoError() throws FileNotFoundException, IOException {
        assertProgramTypeAnalysisErrorCount("struct hello{int h;}; int main(){struct hello a; int b; b = a.h;}",0);
    }


}
