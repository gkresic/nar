package com.steatoda.nar;

/** Interface each enum that describes object's fields should implement */
public interface NarField {

	/**
	 * For fields that describes subobjects with their own fields, this method returns subobject's {@link NarField} class
	 *
	 * @param <F> Field enum describing field type for subobject this field references
	 *
	 * @return this field subobject's {@link NarField} class
	 */
	<F extends Enum<F> & NarField> Class<F> getNarFieldClass();
	
}
