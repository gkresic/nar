package com.steatoda.nar.example.services;

import com.steatoda.nar.NarGraph;
import com.steatoda.nar.example.first_object.Country;
import com.steatoda.nar.service.NarService;

public class CountryService implements NarService<String, Country, Country.Field> {

	@Override
	public Country instance() {
		return new Country();
	}

	@Override
	public Country get(String id, NarGraph<Country.Field> graph) {
		// TODO resolve Country instance according to specified graph
		return null;
	}

}