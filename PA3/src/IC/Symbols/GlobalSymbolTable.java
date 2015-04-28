package IC.Symbols;

import IC.SymbolTypes.SymbolTypeTable;

public class GlobalSymbolTable extends SymbolTable {

	public GlobalSymbolTable(String name, SymbolTypeTable typeTable) {
		super("Global", typeTable);
        setProgramName(name);

	}

    private String programName;

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    @Override
	protected String getSymbolTableTypeString() {
		return "Global Symbol Table";
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString() + "\n" + getTypeTable().toString();
	}
}
