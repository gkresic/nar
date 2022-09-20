---
layout: page
title: Basic concepts
permalink: /basic-concepts/
hero_height: is-small
menubar: menu
---

# Basic concepts

Primary goals of Nar is to provide following features:

* **retrieve** entities from backing store with only some fields initialized
* **keep track** of which fields in such entities are initialized
* **extend** existing entities with new fields (without re-reading already initialized fields from backing store)

Let's see what it takes to accomplish those goals.

## Defining entities

First, let's transform a simple Java bean into "Nar bean".

### Entity

As a starting point, let's use class `City` with four members:

1. `id` simple identifier

2. `name` city name

3. `population` number of people living in a city

5. `streets` list of every street in a city

Here it is:

```Java
import java.util.List;

public class City {

	public String getId() { return id; }
	public City setId(String id) { this.id = id; return this; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public Integer getPopulation() { return population; }
	public void setPopulation(Integer population) { this.population = population; }

	public List<String> getStreets() { return streets; }
	public void setStreets(List<String> streets) { this.streets = streets; }

	private String name;
	private Integer population;
	private List<String> streets;

}
```

To support `null` values (which may represent real, meaningful value and not "uninitialized field"),
we are using `Integer` instead of primitive `int`. Fluent setters are a matter of personal taste, of course.

### NarField

For describing each field (class member) within an entity of certain class, Nar uses enums that implement `NarField`.
You'll need one such enum for each class you plan to convert to Nar entity with one enum member corresponding to exactly
one member in describing class (_except_ for identifier member, for which you don't need an enum field).

You can declare these enums wherever you prefer, but one convenient place is as an inner class on class whose fields it
should describe:

```Java
import java.util.List;
import com.steatoda.nar.NarField;

public class City {

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

	public String getId() { return id; }
	public City setId(String id) { this.id = id; return this; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public Integer getPopulation() { return population; }
	public void setPopulation(Integer population) { this.population = population; }

	public List<String> getStreets() { return streets; }
	public void setStreets(List<String> streets) { this.streets = streets; }

	private String name;
	private Integer population;
	private List<String> streets;

}
```

Due to some short-comings of Java language (like enum classes being final) there is some boilerplate code
needed to implement `NarField` interface, but fortunately it's just one template that can be safely copy/pasted.

### NarEntity/NarEntityBase

Nar uses Java interface `NarEntity` with several `default` methods to augment classes that implement it. In addition to
those provided, implementing classes are required to implement several additional methods, specific to that particular
class.

You can implement `NarEntity` interface directly or simply extend `NarEntityBase` class which provides some nice
implementations for us:

```Java
public class City extends NarEntityBase<String, City, City.Field> {
	// ...
}
```

Specialize `NarEntityBase` with:

1. `<I>`: type of identifier field
2. `<C>`: type of implementing class
3. `<F>`: type of enum describing fields in implementing class

Implementing constructor required by `NarEntityBase` is simple - just give it class of enum used for describing fields:

```Java
public City() {
	super(Field.class);
}
```

One of the core functionalities that `NarEntity` mandates (and `NarEntityBase` implements for us)
is one additional member of type `Set<F>` (with getter/setter pair) which will be used to track which fields
in our entity are actually initialized.

All you have to do is initialize that set properly in your setters, for example:

```Java
public City setName(String name) {
	this.name = name;
	getFields().add(Field.name);	// mark this field as initialized
	return this;
}
```

Optionally, you can check this same set to see whether field is initialized in your getters and raise an exception if
user tries to access uninitialized field:

```Java
public String getName() {
	if (!getFields().contains(Field.name))
		throw new FieldUnavailableException(Field.name);
	return name;
}
```

To reduce boilerplate, you can use provided `fieldGet`/`fieldSet` methods, which are implemented exactly as described
above, to simplify your getters and setters:

```Java
public String getName() { return fieldGet(Field.name, name); }
public City setName(String name) { this.name = fieldSet(Field.name, name); return this; }
```

Also, note that you don't need to implement `getId`/`setId` anymore, since those are provided in `NarEntityBase`.

Next step is to provide an implementation of `ref()` method which must return a 'reference' to current entity: copy of
this exact entity, but with only an identifier initialized. You can implement it alone, but since constructing
references is common task when working with Nar entities it usually makes sense to also implement a static method for
constructing them, like so:

```Java
public static City ref(String id) {
	return new City().setId(id);
}

// ...

@Override
public City ref() {
	return ref(getId());
}

```

Last step is to implement method `pull` which is only non-trivial, but highly templated "working horse" low-level method
which will be used by most other methods Nar provides automatically.

Requirement from this method is that it should copy ('pull') property(es) referenced by specified `field` from another
instance (`other`) obeying following rules:

* if `other == this`, don't copy anything, just return current value
* if `graph == null`, pull whole sub-tree for every sub-entity

Sounds complicated, but most of this logic is implemented in another (provided) methods we can leverage:

```
pull(HasFields, Supplier, Consumer)
```

or

```
pull(HasFields, Supplier, Consumer, Function)
```

In case of our `City` entity, pull method may be as simple as:

```Java
@Override
public Object pull(Field field, City other, NarGraph<Field> graph) {
	switch (field) {
		case name:			return pull(other, other::getName,			this::setName);								// simple pull
		case population:	return pull(other, other::getPopulation,	this::setPopulation);						// simple pull
		case streets:		return pull(other, other::getStreets,		this::setStreets,		ArrayList::new);	// use mapper to create a *copy* of the list
	}
	throw new FieldUnavailableException(field);
}
```

Implementing `field` selector as `switch` statement will ensure we'll get a warning if we miss any field which is very
useful when adding *new* fields and tracing where in your code base you should add logic specific to that field.

Final Nar-enabled implementation of City class:

```Java
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
```

### Hierarchy of entities

Of course, in real world scenarios, often one entity contains other entities as members, and we should be able not only
to pick fields in top-level entities, but also in their sub-entities. Nar supports that pattern, too.

For example, see the following implementation of entity `Country` which has several members of type `City`:

```Java
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
```

Two things deserves explanation here:

1. When declaring `NarField` enum item for a field which is of type that also implements `NarEntity`, you have to pass
   class of `NarField` type that describes fields in that sub-entity (it works both for "simple" sub-entities, but also
   for collections of such entities).
1. In `pull` method for fields that describe Nar sub-entities use mapper function and specialized `clone` which knows
   how to extract sub-tree from `graph`, cloning whole sub-tree if `graph` is `null`. For collections,
   do that for each member.

## Defining graphs

To resolve any `NarEntity` first thing we'll need is a model which will define which exact fields (and, optionally,
their sub-fields) we want resolved. For that purpose, we'll use `NarGraph` which is immutable set with optional method
to retrieve sub-graphs (also of type `NarGraph`) for fields which describe nested `NarEntity`ies. So, "hierarchical set".

