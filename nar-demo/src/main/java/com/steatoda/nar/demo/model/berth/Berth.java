package com.steatoda.nar.demo.model.berth;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.steatoda.nar.NarGraph;
import com.steatoda.nar.FieldUnavailableException;
import com.steatoda.nar.demo.model.DemoEntity;
import com.steatoda.nar.demo.model.boat.Boat;
import com.steatoda.nar.NarField;

/** Docking berth at marina */
public class Berth extends DemoEntity<String, Berth, Berth.Field> {

	public enum Field implements NarField {
		
		boat	(Boat.Field.class);

		@SuppressWarnings("unused")
		Field() { this(null); }
		<F extends Enum<F> & NarField> Field(Class<F> clazz) { this.clazz = clazz; }
		@Override
		@SuppressWarnings("unchecked")
		public <F extends Enum<F> & NarField> Class<F> getNarFieldClass() { return (Class<F>) clazz; }
		private final Class<?> clazz;
		
	}
	
	public static Berth ref(String id) {
		return new Berth().setId(id);
	}

	public Berth() {
		super(Field.class);
	}
	
	/** Docked boat */
	@JsonProperty
	public Boat getBoat() { return fieldGet(Field.boat, boat); }
	public Berth setBoat(Boat boat) { this.boat = fieldSet(Field.boat, boat); return this; }

	@Override
	public Object pull(Field field, Berth other, NarGraph<Field> graph) {
		switch (field) {
			case boat:	return pull(other, other::getBoat,	this::setBoat,	value -> value.clone(field, graph));
		}
		throw new FieldUnavailableException(field);
	}

	@Override
	public Berth ref() {
		return ref(getId());
	}

	private Boat boat;

}
