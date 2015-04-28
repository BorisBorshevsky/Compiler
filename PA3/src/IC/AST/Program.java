package IC.AST;

import IC.Symbols.GlobalSymbolTable;

import java.util.List;

/**
 * Root AST node for an IC program.
 * 
 * @author Tovi Almozlino
 */
public class Program extends ASTNode {

	private List<ICClass> classes;

    @Override
    public <D, U> U accept(semanticCheckVisitor<D, U> v, D context) {
        return v.visit(this, context);
    }

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a new program node.
	 * 
	 * @param classes
	 *            List of all classes declared in the program.
	 */
	public Program(List<ICClass> classes) {
		super(0);
		this.classes = classes;
	}

	public List<ICClass> getClasses() {
		return classes;
	}


    private GlobalSymbolTable symbolTable;

    public GlobalSymbolTable getGlobalSymbolTable() {
        return symbolTable;
    }

    public void setGlobalSymbolTable(GlobalSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

}
