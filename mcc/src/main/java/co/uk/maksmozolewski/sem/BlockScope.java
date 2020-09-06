package co.uk.maksmozolewski.sem;

import java.util.HashMap;
import java.util.Map;

public class BlockScope implements Scope{
    private Scope outer;
	private Map<String, Symbol> symbolTable;

    public BlockScope(Scope outer){
        this.outer = outer;
        symbolTable = new HashMap<String,Symbol>();
    }

    @Override
    public Symbol lookup(String name) {
        Symbol symbol = symbolTable.get(name);
        return symbol == null ? outer.lookup(name) : symbol;
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
