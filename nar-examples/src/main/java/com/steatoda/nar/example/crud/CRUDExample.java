package com.steatoda.nar.example.crud;

import com.steatoda.nar.NarGraph;
import com.steatoda.nar.example.first_object.City;
import com.steatoda.nar.service.crud.NarCRUDBatchBuilder;
import com.steatoda.nar.service.crud.NarCRUDOperation;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

public class CRUDExample {

	public void createExample() {

		CityCRUDService cityService = new CityCRUDService();

		// initialize new instance
		City city = new City()
			.setName("Zagreb")
			.setPopulation(806_341)
		;

		// graph that we'll request service to initialize *after* persisting data
		// this is important if service and/or backing store may do some processing on the data before storing it
		NarGraph<City.Field> graph = NarGraph.of(City.Field.name, City.Field.population, City.Field.streets);

		// create new city (let's assume backend store generates ID automatically)
		cityService.create(city, graph);

		// now we can read city.getName() and city.getPopulation() AND city.getStreets()

	}

	public void modifyExample() {

		CityCRUDService cityService = new CityCRUDService();

		// read existing instance
		City city = cityService.get("zagreb", NarGraph.of(City.Field.population));

		// modify just one attribute (newborn :) )
		city.setPopulation(city.getPopulation() + 1);

		// graph that we'll request service to initialize *after* persisting data
		// this is important if service and/or backing store may do some processing on the data before storing it
		NarGraph<City.Field> graph = NarGraph.of(City.Field.name, City.Field.population);

		// persist just that one attribute, leaving other unchanged
		cityService.modify(city, EnumSet.of(City.Field.population), graph);

		// now we can read city.getName() and city.getPopulation()

	}

	public void queryExample() {

		CityCRUDService cityService = new CityCRUDService();

		// NOTE selector is domain-specific, here we use Void just as a placeholder
		Void selector = null;

		// graph that we'll request
		NarGraph<City.Field> graph = NarGraph.of(City.Field.name, City.Field.population);

		// query cities
		try (Stream<City> cities = cityService.query(selector, graph)) {
			cities.forEach(city -> System.out.printf("%s with population of %d%n", city.getName(), city.getPopulation()));
		}

		// in case we are *sure* all queries cities will fit on heap,
		// we can simplify querying with method 'list'
		for (City city : cityService.list(selector, graph))
			System.out.printf("%s with population of %d%n", city.getName(), city.getPopulation());

	}

	public void batchExample() {

		CityCRUDService cityService = new CityCRUDService();

		// read existing instances
		City zagreb = cityService.get("zagreb", NarGraph.of(City.Field.name));
		City split = cityService.get("split", NarGraph.of(City.Field.name));
		City old = City.ref("old");	// for deleting entities, we need only ID

		// move one citizen from 'zagreb' to 'split'
		zagreb.setPopulation(zagreb.getPopulation() - 1);
		split.setPopulation(split.getPopulation() + 1);

		// build a batch for modifying 'zagreb' and 'split' and deleting 'old'
		// NOTE batched modify persists *all* initialized properties
		List<NarCRUDOperation<City>> batch = new NarCRUDBatchBuilder<City>()
			.addModify(zagreb)
			.addModify(split)
			.addDelete(old)
			.build()
		;

		cityService.batch(batch.stream());

	}

}
