package com.steatoda.nar;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;

import com.steatoda.nar.demo.model.boat.Boat;
import com.steatoda.nar.demo.model.boat.BoatDemoData;
import com.steatoda.nar.demo.model.marina.Marina;
import com.steatoda.nar.demo.model.person.Person;

public class NarObjectTest {

	@Test
	public void testRef() {

		Person dummy = buildDummy();
		
		Person ref = dummy.ref();

		Assert.assertTrue("reference shouldn't have any fields set", ref.getFields().isEmpty());
		Assert.assertNull("reference shouldn't have name set", ref.getIfPresent(ref::getName, Person.Field.name));

	}

	@Test
	public void testHasGraph() {

		Boat wreck = buildWreck();

		NarGraph.Builder<Boat.Field> graphBuilder = NarGraph.Builder.of(Boat.Field.class);

		graphBuilder.add(Boat.Field.name);
		Assert.assertTrue("graph", wreck.hasGraph(graphBuilder.build()));

		graphBuilder.add(Boat.Field.type);
		Assert.assertTrue("graph", wreck.hasGraph(graphBuilder.build()));

		graphBuilder.add(Boat.Field.skipper, NarGraph.of(Person.Field.name));
		Assert.assertTrue("graph", wreck.hasGraph(graphBuilder.build()));

		graphBuilder.add(Boat.Field.homeport, NarGraph.of(Marina.Field.manager));
		Assert.assertFalse("graph", wreck.hasGraph(graphBuilder.build()));

		// finally, test missing graph, too
		
		graphBuilder.add(Boat.Field.homeport, NarGraph.of(Marina.Field.depths));
		graphBuilder.add(Boat.Field.crew, NarGraph.of(Person.Field.permissions));

		NarGraph<Boat.Field> missingGraph = wreck.getMissingGraph(graphBuilder.build());

		NarGraph<Boat.Field> expectedMissingGraph = NarGraph.Builder.of(Boat.Field.class)
			.add(Boat.Field.homeport, NarGraph.Builder.of(Marina.Field.class)
				.add(Marina.Field.manager)
				.add(Marina.Field.depths)
				.build()
			)
			.add(Boat.Field.crew, NarGraph.Builder.of(Person.Field.class)
				.add(Person.Field.permissions)
				.build()
			)
			.build();

		Assert.assertEquals("Missing graph should be " + expectedMissingGraph + ", but we got " + missingGraph, missingGraph, expectedMissingGraph);

	}

