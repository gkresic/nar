package com.steatoda.nar.service.crud;

import com.steatoda.nar.NarEntity;

/**
 * <p>Vanilla implementation of {@link NarCRUDOperation} interface.
 * Use directly or as a base class for project-wide entities to add Jackson-annotations, for example.</p>
 *
 * @param <C> class implementing {@link NarEntity}
 */
public class NarCRUDOperationBase<C extends NarEntity<?, ?, ?>> implements NarCRUDOperation<C> {

	/**
	 * <p>Builder for {@link NarCRUDOperation.Type#Create} operation on {@code entity}.</p>
	 *
	 * @param <C> concrete implementation of class implementing {@link NarEntity}
	 * @param entity entity to create
	 *
	 * @return {@link NarCRUDOperation} of type {@link NarCRUDOperation.Type#Create} over a given {@code entity}
	 */
	public static <C extends NarEntity<?, ?, ?>> NarCRUDOperationBase<C> create(C entity) { return new NarCRUDOperationBase<>(Type.Create, entity); }

	/**
	 * <p>Builder for {@link NarCRUDOperation.Type#Modify} operation on {@code entity}.</p>
	 *
	 * @param <C> concrete implementation of class implementing {@link NarEntity}
	 * @param entity entity to modify
	 *
	 * @return {@link NarCRUDOperation} of type {@link NarCRUDOperation.Type#Modify} over a given {@code entity}
	 */
	public static <C extends NarEntity<?, ?, ?>> NarCRUDOperationBase<C> modify(C entity) { return new NarCRUDOperationBase<>(Type.Modify, entity); }

	/**
	 * <p>Builder for {@link NarCRUDOperation.Type#Delete} operation on {@code entity}.</p>
	 *
	 * @param <C> concrete implementation of class implementing {@link NarEntity}
	 * @param entity entity to delete
	 *
	 * @return {@link NarCRUDOperation} of type {@link NarCRUDOperation.Type#Delete} over a given {@code entity}
	 */
	public static <C extends NarEntity<?, ?, ?>> NarCRUDOperationBase<C> delete(C entity) { return new NarCRUDOperationBase<>(Type.Delete, entity); }

	/**
	 * <p>Constructs {@link NarCRUDOperationBase} of given {@code type} over a given {@code entity}.</p>
	 *
	 * @param type type of operation requested
	 * @param entity entity on which to perform operation
	 */
	public NarCRUDOperationBase(Type type, C entity) {
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
