package com.steatoda.nar.jooq.service;

import com.steatoda.nar.NarEntity;

import com.steatoda.nar.NarField;
import com.steatoda.nar.NarGraph;
import com.steatoda.nar.service.NarService;
import com.steatoda.nar.service.crud.NarCRUDService;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.exception.NoDataFoundException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Implementation of {@link NarCRUDService} using <a href="https://www.jooq.org/">jOOQ</a>.</p>
 *
 * @param <I> ID type
 * @param <C> concrete implementation of class implementing {@link NarEntity}
 * @param <F> field type
 * @param <R> {@link Record} describing entities in database
 * @param <T> {@link Table} describing database table holding entities
 */
public abstract class NarJooqService<I, C extends NarEntity<I, C, F>, F extends Enum<F> & NarField, R extends Record, T extends Table<R>> implements NarService<I, C, F> {

	/**
	 * <p>Constructs {@code NarJooqService}.</p>
	 *
	 * @param table {@link Table} describing database table holding entities
	 */
	protected NarJooqService(T table) {
		this.table = table;
	}

	/**
	 * @return table holding entities
	 */
	public T getTable() { return table; }

	@Override
	public C get(I id, NarGraph<F> graph) {
		try {
			R record = getDSLContext()
				.select(buildDQLFields(graph, true))
				.from(table)
				.where(buildIdentityCondition(id))
				.fetchSingle()
				.into(table)
			;
			return resolveRecord(record, graph);
		} catch (NoDataFoundException e) {
			return null;
		}
	}

	/**
	 * <p>Builds collection of jOOQ's {@link Field}s to retrieve from database for given Nar fields.</p>
	 *
	 * @param fields Nar fields requested
	 * @param includeIdentity should database fields representing entity identifier be included in results
	 *
	 * @return collection of jOOQ's {@link Field}s to retrieve from database
	 */
	protected Set<Field<?>> buildDQLFields(Collection<F> fields, boolean includeIdentity) {
		Set<Field<?>> dqlFields = new HashSet<>(2 * fields.size());
		if (includeIdentity)
			dqlFields.addAll(buildDQLIdentityFields());
		for (F field : fields)
			dqlFields.addAll(buildDQLFields(field));
		return dqlFields;
	}

	/**
	 * <p>Builds collection of jOOQ's {@link Field}s to retrieve from database for constructing entity reference only.</p>
	 *
	 * @return collection of jOOQ's {@link Field}s to retrieve from database
	 */
	protected abstract Set<Field<?>> buildDQLIdentityFields();

	/**
	 * <p>Builds collection of jOOQ's {@link Field}s to retrieve from database for given Nar field.</p>
	 *
	 * @param field Nar field requested
	 *
	 * @return collection of jOOQ's {@link Field}s to retrieve from database
	 */
	protected abstract Set<Field<?>> buildDQLFields(F field);

	/**
	 * <p>Reads given record into <u>new</u> entity instance.</p>
	 *
	 * @param record record to read
	 * @param graph graph describing fields that should be read from record
	 *
	 * @return new entity instance, initialized as described by graph
	 */
	protected C resolveRecord(R record, NarGraph<F> graph) {
		C entity = instance();
		readRecord(entity, record, graph, true);
		return entity;
	}

	/**
	 * <p>Reads given record into <u>existing</u> entity instance (which may be completely blank, not even ID initialized).</p>
	 *
	 * @param entity entity into which to read record
	 * @param record record to read
	 * @param graph graph describing fields that should be read from record
	 * @param readId should database fields representing ID be read also
	 */
	protected abstract void readRecord(C entity, R record, NarGraph<F> graph, boolean readId);

	/**
	 * <p>Builds {@code WHERE} condition that selects exactly one entity with given ID.</p>
	 *
	 * @param id ID for which to build condition
	 *
	 * @return condition that matches given ID
	 */
	protected abstract Condition buildIdentityCondition(I id);

	/**
	 * <p>Builds {@code WHERE} condition that selects exactly one entity equal to the given one.</p>
	 *
	 * @param entity entity for which to build condition
	 *
	 * @return condition that matches given entity
	 */
	protected Condition buildIdentityCondition(C entity) {
		return buildIdentityCondition(entity.getId());
	}

	/**
	 * @return {@link DSLContext} to use when building statements
	 */
	protected abstract DSLContext getDSLContext();

	private final T table;

}
