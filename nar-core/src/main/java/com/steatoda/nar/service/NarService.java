package com.steatoda.nar.service;

import com.steatoda.nar.NarField;
import com.steatoda.nar.NarGraph;
import com.steatoda.nar.NarEntity;

/**
 * Service that can resolve entities with requested ID.
 *
 * @param <I> ID type
 * @param <C> concrete implementation of class implementing {@link NarEntity}
 * @param <F> field type
 */
public interface NarService<I, C extends NarEntity<I, C, F>, F extends Enum<F> & NarField> {

	/**
	 * Returns new, uninitialized instance.
	 */
	C instance();

	/**
	 * Same as {@link #get}, but without actual store lookup when {@code graph} is empty.
	 * Use when existence is sure (e.g. resolving foreign key from relational database).
	 *
	 * @param id entity's ID
	 * @param graph field graph to initialize
	 *
	 * @return entity with given {@code id} with fields initialized as specified by {@code graph}
	 */
	default C construct(I id, NarGraph<F> graph) {
		// short-circuit if nothing is requested
		if (graph.isEmpty()) {
			C entity = instance();
			entity.setId(id);
			return entity;
		}
		// default to full lookup
		return get(id, graph);
	}
	
	/**
	 * Checks for entity existence in backing store
	 * 
	 * @param entity Entity to check (reference only)
	 * 
	 * @return {@code true} if entity exists, {@code false} otherwise.
	 */
	default boolean exists(C entity) {
		return get(entity.getId(), NarGraph.noneOf(entity.getFieldsClass())) != null;
	}

	/**
	 * Resolves entity with given {@code id} with fields initialized as specified by {@code graph}.
	 *
	 * @param id entity's ID
	 * @param graph field graph to initialize
	 *
	 * @return resolved entity or {@code null} if entity could not be found
	 */
	C get(I id, NarGraph<F> graph);
	
}
