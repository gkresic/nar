package com.steatoda.nar.service;

import com.google.common.collect.Sets;
import com.steatoda.nar.NarGraph;
import com.steatoda.nar.demo.model.person.Person;
import com.steatoda.nar.demo.model.person.PersonDemoService;
import com.steatoda.nar.demo.model.person.PersonService;
import com.steatoda.nar.service.crud.NarCRUDBatchBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

public class NarCRUDServiceTest {

	@Before
	public void init() {

		personService = new PersonDemoService();

	}

	@Test
	public void testCreate() {

		String dummyId = createDummy();

		try {

			Assert.assertNotNull("ID of created person should not be null", dummyId);

		} finally {
			personService.delete(Person.ref(dummyId));
		}

	}

	@Test
	public void testModify() {

		final NarGraph<Person.Field> PersonView = NarGraph.allOf(Person.Field.class);

		String dummyId = createDummy();

		try {

			Person person = personService.get(dummyId, PersonView);

			Assert.assertEquals("Wrong name resolved", "Dummy", person.getIfPresent(person::getName, Person.Field.name));
			Assert.assertEquals("Wrong email resolved", "dummy@foo.com", person.getIfPresent(person::getEmail, Person.Field.email));
			Assert.assertFalse("No persmissions resolved",
				CollectionUtils.sizeIsEmpty(
					Optional.of(person)
						.map(p -> p.getIfPresent(p::getPermissions, Person.Field.permissions))
						.orElse(null)
				)
			);

			// change name, email and permissions...
			person.setName("foo");
			person.setEmail("bar");
			person.setPermissions(Collections.emptySet());

			// ...but persist only name and email (email should be read-only)
			personService.modify(person, EnumSet.of(Person.Field.name, Person.Field.email), PersonView);
			person = personService.get(dummyId, PersonView);

			Assert.assertEquals("Wrong name after modify", "foo", person.getIfPresent(person::getName, Person.Field.name));
			Assert.assertEquals("Wrong email after modify", "dummy@foo.com", person.getIfPresent(person::getEmail, Person.Field.email));
			Assert.assertFalse("Empty permissions after modify",
				CollectionUtils.sizeIsEmpty(
					Optional.of(person)
						.map(p -> p.getIfPresent(p::getPermissions, Person.Field.permissions))
						.orElse(null)
				)
			);

			// finally, clean permissions
			person.setPermissions(Collections.emptySet());
			personService.modify(person, EnumSet.of(Person.Field.permissions), PersonView);
			person = personService.get(dummyId, PersonView);
			Assert.assertTrue("Permissions still non-empty after update",
				CollectionUtils.sizeIsEmpty(
					Optional.of(person)
						.map(p -> p.getIfPresent(p::getPermissions, Person.Field.permissions))
						.orElse(null)
				)
			);

		} finally {
			personService.delete(Person.ref(dummyId));
		}

	}

	@Test
	public void testBatch() {

		Person dummyToCreate = initDummy("1");
		Person dummyToModify = initDummy("2");
		Person dummyToDelete = initDummy("3");

		try {

			final NarGraph<Person.Field> PersonView = NarGraph.allOf(Person.Field.class);

			// first create dummies we'll modify/delete in batch
			personService.create(dummyToModify, PersonView);
			personService.create(dummyToDelete, PersonView);

			// update dummyToModify
			dummyToModify.setName("foo");

			// do the batch
			NarCRUDBatchBuilder<Person> batchBuilder = new NarCRUDBatchBuilder<>();
			batchBuilder.addCreate(dummyToCreate);
			batchBuilder.addModify(dummyToModify);
			batchBuilder.addDelete(dummyToDelete);

			personService.batch(batchBuilder.build(), PersonView);

			Assert.assertNotNull("ID of created person should not be null", dummyToCreate.getId());
			Assert.assertEquals("Wrong name of modifies person", "foo", dummyToModify.getIfPresent(dummyToModify::getName, Person.Field.name));
			Assert.assertNull("Deleted entity still exists", personService.get(dummyToDelete.getId(), NarGraph.noneOf(Person.Field.class)));

		} finally {
			personService.delete(Person.ref(dummyToCreate.getId()));
			personService.delete(Person.ref(dummyToModify.getId()));
			personService.delete(Person.ref(dummyToDelete.getId()));
		}

	}

	private Person initDummy() {
		return initDummy(null);
	}

	private Person initDummy(String suffix) {
		return new Person()
			.setName("Dummy" + (suffix != null ? " " + suffix : ""))
			.setEmail("dummy" + (suffix != null ? "-" + suffix : "") + "@foo.com")
			.setPermissions(Sets.newHashSet("read"))
			.setBoat(null)	// just to initialize field
		;
	}

	private String createDummy() {

		Person dummy = initDummy();

		personService.create(dummy, NarGraph.noneOf(Person.Field.class));

		return dummy.getId();

	}

	private PersonService personService;

}
