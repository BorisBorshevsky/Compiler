package IC.AST;

import IC.Parser.StringUtils;
import IC.Symbols.MethodSymbolTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for method AST nodes.
 * 
 * @author Tovi Almozlino
 */
public abstract class Method extends ASTNode {

	protected Type type;

	protected String name;

	protected List<Formal> formals;

	protected List<Statement> statements;

	/**
	 * Constructs a new method node. Used by subclasses.
	 * 
	 * @param type
	 *            Data type returned by method.
	 * @param name
	 *            Name of method.
	 * @param formals
	 *            List of method parameters.
	 * @param statements
	 *            List of method's statements.
	 */
	protected Method(Type type, String name, List<Formal> formals,
			List<Statement> statements) {
		super(type.getLine());
		this.type = type;
		this.name = name;
		this.formals = formals;
		this.statements = statements;
	}

	public Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        List<String> formalsTypeNames = new ArrayList<>();
        for (Formal formal : getFormals()) {
            String typeName = "";
            typeName += formal.getType().getName();
            for (int i = 0 ; i < formal.getType().getDimension(); i++){
                typeName += "[]";
            }
            formalsTypeNames.add(typeName);
        }
        builder.append(StringUtils.joinStrings(formalsTypeNames));
        builder.append(" -> ");
        builder.append(getType().getName());
        for (int i = 0 ; i < getType().getDimension(); i++){
            builder.append("[]");
        }
        builder.append("}");
        return builder.toString();
    }



    public List<Formal> getFormals() {
		return formals;
	}

	public List<Statement> getStatements() {
		return statements;
	}


    MethodSymbolTable symbolTable;

    public MethodSymbolTable getMethodSymbolTable() {
        return symbolTable;
    }

    public void setMethodSymbolTable(MethodSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }
}