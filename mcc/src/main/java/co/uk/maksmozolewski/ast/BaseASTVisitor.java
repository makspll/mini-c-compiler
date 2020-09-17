package co.uk.maksmozolewski.ast;

import java.util.List;

public abstract class BaseASTVisitor<T> implements ASTVisitor<T> {
    
    protected void visitAll(List<? extends ASTNode>...subtrees){
		for (List<? extends ASTNode> list : subtrees) {
			for (ASTNode node : list) {
				if(node != null){
					node.accept(this);

				}
			}
		}
	}
}
