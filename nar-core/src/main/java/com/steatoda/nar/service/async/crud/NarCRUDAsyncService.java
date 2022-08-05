package com.steatoda.nar.service.async.crud;

import java.util.Set;
import java.util.stream.Stream;

import com.steatoda.nar.NarField;
import com.steatoda.nar.NarGraph;
import com.steatoda.nar.NarEntity;
import com.steatoda.nar.service.async.NarAsyncService;
import com.steatoda.nar.service.async.NarRequest;
import com.steatoda.nar.service.async.NarServiceHandler;
import com.steatoda.nar.service.crud.NarCRUDService;

/**
 * <p>'Flat' CRUD async interface.</p>
 *
 * @param <I> ID type
 * @param <C> concrete implementation of class implementing {@link NarEntity}
 * @param <F> field type
 * @param <S> selector used to filter values
 *
 * @see NarCollectionCRUDAsyncService
 * @see NarInstanceCRUDAsyncService
 * @see NarCRUDService
 */
public interface NarCRUDAsyncService<I, C extends NarEntity<I, C, F>, F extends Enum<F> & NarField, S> extends NarAsyncService<I, C, F> {

	/**
	 * Created new entity and populates it with fields specified by {@code graph}.
	 *
	 * @param entity entity to create
	 * @param graph fields graph to resolve
	 * @param handler handler to be notified on different execution stages
	 *
	 * @return {@link NarRequest} describing this asynchronous operation
	 */
	NarRequest create(C entity, NarGraph<F> graph, NarServiceHandler<C> handler);

	/**
	 * Updates all modifiable fields in {@code entity} and pulls {@code graph} into it when done.
	 * 
	 * @param entity entity to update and pull changes to
	 * @param graph graph to pull into {@code entity} after modification
	 * @param handler handler to be notified on different execution stages
	 *
	 * @return {@link NarRequest} describing this asynchronous operation
	 */
	default NarRequest modify(C entity, NarGraph<F> graph, NarServiceHandler<C> handler) {
		return modify(entity, entity.cloneAll(), graph, handler);	// NOTE: full clone is required (not cloneFlat or anything like that), because created patch may contain subentities that have to be stored, too
	}

	/**
	 * <p>Updates fields in {@code entity} specified by {@code fields} and pulls {@code graph} when done.</p>
	 * 
	 * <p>E.g. to update only field {@code bar} in entity {@code foo}, write something like:</p>
	 * 
	 * <pre>
	 * 	modify(foo, EnumSet.of(Foo.Field.bar), ...)
	 * </pre>
	 *
	 * @param entity entity to update and pull changes to
	 * @param fields fields from entity to update (only modifiable fields will be updated)
	 * @param graph graph to pull into {@code entity} after modification
	 * @param handler handler to be notified on different execution stages
	 *
	 * @return {@link NarRequest} describing this asynchronous operation
	 */
	default NarRequest modify(C entity, Set<F> fields, NarGraph<F> graph, NarServiceHandler<C> handler) {
		C patch = entity.ref();
		patch.pull(entity, fields);
		return modify(entity, patch, graph, handler);
	}

	/**
	 * <p>Updates fields in {@code entity} specified by {@code patch} and pulls {@code graph} when done.</p>
	 * 
	 * <p>E.g. to update only field {@code bar} in entity {@code foo}, write something like:</p>
	 * 
	 * <pre>
	 * 	modify(foo, foo.clone(NarGraph.of(Foo.Field.bar)), ...)
	 * </pre>
	 *
	 * @param entity entity to update and pull changes to
	 * @param patch patch containing fields to update (only modifiable fields will be updated)
	 * @param graph graph to pull into {@code entity} after modification
	 * @param handler handler to be notified on different execution stages
	 *
	 * @return {@link NarRequest} describing this asynchronous operation
	 */
	NarRequest modify(C entity, C patch, NarGraph<F> graph, NarServiceHandler<C> handler);

	/**
	 * Deletes entity.
	 *
	 * @param entity entity to delete
	 * @param handler handler to be notified on different execution stages
	 *
	 * @return {@link NarRequest} describing this asynchronous operation
	 */
	NarRequest delete(C entity, NarServiceHandler<Void> handler);

	/**
	 * <p>Retrieves list of entities each set to distinct combination of requested fields.</p>
	 *
	 * <p>NOTE: returned entities are just placeholders e.g. nor do they exist as such in backing store nor they have ID set.
	 * Each of them is used just to represent one combination of requested fields in backing store.
	 * Example usage: pass only one field and get all possible values for it to provide autocomplete functionality.</p>
	 *
	 * @param selector selector to filter entities
	 * @param fields fields on which to determine distinctiveness
	 * @param handler handler to be notified on different execution stages
	 *
	 * @return {@link NarRequest} describing this asynchronous operation
	 */
	NarRequest queryAllFieldValues(S selector, Set<F> fields, NarServiceHandler<Stream<C>> handler);

	/**
	 * Counts entities matched by provided {@code selector}.
	 *
	 * @param selector selector to filter entities
	 * @param handler handler to be notified on different execution stages
	 *
	 * @return {@link NarRequest} describing this asynchronous operation
	 */
	NarRequest count(S selector, NarServiceHandler<Integer> handler);

	/**
	 * Retrieves stream of ALL entities matched by provided {@code selector}.
	 *
	 * @param selector selector to filter entities
	 * @param graph graph to resolve
	 * @param handler handler to be notified on different execution stages
	 *
	 * @return {@link NarRequest} describing this asynchronous operation
	 */
	NarRequest query(S selector, NarGraph<F> graph, NarServiceHandler<Stream<C>> handler);

}
