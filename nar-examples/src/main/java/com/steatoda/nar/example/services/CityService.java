package com.steatoda.nar.example.services;

import com.steatoda.nar.NarGraph;
import com.steatoda.nar.example.first_object.City;
import com.steatoda.nar.service.NarService;

public class CityService implements NarService<String, City, City.Field> {

	@Override
	public City instance() {
		return new City();
	}

	@Override
	public City get(String id, NarGraph<City.Field> graph) {
		// TODO resolve City instance according to specified graph
		throw new UnsupportedOperationException("Not implemented, just a placeholder for an example");
	}

}