package com.steatoda.nar.service.async.crud;

import com.steatoda.nar.NarField;
import com.steatoda.nar.NarGraph;
import com.steatoda.nar.NarEntity;
import com.steatoda.nar.service.async.NarRequest;
import com.steatoda.nar.service.async.NarServiceHandler;

/**
 * <p>'Instance' part of 'Collection/Instance' CRUD async interface.</p>
 *
 * @see NarCollectionCRUDAsyncService
 * @see NarCRUDAsyncService
 */
public interface NarInstanceCRUDAsyncService<I, C extends NarEntity<I, C, F>, F extends Enum<F> & NarField> {

	NarRequest get(NarGraph<F> graph, NarServiceHandler<C> handler);

	/**
	 * <p>Updates fields specified by {@code patch} and pulls {@code graph} when done.</p>
	 * 
	 * <p>E.g. to update only field {@code bar} in entity {@code foo}, write something like:</p>
	 * 
	 * <pre>
	 * 	modify(foo.clone(NarGraph.of(Foo.Field.bar)), ...)
	 * </pre>
	 *
	 * @param patch patch containing fields to update (only modifiable fields will be updated)
	 * @param graph graph to pull into {@code entity} after modification
	 * @param handler handler to be notified on different execution stages
	 *
	 * @return {@link NarRequest} describing this asynchronous operation
	 */
	NarRequest modify(C patch, NarGraph<F> graph, NarServiceHandler<C> handler);

	/**
	 * Deletes entity.
	 *
	 * @param handler handler to be notified on different execution stages
	 *
	 * @return {@link NarRequest} describing this asynchronous operation
	 */
	NarRequest delete(NarServiceHandler<Void> handler);
	
}
