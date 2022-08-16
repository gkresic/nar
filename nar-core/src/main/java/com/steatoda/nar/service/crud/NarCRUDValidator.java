package com.steatoda.nar.service.crud;

import com.steatoda.nar.NarField;
import com.steatoda.nar.NarGraph;
import com.steatoda.nar.NarObject;

public interface NarCRUDValidator<C extends NarObject<C, F>, F extends Enum<F> & NarField> {

	/** Returns FieldGraph this validator expects to be initialized */
	NarGraph<F> graph();

	/**
	 * Validates entity.
	 *
	 * @param entity entity to validate
	 * @param creating {@code true} if entity is being created, {@code false} if being modified
	 */
	void validate(C entity, boolean creating);

}
