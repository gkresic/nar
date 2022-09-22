---
layout: page
title: Advanced
permalink: /advanced/
hero_height: is-small
menubar: menu
---

# Advanced

Nar is designed as a low-level building block, so it can provide lots of non-default usage scenarios.

Here are some of them.

## Batching requests

Often on client side you'll need to retrieve or extend one exact entity from multiple places in your code.
One common example of such scenario are multiple handlers for *create* or *modify* events which will receive instances
of newly created or modified entities, but to do their internal processing, they'll first need to extend those entities
to ensure initialization of all required properties. Using `Nar(Async)Service` each of these components could extend
given entity on their own, but that would imply multiple requests to backing store for reading what often ends up
to be the same set of properties, multiple times.

Meet `NarBatcher` which is a special implementation of `NarAsyncService` and which knows how to use backing
`NarAsyncService` delegate to "batch" multiple retrievals of same entity into only one call to `NarAsyncService.get`
using union of all fields requested by different calling clients. This way multiple network round trips
and entity retrievals from backing store will be replaced with only one, saving time, resources and network bandwidth.

One important thing to consider is when to invoke `NarBatcher.run` which will actually execute all batched invocations.
If client app is running in web browser (remember, Nar is GWT compatible as is this pattern easily portable
to native JavaScript), the obvious place to schedule `run` is at the end of the current event loop.
Consider this GWT template:

```Java
public class CityBatcher extends NarBatcher<String, City, City.Field> {

  public static CityBatcher get() {
    
     if (instance == null)
        instance = new CityBatcher();
    
     if (!scheduled) {
        Scheduler.get().scheduleDeferred(instance::run);
        scheduled = true;
     }

     return instance;

  }

  public CityBatcher() {
     super(CityGwtService.get()); // CityGwtService implements NarAsyncService<String, City, City.Field>
  }

  @Override
  public void run() {

     super.run();
    
     scheduled = false;
    
  }
 
  private static CityBatcher instance = null;

  private static boolean scheduled = false;

}
```

Note that `NarBatcher`, by being an implementation of `NarAsyncService`, is suitable for any other operation where
`NarAsyncService` is needed, like extending objects, for example.

## Caching entities

