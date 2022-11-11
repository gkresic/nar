package com.steatoda.nar;

import java.text.ParseException;

/** Thrown when object's field cannot be parsed to proper enum value. */
public class UnknownFieldException extends ParseException {

	/**
	 * Constructs UnknownFieldException.
	 *
	 * @param value string value being parsed
	 * @param clazz expected enum class
	 * @param offset position within input string (usually string representation of {@link NarGraph}) where unparsable {@code value} was found
	 */
	public UnknownFieldException(String value, Class<? extends Enum<?>> clazz, int offset) {
		super("Unrecognized enum value '" + value + "' for type " + clazz.getName(), offset);
		this.value = value;
		this.clazz = clazz;
	}

	/**
	 * Returns value that couldn't be parsed.
	 */
	public String getValue() { return value; }

	/**
	 * Returns expected enum to which value couldn't be parsed.
	 */
	public Class<? extends Enum<?>> getClazz() { return clazz; }

	private static final long serialVersionUID = 1L;

	private final String value;
	private final Class<? extends Enum<?>> clazz;
	
}
