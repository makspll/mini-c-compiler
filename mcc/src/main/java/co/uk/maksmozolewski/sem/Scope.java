package co.uk.maksmozolewski.sem;

import java.util.Map;

/**
 * Represents scopes amid the program
 */
public interface Scope {
	
	public Symbol lookup(String name);
	public Symbol lookupCurrent(String name);
	public void put(Symbol sym);
}
