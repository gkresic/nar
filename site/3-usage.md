---
layout: page
title: Usage
permalink: /usage/
hero_height: is-small
menubar: menu
---

# Usage

Operations provided to your entities by Nar are intentionally low-level, which makes them suitable for wide array of use
cases. You are free to explore how these Nar primitives could make you life easier and here I'll provide few best
practices that have been battle-tested on projects where I applied Nar. 

## Reading properties

Strictly checking whether properties are initialized or not in your getters is not inherently related to Nar,
but when you are working with partially initialized objects, preventing access to uninitialized properties
could save you a lot of headaches, both during development, but also in production,
since reading something that wasn't previously initialized is rarely a good idea.

Actually, Nar goes even further, offering you to use `fieldGet` method in your getters which will throw
`FieldUnavailableException` if field corresponding to that particular property was not present (check *Basic concepts*
on how to use it). On the other hand, guarding your every getter invocation with `try-catch` would be very troublesome.

Of course, Nar provides solution to this. Actually, two solutions, and you are free to leverage either one interchangeably,
depending on what serve you better at any given moment:

`getIfPresent` method will check if specified field(s) are initialized and only if they are it will invoke given
`Supplier` and return whatever it returns. In case any of the fields were not present in entity, `getIfPresent` will
return `null`.

So, for example, instead of invoking `city.getName()` getter directly, invoke it like this:

```
city.getIfPresent(city::getName, EnumSet.of(City.Field.name));
```

`getOptional` is similar in functionality to `getIfPresent` with only difference being return type: `getOptional`,
as its name suggests, returns `Optional` instead of the value of the property itself.

Use it like this:

```
city.getOptional(city::getName, EnumSet.of(City.Field.name));
```

Note that neither of these methods distinguishes between uninitialized properties and properties holding actual `null`
value. If your use case requires such distinction, then you have to fall back to explicitly checking if required fields
are initialized (using, for example, `hasFields` method).

## Extending entities

One of the main features of Nar is ability to "extent" entities in a way to read from backing store one or more new
properties and then patch them onto existing entity. Usage is already demonstrated in *Basic concepts* and is pretty
simple:

```Java
CityService cityService = ...;

City city = someFunction();

// at this point we can not know, without checking, which fields are initialized,
// so lets extend our entity with fields we need to be sure they are present
	
city.extend(NarGraph.of(City.Field.name, City.Field.streets), cityService);

// now we can read city.getName() and city.getStreets() for sure
```

Note that extending an entity with fields that are already initialized is a no-op, which makes this operation suitable
for invocation in every code segment that works on given entity. 

This pattern allows us to gradually "build" our objects, filling them with more and more properties as they are being
passed from one function to another, each of them extending an entity with properties it needs.

Of course, each round-trip to backing store to fetch new properties has some inherent cost we have to factor in.
Luckily, often we know not only object graph our function needs, but also what graph is needed by every other component
we intend to pass our entity to. In those cases we can eagerly fetch complete object graph we know it will be required
for processing given entity down the execution chain:

```Java
CityService cityService = ...;

City city = someFunction();

// let's say we have few components that will have to do something with our 'city' instance: 
CityProcessor processor = ...;
CityDisplay display = ...;

// if all those components "publish" graph they work with, we could include their graph into our own and extend
// our instance in a way that those subcomponents, once we pass our entity to them, get already initialized instance for
// which no further extends are needed (also, note that using NarGraph.Builder is usually better choice then NarGraph.of):

FieldGraph<City.Field> graph = FieldGraph.Builder.of(City.Field.class)
	// append complete graph 'processor' needs
	.add(processor.graph())
	// append complete graph 'display' needs
	.add(display.graph())
	// rest are the fields we'll need in this particuly function
	.add(City.Field.name)
	.add(City.Field.streets)
	.build();
	
city.extend(graph, cityService);

// now we can read city.getName() and city.getStreets() for sure...

// ...but also pass our instance to other components knowing they won't have to extend entity any more:

processor.process(city);
display.show(city);
```

Obviously, `processor.graph()` and `display.graph()` may include graphs from subcomponents they'll use, and that
subcomponents may include graphs from their subcomponents etc. making propagation of whole graphs that may be needed easy,
so that you can prepare your entity for the whole process in which it will be involved with only one call to `extend`
(or, even better, fetch complete graph in the first place, during invocation of initial `get`).

Of course, these optimizations work only if your code is linear - if any components can branch into different execution
paths, each requiring different properties, then gradual extending is the only way to go.

In a scenario where you can expect that multiple components will try to extend same instance simultaneously
(for example, as a reaction to some form of a 'modify' event) you can further optimize execution by grouping those
multiple extends into only one using `NarBatcher` (see how to use it in *Advanced usage*).

