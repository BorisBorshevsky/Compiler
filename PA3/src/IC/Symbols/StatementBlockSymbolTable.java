package IC.Symbols;

import IC.SymbolTypes.SymbolTypeTable;

public class StatementBlockSymbolTable extends SymbolTable {
    private String statmentOutputString;

    public StatementBlockSymbolTable(SymbolTypeTable typeTable) {
		super("statement block", typeTable);
	};

	public void setParentName(String parentName) {
		this.setName("statement block in " + parentName);
	}

    public void setStatementOutputString(String parentName){
        statmentOutputString = "( located in " + parentName + " )";
    }

    public String getStatmentOutputString() {
        return statmentOutputString;
    }

    @Override
	protected String getSymbolTableTypeString() {
		return "Statement Block Symbol Table";
	}


}
