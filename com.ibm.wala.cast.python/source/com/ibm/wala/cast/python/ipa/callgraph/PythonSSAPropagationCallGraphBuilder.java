/******************************************************************************
 * Copyright (c) 2018 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *****************************************************************************/
package com.ibm.wala.cast.python.ipa.callgraph;

import com.ibm.wala.cast.ipa.callgraph.AstSSAPropagationCallGraphBuilder;
import com.ibm.wala.cast.ipa.callgraph.GlobalObjectKey;
import com.ibm.wala.cast.python.ir.PythonLanguage;
import com.ibm.wala.cast.python.ssa.PythonInstructionVisitor;
import com.ibm.wala.cast.python.ssa.PythonInvokeInstruction;
import com.ibm.wala.cast.python.ssa.PythonStoreProperty;
import com.ibm.wala.cast.python.types.PythonTypes;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.callgraph.propagation.AbstractFieldPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKeyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSABinaryOpInstruction;
import com.ibm.wala.util.strings.Atom;

public class PythonSSAPropagationCallGraphBuilder extends AstSSAPropagationCallGraphBuilder {

	public PythonSSAPropagationCallGraphBuilder(IClassHierarchy cha, AnalysisOptions options, IAnalysisCacheView cache,
			PointerKeyFactory pointerKeyFactory) {
		super(PythonLanguage.Python.getFakeRootMethod(cha, options, cache), options, cache, pointerKeyFactory);
	}

	@Override
	protected boolean useObjectCatalog() {
		return true;
	}

	@Override
	public GlobalObjectKey getGlobalObject(Atom language) {
		assert language.equals(PythonLanguage.Python.getName());
		return new GlobalObjectKey(cha.lookupClass(PythonTypes.Root));
	}

	@Override
	protected AbstractFieldPointerKey fieldKeyForUnknownWrites(AbstractFieldPointerKey fieldKey) {
		return null;
	}

	@Override
	protected boolean sameMethod(CGNode opNode, String definingMethod) {
	    return definingMethod.equals(opNode.getMethod().getReference().getDeclaringClass().getName().toString());
	}

	public static class PythonConstraintVisitor extends AstConstraintVisitor implements PythonInstructionVisitor {

		public PythonConstraintVisitor(AstSSAPropagationCallGraphBuilder builder, CGNode node) {
			super(builder, node);
		}

		@Override
		public void visitPythonInvoke(PythonInvokeInstruction inst) {
	        visitInvokeInternal(inst, new DefaultInvariantComputer());
		}

		@Override
		public void visitPythonStoreProperty(PythonStoreProperty inst) {
			newFieldWrite(node, inst.getArrayRef(), inst.getIndex(), inst.getValue());
		}
		
		
	}

	@Override
	protected void processCallingConstraints(CGNode caller, SSAAbstractInvokeInstruction instruction, CGNode target,
			InstanceKey[][] constParams, PointerKey uniqueCatchKey) {
		
		if (target.toString().contains("numpy_input_fn")) {
			System.err.println(target);
		}
		
		if (! (instruction instanceof PythonInvokeInstruction)) {
			super.processCallingConstraints(caller, instruction, target, constParams, uniqueCatchKey);
		} else {
			// positional parameters
			PythonInvokeInstruction call = (PythonInvokeInstruction) instruction;
			for(int i = 0; i < call.getNumberOfPositionalParameters(); i++) {
				PointerKey lval = getPointerKeyForLocal(target, i+1);
				if (constParams != null && constParams[i] != null) {
					InstanceKey[] ik = constParams[i];
					for (InstanceKey element : ik) {
						system.newConstraint(lval, element);
					}		
				} else {
					PointerKey rval = getPointerKeyForLocal(caller, call.getUse(i));
					getSystem().newConstraint(lval, assignOperator, rval);
				}
			}
			
			// keyword arguments
			for(int i = 0; i < call.getNumberOfTotalParameters() && i < target.getIR().getSymbolTable().getMaxValueNumber(); i++) {
				String[] names = target.getIR().getLocalNames(0, i+1);
				if (names != null) {
					for(String destName : names) {
						if ("x".equals(destName)) {
							System.err.println(target);
						}
						int src = call.getUse(destName);
						if (src != -1) {
							PointerKey lval = getPointerKeyForLocal(target, i+1);
							int p = call.getNumberOfPositionalParameters() + i - 1;
							if (constParams != null && constParams[p] != null) {
								InstanceKey[] ik = constParams[p];
								for (InstanceKey element : ik) {
									system.newConstraint(lval, element);
								}		
							} else {
								PointerKey rval = getPointerKeyForLocal(caller, src);
								getSystem().newConstraint(lval, assignOperator, rval);
							}
						}
					}
				}
			}
			
			// return values
			PointerKey rret = getPointerKeyForReturnValue(target);
			PointerKey lret = getPointerKeyForLocal(caller, call.getReturnValue(0));
			getSystem().newConstraint(lret, assignOperator, rret);
		}
	}

	@Override
	public PythonConstraintVisitor makeVisitor(CGNode node) {
		return new PythonConstraintVisitor(this, node);
	}

	public static class PythonInterestingVisitor extends AstInterestingVisitor implements PythonInstructionVisitor {
		public PythonInterestingVisitor(int vn) {
			super(vn);
		}

		@Override
		public void visitBinaryOp(final SSABinaryOpInstruction instruction) {
			bingo = true;
		}

		@Override
		public void visitPythonInvoke(PythonInvokeInstruction inst) {
			bingo = true;
		}
	}

	@Override
	protected InterestingVisitor makeInterestingVisitor(CGNode node, int vn) {
		return new PythonInterestingVisitor(vn);
	}

}
