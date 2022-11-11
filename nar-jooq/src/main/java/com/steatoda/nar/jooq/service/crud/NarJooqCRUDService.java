package com.steatoda.nar.jooq.service.crud;

import com.google.common.collect.Sets;
import com.steatoda.nar.FieldUnavailableException;
import com.steatoda.nar.NarEntity;
import com.steatoda.nar.NarField;
import com.steatoda.nar.NarGraph;
import com.steatoda.nar.jooq.service.NarJooqService;
import com.steatoda.nar.service.crud.NarCRUDService;
import com.steatoda.nar.service.crud.NarCRUDValidator;
import org.jooq.Condition;
import org.jooq.OrderField;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.Select;
import org.jooq.SelectWhereStep;
import org.jooq.Table;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * <p>Implementation of {@link NarCRUDService} using <a href="https://www.jooq.org/">jOOQ</a>.</p>
 *
 * <p><b>IMPORTANT:</b>don't forget to close all returned streams, because they are holding active cursors in database
 * until they are explicitly closed.</p>
 *
 * @param <I> ID type
 * @param <C> concrete implementation of class implementing {@link NarEntity}
 * @param <F> field type
 * @param <S> selector used to filter values
 * @param <R> {@link Record} describing entities in database
 * @param <T> {@link Table} describing database table holding entities
 */
public abstract class NarJooqCRUDService<I, C extends NarEntity<I, C, F>, F extends Enum<F> & NarField, S, R extends Record, T extends Table<R>> extends NarJooqService<I, C, F, R, T> implements NarCRUDService<I, C, F, S> {

	/** Operations supported by this implementation. */
	public enum Op {
		
		/** Represents {@link #create} */
		Create,

		/** Represents {@link #modify} */
		Modify,

		/** Represents {@link #delete} */
		Delete,

		/** Represents {@link #count}, {@link #query} and {@link #queryAllFieldValues}*/
		Query
		
	}

	/**
	 * <p>Constructs {@code NarJooqCRUDService}.</p>
	 *
	 * @param table {@link Table} describing database table holding entities
	 * @param mandatoryFields fields required to be present (but <u>may</u> be {@code null}) when creating entities
	 * @param modifiableFields fields that are allowed to be modified
	 * @param ops which operations this service should allow
	 */
	protected NarJooqCRUDService(T table, Set<F> mandatoryFields, Set<F> modifiableFields, Set<Op> ops) {
		super(table);
		this.mandatoryFields = mandatoryFields;
		this.modifiableFields = modifiableFields;
		this.ops = ops;
	}

	/**
	 * Fields required to be present (but <u>may</u> be {@code null}) when creating entities.
	 *
	 * @return fields required to be present (but <u>may</u> be {@code null}) when creating entities.
	 */
	public Set<F> getMandatoryFields() { return Collections.unmodifiableSet(mandatoryFields); }

	/**
	 * Fields that are allowed to be modified.
	 *
	 * @return fields that are allowed to be modified
	 */
	public Set<F> getModifiableFields() { return Collections.unmodifiableSet(modifiableFields); }

	/**
	 * Validator used to validate entities upon creating and modifying.
	 *
	 * @return validator used to validate entities upon creating and modifying
	 */
	public NarCRUDValidator<C, F> getValidator() { return validator; }

	/**
	 * Sets validator used to validate entities upon creating and modifying
	 *
	 * @param validator validator to use
	 */
	public void setValidator(NarCRUDValidator<C, F> validator) { this.validator = validator; }

	/**
	 * Fetch size used when reading records from database (see {@link ResultQuery#fetchSize(int)}).
	 *
	 * @return fetch size used when reading records from database (see {@link ResultQuery#fetchSize(int)})
	 */
	public int getQueryFetchSize() { return queryFetchSize; }

	/**
	 * Sets fetch size used when reading records from database (see {@link ResultQuery#fetchSize(int)})
	 *
	 * @param queryFetchSize fetch size to use
	 */
	public void setQueryFetchSize(int queryFetchSize) { this.queryFetchSize = queryFetchSize; }

	@Override
	public void create(C entity, NarGraph<F> graph) {

		if (!ops.contains(Op.Create))
			throw new UnsupportedOperationException("Creating new entities is not enabled");

		checkMandatoryFields(entity);

		if (validator != null)
			validator.validate(entity, true);

		R record = getDSLContext()
			.insertInto(getTable())
			.set(buildCreateRecord(entity))
			.returningResult(buildDQLFields(graph, true))
			.fetchSingle()
			.into(getTable())
		;

		readRecord(entity, record, graph, true);

	}

	/**
	 * <p>Builds {@link Record} representing entity for use in {@link #create}.</p>
	 *
	 * <p>Defaults to {@link #buildDMLRecord(C)}. Override to customize.</p>
	 *
	 * @param entity entity that is being created
	 *
	 * @return initialized record
	 */
	protected R buildCreateRecord(C entity) { return buildDMLRecord(entity); }

