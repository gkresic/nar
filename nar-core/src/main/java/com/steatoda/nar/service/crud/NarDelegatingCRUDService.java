package com.steatoda.nar.service.crud;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.steatoda.nar.NarField;
import com.steatoda.nar.NarGraph;
import com.steatoda.nar.NarEntity;

/**
 * {@link NarCRUDService} implementation that delegates all calls to another CRUDFieldsService.
 * Override and provide decorated functionality.
 *
 * @param <I> ID type
 * @param <C> concrete implementation of class implementing {@link NarEntity}
 * @param <F> field type
 * @param <S> selector used to filter values
 *
 * @see NarCRUDService
 */
public class NarDelegatingCRUDService<I, C extends NarEntity<I, C, F>, F extends Enum<F> & NarField, S> implements NarCRUDService<I, C, F, S> {

	/**
	 * Constructs {@link NarDelegatingCRUDService} using {@code delegate} as delegate.
	 *
	 * @param delegate delegate service to forward calls to
	 */
	public NarDelegatingCRUDService(NarCRUDService<I, C, F, S> delegate) {
		this.delegate = delegate;
	}

	@Override
	public C construct(I id, NarGraph<F> graph) {
		return delegate.construct(id, graph);
	}

	@Override
	public boolean exists(C entity) {
		return delegate.exists(entity);
	}

	@Override
	public C instance() {
		return delegate.instance();
	}

	@Override
	public C get(I id, NarGraph<F> graph) {
		return delegate.get(id, graph);
	}

	@Override
	public int count(S selector) {
		return delegate.count(selector);
	}

	@Override
	public Stream<C> query(S selector, NarGraph<F> graph) {
		return delegate.query(selector, graph);
	}

	@Override
	public void batch(List<? extends NarCRUDOperation<C>> operations, NarGraph<F> graph) {
		delegate.batch(operations, graph);
	}

	@Override
	public void batch(Stream<? extends NarCRUDOperation<C>> operations) {
		delegate.batch(operations);
	}

	@Override
	public void perform(NarCRUDOperation<C> operation, NarGraph<F> graph) {
		delegate.perform(operation, graph);
	}

	@Override
	public void create(C entity, NarGraph<F> graph) {
		delegate.create(entity, graph);
	}

	@Override
	public void modify(C entity, NarGraph<F> graph) {
		delegate.modify(entity, graph);
	}

	@Override
	public void modify(C entity, Set<F> fields, NarGraph<F> graph) {
		delegate.modify(entity, fields, graph);
	}

	@Override
	public void modify(C entity, C patch, NarGraph<F> graph) {
		delegate.modify(entity, patch, graph);
	}

	@Override
	public void delete(C entity) {
		delegate.delete(entity);
	}

	@Override
	public Stream<C> queryAllFieldValues(S selector, Set<F> fields) {
		return delegate.queryAllFieldValues(selector, fields);
	}

	private final NarCRUDService<I, C, F, S> delegate;

}