	@Test
	public void testGetFieldValue() {
		
		Boat wreck = buildWreck();

		Assert.assertNotNull("wreck.name", wreck.getIfPresent(wreck::getName, Boat.Field.name));
		Assert.assertNotNull("wreck.homeport", wreck.getIfPresent(wreck::getHomeport, Boat.Field.homeport));
			Assert.assertNotNull("wreck.homeport.name",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getName, Marina.Field.name))
			);
			Assert.assertEquals("wreck.homeport.name should be \"Liverpool\"", "Liverpool", wreck.getHomeport().getName());
		
		Object value = wreck.getFieldValue(Boat.Field.homeport);
		
		Assert.assertTrue("homeport value should be of type Marina", value instanceof Marina);
		
		Marina homeport = (Marina) value;
		Assert.assertNotNull("homeport", homeport);
		Assert.assertTrue("homeport.name present", homeport.getOptional(homeport::getName, Marina.Field.name).isPresent());
		Assert.assertNotNull("homeport.name", homeport.getIfPresent(homeport::getName, Marina.Field.name));
		Assert.assertEquals("homeport.name should be \"Liverpool\"", "Liverpool", homeport.getIfPresent(homeport::getName, Marina.Field.name));

		Assert.assertTrue("wreck.name present", wreck.getOptional(wreck::getName, Boat.Field.name).isPresent());
		Assert.assertNotNull("wreck.name", wreck.getIfPresent(wreck::getName, Boat.Field.name));
		Assert.assertTrue("wreck.homeport present", wreck.getOptional(wreck::getHomeport, Boat.Field.homeport).isPresent());
		Assert.assertNotNull("wreck.homeport", wreck.getIfPresent(wreck::getHomeport, Boat.Field.homeport));
			Assert.assertTrue("wreck.homeport.name present", wreck.getOptional(wreck::getHomeport).flatMap(m -> m.getOptional(m::getName, Marina.Field.name)).isPresent());
			Assert.assertNotNull("wreck.homeport.name",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getName, Marina.Field.name))
			);

	}
	
	@Test
	public void testGetIfPresent() {
		
		Person dummy = buildDummy();

		dummy.clearFields(Person.Field.permissions);

		Assert.assertTrue("name should be initialized", dummy.hasFields(Person.Field.name));
		Assert.assertTrue("name should be present", dummy.getOptional(dummy::getName, Person.Field.name).isPresent());
		Assert.assertNotNull("name should not be null", dummy.getIfPresent(dummy::getName, Person.Field.name));
		Assert.assertNull("name shouldn't be present when requested with permissions fields", dummy.getIfPresent(dummy::getName, Person.Field.name, Person.Field.permissions));
		Assert.assertFalse("name shouldn't be present when requested with permissions fields", dummy.getOptional(dummy::getName, Person.Field.name, Person.Field.permissions).isPresent());

	}

	@Test
	public void testClearFields() {
		
		Person dummy = buildDummy();
		
		Assert.assertTrue("name should be initialized", dummy.hasFields(Person.Field.name));
		Assert.assertNotNull("name should not be null", dummy.getIfPresent(dummy::getName, Person.Field.name));
		Assert.assertTrue("email should be initialized", dummy.hasFields(Person.Field.email));
		Assert.assertNotNull("email should not be null", dummy.getIfPresent(dummy::getEmail, Person.Field.email));
		Assert.assertTrue("boat should be initialized", dummy.hasFields(Person.Field.boat));
		Assert.assertNotNull("boat should not be null", dummy.getIfPresent(dummy::getBoat, Person.Field.boat));

		boolean result = dummy.clearFields(Person.Field.name, Person.Field.boat);

		Assert.assertTrue("clearFields should return true", result);

		Assert.assertFalse("name should be cleared", dummy.hasFields(Person.Field.name));
		Assert.assertNull("name should be null", dummy.getIfPresent(dummy::getName, Person.Field.name));
		Assert.assertTrue("email should be initialized", dummy.hasFields(Person.Field.email));
		Assert.assertNotNull("email should not be null", dummy.getIfPresent(dummy::getEmail, Person.Field.email));
		Assert.assertFalse("boat should be cleared", dummy.hasFields(Person.Field.boat));
		Assert.assertNull("boat should be null", dummy.getIfPresent(dummy::getBoat, Person.Field.boat));

	}

	@Test
	public void testPullWholeEntity() {
		
		Person dummy = buildDummy();
		
		dummy = dummy.clone(NarGraph.of(Person.Field.name, Person.Field.email, Person.Field.boat));
		
		Person other = new Person();
		other.setName("Extension");
		other.setEmail("extension@foo.com");
		other.setPermissions(Sets.newHashSet("do"));

		dummy.pull(other);

		Assert.assertEquals("name should be updated", dummy.getIfPresent(dummy::getName, Person.Field.name), other.getIfPresent(dummy::getName, Person.Field.name));
		Assert.assertEquals("email should be updated", dummy.getIfPresent(dummy::getEmail, Person.Field.email), other.getIfPresent(dummy::getEmail, Person.Field.email));
		Assert.assertEquals("permissions should be updated", dummy.getIfPresent(dummy::getPermissions, Person.Field.permissions), other.getIfPresent(dummy::getPermissions, Person.Field.permissions));
		Assert.assertNotEquals("boat shouldn't be updated", dummy.getIfPresent(dummy::getBoat, Person.Field.boat), other.getIfPresent(other::getBoat, Person.Field.boat));

	}
	
	@Test
	public void testFlatten() {

		Boat wreck = buildWreck();

		Assert.assertNotNull("wreck.name", wreck.getIfPresent(wreck::getName, Boat.Field.name));
		Assert.assertNotNull("wreck.homeport", wreck.getIfPresent(wreck::getHomeport, Boat.Field.homeport));
			Assert.assertNotNull("wreck.homeport.name",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getName, Marina.Field.name))
					.orElse(null)
			);
			Assert.assertNotNull("wreck.homeport.latitude",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getLatitude, Marina.Field.latitude))
					.orElse(null)
			);
			Assert.assertNotNull("wreck.homeport.longitude",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getLongitude, Marina.Field.longitude))
					.orElse(null)
			);
		Assert.assertNotNull("wreck.skipper", wreck.getIfPresent(wreck::getSkipper, Boat.Field.skipper));
			Assert.assertNotNull("wreck.skipper.name",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.map(p -> p.getIfPresent(p::getName, Person.Field.name))
					.orElse(null)
			);
			Assert.assertNotNull("wreck.skipper.email",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.map(p -> p.getIfPresent(p::getEmail, Person.Field.email))
					.orElse(null)
			);
			Assert.assertFalse("wreck.skipper.permissions",
				CollectionUtils.sizeIsEmpty(
					Optional.of(wreck)
						.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
						.map(p -> p.getIfPresent(p::getPermissions, Person.Field.permissions))
						.orElse(null)
				)
			);
			Assert.assertNotNull("wreck.skipper.boat",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
					.orElse(null)
			);
		Assert.assertFalse("wreck.crew", CollectionUtils.sizeIsEmpty(wreck.getIfPresent(wreck::getCrew, Boat.Field.crew)));
		for (Person person : wreck.getCrew()) {
			Assert.assertNotNull("wreck.crew.name", person.getIfPresent(person::getName, Person.Field.name));
			Assert.assertNotNull("wreck.crew.email", person.getIfPresent(person::getEmail, Person.Field.email));
		}

		wreck.flatten();

		Assert.assertNotNull("flatten.name", wreck.getIfPresent(wreck::getName, Boat.Field.name));
		Assert.assertNotNull("flatten.homeport", wreck.getIfPresent(wreck::getHomeport, Boat.Field.homeport));
			Assert.assertNull("flatten.homeport.name",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getName, Marina.Field.name))
					.orElse(null)
			);
			Assert.assertNull("flatten.homeport.latitude",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getLatitude, Marina.Field.latitude))
					.orElse(null)
			);
			Assert.assertNull("flatten.homeport.longitude",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getLongitude, Marina.Field.longitude))
					.orElse(null)
			);
		Assert.assertNotNull("flatten.skipper", wreck.getIfPresent(wreck::getSkipper, Boat.Field.skipper));
			Assert.assertNull("flatten.skipper.name",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.map(p -> p.getIfPresent(p::getName, Person.Field.name))
					.orElse(null)
			);
			Assert.assertNull("flatten.skipper.email",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.map(p -> p.getIfPresent(p::getEmail, Person.Field.email))
					.orElse(null)
			);
			Assert.assertTrue("flatten.skipper.permissions",
				CollectionUtils.sizeIsEmpty(
					Optional.of(wreck)
						.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
						.map(p -> p.getIfPresent(p::getPermissions, Person.Field.permissions))
						.orElse(null)
				)
			);
			Assert.assertNull("flatten.skipper.boat",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
					.orElse(null)
			);
		Assert.assertFalse("flatten.crew", wreck.getCrew().isEmpty());
		for (Person person : wreck.getCrew()) {
			Assert.assertNull("flatten.crew.name", person.getIfPresent(person::getName, Person.Field.name));
			Assert.assertNull("flatten.crew.email", person.getIfPresent(person::getEmail, Person.Field.email));
		}

	}

	@Test
	public void testClone() {

		Person dummy = buildDummy();

		Person clone = dummy.clone();

		Assert.assertTrue("clone should have same fields set", Sets.difference(clone.getFields(), dummy.getFields()).isEmpty());
		Assert.assertNotNull("clone should have name set", clone.getIfPresent(clone::getName, Person.Field.name));

	}

	@Test
	public void testCloneFlat() {

		Boat wreck = buildWreck();

		Assert.assertNotNull("wreck.name", wreck.getIfPresent(wreck::getName, Boat.Field.name));
		Assert.assertNotNull("wreck.homeport", wreck.getIfPresent(wreck::getHomeport, Boat.Field.homeport));
			Assert.assertNotNull("wreck.homeport.name",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getName, Marina.Field.name))
					.orElse(null)
			);
			Assert.assertNotNull("wreck.homeport.latitude",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getLatitude, Marina.Field.latitude))
					.orElse(null)
			);
			Assert.assertNotNull("wreck.homeport.longitude",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getLongitude, Marina.Field.longitude))
					.orElse(null)
			);
		Assert.assertNotNull("wreck.skipper", wreck.getIfPresent(wreck::getSkipper, Boat.Field.skipper));
			Assert.assertNotNull("wreck.skipper.name",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.map(p -> p.getIfPresent(p::getName, Person.Field.name))
					.orElse(null)
			);
			Assert.assertNotNull("wreck.skipper.email",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.map(p -> p.getIfPresent(p::getEmail, Person.Field.email))
					.orElse(null)
			);
			Assert.assertFalse("wreck.skipper.permissions",
				CollectionUtils.sizeIsEmpty(
					Optional.of(wreck)
						.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
						.map(p -> p.getIfPresent(p::getPermissions, Person.Field.permissions))
						.orElse(null)
				)
			);
			Assert.assertNotNull("wreck.skipper.boat",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
					.orElse(null)
			);
		Assert.assertFalse("wreck.crew", CollectionUtils.sizeIsEmpty(wreck.getIfPresent(wreck::getCrew, Boat.Field.crew)));
		for (Person person : wreck.getCrew()) {
			Assert.assertNotNull("wreck.crew.name", person.getIfPresent(person::getName, Person.Field.name));
			Assert.assertNotNull("wreck.crew.email", person.getIfPresent(person::getEmail, Person.Field.email));
		}

		Boat clone = wreck.cloneFlat();

		Assert.assertNotNull("clone.name", clone.getIfPresent(clone::getName, Boat.Field.name));
		Assert.assertNotNull("clone.homeport", clone.getIfPresent(clone::getHomeport, Boat.Field.homeport));
			Assert.assertNull("clone.homeport.name",
				Optional.of(clone)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getName, Marina.Field.name))
					.orElse(null)
			);
			Assert.assertNull("clone.homeport.latitude",
				Optional.of(clone)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getLatitude, Marina.Field.latitude))
					.orElse(null)
			);
			Assert.assertNull("clone.homeport.longitude",
				Optional.of(clone)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getLongitude, Marina.Field.longitude))
					.orElse(null)
			);
		Assert.assertNotNull("clone.skipper", clone.getSkipper());
			Assert.assertNull("clone.skipper.name",
				Optional.of(clone)
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.map(p -> p.getIfPresent(p::getName, Person.Field.name))
					.orElse(null)
			);
			Assert.assertNull("clone.skipper.email",
				Optional.of(clone)
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.map(p -> p.getIfPresent(p::getEmail, Person.Field.email))
					.orElse(null)
			);
			Assert.assertTrue("clone.skipper.permissions",
				CollectionUtils.sizeIsEmpty(
					Optional.of(clone)
						.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
						.map(p -> p.getIfPresent(p::getPermissions, Person.Field.permissions))
						.orElse(null)
				)
			);
			Assert.assertNull("clone.skipper.boat",
				Optional.of(clone)
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
					.orElse(null)
			);
		Assert.assertFalse("clone.crew", CollectionUtils.sizeIsEmpty(clone.getIfPresent(clone::getCrew, Boat.Field.crew)));
		for (Person person : clone.getCrew()) {
			Assert.assertNull("clone.crew.name", person.getIfPresent(person::getName, Person.Field.name));
			Assert.assertNull("clone.crew.email", person.getIfPresent(person::getEmail, Person.Field.email));
		}

	}

	@Test
	public void testCloneWithGraph() {

		NarGraph<Boat.Field> graph = NarGraph.Builder.of(Boat.Field.class)
			.add(Boat.Field.name)
			// no type!
			.add(Boat.Field.homeport, NarGraph.Builder.of(Marina.Field.class)
				.add(Marina.Field.name)
				// no latitude!
				// no longitude!
				.build()
			)
			.add(Boat.Field.skipper, NarGraph.Builder.of(Person.Field.class)
				.add(Person.Field.name)
				.add(Person.Field.email)
				// no persmissions!
				// no boat!
				.build()
			)
			.add(Boat.Field.crew, NarGraph.Builder.of(Person.Field.class)
				.add(Person.Field.name)
				// no email!
				.build()
			)
			.build()
		;

		Boat wreck = buildWreck();

		Assert.assertNotNull("wreck.name", wreck.getIfPresent(wreck::getName, Boat.Field.name));
		Assert.assertNotNull("wreck.homeport", wreck.getIfPresent(wreck::getHomeport, Boat.Field.homeport));
			Assert.assertNotNull("wreck.homeport.name",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getName, Marina.Field.name))
					.orElse(null)
			);
			Assert.assertNotNull("wreck.homeport.latitude",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getLatitude, Marina.Field.latitude))
					.orElse(null)
			);
			Assert.assertNotNull("wreck.homeport.longitude",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getLongitude, Marina.Field.longitude))
					.orElse(null)
			);
		Assert.assertNotNull("wreck.skipper", wreck.getIfPresent(wreck::getSkipper, Boat.Field.skipper));
			Assert.assertNotNull("wreck.skipper.name",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.map(p -> p.getIfPresent(p::getName, Person.Field.name))
					.orElse(null)
			);
			Assert.assertNotNull("wreck.skipper.email",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.map(p -> p.getIfPresent(p::getEmail, Person.Field.email))
					.orElse(null)
			);
			Assert.assertFalse("wreck.skipper.permissions",
				CollectionUtils.sizeIsEmpty(
					Optional.of(wreck)
						.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
						.map(p -> p.getIfPresent(p::getPermissions, Person.Field.permissions))
						.orElse(null)
				)
			);
			Assert.assertNotNull("wreck.skipper.boat",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
					.orElse(null)
			);
		Assert.assertFalse("wreck.crew", CollectionUtils.sizeIsEmpty(wreck.getIfPresent(wreck::getCrew, Boat.Field.crew)));
		for (Person person : wreck.getCrew()) {
			Assert.assertNotNull("wreck.crew.name", person.getIfPresent(person::getName, Person.Field.name));
			Assert.assertNotNull("wreck.crew.email", person.getIfPresent(person::getEmail, Person.Field.email));
		}

		Boat clone = wreck.clone(graph);

		Assert.assertNotNull("clone.name", clone.getIfPresent(clone::getName, Boat.Field.name));
		Assert.assertNotNull("clone.homeport", clone.getIfPresent(clone::getHomeport, Boat.Field.homeport));
			Assert.assertNotNull("clone.homeport.name",
				Optional.of(clone)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getName, Marina.Field.name))
					.orElse(null)
			);
			Assert.assertNull("clone.homeport.latitude",
				Optional.of(clone)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getLatitude, Marina.Field.latitude))
					.orElse(null)
			);
			Assert.assertNull("clone.homeport.longitude",
				Optional.of(clone)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getLongitude, Marina.Field.longitude))
					.orElse(null)
			);
		Assert.assertNotNull("clone.skipper", clone.getIfPresent(clone::getSkipper, Boat.Field.skipper));
			Assert.assertNotNull("clone.skipper.name",
				Optional.of(clone)
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.map(p -> p.getIfPresent(p::getName, Person.Field.name))
					.orElse(null)
			);
			Assert.assertNotNull("clone.skipper.email",
				Optional.of(clone)
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.map(p -> p.getIfPresent(p::getEmail, Person.Field.email))
					.orElse(null)
			);
			Assert.assertTrue("clone.skipper.permissions",
				CollectionUtils.sizeIsEmpty(
					Optional.of(clone)
						.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
						.map(p -> p.getIfPresent(p::getPermissions, Person.Field.permissions))
						.orElse(null)
				)
			);
			Assert.assertNull("clone.skipper.boat",
				Optional.of(clone)
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
					.orElse(null)
			);
		Assert.assertFalse("clone.crew", CollectionUtils.sizeIsEmpty(clone.getIfPresent(clone::getCrew, Boat.Field.crew)));
		for (Person person : clone.getCrew()) {
			Assert.assertNotNull("clone.crew.name", person.getIfPresent(person::getName, Person.Field.name));
			Assert.assertNull("clone.crew.email", person.getIfPresent(person::getEmail, Person.Field.email));
		}
		
		// modify original and inspect clone
		
		wreck.setName(wreck.getName() + "*");
		wreck.getHomeport().setName(wreck.getHomeport().getName() + "*");
		wreck.getSkipper().setName(wreck.getSkipper().getName() + "*");
		wreck.getCrew().forEach(person -> person.setName(person.getName() + "*"));

		Assert.assertNotEquals("clone.name should not have changed",
			wreck.getIfPresent(wreck::getName, Boat.Field.name),
			clone.getIfPresent(clone::getName, Boat.Field.name)
		);
		Assert.assertNotEquals("clone.homeport.name should not have changed",
			Optional.of(wreck)
				.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
				.map(m -> m.getIfPresent(m::getName, Marina.Field.name))
				.orElse(null),
			Optional.of(clone)
				.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
				.map(m -> m.getIfPresent(m::getName, Marina.Field.name))
				.orElse(null)
		);
		Assert.assertNotEquals("clone.skipper.name should not have changed",
			Optional.of(wreck)
				.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
				.map(p -> p.getIfPresent(p::getName, Person.Field.name))
				.orElse(null),
			Optional.of(clone)
				.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
				.map(p -> p.getIfPresent(p::getName, Person.Field.name))
				.orElse(null)
		);
		for (Person person : clone.getCrew())
			Assert.assertNotNull("clone.crew.name should not have changed", person.getIfPresent(person::getName, Person.Field.name));

	}

	@Test
	public void testIntersect() {

		NarGraph<Boat.Field> fields = NarGraph.Builder.of(Boat.Field.class)
			.add(Boat.Field.name)
			// no type!
			.add(Boat.Field.homeport, NarGraph.Builder.of(Marina.Field.class)
				.add(Marina.Field.name)
				// no latitude!
				// no longitude!
				.add(Marina.Field.manager)	// doesn't exist in wreck!
				.build()
			)
			.add(Boat.Field.skipper, NarGraph.Builder.of(Person.Field.class)
				.add(Person.Field.name)
				.add(Person.Field.email)
				// no persmissions!
				// no boat!
				.build()
			)
			.add(Boat.Field.crew, NarGraph.Builder.of(Person.Field.class)
				.add(Person.Field.name)
				// no email!
				.build()
			)
			.build()
		;

		Boat wreck = buildWreck();

		wreck.intersect(fields);
		
		Assert.assertNotNull("name", wreck.getIfPresent(wreck::getName, Boat.Field.name));
		Assert.assertNotNull("homeport", wreck.getIfPresent(wreck::getHomeport, Boat.Field.homeport));
			Assert.assertNotNull("homeport.name",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getName, Marina.Field.name))
					.orElse(null)
			);
			Assert.assertNull("homeport.latitude",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getLatitude, Marina.Field.latitude))
					.orElse(null)
			);
			Assert.assertNull("homeport.longitude",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getLongitude, Marina.Field.longitude))
					.orElse(null)
			);
			Assert.assertNull("homeport.manager",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.map(m -> m.getIfPresent(m::getManager, Marina.Field.manager))
					.orElse(null)
			);
		Assert.assertNotNull("skipper", wreck.getIfPresent(wreck::getSkipper, Boat.Field.skipper));
			Assert.assertNotNull("skipper.name",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.map(p -> p.getIfPresent(p::getName, Person.Field.name))
					.orElse(null)
			);
			Assert.assertNotNull("skipper.email",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.map(p -> p.getIfPresent(p::getEmail, Person.Field.email))
					.orElse(null)
			);
			Assert.assertTrue("skipper.permissions",
				CollectionUtils.sizeIsEmpty(
					Optional.of(wreck)
						.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
						.map(p -> p.getIfPresent(p::getPermissions, Person.Field.permissions))
						.orElse(null)
				)
			);
			Assert.assertNull("skipper.boat",
				Optional.of(wreck)
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
					.orElse(null)
			);
		Assert.assertFalse("crew", CollectionUtils.sizeIsEmpty(wreck.getIfPresent(wreck::getCrew, Boat.Field.crew)));
		for (Person person : wreck.getCrew()) {
			Assert.assertNotNull("crew.name", person.getIfPresent(person::getName, Person.Field.name));
			Assert.assertNull("crew.email", person.getIfPresent(person::getEmail, Person.Field.email));
		}

	}

	private static Person buildDummy() {
		Person dummy = new Person();
		dummy.setId("dummy");
		dummy.setName("Dummy");
		dummy.setEmail("dummy@foo.com");
		dummy.setPermissions(Sets.newHashSet("read"));
		dummy.setBoat(Boat.ref(BoatDemoData.AlphaId));
		return dummy;
	}

	private static Boat buildWreck() {
		Boat wreck = new Boat();
		wreck.setId("titanic");
		wreck.setName("Titanic");
		wreck.setType("Cruiser");
			Marina liverpool = new Marina();
			liverpool.setId("liverpool");
			liverpool.setName("Liverpool");
			liverpool.setLatitude(53.4);
			liverpool.setLongitude(-2.983333);
		wreck.setHomeport(liverpool);
		wreck.setSkipper(buildDummy());
			List<Person> crew = new ArrayList<>();
			for (int i = 1; i < 10; ++i) {
				Person person = new Person();
				person.setId("poor-fella-" + i);
				person.setName("Poor Fella " + i);
				person.setEmail((Math.random() < 0.5 ? "heaven" : "hell") + "@afterlife.org");
				crew.add(person);
			}
		wreck.setCrew(crew);
		return wreck;
	}
	
}
