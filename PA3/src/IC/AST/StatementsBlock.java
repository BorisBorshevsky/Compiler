package IC.AST;

import IC.Symbols.StatementBlockSymbolTable;

import java.util.List;

/**
 * Statements block AST node.
 * 
 * @author Tovi Almozlino
 */
public class StatementsBlock extends Statement {

	private List<Statement> statements;

    @Override
    public <D, U> U accept(semanticCheckVisitor<D, U> v, D context) {
        return v.visit(this, context);
    }

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a new statements block node.
	 * 
	 * @param line
	 *            Line number where block begins.
	 * @param statements
	 *            List of all statements in block.
	 */
	public StatementsBlock(int line, List<Statement> statements) {
		super(line);
		this.statements = statements;
	}

	public List<Statement> getStatements() {
		return statements;
	}


    StatementBlockSymbolTable symbolTable;

    public StatementBlockSymbolTable getStatementsBlockSymbolTable() {
        return symbolTable;
    }

    public void setBlockSymbolTable(StatementBlockSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }
}