Please do note that the entity may change between the time it was originally retrieved from the backing store
(using `get`) and the moment you `extend` it leading for data inconsistencies (in an extreme case entity may
even get deleted by the time you try to extend it in which case you'll get `EntityUnavailableException`).
So, it is wise to limit usage of `extend` to isolated execution (for example one transaction on a backend
or one event loop cycle on frontend) or include some domain-specific logic for handling these edge cases.

## Filtering entities

Opposite process of extending entities is their reduction to not contain any unneeded fields.
One of the cases in which this is useful (if not mandatory) is at the end of th client-facing entrypoints into your code
(like API endpoints). Those entrypoints usually accept from the client graph that should be returned,
fetch entity with (at least) that graph initialized, but then usually process it in some way
(most obviously to do some access control checks) before returning it back to client.
As we have shown in the previous chapter, each of that processing step may have extended our entity
with some additional fields it needed, but which were not requested by the client and should not be returned to him.

So, before we return requested entity back to client, we need to "strip" from it any unnecessary fields.

Nar provides one operation for that exact purpose: `intersect` method.
It accepts `NarGraph` defining what should be left in the entity and any field not contained in that graph will be
cleared from the entity.

```Java
private Country getHomeland(NarGraph<Country.Field> requestedGraph) {

	CountryService countryService = ...

	// build graph that will contain whatever client requested combined with what we'll need internally
	NarGraph<Country.Field> workGraph = NarGraph.Builder.of(Country.Field.class)
		// add whatever client requested
		.add(requestedGraph)
		// add fields we'll need in this function internally
		.add(Country.Field.capital, NarGraph.Builder.of(City.Field.class)
			.add(City.Field.name)
			.add(City.Field.population)
			.build()
		)
		.build();

	// resolve country with "extended" graph
	Country country = countryService.get("croatia", workGraph);

	// process country...

	// strip country to contain only fields client requested
	country.intersect(requestedGraph);
	
	return country;

}
```

Of course, and like any other operation Nar provides, intersection is not limited to this one demonstrated functionality.
Use it wherever you need to ensure your entities do not contain anything other that that defined by some "field mask".
For example, you can use it to "whitelist" properties that are allowed to be persisted when modifying an entity etc.

## Jackson support

(De)Serializing Nar entities may require some fine-tuning of your (de)serialization mechanism.
Here you can find what is needed to configure [Jackson](https://github.com/FasterXML/jackson)
for (de)serializing to/from JSON, but similar rules may apply to other libraries and formats.

Note: all classes mentioned here are part of separate subproject `nar-jackson`.

First, we need to make sure our Jackson serializers won't exclude properties with `null` value,
since having them present in resulting JSON will ensure proper setter will be invoked upon deserialization
which will then mark that field as initialized.

Luckily, Jackson will write `null` for any property holding such value and all we have to do is make sure
we don't disable that behaviour by ourselves.

So, just make sure you are **not** annotating any Nar entities or any of it's field-managed property with
`@JsonInclude(Include.NON_NULL)`. Also, make sure your mapper configuration **doesn't** turn that feature globally:

```Java
ObjectMapper mapper = new ObjectMapper();
mapper.setSerializationInclusion(Include.NON_NULL);	// DON'T do this
```

Next thing is preventing Jackson from serializing getters for uninitialized properties.
This is important for the same reason we needed `null`s serialized previously: on deserialization Jackson will invoke
setter for each JSON property it finds, which may lead to falsely initializing to `null` properties in deserialized
object which on serialization were actually uninitialized. This is even more critical if your getter will throw any
exceptions for properties that are not initialized (which is actually a recommended pattern!) since that will break
serialization altogether for any non-fully initialized entity.

Here we have two approaches:

1. Include `NarPropertyFilter` to your `ObjectMapper`:
   ```Java
   ObjectMapper mapper = new ObjectMapper();
   SimpleFilterProvider filterProvider = new SimpleFilterProvider();
   filterProvider.addFilter(NarPropertyFilter.Name, new NarPropertyFilter());
   mapper.setFilterProvider(filterProvider);
   ```
   and then annotate each getter with `NarProperty` annotation specifying which fields should be present for Jackson
   to include that property in generated JSON:
   ```Java
   @NarProperty("name")
   public String getName() { return name; }
   ```

2. Include `NarPropertyFilter` to your `ObjectMapper`, but also configure it to ignore `FieldUnavailableException`s
   during serializations:
   ```Java
   ObjectMapper mapper = new ObjectMapper();
   SimpleFilterProvider filterProvider = new SimpleFilterProvider();
   filterProvider.addFilter(NarPropertyFilter.Name, new NarPropertyFilter().setIgnoreFieldUnavailableException(true));
   mapper.setFilterProvider(filterProvider);
   ```
   Now you won't need `NarProperty` annotations, but you'll have to throw `FieldUnavailableException` for every
   uninitialized property (recommended and easiest to do by using provided `fieldGet`):
   ```Java
   public String getName() { return fieldGet(Field.name, name); }
   ```

Last configuration option Nar offers to Jackson users is `NarSerializerModifier` usage of which is purely aesthetic:
it ensures ID property is serialized first and field describing `fields` property itself
is serialized last. Note that serializing `fields` property is not needed at all for JSON, but it may be needed
when serializing Nar entities to other formats that do not have a concept of `null`
and always skip properties having that value during serialization, which in turn prevents invoking setters
on deserialization and thus properly initializing entity's fields set (XML is one such format).

In any case, if you prefer to at least have ID property serialized first, configure `NarSerializerModifier` like this:

```Java
ObjectMapper mapper = new ObjectMapper();
SimpleModule narSerializerModule = new SimpleModule();
narSerializerModule.setSerializerModifier(new NarSerializerModifier(/*your ID property name*/));
mapper.registerModule(narSerializerModule);
```
