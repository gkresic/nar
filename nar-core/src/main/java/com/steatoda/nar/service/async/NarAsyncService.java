package com.steatoda.nar.service.async;

import com.steatoda.nar.NarField;
import com.steatoda.nar.NarGraph;
import com.steatoda.nar.NarEntity;

/**
 * Service that can asynchronously resolve entities with requested ID.
 *
 * @param <I> ID type
 * @param <C> concrete implementation of class implementing {@link NarEntity}
 * @param <F> field type
 */
public interface NarAsyncService<I, C extends NarEntity<I, C, F>, F extends Enum<F> & NarField> {

	/**
	 * @return new, uninitialized instance
	 */
	C instance();

	/**
	 * Same as {@link #get}, but without backend lookup when {@code graph} is empty.
	 * Use when existence is sure (e.g. resolving foreign key from relational database).
	 *
	 * @param id entity's ID
	 * @param graph field graph to initialize
	 * @param handler handler to be notified on different execution stages
	 *
	 * @return {@link NarRequest} describing this asynchronous operation
	 */
	default NarRequest construct(I id, NarGraph<F> graph, NarServiceHandler<C> handler) {
		// short-circuit if nothing is requested
		if (graph.isEmpty()) {
			C entity = instance();
			entity.setId(id);
			handler.onSuccess(entity);
			handler.onFinish();
			handler.onDestroy();
			return null;
		}
		// default to full lookup
		return get(id, graph, handler);
	}

	/**
	 * Resolves entity with given {@code id} with fields initialized as specified by {@code graph}.
	 *
	 * @param id entity's ID
	 * @param graph field graph to initialize
	 * @param handler handler to be notified on different execution stages
	 *
	 * @return {@link NarRequest} describing this asynchronous operation
	 */
	NarRequest get(I id, NarGraph<F> graph, NarServiceHandler<C> handler);
	
}
