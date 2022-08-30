package com.steatoda.nar.example.services;

import com.steatoda.nar.NarGraph;
import com.steatoda.nar.example.first_object.City;
import com.steatoda.nar.example.first_object.Country;

import java.util.EnumSet;

public class ServiceExample {

	public void extendExample() {

		CityService cityService = new CityService();

		City city = cityService.get("zagreb", NarGraph.of(City.Field.name, City.Field.population));

		// now we can read city.getName() and city.getPopulation(), but NOT city.getStreets()

		// extend entity with two fields: 'name' and 'streets'
		// NOTE: only 'streets' are fetched from service, since 'name' was already initialized
		city.extend(NarGraph.of(City.Field.name, City.Field.streets), cityService);

		// now we can read city.getName() and city.getPopulation() AND city.getStreets()

	}

	public void getOptionalExample() {

		CityService cityService = new CityService();

		City city = cityService.get("zagreb", NarGraph.of(City.Field.name));

		city.getIfPresent(city::getName, EnumSet.of(City.Field.name));

		city.getOptional(city::getName, EnumSet.of(City.Field.name));

	}

	public Country getHomelandExample(NarGraph<Country.Field> requestedGraph) {

		CountryService countryService = new CountryService();

		// build graph that will contain whatever client requested combined with what we'll need internally
		NarGraph<Country.Field> workGraph = NarGraph.Builder.of(Country.Field.class)
			// add whatever client requested
			.add(requestedGraph)
			// add fields we'll need in this function internally
			.add(Country.Field.capital, NarGraph.Builder.of(City.Field.class)
				.add(City.Field.name)
				.add(City.Field.population)
				.build()
			)
			.build();

		// resolve country with "extended" graph
		Country country = countryService.get("croatia", workGraph);

		// process country...

		// strip country to contain only fields client requested
		country.intersect(requestedGraph);

		return country;

	}

}
