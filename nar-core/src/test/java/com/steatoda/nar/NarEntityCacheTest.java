package com.steatoda.nar;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;

import com.steatoda.nar.demo.model.person.Person;
import com.steatoda.nar.demo.model.person.PersonDemoData;
import com.steatoda.nar.demo.model.person.PersonDemoService;
import com.steatoda.nar.demo.model.person.PersonService;

public class NarEntityCacheTest {

	@Test
	public void testIncrementalGet() {

		Set<Person.Field> fieldsWhitelist = EnumSet.noneOf(Person.Field.class);	// fields which MUST be fetched from underlying service
		Set<Person.Field> fieldsBlacklist = EnumSet.noneOf(Person.Field.class);	// fields which MUST NOT be fetched from underlying service
		
		PersonService personService = new PersonDemoService() {
			@Override
			public Person get(String id, NarGraph<Person.Field> graph) {
				Assert.assertTrue("Requested " + new HashSet<>(graph) + ", but whitelisted are " + fieldsWhitelist, graph.size() == fieldsWhitelist.size() && graph.containsAll(fieldsWhitelist));
				Assert.assertTrue("Requested " + new HashSet<>(graph) + ", but blacklisted are " + fieldsBlacklist, Sets.intersection(graph, fieldsBlacklist).isEmpty());
				return super.get(id, graph);
			}
		};

		NarEntityCache<String, Person, Person.Field> cache = new NarEntityCache<>(EnumSet.of(Person.Field.name, Person.Field.email, Person.Field.boat));
		
		Person person;
		
		// initial fetch: name + boat
		fieldsWhitelist.clear();
		fieldsWhitelist.add(Person.Field.name);
		fieldsWhitelist.add(Person.Field.boat);
		fieldsBlacklist.clear();
		person = cache.get(PersonDemoData.RonId, NarGraph.of(Person.Field.name, Person.Field.boat), personService);
		Assert.assertNotNull("name should not be null", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertNull("email should be null", person.getIfPresent(person::getEmail, Person.Field.email));
		Assert.assertNotNull("boat should not be null", person.getIfPresent(person::getBoat, Person.Field.boat));
		Assert.assertTrue("boat fields should be empty", person.getBoat().getFields().isEmpty());

		// fetch: name + email + boat (only email should be fetched)
		fieldsWhitelist.clear();
		fieldsWhitelist.add(Person.Field.email);
		fieldsBlacklist.clear();
		fieldsBlacklist.add(Person.Field.name);
		fieldsBlacklist.add(Person.Field.boat);
		person = cache.get(PersonDemoData.RonId, NarGraph.of(Person.Field.name, Person.Field.email, Person.Field.boat), personService);
		Assert.assertNotNull("name should not be null", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertNotNull("email should not be null", person.getIfPresent(person::getEmail, Person.Field.email));
		Assert.assertNotNull("boat should not be null", person.getIfPresent(person::getBoat, Person.Field.boat));
		Assert.assertTrue("boat fields should be empty", person.getBoat().getFields().isEmpty());

		// fetch: name + email + boat (nothing should be fetched)
		fieldsWhitelist.clear();
		fieldsBlacklist.clear();
		fieldsBlacklist.add(Person.Field.name);
		fieldsBlacklist.add(Person.Field.email);
		fieldsBlacklist.add(Person.Field.boat);
		person = cache.get(PersonDemoData.RonId, NarGraph.of(Person.Field.name, Person.Field.email, Person.Field.boat), personService);
		Assert.assertNotNull("name should not be null", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertNotNull("email should not be null", person.getIfPresent(person::getEmail, Person.Field.email));
		Assert.assertNotNull("boat should not be null", person.getIfPresent(person::getBoat, Person.Field.boat));
		Assert.assertTrue("boat fields should be empty", person.getBoat().getFields().isEmpty());

		// fetch: name + email + boat + permissions (only permissions should be fetched, but they are not cached)
		fieldsWhitelist.clear();
		fieldsWhitelist.add(Person.Field.permissions);
		fieldsBlacklist.clear();
		fieldsBlacklist.add(Person.Field.name);
		fieldsBlacklist.add(Person.Field.email);
		fieldsBlacklist.add(Person.Field.boat);
		person = cache.get(PersonDemoData.RonId, NarGraph.of(Person.Field.name, Person.Field.email, Person.Field.boat, Person.Field.permissions), personService);
		Assert.assertNotNull("name should not be null", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertNotNull("email should not be null", person.getIfPresent(person::getEmail, Person.Field.email));
		Assert.assertNotNull("boat should not be null", person.getIfPresent(person::getBoat, Person.Field.boat));
		Assert.assertTrue("boat fields should be empty", person.getBoat().getFields().isEmpty());
		Assert.assertFalse("Permissions sholud not be empty", person.getPermissions().isEmpty());

		// fetch: name + email + boat + permissions (only permissions should be fetched (again, since they are not cached)
		fieldsWhitelist.clear();
		fieldsWhitelist.add(Person.Field.permissions);
		fieldsBlacklist.clear();
		fieldsBlacklist.add(Person.Field.name);
		fieldsBlacklist.add(Person.Field.email);
		fieldsBlacklist.add(Person.Field.boat);
		person = cache.get(PersonDemoData.RonId, NarGraph.of(Person.Field.name, Person.Field.email, Person.Field.boat, Person.Field.permissions), personService);
		Assert.assertNotNull("name should not be null", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertNotNull("email should not be null", person.getIfPresent(person::getEmail, Person.Field.email));
		Assert.assertNotNull("boat should not be null", person.getIfPresent(person::getBoat, Person.Field.boat));
		Assert.assertTrue("boat fields should be empty", person.getBoat().getFields().isEmpty());
		Assert.assertFalse("Permissions should not be empty", person.getPermissions().isEmpty());

		// fetch another entity: name + boat
		fieldsWhitelist.clear();
		fieldsWhitelist.add(Person.Field.name);
		fieldsWhitelist.add(Person.Field.boat);
		fieldsBlacklist.clear();
		person = cache.get(PersonDemoData.PirateId, NarGraph.of(Person.Field.name, Person.Field.boat), personService);
		Assert.assertNotNull("name should not be null", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertNull("email should be null", person.getIfPresent(person::getEmail, Person.Field.email));
		Assert.assertNotNull("boat should not be null", person.getIfPresent(person::getBoat, Person.Field.boat));
		Assert.assertTrue("boat fields should be empty", person.getBoat().getFields().isEmpty());

	}

	@Test
	public void testIncrementalGetPrecached() {

		Set<Person.Field> fieldsWhitelist = EnumSet.noneOf(Person.Field.class);	// fields which MUST be fetched from underlying service
		Set<Person.Field> fieldsBlacklist = EnumSet.noneOf(Person.Field.class);	// fields which MUST NOT be fetched from underlying service

		PersonService personService = new PersonDemoService() {
			@Override
			public Person get(String id, NarGraph<Person.Field> graph) {
				Assert.assertTrue("Requested " + new HashSet<>(graph) + ", but whitelisted are " + fieldsWhitelist, graph.size() == fieldsWhitelist.size() && graph.containsAll(fieldsWhitelist));
				Assert.assertTrue("Requested " + new HashSet<>(graph) + ", but blacklisted are " + fieldsBlacklist, Sets.intersection(graph, fieldsBlacklist).isEmpty());
				return super.get(id, graph);
			}
		};

		NarEntityCache<String, Person, Person.Field> cache = new NarEntityCache<>(
			EnumSet.of(Person.Field.name, Person.Field.email, Person.Field.boat),	// cacheableFields
			EnumSet.of(Person.Field.name, Person.Field.email)						// precachedFields
		);

		Person person;

		// initial fetch: name
		fieldsWhitelist.clear();
		fieldsWhitelist.add(Person.Field.name);
		fieldsWhitelist.add(Person.Field.email);
		fieldsBlacklist.clear();
		person = cache.get(PersonDemoData.RonId, NarGraph.of(Person.Field.name), personService);
		Assert.assertNotNull("name should not be null", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertNull("email should be null", person.getIfPresent(person::getEmail, Person.Field.email));

		// fetch: name + email + boat (only boat should be fetched)
		fieldsWhitelist.clear();
		fieldsWhitelist.add(Person.Field.boat);
		fieldsBlacklist.clear();
		fieldsBlacklist.add(Person.Field.name);
		fieldsBlacklist.add(Person.Field.email);
		person = cache.get(PersonDemoData.RonId, NarGraph.of(Person.Field.name, Person.Field.email, Person.Field.boat), personService);
		Assert.assertNotNull("name should not be null", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertNotNull("email should not be null", person.getIfPresent(person::getEmail, Person.Field.email));
		Assert.assertNotNull("boat should not be null", person.getIfPresent(person::getBoat, Person.Field.boat));
		Assert.assertTrue("boat fields should be empty", person.getBoat().getFields().isEmpty());

		// fetch: name + email + boat (nothing should be fetched)
		fieldsWhitelist.clear();
		fieldsBlacklist.clear();
		fieldsBlacklist.add(Person.Field.name);
		fieldsBlacklist.add(Person.Field.email);
		fieldsBlacklist.add(Person.Field.boat);
		person = cache.get(PersonDemoData.RonId, NarGraph.of(Person.Field.name, Person.Field.email, Person.Field.boat), personService);
		Assert.assertNotNull("name should not be null", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertNotNull("email should not be null", person.getIfPresent(person::getEmail, Person.Field.email));
		Assert.assertNotNull("boat should not be null", person.getIfPresent(person::getBoat, Person.Field.boat));
		Assert.assertTrue("boat fields should be empty", person.getBoat().getFields().isEmpty());

	}

	@Test
	public void testGet() {

		PersonService personService = new PersonDemoService();
		NarEntityCache<String, Person, Person.Field> cache = new NarEntityCache<>(EnumSet.of(Person.Field.name, Person.Field.email, Person.Field.boat));

		Person person;

		person = cache.get(PersonDemoData.RonId);
		Assert.assertNull("person should be null", person);

		person = cache.get(PersonDemoData.RonId, NarGraph.of(Person.Field.name), personService);
		Assert.assertNotNull("person should not be null", person);
		Assert.assertNotNull("name should not be null", person.getIfPresent(person::getName, Person.Field.name));

		person = cache.get(PersonDemoData.RonId);
		Assert.assertNotNull("person should not be null", person);
		Assert.assertNotNull("name should not be null", person.getIfPresent(person::getName, Person.Field.name));

	}
	
	@Test
	public void testPut() {

		PersonService personService = new PersonDemoService();
		NarEntityCache<String, Person, Person.Field> cache = new NarEntityCache<>(EnumSet.of(Person.Field.name, Person.Field.email, Person.Field.boat));

		Person person;

		// person should not be in cache
		person = cache.get(PersonDemoData.RonId);
		Assert.assertNull("person should be null", person);

		// fetch person *directly*
		person = personService.get(PersonDemoData.RonId, NarGraph.of(Person.Field.name, Person.Field.permissions));
		Assert.assertNotNull("person should not be null", person);
		Assert.assertNotNull("name should not be null", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertNull("email should be null", person.getIfPresent(person::getEmail, Person.Field.email));
		Assert.assertFalse("Permissions should not be empty", CollectionUtils.isEmpty(person.getIfPresent(person::getPermissions, Person.Field.permissions)));

		// store in cache (only name should be stored)
		cache.put(person);
		
		// get cached value
		person = cache.get(PersonDemoData.RonId);
		Assert.assertNotNull("person should not be null", person);
		Assert.assertNotNull("name should not be null", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertNull("email should be null", person.getIfPresent(person::getEmail, Person.Field.email));
		Assert.assertTrue("Permissions should be empty", CollectionUtils.isEmpty(person.getIfPresent(person::getPermissions, Person.Field.permissions)));

	}

	@Test
	public void testMerge() {

		PersonService personService = new PersonDemoService();
		NarEntityCache<String, Person, Person.Field> cache = new NarEntityCache<>(EnumSet.of(Person.Field.name, Person.Field.email, Person.Field.boat));

		Person person;
		
		// initial fetch: name + boat
		person = cache.get(PersonDemoData.RonId, NarGraph.of(Person.Field.name, Person.Field.boat), personService);
		Assert.assertNotNull("name should not be null", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertNull("email should be null", person.getIfPresent(person::getEmail, Person.Field.email));
		Assert.assertNotNull("boat should not be null", person.getIfPresent(person::getBoat, Person.Field.boat));
		Assert.assertTrue("boat fields should be empty", person.getBoat().getFields().isEmpty());

		// fetch person *directly* with additional fields (email + permissions)
		person = personService.get(PersonDemoData.RonId, NarGraph.of(Person.Field.name, Person.Field.email, Person.Field.permissions));
		Assert.assertNotNull("person should not be null", person);
		Assert.assertNotNull("name should not be null", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertNotNull("email should not be null", person.getIfPresent(person::getEmail, Person.Field.email));
		Assert.assertNull("boat should be null", person.getIfPresent(person::getBoat, Person.Field.boat));
		Assert.assertFalse("Permissions should not be empty", CollectionUtils.isEmpty(person.getIfPresent(person::getPermissions, Person.Field.permissions)));

		// overwrite name
		person.setName("Dummy");

		// merge this extended person with one already in cache
		cache.merge(person);
		
		// get cached value
		person = cache.get(PersonDemoData.RonId);
		Assert.assertNotNull("person should not be null", person);
		Assert.assertNotNull("name should not be null", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertEquals("name should be updated", person.getIfPresent(person::getName, Person.Field.name), "Dummy");
		Assert.assertNotNull("email should not be null", person.getIfPresent(person::getEmail, Person.Field.email));
		Assert.assertNotNull("boat should not be null", person.getIfPresent(person::getBoat, Person.Field.boat));
		Assert.assertTrue("Permissions should be empty", CollectionUtils.isEmpty(person.getIfPresent(person::getPermissions, Person.Field.permissions)));

	}

	@Test
	public void testClearEntityFields() {

		PersonService personService = new PersonDemoService();
		NarEntityCache<String, Person, Person.Field> cache = new NarEntityCache<>(EnumSet.of(Person.Field.name, Person.Field.email, Person.Field.boat));

		Person person;

		// initial fetch: name + boat
		person = cache.get(PersonDemoData.RonId, NarGraph.of(Person.Field.name, Person.Field.boat), personService);
		Assert.assertNotNull("name should not be null", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertNull("email should be null", person.getIfPresent(person::getEmail, Person.Field.email));
		Assert.assertNotNull("boat should not be null", person.getIfPresent(person::getBoat, Person.Field.boat));
		Assert.assertTrue("boat fields should be empty", person.getBoat().getFields().isEmpty());

		// assert cached value
		person = cache.get(PersonDemoData.RonId);
		Assert.assertNotNull("person should not be null", person);
		Assert.assertNull("email should be null", person.getIfPresent(person::getEmail, Person.Field.email));
		Assert.assertNotNull("boat should not be null", person.getIfPresent(person::getBoat, Person.Field.boat));
		Assert.assertTrue("boat fields should be empty", person.getBoat().getFields().isEmpty());

		// clear boat field
		cache.clear(PersonDemoData.RonId, EnumSet.of(Person.Field.boat));

		// assert cached value
		person = cache.get(PersonDemoData.RonId);
		Assert.assertNotNull("person should not be null", person);
		Assert.assertNull("email should be null", person.getIfPresent(person::getEmail, Person.Field.email));
		Assert.assertNull("boat should be null", person.getIfPresent(person::getBoat, Person.Field.boat));

	}
	
}
