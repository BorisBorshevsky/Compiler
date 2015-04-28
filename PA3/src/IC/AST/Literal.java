package IC.AST;

import IC.LiteralTypes;
import IC.Symbols.SymbolTable;

/**
 * Literal value AST node.
 * 
 * @author Tovi Almozlino
 */
public class Literal extends Expression {

	private LiteralTypes type;

	private Object value;

    private boolean parentIsUMinus;


	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

    @Override
    public <D, U> U accept(semanticCheckVisitor<D, U> v, D context) {
        return v.visit(this, context);
    }
	/**
	 * Constructs a new literal node.
	 * 
	 * @param line
	 *            Line number of the literal.
	 * @param type
	 *            Literal type.
	 */
	public Literal(int line, LiteralTypes type) {
		super(line);
		this.type = type;
		value = type.getValue();
	}

	/**
	 * Constructs a new literal node, with a value.
	 * 
	 * @param line
	 *            Line number of the literal.
	 * @param type
	 *            Literal type.
	 * @param value
	 *            Value of literal.
	 */
	public Literal(int line, LiteralTypes type, Object value) {
		this(line, type);
		this.value = value;
	}

	public LiteralTypes getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}


    public void setType(LiteralTypes type) {
        this.type = type;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isParentIsUMinus() {
        return parentIsUMinus;
    }

    public void setParentIsUMinus(boolean parentIsUMinus) {
        this.parentIsUMinus = parentIsUMinus;
    }

    public SymbolTable getParent() {
        return parent;
    }

    public void setParent(SymbolTable parent) {
        this.parent = parent;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public void yourParentIsUMinus() {
        this.parentIsUMinus = true;
    }

    public boolean isParentUMinus() {
        return parentIsUMinus;
    }

}
