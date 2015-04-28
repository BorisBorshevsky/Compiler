package IC.AST;

import IC.Symbols.SymbolTable;

/**
 * Abstract AST node base class.
 * 
 * @author Tovi Almozlino
 */
public abstract class ASTNode {

	private int line;

	/**
	 * Double dispatch method, to allow a visitor to visit a specific subclass.
	 * 
	 * @param visitor
	 *            The visitor.
	 * @return A value propagated by the visitor.
	 */
	public abstract Object accept(Visitor visitor);

    /** accept propagating visitor **/
    public abstract <D, U> U accept(semanticCheckVisitor<D, U> v, D context);

	/**
	 * Constructs an AST node corresponding to a line number in the original
	 * code. Used by subclasses.
	 * 
	 * @param line
	 *            The line number.
	 */
    ASTNode(int line) {
		this.line = line;
	}

	public int getLine() {
		return line;
	}

    public void setLine(int line) {
        this.line = line;
    }

    SymbolTable parent;

    public SymbolTable getParent() {
        return parent;
    }

    public void setParent(SymbolTable parent) {
        this.parent = parent;
    }
}
