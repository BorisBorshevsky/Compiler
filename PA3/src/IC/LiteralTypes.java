package IC;

/**
 * Enum of the IC language's literal value types. Includes methods for creating
 * a string representation of each type of value.
 * 
 * @author Tovi Almozlino
 */
public enum LiteralTypes {

	INTEGER(DataTypes.INT.getDefaultValue(), "Integer literal", "int"),
	STRING(DataTypes.STRING.getDefaultValue(), "String literal", "string") {
		private void replaceEscapeSequences(StringBuffer string) {
			for (int i = 0; i < string.length(); ++i) {
				String replacement = String.valueOf(string.charAt(i));

				if (string.charAt(i) == '\"')
					replacement = "\\\"";
				else if (string.charAt(i) == '\\')
					replacement = "\\\\";
				else if (string.charAt(i) == '\n')
					replacement = "\\n";
				else if (string.charAt(i) == '\t')
					replacement = "\\t";
				string.replace(i, i + 1, replacement);
				i += replacement.length() - 1;
			}
		}

		public String toFormattedString(Object value) {
			if (value == null)
				return String.valueOf(value);
			StringBuffer formattedString = new StringBuffer(value.toString());

			replaceEscapeSequences(formattedString);
			return "\"" + formattedString.toString() + "\"";
		}
	},
	TRUE(true, "Boolean literal", "boolean"),
	FALSE(false, "Boolean literal", "boolean"),
	NULL(null, "Literal", "null");

    public String getShortName() {
        return shortName;
    }

    private String shortName;

    private Object value;
	
	private String description;

	private LiteralTypes(Object value, String description) {
		this.value = value;
		this.description = description;
	}

    private LiteralTypes(Object value, String description, String shortName) {
        this.value = value;
        this.description = description;
        this.shortName = shortName;
    }


	/**
	 * Returns the intrinsic value of the literal.
	 * 
	 * @return The value.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Returns a formatted string representation of a literal value.
	 * 
	 * @param value
	 *            The value.
	 * @return The string.
	 */
	public String toFormattedString(Object value) {
		return String.valueOf(value);
	}

	/**
	 * Returns a description of the literal type.
	 * 
	 * @return The description.
	 */
	public String getDescription() {
		return description;
	}	
}