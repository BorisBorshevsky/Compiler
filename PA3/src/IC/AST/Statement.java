package IC.AST;

import IC.Symbols.SymbolTable;

/**
 * Abstract base class for statement AST nodes.
 * 
 * @author Tovi Almozlino
 */
public abstract class Statement extends ASTNode {

	/**
	 * Constructs a new statement node. Used by subclasses.
	 * 
	 * @param line
	 *            Line number of statement.
	 */
	protected Statement(int line) {
		super(line);
	}


    public SymbolTable getParent() {
        return parent;
    }

    public void setParent(SymbolTable parent) {
        this.parent = parent;
    }
}
