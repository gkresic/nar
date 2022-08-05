package com.steatoda.nar;

import java.util.Arrays;
import java.util.Collection;

/** Thrown when requested fields cannot be retrieved */
public class FieldUnavailableException extends IllegalStateException {

	public FieldUnavailableException(Enum<?>... fields) {
		this(Arrays.asList(fields));
	}
		
	public FieldUnavailableException(Collection<? extends Enum<?>> fields) {
		super("Accessing non-existant field(s): " + fields);
		this.fields = fields;
	}

	/**
	 * @return unavailable fields
	 */
	public Collection<? extends Enum<?>> getFields() { return fields; }

	private static final long serialVersionUID = 1L;

	private final Collection<? extends Enum<?>> fields;
	
}
