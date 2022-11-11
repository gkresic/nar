package com.steatoda.nar.demo.model.person;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.steatoda.nar.NarGraph;
import com.steatoda.nar.FieldUnavailableException;
import com.steatoda.nar.NarField;
import com.steatoda.nar.demo.model.DemoEntity;
import com.steatoda.nar.demo.model.boat.Boat;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/** Any person in app, may it be marina staff, boat crew, skippers, etc. */
public class Person extends DemoEntity<String, Person, Person.Field> {

	public enum Field implements NarField {
		
		name,
		email,
		permissions,
		boat		(Boat.Field.class);
		
		Field() { this(null); }
		<F extends Enum<F> & NarField> Field(Class<F> clazz) { this.clazz = clazz; }
		@Override
		@SuppressWarnings("unchecked")
		public <F extends Enum<F> & NarField> Class<F> getNarFieldClass() { return (Class<F>) clazz; }
		private final Class<?> clazz;
		
	}

	public static Person ref(String id) {
		return new Person().setId(id);
	}

	public Person() {
		super(Field.class);
	}
	
	/** Persons full name */
	@JsonProperty
	public String getName() { return fieldGet(Field.name, name); }
	public Person setName(String name) { this.name = fieldSet(Field.name, name); return this; }

	/** E-mail (mind the GDPR!) */
	@JsonProperty
	public String getEmail() { return fieldGet(Field.email, email); }
	public Person setEmail(String email) { this.email = fieldSet(Field.email, email); return this; }

	/** Permissions in app */
	@JsonProperty
	public Set<String> getPermissions() { return fieldGet(Field.permissions, permissions); }
	public Person setPermissions(Set<String> permissions) { this.permissions = fieldSet(Field.permissions, Optional.ofNullable(permissions).orElse(new HashSet<>(0))); return this; }

	/** Boat where person currently is */
	@JsonProperty
	public Boat getBoat() { return fieldGet(Field.boat, boat); }
	public Person setBoat(Boat boat) { this.boat = fieldSet(Field.boat, boat); return this; }

	@Override
	public Object pull(Field field, Person other, NarGraph<Field> graph) {
		switch (field) {
			case name:			return pull(other, other::getName,			this::setName);
			case email:			return pull(other, other::getEmail,			this::setEmail);
			case permissions:	return pull(other, other::getPermissions,	this::setPermissions,	HashSet::new);
			case boat:			return pull(other, other::getBoat,			this::setBoat,			value -> value.clone(field, graph));
		}
		throw new FieldUnavailableException(field);
	}

	@Override
	public Person ref() {
		return ref(getId());
	}

	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder(getId());
		if (getFields().contains(Field.name))
			strBuilder.append(" (").append(name).append(")");
		return strBuilder.toString();
	}

	private String name;
	private String email;
	private Set<String> permissions;
	private Boat boat;

}
