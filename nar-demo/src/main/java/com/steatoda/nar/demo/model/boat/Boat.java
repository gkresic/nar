package com.steatoda.nar.demo.model.boat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.steatoda.nar.NarGraph;
import com.steatoda.nar.FieldUnavailableException;
import com.steatoda.nar.NarField;
import com.steatoda.nar.demo.model.DemoEntity;
import com.steatoda.nar.demo.model.marina.Marina;
import com.steatoda.nar.demo.model.person.Person;

/** Any vessel in app */
public class Boat extends DemoEntity<String, Boat, Boat.Field> {

	public enum Field implements NarField {
		
		name,
		type,
		homeport	(Marina.Field.class),
		skipper		(Person.Field.class),
		crew		(Person.Field.class);
		
		Field() { this(null); }
		<F extends Enum<F> & NarField> Field(Class<F> clazz) { this.clazz = clazz; }
		@Override
		@SuppressWarnings("unchecked")
		public <F extends Enum<F> & NarField> Class<F> getNarFieldClass() { return (Class<F>) clazz; }
		private final Class<?> clazz;
		
	}
	
	public static Boat ref(String id) {
		return new Boat().setId(id);
	}

	public Boat() {
		super(Field.class);
	}

	/** Official boat name */
	@JsonProperty
	public String getName() { return fieldGet(Field.name, name); }
	public Boat setName(String name) { this.name = fieldSet(Field.name, name); return this; }

	/** Type of boat (motorboat, sailboat etc.) */
	@JsonProperty
	public String getType() { return fieldGet(Field.type, type); }
	public Boat setType(String type) { this.type = fieldSet(Field.type, type); return this; }

	/** Marina where boat is registered */
	@JsonProperty
	public Marina getHomeport() { return fieldGet(Field.homeport, homeport); }
	public Boat setHomeport(Marina homeport) { this.homeport = fieldSet(Field.homeport, homeport); return this; }

	/** Boat's skipper */
	@JsonProperty
	public Person getSkipper() { return fieldGet(Field.skipper, skipper); }
	public Boat setSkipper(Person skipper) { this.skipper = fieldSet(Field.skipper, skipper); return this; }

	/** Current crew */
	@JsonProperty
	public List<Person> getCrew() { return fieldGet(Field.crew, crew); }
	public Boat setCrew(List<Person> crew) { this.crew = fieldSet(Field.crew, Optional.ofNullable(crew).orElse(new ArrayList<>(0))); return this; }
	
	@Override
	public Object pull(Field field, Boat other, NarGraph<Field> graph) {
		switch (field) {
			case name:		return pull(other, other::getName,		this::setName);
			case type:		return pull(other, other::getType,		this::setType);
			case homeport:	return pull(other, other::getHomeport,	this::setHomeport,	value -> value.clone(field, graph));
			case skipper:	return pull(other, other::getSkipper,	this::setSkipper,	value -> value.clone(field, graph));
			case crew:		return pull(other, other::getCrew,		this::setCrew,		value -> value.stream().map(object -> object.clone(field, graph)).collect(Collectors.toList()));
		}
		throw new FieldUnavailableException(field);
	}

	@Override
	public Boat ref() {
		return ref(getId());
	}

	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder(Objects.toString(getId(), "<NEW>"));
		if (hasFields(Field.name))
			strBuilder.append(" (").append(name).append(")");
		return strBuilder.toString();
	}

	private String name;
	private String type;
	private Marina homeport;
	private Person skipper;
	private List<Person> crew;

}