	@Override
	public void modify(C entity, C patch, NarGraph<F> graph) {

		if (!ops.contains(Op.Modify))
			throw new UnsupportedOperationException("Modifying entities is not enabled");

		// NOTE: be carefully not to modify patch, because it may hold data valuable to caller (e.g. regarding modifying sub-entites etc.)

		patch = patch.cloneFlat();
		patch.intersect(NarGraph.of(modifiableFields));

		if (validator != null) {

			NarGraph<F> graphToExtend = NarGraph.Builder.of(instance().getFieldsClass())
				.add(validator.graph())
				.build();

			entity.extend(graphToExtend, this);

			// create mockup for validation
			C mockup = entity.cloneAll();
			mockup.pull(patch);

			validator.validate(mockup, false);

		}

		R dmlRecord = buildModifyRecord(patch);

		if (dmlRecord.size() == 0) {
			// nothing to update
			entity.extend(graph, this);
			return;
		}

		R record = getDSLContext()
			.update(getTable())
			.set(dmlRecord)
			.where(buildIdentityCondition(entity))
			.returningResult(buildDQLFields(graph, false))
			.fetchSingle()
			.into(getTable())
		;

		readRecord(entity, record, graph, false);

	}

	/**
	 * <p>Builds {@link Record} representing patch for use in {@link #modify}.</p>
	 *
	 * <p>Defaults to {@link #buildDMLRecord(C)}. Override to customize.</p>
	 *
	 * @param patch patch that is being applied
	 *
	 * @return initialized record
	 */
	protected R buildModifyRecord(C patch) { return buildDMLRecord(patch); }

	@Override
	public void delete(C entity) {

		if (!ops.contains(Op.Delete))
			throw new UnsupportedOperationException("Deleting entities is not enabled");

		getDSLContext()
			.deleteFrom(getTable())
			.where(buildIdentityCondition(entity))
			.execute();

	}

	@Override
	public Stream<C> queryAllFieldValues(S selector, Set<F> fields) {

		if (!ops.contains(Op.Query))
			throw new UnsupportedOperationException("Querying all field values is not enabled");

		Select<Record> query;

		SelectWhereStep<Record> step = getDSLContext()
			.selectDistinct(buildDQLFields(fields, false))
			.from(getTable())
		;

		if (selector != null)
			query = step.where(buildSelectorCondition(selector));
		else
			query = step;

		return executeQuery(query, NarGraph.of(fields));

	}

	@Override
	public int count(S selector) {

		if (!ops.contains(Op.Query))
			throw new UnsupportedOperationException("Counting entities is not enabled");

		return getDSLContext()
			.selectCount()
			.from(getTable())
			.where(buildSelectorCondition(selector))
			.fetchSingle()
			.value1()
		;

	}

	@Override
	public Stream<C> query(S selector, NarGraph<F> graph) {

		if (!ops.contains(Op.Query))
			throw new UnsupportedOperationException("Querying entities is not enabled");

		return executeQuery(
			getDSLContext()
				.select(buildDQLFields(graph, true))
				.from(getTable())
				.where(buildSelectorCondition(selector))
				.orderBy(buildSelectorOrderFields(selector))
				.limit(buildSelectorLimit(selector))
			,
			graph
		);

	}

	/**
	 * <p>Returns fields on which results returned from {@link #query} should be sorted.</p>
	 *
	 * <p>Defaults to empty list (no ordering). Override to customize.</p>
	 *
	 * @param selector selector used in {@link #query}
	 *
	 * @return fields to sort on
	 */
	@SuppressWarnings("unused")
	protected Collection<OrderField<?>> buildSelectorOrderFields(S selector) { return List.of(); }

	/**
	 * <p>Builds limit to apply when returning results from {@link #query}.</p>
	 *
	 * <p>Defaults to {@code null} (no ordering). Override to customize.</p>
	 *
	 * @param selector selector used in {@link #query}
	 *
	 * @return limit to apply
	 */
	@SuppressWarnings("unused")
	protected Number buildSelectorLimit(S selector) { return null; }

	/**
	 * <p>Executes select and returns stream of returned entities.</p>
	 *
	 * <p><b>IMPORTANT:</b>Returned stream <b>MUST BE CLOSED</b> to release all underlying resources.</p>
	 *
	 * @param query query to execute
	 * @param graph fields graph to resolve
	 *
	 * @return stream of resolved entities
	 */
	@SuppressWarnings("resource")
	protected Stream<C> executeQuery(Select<? extends Record> query, NarGraph<F> graph) {
		return query
			.fetchSize(queryFetchSize)
			.fetchLazy()
			.stream()
			.map(record -> record.into(getTable()))
			.map(record -> resolveRecord(record, graph))
		;
	}

	/**
	 * Checks if entity has all mandatory fields set.
	 *
	 * @param entity entity to check
	 */
	protected void checkMandatoryFields(C entity) {
		Set<F> missingFields = Sets.difference(mandatoryFields, entity.getFields());
		if (!missingFields.isEmpty())
			throw new FieldUnavailableException(missingFields);
	}

	/**
	 * <p>Builds {@link Record} representing entity/patch to write to database in {@link #create} and/or {@link #modify}.</p>
	 *
	 * @param entity entity (may be partial - patch) that is being persisted
	 *
	 * @return initialized record
	 */
	protected abstract R buildDMLRecord(C entity);

	/**
	 * <p>Builds {@code WHERE} condition based on given selector.</p>
	 *
	 * @param selector selector for which condition is required
	 *
	 * @return condition  to apply when buildingh query
	 */
	protected abstract Condition buildSelectorCondition(S selector);

	private final Set<F> mandatoryFields;
	private final Set<F> modifiableFields;
	private final Set<Op> ops;

	private NarCRUDValidator<C, F> validator = null;

	private int queryFetchSize = 100;

}