Caching is hard. Not as hard as [naming things](https://www.mediawiki.org/wiki/Naming_things), but still... hard.

However, having the ability to cache only *partial* entities, multiple operations can be optimized and simplified:

1. Cache implementations can build their content incrementally, with each new request fetching from backing store
   only those properties that are not already present in cache.
2. Cache invalidation can be greatly simplified, since we can decide to cache only subset of properties
   we can easily invalidate and default to always reading from backing store those other properties that
   would be too hard to cache properly, since their invalidation would be too hard.

`NarEntityCache` was designed to laverage those exact patterns.

`NarEntityCache` requires you to specify which fields to cache and, optionally, which fields to **pre**cache.
Use precaching in cases when backing service have roughly the same performance when fetching one
vs. fetching many properties, such is the case when reading row from relational database where major cost
is *locating* row on disk, after which reading actual columns from that row is rather fast.

## NarCRUDService

As we already saw, `Nar(Async)Service` gives us the ability to retrieve and extend Nar entities,
but what about creating, modifying or even deleting them? Of course, you are free to implement those ops
in any way you prefer, but in Nar I've included one opinionated,
but versatile and battle-tested interface: `NarCRUDService`.
`NarCRUDService` proposes few approaches you can follow when designing your CRUD ops:

* Both `create` and `modify` accept a graph describing what properties should be re-read into entities
  after persisting them:
  ```Java
  CityCRUDService cityService = ...;

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
  ```

* Several `modify` methods are available for modifying only designated *parts* of your entities. This way,
  we can avoid that common scenario of having a lot of specialized `modifyThis` and `modifyThat` methods
  with only one `modify` which accepts list of fields describing properties which should be persisted
  (other properties, even if they are present in an entity, are ignored):
  ```Java
  CityCRUDService cityService = ...;

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
  ```

* `NarCRUDService` uses a concept of *selector* (any Java class that can, in any way,
  define criteria for matching entities) to count and/or retrieve collection of entities.
  You may define as many selectors as you want, but to give you just a few examples:
  * "collection" selectors that hold list of IDs describing entities to query/count
  * "filter" selectors defining criteria to which entities are compared against
  * etc.

* Working with large collections of entities is made possible by having `query` method which returns `Stream` of results,
  providing you the ability to produce entities in parallel to consuming them to avoid having large collections
  stored on the heap (just remember to close those streams!). `list` method is utility method for querying small
  collections of entities which collects stream returned by `query` into a `List` and closes stream afterwards.
  ```Java
  CityCRUDService cityService = ...;

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
  ```

* `batch` operation enables executing multiple `Create`, `Modify` and `Delete` operations as one,
  possibly transactional, operation. By default, they are just executing given collection of `NarCRUDOperation`s
  in a loop (which is just fine if your service implementation is working within a transaction itself
  like any database-specific implementation should), but you may override them and instead sent that whole collection
  to any backend to execute them in one step (this would make sense if your implementation is calling REST endpoints
  on backend with each request being one transactional unit of work).
  
  You may use `NarCRUDBatchBuilder` to help you with building batches of `NarCRUDOperation`s:
  ```Java
  CityCRUDService cityService = ...;

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
  ```

For one possible implementation of `NarCRUDService`, look at `NarJooqCRUDService` from `nar-jooq` subproject
which provides base implementation backed by relational database using [jOOQ](https://www.jooq.org/). 

`NarCRUDAsyncService` is asynchronous version of `NarCRUDService`.

`NarCachingCRUDService` is an implementation which leverages `NarEntityCache` to (partially) cache entities.

## Complex IDs

Nar requires that entities have exactly one unique identifier. However, often our entities model some _relationship_
or similar concept in which two or more values together compose a "complex" (or "composed") ID.

To accommodate such entities into Nar data model, you can introduce one additional type that wraps all the properties
that uniquely describes your entity and that implement proper `hashCode()` and `equals()` over them:

```Java
public class Budget extends NarEntityBase<Budget.Key, Budget, Budget.Field> {

	public static class Key {

		public Key(String countryId, String cityId) {
			this.countryId = countryId;
			this.cityId = cityId;
		}

		public String getCountryId() { return countryId; }
		public String getCityId() { return cityId; }

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + countryId.hashCode();
			result = prime * result + cityId.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Key))
				return false;
			Key other = (Key) obj;
			return countryId.equals(other.countryId) && cityId.equals(other.cityId);
		}

		private final String countryId;
		private final String cityId;

	}

	public enum Field implements NarField {

		country		(Country.Field.class),
		city		(City.Field.class),
		amount;

		Field() { this(null); }
		<F extends Enum<F> & NarField> Field(Class<F> clazz) { this.clazz = clazz; }
		@Override
		@SuppressWarnings("unchecked")
		public <F extends Enum<F> & NarField> Class<F> getNarFieldClass() { return (Class<F>) clazz; }
		private final Class<?> clazz;

	}

	public static Budget ref(Key id) {
		return new Budget().setId(id);
	}

	public Budget() {
		super(Field.class);
	}

	public Country getCountry() { return fieldGet(Field.country, country); }
	public Budget setCountry(Country country) { this.country = fieldSet(Field.country, country); return this; }

	public City getCity() { return fieldGet(Field.city, city); }
	public Budget setCity(City city) { this.city = fieldSet(Field.city, city); return this; }

	public Integer getAmount() { return fieldGet(Field.amount, amount); }
	public Budget setAmount(Integer amount) { this.amount = fieldSet(Field.amount, amount); return this; }

	@Override
	public Object pull(Field field, Budget other, NarGraph<Field> graph) {
		switch (field) {
			case country:	return pull(other, other::getCountry,	this::setCountry,	value -> value.clone(field, graph));
			case city:		return pull(other, other::getCity,		this::setCity,		value -> value.clone(field, graph));
			case amount:	return pull(other, other::getAmount,	this::setAmount);
		}
		throw new FieldUnavailableException(field);
	}

	@Override
	public Budget ref() {
		return ref(getId());
	}

	private Country country;
	private City city;
	private Integer amount;

}
```

## Entity vs. Object

Sometimes we are working with "data" objects that do not have unique identifier and hence could not be called
an "entity". Obviously, those object could not support any operation that includes `Nar(Async)Service`
(like `get` or `extend`), but that doesn't mean we should not be able to partially retrieve them 
when they are part of some entity graph or in some other domain-specific way.

For such objects, Nar provides `NarObject` interface (and `NarObjectBase` base implementation).
Actually, most of the core methods are defined in `NarObject` and `NarEntity` just extends it
with an `id` and `extend` support.