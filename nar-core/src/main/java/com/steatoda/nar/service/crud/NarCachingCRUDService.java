package com.steatoda.nar.service.crud;

import com.steatoda.nar.NarField;
import com.steatoda.nar.NarGraph;
import com.steatoda.nar.NarEntityCache;
import com.steatoda.nar.NarEntity;

import java.util.stream.Stream;

/**
 * <p>{@link NarCRUDService} implementation that caches objects incrementally.</p>
 *
 * <p>Populates cache in every method, but uses it only in {@link #construct(Object, NarGraph)} and {@link #get(Object, NarGraph)}.
 * Causes very little overhead over {@code service} itself.</p>

 * @param <I> ID type
 * @param <C> concrete implementation of class implementing {@link NarEntity}
 * @param <F> field type
 * @param <S> selector used to filter values
 *
 * @see NarCRUDService
 * @see NarEntityCache
 */
public class NarCachingCRUDService<I, C extends NarEntity<I, C, F>, F extends Enum<F> & NarField, S> extends NarDelegatingCRUDService<I, C, F, S> {

	/**
	 * @param cache entity cache
	 * @param service service that can resolve entities
	 */
	public NarCachingCRUDService(NarEntityCache<I, C, F> cache, NarCRUDService<I, C, F, S> service) {
		super(service);
		this.cache = cache;
		this.service = service;
	}

	@Override
	public C construct(I id, NarGraph<F> graph) {
		if (graph.isEmpty())
			return service.construct(id, graph);
		return get(id, graph);
	}

	@Override
	public C get(I id, NarGraph<F> graph) {
		return cache.get(id, graph, service);
	}

	@Override
	public Stream<C> query(S selector, NarGraph<F> graph) {
		return service.query(selector, graph).peek(cache::merge);
	}

	@Override
	public void create(C entity, NarGraph<F> graph) {
		service.create(entity, graph);
		cache.put(entity);
	}

	@Override
	public void modify(C entity, C patch, NarGraph<F> graph) {
		cache.clear(entity);
		service.modify(entity, patch, graph);
		cache.put(entity);
	}

	@Override
	public void delete(C entity) {
		cache.clear(entity);
		service.delete(entity);
	}

	private final NarEntityCache<I, C, F> cache;
	private final NarCRUDService<I, C, F, S> service;

}
