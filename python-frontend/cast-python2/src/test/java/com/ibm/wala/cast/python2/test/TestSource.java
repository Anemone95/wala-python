package com.ibm.wala.cast.python2.test;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;

public class TestSource extends TestPythonCallGraphShape {

	@Ignore
    @Test
	public void testSource1() throws ClassHierarchyException, IllegalArgumentException, CancelException, IOException {
		CallGraph CG = process("src1.py");
		CG.forEach((n) -> { System.err.println(n.getIR()); });
		//verifyGraphAssertions(CG, assertionsCalls1);
	}
	

}
