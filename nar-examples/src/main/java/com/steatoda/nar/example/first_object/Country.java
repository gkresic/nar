package com.steatoda.nar.example.first_object;

import java.util.List;
import java.util.stream.Collectors;

import com.steatoda.nar.FieldUnavailableException;
import com.steatoda.nar.NarEntityBase;
import com.steatoda.nar.NarField;
import com.steatoda.nar.NarGraph;

public class Country extends NarEntityBase<String, Country, Country.Field> {

	public enum Field implements NarField {

		name,
		capital		(City.Field.class),
		cities		(City.Field.class);

		Field() { this(null); }
		<F extends Enum<F> & NarField> Field(Class<F> clazz) { this.clazz = clazz; }
		@Override
		@SuppressWarnings("unchecked")
		public <F extends Enum<F> & NarField> Class<F> getNarFieldClass() { return (Class<F>) clazz; }
		private final Class<?> clazz;

	}

	public static Country ref(String id) {
		return new Country().setId(id);
	}

	public Country() {
		super(Field.class);
	}

	public String getName() { return fieldGet(Field.name, name); }
	public Country setName(String name) { this.name = fieldSet(Field.name, name); return this; }

	public City getCapital() { return fieldGet(Field.capital, capital); }
	public Country setCapital(City capital) { this.capital = fieldSet(Field.capital, capital); return this; }

	public List<City> getCities() { return fieldGet(Field.cities, cities); }
	public Country setCities(List<City> cities) { this.cities = fieldSet(Field.cities, cities); return this; }

	@Override
	public Object pull(Field field, Country other, NarGraph<Field> graph) {
		switch (field) {
			case name:		return pull(other, other::getName,		this::setName);												// simple pull
			case capital:	return pull(other, other::getCapital,	this::setCapital,	value -> value.clone(field, graph));	// deep pull using 'clone'
			case cities:	return pull(other, other::getCities,	this::setCities,	values -> values.stream().map(value -> value.clone(field, graph)).collect(Collectors.toList()));	// deep pull of every member in a list
		}
		throw new FieldUnavailableException(field);
	}

	@Override
	public Country ref() {
		return ref(getId());
	}

	private String name;
	private City capital;
	private List<City> cities;

}
