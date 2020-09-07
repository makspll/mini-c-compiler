package co.uk.maksmozolewski.sem;

public abstract class Symbol {
	public String name;
	
	
	public Symbol(String name) {
		this.name = name;
	}

	public abstract boolean isVar();
	public abstract boolean isFunc();
	public abstract boolean isStruct();
}
