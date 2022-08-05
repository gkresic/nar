package com.steatoda.nar.example.first_object;

import java.util.ArrayList;
import java.util.List;

import com.steatoda.nar.FieldUnavailableException;
import com.steatoda.nar.NarEntityBase;
import com.steatoda.nar.NarField;
import com.steatoda.nar.NarGraph;

public class City extends NarEntityBase<String, City, City.Field> {

	public enum Field implements NarField {

		name,
		population,
		streets;

		Field() { this(null); }
		<F extends Enum<F> & NarField> Field(Class<F> clazz) { this.clazz = clazz; }
		@Override
		@SuppressWarnings("unchecked")
		public <F extends Enum<F> & NarField> Class<F> getNarFieldClass() { return (Class<F>) clazz; }
		private final Class<?> clazz;

	}

	public static City ref(String id) {
		return new City().setId(id);
	}

	public City() {
		super(Field.class);
	}

	public String getName() { return fieldGet(Field.name, name); }
	public City setName(String name) { this.name = fieldSet(Field.name, name); return this; }

	public Integer getPopulation() { return fieldGet(Field.population, population); }
	public City setPopulation(Integer population) { this.population = fieldSet(Field.population, population); return this; }

	public List<String> getStreets() { return fieldGet(Field.streets, streets); }
	public City setStreets(List<String> streets) { this.streets = fieldSet(Field.streets, streets); return this; }

	@Override
	public Object pull(Field field, City other, NarGraph<Field> graph) {
		switch (field) {
			case name:			return pull(other, other::getName,			this::setName);								// simple pull
			case population:	return pull(other, other::getPopulation,	this::setPopulation);						// simple pull
			case streets:		return pull(other, other::getStreets,		this::setStreets,		ArrayList::new);	// use mapper to pull *copy* of the list
		}
		throw new FieldUnavailableException(field);
	}

	@Override
	public City ref() {
		return ref(getId());
	}

	private String name;
	private Integer population;
	private List<String> streets;

}