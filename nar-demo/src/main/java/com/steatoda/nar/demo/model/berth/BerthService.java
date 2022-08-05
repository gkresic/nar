package com.steatoda.nar.demo.model.berth;

import java.util.Set;

import com.steatoda.nar.NarGraph;
import com.steatoda.nar.service.NarService;

public interface BerthService extends NarService<String, Berth, Berth.Field> {

	@Override
	default Berth instance() {
		return new Berth();
	}

	/**
	 * Gets berth with given id.
	 * 
	 * @param id Berth id.
	 * @param fields Fields to initialize.
	 * 
	 * @return {@link Berth} for given id with requested fields initialized
	 * 			or <code>null</code> if berth with given id doesn't exist
	 */
	@Override
	Berth get(String id, NarGraph<Berth.Field> fields);

	/**
	 * Stores new berth in store and populates generated fields.
	 * 
	 * @param berth {@link Berth} with <u>all</u> fields set.
	 * 
	 * @throws com.steatoda.nar.FieldUnavailableException if not all fields are initialized
	 */
	void create(Berth berth);
	
	/**
	 * Modifies berth in store. Fields to update are taken from {@link Berth#getFields()}.
	 * 
	 * @param berth Berth to modify.
	 */
	default void modify(Berth berth) {
		modify(berth, berth.getFields());
	}
	
	/**
	 * Modifies requested fields for berth in store.
	 * 
	 * @param berth Berth to modify.
	 * @param fields Which fields in berth to modify.
	 */
	void modify(Berth berth, Set<Berth.Field> fields);

	/**
	 * Deletes berth from store.
	 * 
	 * @param berth {@link Berth} with only id required.
	 */
	void delete(Berth berth);

}
