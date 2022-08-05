package com.steatoda.nar.model.carrier;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.steatoda.nar.NarField;
import com.steatoda.nar.NarGraph;
import com.steatoda.nar.FieldUnavailableException;
import com.steatoda.nar.NarEntityBase;
import com.steatoda.nar.demo.model.person.Person;

/** Complete entity with many levels of nested subentites */
public class Carrier extends NarEntityBase<String, Carrier, Carrier.Field> {

	public enum Field implements NarField {
		
		name,
		entity			(Person.Field.class),
		list			(Person.Field.class),
		set				(Person.Field.class),
		map				(Person.Field.class),
		listlist		(Person.Field.class),
		setset			(Person.Field.class),
		maplist			(Person.Field.class),
		mapset			(Person.Field.class),
		mapmap			(Person.Field.class),
		mapentitylist	(Person.Field.class);
		
		Field() { this(null); }
		<F extends Enum<F> & NarField> Field(Class<F> clazz) { this.clazz = clazz; }
		@Override
		@SuppressWarnings("unchecked")
		public <F extends Enum<F> & NarField> Class<F> getNarFieldClass() { return (Class<F>) clazz; }
		private final Class<?> clazz;
		
	}
	
	public static Carrier ref(String id) {
		return new Carrier().setId(id);
	}

	public Carrier() {
		super(Field.class);
	}

	public String getName() { return fieldGet(Field.name, name); }
	public Carrier setName(String name) { this.name = fieldSet(Field.name, name); return this; }

	public Person getEntity() { return fieldGet(Field.entity, entity); }
	public Carrier setEntity(Person entity) { this.entity = fieldSet(Field.entity, entity); return this; }

	public List<Person> getList() { return fieldGet(Field.list, list); }
	public Carrier setList(List<Person> list) { this.list = fieldSet(Field.list, Optional.ofNullable(list).orElse(new ArrayList<>(0))); return this; }

	public Set<Person> getSet() { return fieldGet(Field.set, set); }
	public Carrier setSet(Set<Person> set) { this.set = fieldSet(Field.set, Optional.ofNullable(set).orElse(new HashSet<>(0))); return this; }

	public Map<String, Person> getMap() { return fieldGet(Field.map, map); }
	public Carrier setMap(Map<String, Person> map) { this.map = fieldSet(Field.map, Optional.ofNullable(map).orElse(new HashMap<>(0))); return this; }

	public List<List<Person>> getListList() { return fieldGet(Field.listlist, listlist); }
	public Carrier setListList(List<List<Person>> listlist) { this.listlist = fieldSet(Field.listlist, Optional.ofNullable(listlist).orElse(new ArrayList<>(0))); return this; }

	public Set<Set<Person>> getSetSet() { return fieldGet(Field.setset, setset); }
	public Carrier setSetSet(Set<Set<Person>> setset) { this.setset = fieldSet(Field.setset, Optional.ofNullable(setset).orElse(new HashSet<>(0))); return this; }

	public Map<String, List<Person>> getMapList() { return fieldGet(Field.maplist, maplist); }
	public Carrier setMapList(Map<String, List<Person>> maplist) { this.maplist = fieldSet(Field.maplist, Optional.ofNullable(maplist).orElse(new HashMap<>(0))); return this; }

	public Map<String, Set<Person>> getMapSet() { return fieldGet(Field.mapset, mapset); }
	public Carrier setMapSet(Map<String, Set<Person>> mapset) { this.mapset = fieldSet(Field.mapset, Optional.ofNullable(mapset).orElse(new HashMap<>(0))); return this; }

	public Map<String, Map<String, Person>> getMapMap() { return fieldGet(Field.mapmap, mapmap); }
	public Carrier setMapMap(Map<String, Map<String, Person>> mapmap) { this.mapmap = fieldSet(Field.mapmap, Optional.ofNullable(mapmap).orElse(new HashMap<>(0))); return this; }

	public Map<Person, List<Person>> getMapEntityList() { return fieldGet(Field.mapentitylist, mapentitylist); }
	public Carrier setMapEntityList(Map<Person, List<Person>> mapentitylist) { this.mapentitylist = fieldSet(Field.mapentitylist, Optional.ofNullable(mapentitylist).orElse(new HashMap<>(0))); return this; }

	@Override
	public Object pull(Field field, Carrier other, NarGraph<Field> graph) {
		switch (field) {
			case name:			return pull(other, other::getName,			this::setName);
			case entity:		return pull(other, other::getEntity,		this::setEntity,		value -> value.clone(field, graph));
			case list:			return pull(other, other::getList,			this::setList,			value -> value.stream().map(object -> object.clone(field, graph)).collect(Collectors.toList()));
			case set:			return pull(other, other::getSet,			this::setSet,			value -> value.stream().map(object -> object.clone(field, graph)).collect(Collectors.toSet()));
			case map:			return pull(other, other::getMap,			this::setMap,			value -> value.entrySet().stream().map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().clone(field, graph))).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
			case listlist:		return pull(other, other::getListList,		this::setListList,		value -> value.stream().map(list -> list.stream().map(object -> object.clone(field, graph)).collect(Collectors.toList())).collect(Collectors.toList()));
			case setset:		return pull(other, other::getSetSet,		this::setSetSet,		value -> value.stream().map(set -> set.stream().map(object -> object.clone(field, graph)).collect(Collectors.toSet())).collect(Collectors.toSet()));
			case maplist:		return pull(other, other::getMapList,		this::setMapList,		value -> value.entrySet().stream().map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().stream().map(object -> object.clone(field, graph)).collect(Collectors.toList()))).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
			case mapset:		return pull(other, other::getMapSet,		this::setMapSet,		value -> value.entrySet().stream().map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().stream().map(object -> object.clone(field, graph)).collect(Collectors.toSet()))).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
			case mapmap:		return pull(other, other::getMapMap,		this::setMapMap,		value -> value.entrySet().stream().map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().entrySet().stream().map(entry2 -> new AbstractMap.SimpleEntry<>(entry2.getKey(), entry2.getValue().clone(field, graph))).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)))).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
			case mapentitylist:	return pull(other, other::getMapEntityList,	this::setMapEntityList,	value -> value.entrySet().stream().map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey().clone(field, graph), entry.getValue().stream().map(object -> object.clone(field, graph)).collect(Collectors.toList()))).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
		}
		throw new FieldUnavailableException(field);
	}

	public Carrier ref() {
		return ref(getId());
	}

	public String toString() {
		StringBuilder strBuilder = new StringBuilder(getId());
		if (getFields().contains(Field.name))
			strBuilder.append(" (").append(name).append(")");
		return strBuilder.toString();
	}

	private String name;
	private Person entity;
	private List<Person> list;
	private Set<Person> set;
	private Map<String, Person> map;
	private List<List<Person>> listlist;
	private Set<Set<Person>> setset;
	private Map<String, List<Person>> maplist;
	private Map<String, Set<Person>> mapset;
	private Map<String, Map<String, Person>> mapmap;
	private Map<Person, List<Person>> mapentitylist;
	
}
