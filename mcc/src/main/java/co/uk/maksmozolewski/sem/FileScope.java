package co.uk.maksmozolewski.sem;

import java.util.HashMap;
import java.util.Map;

/** all things declared within a file */
public class FileScope implements Scope {
    GlobalScope gs;
    private Map<String, Symbol> symbolTable;

    public FileScope(GlobalScope gs){
        this.gs = gs;
        symbolTable = new HashMap<String,Symbol>();
    }

    @Override
    public Symbol lookup(String name) {
        Symbol symbol = symbolTable.get(name);
        return symbol == null ? gs.lookup(name) : symbol;
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
