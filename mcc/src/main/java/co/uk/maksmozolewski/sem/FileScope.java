package co.uk.maksmozolewski.sem;

import java.util.HashMap;
import java.util.Map;

public class FileScope implements Scope {
	private Map<String, Symbol> symbolTable;

    public FileScope(){
        symbolTable = new HashMap<String,Symbol>();
    }

    @Override
    public Symbol lookup(String name) {
        Symbol symbol = symbolTable.get(name);
        return symbol;
    }

    @Override
    public Symbol lookupCurrent(String name) {
        return symbolTable.get(name);
    }

    @Override
    public void put(Symbol sym) {
        symbolTable.put(sym.name, sym);
    }
	
    
}
