package IC.SymbolTypes;


/**
 * base for all symbol types
 */
public abstract class SymbolType {
	@Override
	public abstract String toString();

	@Override
	public abstract boolean equals(Object obj);
	@Override
	public abstract int hashCode();

	// For printing the symbol types table.
	public abstract String getHeader();
	public abstract int getDisplaySortIndex();

    //needed in to string

    /**
     * added to end of string when needed
     * @return
     */
    public String additionalStringData() {
		return "";
	}

	// For type comparison
	public abstract boolean isReferenceType();

}
