package com.ibm.wala.cast.python.test;

import java.io.IOException;

import org.junit.Test;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;

public class TestLambda extends TestPythonCallGraphShape {

	 protected static final Object[][] assertionsLambda1 = new Object[][] {
		    new Object[] { ROOT, new String[] { "script lambda1.py" } },
		    new Object[] {
		        "script lambda1.py",
		        new String[] { "script lambda1.py/Foo", "$script lambda1.py/Foo/foo:trampoline3", "script lambda1.py/lambda1", "script lambda1.py/lambda2" } },
		    new Object[] {
		    	"$script lambda1.py/Foo/foo:trampoline3",
		    	new String[] { "script lambda1.py/Foo/foo" } }
	 };

	@Test
	public void testLambda1() throws ClassHierarchyException, IllegalArgumentException, CancelException, IOException {
		CallGraph CG = process("lambda1.py");
		verifyGraphAssertions(CG, assertionsLambda1);
	}

}
