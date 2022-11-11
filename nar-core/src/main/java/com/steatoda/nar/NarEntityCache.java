package com.steatoda.nar;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

import com.google.common.collect.Sets;

import com.steatoda.nar.service.NarService;

/**
 * <p>Cache for <b>shallow</b> {@link NarEntity} instances (child entities are stored as refs only).
 * Entities stored will have initialized only fields this cache is configured to store
 * (see {@link #NarEntityCache(Set)} and {@link #getCacheableFields}.</p>
 * 
 * <p>Entities will be stored incrementally, as additional fields are requested.</p>
 * 
 * <p>Thread-safe.</p>

 * @param <I> ID type
 * @param <C> class implementing {@link NarEntity}
 * @param <F> field type
 */
public class NarEntityCache<I, C extends NarEntity<I, C, F>, F extends Enum<F> & NarField> {

	/**
	 * Constructs cache which will incrementally cache requested entity fields.
	 *
	 * @param cacheableFields fields to cache
	 */
	public NarEntityCache(Set<F> cacheableFields) {
		this(cacheableFields, null);
	}

	/**
	 * <p>
	 *     Constructs cache which will cache requested entity fields initially fetching all configured precached fields.
	 * </p>
	 * <p>
	 *     Use precached fields in situations where service(s) used in {@link #get(Object, NarGraph, NarService)}
	 *     require little additional overhead to fetch these fields in comparison to locating entity in store (e.g. all
	 *     rows in relation database's table can be retrieved relatively cheaply once row is found on disk using indexes).
	 *     <br>
	 *     <u>NOTE</u>: precached fields are fetched only on initial entity retrieval, but cache may end up holding entities with
	 *     only subset of those fields present if methods like {@link #put(NarEntity)} or {@link #clear(Object, Set)} are used.
	 * </p>
	 *
	 * @param cacheableFields fields to cache
	 *
	 * @param precachedFields fields to cache on initial fetch
	 */
	public NarEntityCache(Set<F> cacheableFields, Set<F> precachedFields) {

		if (cacheableFields.isEmpty())
			throw new IllegalArgumentException("cacheableFields is empty");

		if (precachedFields == null)
			precachedFields = EnumSet.noneOf(cacheableFields.iterator().next().getDeclaringClass());
		else if (!cacheableFields.containsAll(precachedFields))
			throw new IllegalArgumentException("precachedFields not present in cacheableFields: " + Sets.difference(precachedFields, cacheableFields));

		this.cacheableFields = cacheableFields;
		this.precachedFields = precachedFields;

	}

	/**
	 * Cacheable fields. Entity stored in this cache will have (at most) these fields initialized.
	 *
	 * @return cacheable fields
	 */
	public Set<F> getCacheableFields() {
		return cacheableFields;
	}

	/**
	 * Returns precached fields. Entity stored in this cache will always have these fields initialized.
	 *
	 * @return precached fields
	 */
	public Set<F> getPrecachedFields() {
		return precachedFields;
	}

	/**
	 * <p>Returns entity using any stored data that may be available.</p>
	 * <p>Entity represents <u>copy</u> of cached value, so it's safe to further modify it.</p>
	 * <p>Once retrieved, entity is cached (requested fields + precached fields) so subsequesnt retrievals will only use cache.</p>
	 *
	 * @param id ID of entity to retrieve
	 * @param graph fields to retrieve
	 * @param service {@link NarService} implementation to use when ID is not found in cache (or some fields are missing)
	 *
	 * @return entity with (sub)fields initialized as specified by {@code graph} or {@code null} if entity doesn't exist
	 */
	public C get(I id, NarGraph<F> graph, NarService<I, C, F> service) {

		C entity = get(id);

		// refresh cache for sure if we didn't had entity cached at all
		boolean refreshCache = entity == null;

		NarGraph<F> extendedGraph = NarGraph.Builder.of(graph).add(precachedFields).build();

		if (entity == null) {
			// we don't have this entity cached, delegate to service
			entity = service.get(id, extendedGraph);
			if (entity == null)
				return null;    // entity doesn't exist
		} else {
			// we found (something) stored
			Set<F> cachedFields = entity.getFields().isEmpty() ? EnumSet.noneOf(entity.getFieldsClass()) : EnumSet.copyOf(entity.getFields());
			entity.extend(extendedGraph, service);
			// refresh cache if we now (after extend) have more cacheable fields than was present in cache
			refreshCache = !cachedFields.containsAll(Sets.intersection(entity.getFields(), cacheableFields));
		}

		if (refreshCache)
			merge(entity);
			
		// strip to only fields caller requested
		entity.intersect(graph);

		return entity;
		
	}

