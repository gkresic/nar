package com.steatoda.nar.service.crud;

import com.steatoda.nar.NarEntity;

/**
 * <p>Descriptor for one CRUD operation on {@link NarEntity}s.</p>
 *
 * @param <C> class implementing {@link NarEntity}
 */
public interface NarCRUDOperation<C extends NarEntity<?, ?, ?>> {

	/** Type of operation */
	enum Type {
		Create,
		Modify,
		Delete
	}

	/**
	 * @return type of operation
	 */
	Type getType();

	/**
	 * @return entity on which operation is to be performed
	 */
	C getEntity();

}
