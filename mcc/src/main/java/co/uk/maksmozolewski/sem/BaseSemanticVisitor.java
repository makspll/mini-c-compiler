package co.uk.maksmozolewski.sem;

import java.util.List;

import co.uk.maksmozolewski.ast.ASTNode;
import co.uk.maksmozolewski.ast.BaseASTVisitor;

/**
 * 
 * @author dhil A base class providing basic error accumulation.
 */
public abstract class BaseSemanticVisitor<T> extends BaseASTVisitor<T> implements SemanticVisitor<T>{
	private int errors;
	
	protected void visitAll(List<? extends ASTNode>...subtrees){
		for (List<? extends ASTNode> list : subtrees) {
			if(list != null){
				for (ASTNode node : list) {
					if(node != null){
						node.accept(this);
	
					}
				}
			}
			
		}
	}
	public BaseSemanticVisitor() {
		errors = 0;
	}
	
	public int getErrorCount() {
		return errors;
	}
	
	protected void error(String message) {
		System.err.println("semantic error: " + message);
		errors++;
	}
}