	/**
	 * <p>Returns cached entity, returning {@code null} if entity doesn't exist in cache.</p>
	 * <p>Entity represents <u>copy</u> of cached value, so it's safe to further modify it.</p>
	 *
	 * @param id ID of entity to retrieve
	 *
	 * @return entity with cached fields initialized or {@code null} if entity isn't cached
	 */
	public C get(I id) {
		lock.readLock().lock();
		try {
			C entity = cache.get(id);
			if (entity != null)
				entity = entity.cloneAll();	// clone stored value for safe future modifications
			return entity;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * <p>Stores given entity, overwriting any previously stored value if it existed.</p>
	 * <p>In most cases, {@link #merge(NarEntity)} is a better choice.</p>
	 *
	 * @param entity entity to store
	 */
	public void put(C entity) {

		C entityToCache = toCacheableEntity(entity);

		if (entityToCache.getFields().isEmpty())
			return;	// nothing to cache
		
		lock.writeLock().lock();
		try {
			cache.put(entityToCache.getId(), entityToCache);
		} finally {
			lock.writeLock().unlock();
		}
		
	}

	/**
	 * Stores given entity in cache, merging it with any previously cached value if it existed.
	 *
	 * @param entity entity to store
	 */
	public void merge(C entity) {

		C entityToCache = toCacheableEntity(entity);

		if (entityToCache.getFields().isEmpty())
			return;	// nothing to cache
		
		lock.readLock().lock();
		
		try {
		
			C cached = cache.get(entityToCache.getId());
			
			lock.readLock().unlock();
			lock.writeLock().lock();
			
			try {

				if (cached == null)
					cache.put(entityToCache.getId(), entityToCache);
				else
					cached.pull(entityToCache);

				lock.readLock().lock();
				
			} finally {
				lock.writeLock().unlock();
			}

		} finally {
			lock.readLock().unlock();
		}

	}

	/** Removes all cached entities. */
	public void clear() {
		lock.writeLock().lock();
		try {
			cache.clear();
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Removes entity from cache.
	 *
	 * @param entity entity to remove
	 */
	public void clear(C entity) {
		clear(entity.getId());
	}

	/**
	 * Removes from cache entity with given ID.
	 *
	 * @param id entity's ID
	 */
	public void clear(I id) {
		lock.writeLock().lock();
		try {
			cache.remove(id);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Clears requested fields from cached entity.
	 *
	 * @param entity entity whose fields to clear
	 * @param fields fields to clear
	 */
	public void clear(C entity, Set<F> fields) {
		clear(entity.getId(), fields);
	}

	/**
	 * Clears requested fields from cached entity with given ID.
	 *
	 * @param id entity's ID
	 * @param fields fields to clear
	 */
	public void clear(I id, Set<F> fields) {
		lock.readLock().lock();
		try {
			C cached = cache.get(id);
			if (cached == null)
				return;
			lock.readLock().unlock();
			lock.writeLock().lock();
			try {
				cached.clearFields(fields);
				lock.readLock().lock();
			} finally {
				lock.writeLock().unlock();
			}
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Removes from cache entities matched by given filter.
	 *
	 * @param filter filter to select entities to clear
	 */
	public void clearMatching(Predicate<C> filter) {
		lock.writeLock().lock();
		try {
			cache.values().removeIf(filter);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Clears requested fields from entities matched by given filter.
	 *
	 * @param filter filter to select entities which fields should be cleared
	 * @param fields fields to clear
	 */
	public void clearMatching(Predicate<C> filter, Set<F> fields) {
		lock.writeLock().lock();
		try {
			for (C cached : cache.values())
				if (!Sets.intersection(cached.getFields(), fields).isEmpty() && filter.test(cached))
					cached.clearFields(fields);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Removes from cache given entities.
	 *
	 * @param entities entities to remove from cache
	 */
	public void clearEntities(Collection<C> entities) {
		lock.writeLock().lock();
		try {
			cache.values().removeAll(entities);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Clears requested fields from given entities.
	 *
	 * @param entities entities whose fields to clear
	 * @param fields fields to clear from given entities
	 */
	public void clearEntities(Collection<C> entities, Set<F> fields) {
		entities.forEach(entity -> clear(entity, fields));
	}

	/**
	 * Removes from cache entities with given IDs.
	 *
	 * @param ids IDs of entities to remove from cache
	 */
	public void clearAll(Collection<I> ids) {
		lock.writeLock().lock();
		try {
			cache.keySet().removeAll(ids);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Clears requested fields from entities with given IDs.
	 *
	 * @param ids IDs of entities whose fields to clear
	 * @param fields fields to clear from given entities
	 */
	public void clearAll(Collection<I> ids, Set<F> fields) {
		ids.forEach(id -> clear(id, fields));
	}

	/**
	 * Clone and strip {@code entity} to only interested fields.
	 *
	 * @param entity entity to convert
	 *
	 * @return {@code entity}'s clone suitable to store in cace
	 */
	private C toCacheableEntity(C entity) {
		
		Set<F> fieldsIntersection = Sets.intersection(entity.getFields(), cacheableFields);
		NarGraph<F> graph = !fieldsIntersection.isEmpty() ? NarGraph.of(fieldsIntersection) : NarGraph.noneOf(entity.getFieldsClass());
		
		return entity.clone(graph);
		
	}

	private final Set<F> cacheableFields;
	private final Set<F> precachedFields;
	private final Map<I, C> cache = new HashMap<>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

}
