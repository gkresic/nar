package com.steatoda.nar.example.crud;

import com.steatoda.nar.NarGraph;
import com.steatoda.nar.example.first_object.City;
import com.steatoda.nar.example.services.CityService;
import com.steatoda.nar.service.crud.NarCRUDService;

import java.util.Set;
import java.util.stream.Stream;

public class CityCRUDService extends CityService implements NarCRUDService<String, City, City.Field, Void> {

	@Override
	public void create(City city, NarGraph<City.Field> graph) {
		// TODO persist 'city' and initialize it with specified graph
		throw new UnsupportedOperationException("Not implemented, just a placeholder for an example");
	}

	@Override
	public void modify(City city, City patch, NarGraph<City.Field> graph) {
		// TODO persist properties specified by 'patch' and initialize 'city' with specified graph
		throw new UnsupportedOperationException("Not implemented, just a placeholder for an example");
	}

	@Override
	public void delete(City city) {
		// TODO delete 'city'
		throw new UnsupportedOperationException("Not implemented, just a placeholder for an example");
	}

	@Override
	public Stream<City> queryAllFieldValues(Void selector, Set<City.Field> fields) {
		throw new UnsupportedOperationException("Not implemented, just a placeholder for an example");
	}

	@Override
	public int count(Void selector) {
		throw new UnsupportedOperationException("Not implemented, just a placeholder for an example");
	}

	@Override
	public Stream<City> query(Void selector, NarGraph<City.Field> graph) {
		throw new UnsupportedOperationException("Not implemented, just a placeholder for an example");
	}

}