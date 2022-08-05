package com.steatoda.nar.service.async.crud;

import java.util.Set;
import java.util.stream.Stream;

import com.steatoda.nar.NarField;
import com.steatoda.nar.NarGraph;
import com.steatoda.nar.NarEntity;
import com.steatoda.nar.service.async.NarRequest;
import com.steatoda.nar.service.async.NarServiceHandler;

/**
 * <p>'Collection' part of 'Collection/Instance' CRUD async interface.</p>
 * 
 * @see NarInstanceCRUDAsyncService
 * @see NarCRUDAsyncService
 */
public interface NarCollectionCRUDAsyncService<I, C extends NarEntity<I, C, F>, F extends Enum<F> & NarField, S> extends NarCRUDAsyncService<I, C, F, S> {

	@Override
	default NarRequest get(I id, NarGraph<F> graph, NarServiceHandler<C> handler) {
		return instance(id).get(graph, handler);
	}

	@Override
	NarRequest create(C entity, NarGraph<F> graph, NarServiceHandler<C> handler);

	@Override
	default NarRequest modify(C entity, NarGraph<F> graph, NarServiceHandler<C> handler) {
		return modify(entity, entity.cloneAll(), graph, handler);	// NOTE: full clone is required (not cloneFlat or anything like that), because created patch may contain subentities that have to be stored, too
	}

	@Override
	default NarRequest modify(C entity, Set<F> fields, NarGraph<F> graph, NarServiceHandler<C> handler) {
		C patch = entity.ref();
		patch.pull(entity, fields);
		return modify(entity, patch, graph, handler);
	}

	@Override
	default NarRequest modify(C entity, C patch, NarGraph<F> graph, NarServiceHandler<C> handler) {
		return instance(entity).modify(patch, graph, handler);
	}

	@Override
	default NarRequest delete(C entity, NarServiceHandler<Void> handler) {
		return instance(entity).delete(handler);
	}

	@Override
	NarRequest queryAllFieldValues(S selector, Set<F> fields, NarServiceHandler<Stream<C>> handler);

	@Override
	NarRequest count(S selector, NarServiceHandler<Integer> handler);

	@Override
	NarRequest query(S selector, NarGraph<F> graph, NarServiceHandler<Stream<C>> handler);

	/**
	 * <p>Constructs {@link NarInstanceCRUDAsyncService} for operations on entity with given ID.</p>
	 *
	 * @param id ID if the entity on which returned {@link NarInstanceCRUDAsyncService} will operate on
	 *
	 * @return {@link NarInstanceCRUDAsyncService} for operations on entity with given ID
	 */
	NarInstanceCRUDAsyncService<I, C, F> instance(I id);

	/**
	 * <p>Constructs {@link NarInstanceCRUDAsyncService} for operations on given entity.</p>
	 *
	 * @param entity entity on which returned {@link NarInstanceCRUDAsyncService} will operate on
	 *
	 * @return {@link NarInstanceCRUDAsyncService} for operations on given entity
	 */
	default NarInstanceCRUDAsyncService<I, C, F> instance(C entity) { return instance(entity.getId()); }

}
