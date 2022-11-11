package com.steatoda.nar;

import com.steatoda.nar.service.NarService;
import com.steatoda.nar.service.async.NarDelegatingServiceHandler;
import com.steatoda.nar.service.async.NarAsyncService;
import com.steatoda.nar.service.async.NarRequest;
import com.steatoda.nar.service.async.NarServiceHandler;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Interface that gives implementing classes fields entity operations (like extending using service etc.).
 *
 * @param <I> ID type
 * @param <C> class implementing this {@link NarEntity}
 * @param <F> field type
 */
public interface NarEntity<I, C extends NarEntity<I, C, F>, F extends Enum<F> & NarField> extends NarObject<C, F> {

	/**
	 * Returns entity's unique ID.
	 */
	I getId();

	/**
	 * Sets entity's unique ID.
	 *
	 * @param id entity's unique ID
	 *
	 * @return this
	 */
	C setId(I id);

	/**
	 * Ensures entity has all requested fields, fetching missing ones using {@code resolver} if necessary.
	 * Descends to subobject and fetches their fields, too.
	 *
	 * @param graph field graph to set if missing
	 * @param resolver resolver which can provide entity with missing fields
	 *
	 * @return {@code true} if entity was extended, {@code false} if no extension was needed (nor performed)
	 *
	 * @throws EntityUnavailableException if missing (sub)fields can't be fetched from service
	 * 			(includes missing collection members)
	 */
	default boolean extend(NarGraph<F> graph, Function<NarGraph<F>, C> resolver) throws EntityUnavailableException {

		NarGraph<F> missingGraph = getMissingGraph(graph);

		if (missingGraph.isEmpty())
			return false;

		C extension;
		try {
			extension = resolver.apply(missingGraph);
		} catch (Exception e) {
			throw new EntityUnavailableException(getId().toString(), e);
		}

		if (extension == null)
			throw new EntityUnavailableException(getId());

		_extend(extension, missingGraph);

		return true;

	}

	/**
	 * Ensures entity has all requested fields, fetching missing ones using {@code service} if necessary.
	 * Descends to subobject and fetches their fields, too.
	 *
	 * @param graph field graph to set if missing
	 * @param service service which can provide entity with missing fields
	 *
	 * @return {@code true} if entity was extended, {@code false} if no extension was needed (nor performed)
	 *
	 * @throws EntityUnavailableException if missing (sub)fields can't be fetched from service
	 * 			(includes missing collection members)
	 */
	default boolean extend(NarGraph<F> graph, NarService<I, C, F> service) throws EntityUnavailableException {
		return extend(graph, missingGraph -> service.get(getId(), missingGraph));
	}

	/**
	 * Ensures entity has all requested fields, fetching missing ones <u>asynchronously</u> using {@code resolver} if necessary.
	 * Descends to subobject and fetches their fields, too.
	 *
	 * @param graph field graph to set if missing
	 * @param resolver resolver which can provide entity with missing fields
	 * @param handler asynchronous handler
	 *
	 * @return {@link NarRequest} describing this asynchronous operation
	 */
	@SuppressWarnings("unchecked")
	default NarRequest extend(NarGraph<F> graph, BiFunction<NarGraph<F>, NarServiceHandler<C>, NarRequest> resolver, NarServiceHandler<C> handler) {

		NarGraph<F> missingGraph = getMissingGraph(graph);

		if (missingGraph.isEmpty()) {
			handler.onSuccess((C) this);
			handler.onFinish();
			handler.onDestroy();
			return null;
		}

		return resolver.apply(missingGraph, new NarDelegatingServiceHandler<>(handler) {
			@Override
			public void onSuccess(C extension) {
				_extend(extension, graph);
				super.onSuccess((C) NarEntity.this);
			}
		});

	}

	/**
	 * Ensures entity has all requested fields, fetching missing ones <u>asynchronously</u> using {@code resolver} if necessary.
	 * Descends to subobject and fetches their fields, too.
	 *
	 * @param graph field graph to set if missing
	 * @param service asynchronous service which can provide entity with missing fields
	 * @param handler asynchronous handler
	 *
	 * @return {@link NarRequest} describing this asynchronous operation
	 */
	default NarRequest extend(NarGraph<F> graph, NarAsyncService<I, C, F> service, NarServiceHandler<C> handler) {
		return extend(graph, (missingGraph, handler2) -> service.get(getId(), missingGraph, handler2), handler);
	}

}
