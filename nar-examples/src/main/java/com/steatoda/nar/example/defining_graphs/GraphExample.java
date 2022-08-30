package com.steatoda.nar.example.defining_graphs;

import com.steatoda.nar.NarGraph;
import com.steatoda.nar.example.first_object.City;
import com.steatoda.nar.example.first_object.Country;

public class GraphExample {

	// most simple graphs defining only 'name' and 'population' fields for a City
	public static NarGraph<City.Field> SimpleGraph = NarGraph.of(City.Field.name, City.Field.population);

	// other EnumSet-like methods
	public static NarGraph<City.Field> EmptyGraph = NarGraph.noneOf(City.Field.class);
	public static NarGraph<City.Field> FullGraph = NarGraph.allOf(City.Field.class);
	public static NarGraph<City.Field> WithoutStreetsGraph = NarGraph.complementOf(NarGraph.of(City.Field.streets));

	// Country graph, but without any subfields in City sub-entities
	public static NarGraph<Country.Field> FlatCountryGraph = NarGraph.of(Country.Field.name, Country.Field.capital);

	// hierarchical Country graph
	public static NarGraph<Country.Field> CountryWithCapitalGraph = NarGraph.Builder.of(Country.Field.class)
		.add(Country.Field.name)
		.add(Country.Field.capital, NarGraph.Builder.of(City.Field.class)
			.add(City.Field.name)
			.add(City.Field.population)
			.build()
		)
		.build()
	;

	// combined Country graph
	public static NarGraph<Country.Field> CombinedCapitalGraph = NarGraph.Builder.of(Country.Field.class)
		.add(FlatCountryGraph)
		.add(CountryWithCapitalGraph)
		.add(Country.Field.name)
		.add(Country.Field.capital, NarGraph.Builder.of(City.Field.class)
			.add(City.Field.streets)
			.build()
		)
		.build()
	;

	// sub-graph extracted from CountryWithCapitalGraph
	public static NarGraph<City.Field> CapitalGraph = CountryWithCapitalGraph.getGraph(Country.Field.capital, City.Field.class);

}
