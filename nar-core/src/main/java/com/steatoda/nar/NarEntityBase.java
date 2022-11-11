package com.steatoda.nar;

import java.util.Objects;

/**
 * <p>Vanilla implementation of {@link NarEntity} interface.</p>
 *
 * @param <I> ID type
 * @param <C> class implementing this {@link NarEntity}
 * @param <F> field type
 */
public abstract class NarEntityBase<I, C extends NarEntityBase<I, C, F>, F extends Enum<F> & NarField> extends NarObjectBase<C, F> implements NarEntity<I, C, F> {

	/**
	 * <p>Constructs this class using {@code fieldsClass} as field descriptor.</p>
	 *
	 * @param fieldsClass field descriptor
	 */
	protected NarEntityBase(Class<F> fieldsClass) {
		super(fieldsClass);
	}

	@Override
	public I getId() {
		return id;
	}

	@Override
	@SuppressWarnings("unchecked")
	public C setId(I id) {
		this.id = id;
		return (C) this;
	}

	@Override
	public String toString() {
		return Objects.toString(id);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof NarEntityBase))
			return false;
		NarEntityBase<?, ?, ?> other = (NarEntityBase<?, ?, ?>) obj;
		if (id == null)
			return other.id == null;
		else
			return id.equals(other.id);
	}

	private I id;
	
}