In its most simple use case, `NarGraph` can be constructed much like regular `EnumSet`:

```Java
NarGraph<City.Field> SimpleGraph = NarGraph.of(City.Field.name, City.Field.population);
```

There are also other `EnumSet`-like creator methods:

```Java
NarGraph<City.Field> EmptyGraph = NarGraph.noneOf(City.Field.class);
NarGraph<City.Field> FullGraph = NarGraph.allOf(City.Field.class);
NarGraph<City.Field> WithoutStreetsGraph = NarGraph.complementOf(NarGraph.of(City.Field.streets));
```

These creators are simple, but can define fields only for entity itself and not for its sub-entities.
So, for example, graph for `Country` defined as:

```Java
NarGraph<Country.Field> FlatCountryGraph = NarGraph.of(Country.Field.name, Country.Field.capital);
```

Will have initialized field `capital` (of type `City`), but that sub-entity won't have any fields initialized
(besides mandatory `id`).

To define hierarchical graphs, we'll need `NarGraph.Builder`:

```Java
NarGraph<Country.Field> CountryWithCapitalGraph = NarGraph.Builder.of(Country.Field.class)
	.add(Country.Field.name)
	.add(Country.Field.capital, NarGraph.Builder.of(City.Field.class)
		.add(City.Field.name)
		.add(City.Field.population)
		.build()
	)
	.build()
;
```

`NarGraph.Builder` also supports combining multiple graphs, so you can attach graphs from other components
when building yours:

```Java
NarGraph<Country.Field> CombinedCapitalGraph = NarGraph.Builder.of(Country.Field.class)
	.add(FlatCountryGraph)
	.add(CountryWithCapitalGraph)
	.add(Country.Field.name)
	.add(Country.Field.capital, NarGraph.Builder.of(City.Field.class)
		.add(City.Field.streets)
		.build()
	)
	.build()
;
```

This `CombinedCapitalGraph` will now have the following structure:

```
┌name
└capital
  ├name
  ├population
  └streets
```

As has already been said, `NarGraph` implements `Set` interface, but also enables retrieving sub-graphs using `getGraph`
method:

```Java
NarGraph<City.Field> CapitalGraph = CountryWithCapitalGraph.getGraph(Country.Field.capital, City.Field.class);
```

`NarGraph` can be (de)serialized to/from string using `toString()` and `of(String, Class)`.
String representations use comma as field delimiter and curly braces for declaring sub-graphs:

```
name,capital{name,population},cities{name,streets}
```

## Resolving entities

Finally, we'll use `NarService` to resolve entities having only specified graph initialized (implementations of that
interface depend on your backing store):

```Java
public class CityService implements NarService<String, City, City.Field> {

	@Override
	public City instance() {
		return new City();
	}

	@Override
	public City get(String id, NarGraph<City.Field> graph) {
		// TODO resolve City instance according to specified graph
	}
	
}
```

And then:

```Java
CityService cityService = new CityService();
		
City city = cityService.get("zagreb", NarGraph.of(City.Field.name, City.Field.population));

// now we can read city.getName() and city.getPopulation(), but NOT city.getStreets()
```

For one possible implementation of `NarService` check `NarJooqService` from `nar-jooq` subproject which provides
concrete base implementation for reading entities from relational database using [jOOQ](https://www.jooq.org/).

In case you need asynchronous pattern, you should implement `NarAsyncService` which is an asynchronous
version of `NarService`, but with the same purpose. Use in cases where synchronous logic is not suitable
(like fetching objects over the network without blocking invoking thread). For handling responses
`NarAsyncService` uses `NarServiceHandler` which should be abstract enough to serve as a handler for any type of
asynchronous mechanism used in concrete implementations.

## Extending entities

`NarService` can also be used to extend existing entities with new fields. Extending works by first inspecting which
fields (if any!) are missing and then using provided `NarService` to retrieve from backing store only fields that are
missing and "patch" them onto entity.

```Java
CityService cityService = new CityService();

City city = cityService.get("zagreb", NarGraph.of(City.Field.name, City.Field.population));

// now we can read city.getName() and city.getPopulation(), but NOT city.getStreets()

// extend entity with two fields: 'name' and 'streets'
// NOTE: only 'streets' is fetched from cityService, since 'name' is already initialized
city.extend(NarGraph.of(City.Field.name, City.Field.streets), cityService);

// now we can read city.getName(), city.getPopulation() AND city.getStreets()
```