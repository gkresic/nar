package com.steatoda.nar.service.crud;

import com.steatoda.nar.NarEntity;

/**
 * <p>Vanilla implementation of {@link CRUDOperation} interface.
 * Use directly or as a base class for project-wide entities to add Jackson-annotations, for example.</p>
 *
 * @param <C> class implementing {@link NarEntity}
 */
public class CRUDOperationBase<C extends NarEntity<?, ?, ?>> implements CRUDOperation<C> {

	/**
	 * <p>Builder for {@link CRUDOperation.Type#Create} operation on {@code entity}.</p>
	 *
	 * @param <C> concrete implementation of class implementing {@link NarEntity}
	 * @param entity entity to create
	 *
	 * @return {@link CRUDOperation} of type {@link CRUDOperation.Type#Create} over a given {@code entity}
	 */
	public static <C extends NarEntity<?, ?, ?>> CRUDOperationBase<C> create(C entity) { return new CRUDOperationBase<>(Type.Create, entity); }

	/**
	 * <p>Builder for {@link CRUDOperation.Type#Modify} operation on {@code entity}.</p>
	 *
	 * @param <C> concrete implementation of class implementing {@link NarEntity}
	 * @param entity entity to modify
	 *
	 * @return {@link CRUDOperation} of type {@link CRUDOperation.Type#Modify} over a given {@code entity}
	 */
	public static <C extends NarEntity<?, ?, ?>> CRUDOperationBase<C> modify(C entity) { return new CRUDOperationBase<>(Type.Modify, entity); }

	/**
	 * <p>Builder for {@link CRUDOperation.Type#Delete} operation on {@code entity}.</p>
	 *
	 * @param <C> concrete implementation of class implementing {@link NarEntity}
	 * @param entity entity to delete
	 *
	 * @return {@link CRUDOperation} of type {@link CRUDOperation.Type#Delete} over a given {@code entity}
	 */
	public static <C extends NarEntity<?, ?, ?>> CRUDOperationBase<C> delete(C entity) { return new CRUDOperationBase<>(Type.Delete, entity); }

	/**
	 * <p>Constructs {@link CRUDOperationBase} of given {@code type} over a given {@code entity}.</p>
	 *
	 * @param type type of operation requested
	 * @param entity entity on which to perform operation
	 */
	public CRUDOperationBase(Type type, C entity) {
		this.type = type;
		this.entity = entity;
	}

	/** Type of operation */
	@Override
	public Type getType() { return type; }

	/** Entity on which to perform operation */
	@Override
	public C getEntity() { return entity; }

	private final Type type;
	private final C entity;

}
