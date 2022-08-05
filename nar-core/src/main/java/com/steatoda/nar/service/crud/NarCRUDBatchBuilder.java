package com.steatoda.nar.service.crud;

import com.steatoda.nar.NarEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * <p>Builder for batched {@link CRUDOperation}s</p>
 *
 * @param <C> class implementing {@link NarEntity}
 */
public class NarCRUDBatchBuilder<C extends NarEntity<?, ?, ?>> {

	/**
	 * <p>Factory for building instances of {@link CRUDOperation}s.</p>
	 *
	 * @param <C> class implementing {@link NarEntity}
	 */
	@FunctionalInterface
	public interface OperationFactory<C extends NarEntity<?, ?, ?>> {
		CRUDOperation<C> get(CRUDOperation.Type type, C entity);
	}

	/**
	 * <p>Constructs {@link NarCRUDBatchBuilder} that builds operations of type {@link CRUDOperationBase}.</p>
	 */
	public NarCRUDBatchBuilder() {
		this(CRUDOperationBase::new);
	}

	/**
	 * <p>Constructs {@link NarCRUDBatchBuilder} that builds operations using provided factory.</p>
	 *
	 * @param operationFactory factory for building new {@link CRUDOperation}s
	 */
	public NarCRUDBatchBuilder(OperationFactory<C> operationFactory) {
		this.operationFactory = operationFactory;
	}

	/**
	 * <p>Adds new {@link CRUDOperation} that should be performed in a batch.</p>
	 *
	 * @param operation operation to add
	 *
	 * @return {@code this}
	 */
	public NarCRUDBatchBuilder<C> add(CRUDOperation<C> operation) { this.operations.add(operation); return this; }

	/**
	 * <p>Adds new {@link CRUDOperation}s that should be performed in a batch.</p>
	 *
	 * @param operations operations to add
	 *
	 * @return {@code this}
	 */
	@SafeVarargs
	public final NarCRUDBatchBuilder<C> add(CRUDOperation<C>... operations) { add(Arrays.asList(operations)); return this; }

	/**
	 * <p>Adds new {@link CRUDOperation}s that should be performed in a batch.</p>
	 *
	 * @param operations operations to add
	 *
	 * @return {@code this}
	 */
	public final NarCRUDBatchBuilder<C> add(Collection<CRUDOperation<C>> operations) { this.operations.addAll(operations); return this; }

	/**
	 * <p>Adds new {@link CRUDOperation} for creating new entity.</p>
	 *
	 * @param entity entity to create
	 *
	 * @return {@code this}
	 */
	public NarCRUDBatchBuilder<C> addCreate(C entity) { add(operationFactory.get(CRUDOperation.Type.Create, entity)); return this; }

	/**
	 * <p>Adds new {@link CRUDOperation}s for creating new entities.</p>
	 *
	 * @param entities entities to create
	 *
	 * @return {@code this}
	 */
	@SafeVarargs
	public final NarCRUDBatchBuilder<C> addCreate(C... entities) { Arrays.asList(entities).forEach(this::addCreate); return this; }

	/**
	 * <p>Adds new {@link CRUDOperation}s for creating new entities.</p>
	 *
	 * @param entities entities to create
	 *
	 * @return {@code this}
	 */
	public final NarCRUDBatchBuilder<C> addCreate(Collection<C> entities) { entities.forEach(this::addCreate); return this; }

	/**
	 * <p>Adds new {@link CRUDOperation} for modifying existing entity.</p>
	 *
	 * @param entity entity to modify
	 *
	 * @return {@code this}
	 */
	public NarCRUDBatchBuilder<C> addModify(C entity) { add(operationFactory.get(CRUDOperation.Type.Modify, entity)); return this; }

	/**
	 * <p>Adds new {@link CRUDOperation}s for modifying existing entities.</p>
	 *
	 * @param entities entities to modify
	 *
	 * @return {@code this}
	 */
	@SafeVarargs
	public final NarCRUDBatchBuilder<C> addModify(C... entities) { Arrays.asList(entities).forEach(this::addModify); return this; }

	/**
	 * <p>Adds new {@link CRUDOperation}s for modifying existing entities.</p>
	 *
	 * @param entities entities to modify
	 *
	 * @return {@code this}
	 */
	public final NarCRUDBatchBuilder<C> addModify(Collection<C> entities) { entities.forEach(this::addModify); return this; }

	/**
	 * <p>Adds new {@link CRUDOperation} for deleting existing entity.</p>
	 *
	 * @param entity entity to delete
	 *
	 * @return {@code this}
	 */
	public NarCRUDBatchBuilder<C> addDelete(C entity) { add(operationFactory.get(CRUDOperation.Type.Delete, entity)); return this; }

	/**
	 * <p>Adds new {@link CRUDOperation}s for deleting existing entities.</p>
	 *
	 * @param entities entities to delete
	 *
	 * @return {@code this}
	 */
	@SafeVarargs
	public final NarCRUDBatchBuilder<C> addDelete(C... entities) { Arrays.asList(entities).forEach(this::addDelete); return this; }

	/**
	 * <p>Adds new {@link CRUDOperation}s for deleting existing entities.</p>
	 *
	 * @param entities entities to delete
	 *
	 * @return {@code this}
	 */
	public final NarCRUDBatchBuilder<C> addDelete(Collection<C> entities) { entities.forEach(this::addDelete); return this; }

	/**
	 * @return {@code true} if this builder holds no operations and {@code false} otherwise
	 */
	public boolean isEmpty() {
		return operations.isEmpty();
	}

	/**
	 * @return number of operations in this batch
	 */
	public int size() {
		return operations.size();
	}

	/**
	 * <p>Constructs {@link List} of {@link CRUDOperation}s added to this builder.</p>
	 *
	 * @return {@link List} of {@link CRUDOperation}s added to this builder
	 */
	public List<CRUDOperation<C>> build() {
		return Collections.unmodifiableList(operations);
	}

	private final OperationFactory<C> operationFactory;

	private final List<CRUDOperation<C>> operations = new ArrayList<>();

}
