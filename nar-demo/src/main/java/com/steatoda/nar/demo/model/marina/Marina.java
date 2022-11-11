package com.steatoda.nar.demo.model.marina;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.steatoda.nar.NarGraph;
import com.steatoda.nar.NarField;
import com.steatoda.nar.FieldUnavailableException;
import com.steatoda.nar.demo.model.DemoEntity;
import com.steatoda.nar.demo.model.berth.Berth;
import com.steatoda.nar.demo.model.person.Person;

/** Place where boats sleep */
public class Marina extends DemoEntity<String, Marina, Marina.Field> {

	public enum Field implements NarField {
		
		name,
		manager		(Person.Field.class),
		latitude,
		longitude,
		berths		(Berth.Field.class),
		depths;
		
		Field() { this(null); }
		<F extends Enum<F> & NarField> Field(Class<F> clazz) { this.clazz = clazz; }
		@Override
		@SuppressWarnings("unchecked")
		public <F extends Enum<F> & NarField> Class<F> getNarFieldClass() { return (Class<F>) clazz; }
		private final Class<?> clazz;
		
	}
	
	public static Marina ref(String id) {
		return new Marina().setId(id);
	}

	public Marina() {
		super(Field.class);
	}
	
	/** Official name */
	@JsonProperty
	public String getName() { return fieldGet(Field.name, name); }
	public Marina setName(String name) { this.name = fieldSet(Field.name, name); return this; }
	
	/** Manager in charge */
	@JsonProperty
	public Person getManager() { return fieldGet(Field.manager, manager); }
	public Marina setManager(Person manager) { this.manager = fieldSet(Field.manager, manager); return this; }
	
	/** Latitude in EPSG:4326 */
	@JsonProperty
	public Double getLatitude() { return fieldGet(Field.latitude, latitude); }
	public Marina setLatitude(Double latitude) { this.latitude = fieldSet(Field.latitude, latitude); return this; }
	
	/** Longitude in EPSG:4326 */
	@JsonProperty
	public Double getLongitude() { return fieldGet(Field.longitude, longitude); }
	public Marina setLongitude(Double longitude) { this.longitude = fieldSet(Field.longitude, longitude); return this; }
	
	/** List of available berths */
	@JsonProperty
	public List<Berth> getBerths() { return fieldGet(Field.berths, berths); }
	public Marina setBerths(List<Berth> berths) { this.berths = fieldSet(Field.berths, Optional.ofNullable(berths).orElse(new ArrayList<>(0))); return this; }
	
	/** Sounding depths (*LARGE* blob) */
	@JsonProperty
	public Integer[][] getDepths() { return fieldGet(Field.depths, depths); }
	public Marina setDepths(Integer[][] depths) { this.depths = fieldSet(Field.depths, Optional.ofNullable(depths).orElse(new Integer[0][0])); return this; }

	@Override
	public Object pull(Field field, Marina other, NarGraph<Field> graph) {
		switch (field) {
			case name:		return pull(other, other::getName,		this::setName);
			case manager:	return pull(other, other::getManager,	this::setManager,	value -> value.clone(field, graph));
			case latitude:	return pull(other, other::getLatitude,	this::setLatitude);
			case longitude:	return pull(other, other::getLongitude,	this::setLongitude);
			case berths:	return pull(other, other::getBerths,	this::setBerths,	value -> value.stream().map(object -> object.clone(field, graph)).collect(Collectors.toList()));
			case depths:	return pull(other, other::getDepths,	this::setDepths,	value -> Arrays.stream(value).map(Integer[]::clone).toArray(Integer[][]::new));
		}
		throw new FieldUnavailableException(field);
	}

	@Override
	public Marina ref() {
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
	private Person manager;
	private Double latitude;
	private Double longitude;
	private List<Berth> berths;
	private Integer[][] depths = new Integer[0][0];

}
