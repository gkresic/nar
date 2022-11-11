package com.steatoda.nar;

import java.util.EnumSet;
import java.util.Set;

/**
 * <p>Vanilla implementation of {@link NarObject} interface.</p>
 *
 * @param <C> class implementing this {@link NarObject}
 * @param <F> field type
 */
public abstract class NarObjectBase<C extends NarObject<C, F>, F extends Enum<F> & NarField> implements NarObject<C, F>, Cloneable {

	/**
	 * <p>Constructs this class using {@code fieldsClass} as field descriptor.</p>
	 *
	 * @param fieldsClass field descriptor
	 */
	protected NarObjectBase(Class<F> fieldsClass) {
		this.fieldsClass = fieldsClass;
		fields = EnumSet.noneOf(fieldsClass);
	}
	
	@Override
	public Class<F> getFieldsClass() { return fieldsClass; }

	@Override
	public Set<F> getFields() { return fields; }
	
	@Override
	public void setFields(Set<F> fields) { this.fields = fields; }

	@Override
	@SuppressWarnings("MethodDoesntCallSuperMethod")
	public C clone() {
		return cloneAll();
	}
	
	private final Class<F> fieldsClass;
	
	private Set<F> fields;
	
}
