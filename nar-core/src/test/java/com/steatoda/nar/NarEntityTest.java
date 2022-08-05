package com.steatoda.nar;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.steatoda.nar.demo.model.boat.Boat;
import com.steatoda.nar.demo.model.marina.Marina;
import com.steatoda.nar.demo.model.person.Person;
import com.steatoda.nar.demo.model.person.PersonAsyncService;
import com.steatoda.nar.demo.model.person.PersonDemoAsyncService;
import com.steatoda.nar.demo.model.person.PersonDemoData;
import com.steatoda.nar.demo.model.person.PersonDemoService;
import com.steatoda.nar.demo.model.person.PersonService;
import com.steatoda.nar.service.async.NarBatcher;
import com.steatoda.nar.service.async.NarRequest;
import com.steatoda.nar.service.async.NarServiceHandler;

public class NarEntityTest {

	@Before
	public void init() {

		personService = new PersonDemoService();
		personAsyncService = new PersonDemoAsyncService();
		
	}
	
	@Test
	public void testGetFlat() {

		final NarGraph<Person.Field> PersonView = NarGraph.of(Person.Field.name, Person.Field.boat);

		Person person = personService.get(PersonDemoData.RonId, PersonView);

		Assert.assertNotNull("name should not be null", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertNull("email should be null", person.getIfPresent(person::getEmail, Person.Field.email));
		Assert.assertNotNull("boat should not be null", person.getIfPresent(person::getBoat, Person.Field.boat));
		Assert.assertTrue("boat fields should be empty", person.getBoat().getFields().isEmpty());
		
	}

	@Test
	public void testGetHierarchical() {

		NarGraph<Person.Field> PersonView = NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.name)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.add(Boat.Field.type)
				.add(Boat.Field.homeport, NarGraph.Builder.of(Marina.Field.class)
					.add(Marina.Field.name)
					.add(Marina.Field.manager, NarGraph.Builder.of(Person.Field.class)
						.add(Person.Field.name)
						.build()
					)
					.build()
				)
				.add(Boat.Field.crew, NarGraph.Builder.of(Person.Field.class)
					.add(Person.Field.name)
					.build()
				)
				.build()
			)
			.build()
		;

		Person person = personService.get(PersonDemoData.RonId, PersonView);

		Assert.assertNotNull("person.name should not be null", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertNull("person.email should be null", person.getIfPresent(person::getEmail, Person.Field.email));
		Assert.assertNotNull("person.boat should not be null", person.getIfPresent(person::getBoat, Person.Field.boat));
			Assert.assertNotNull("person.boat.name should not be null",
				Optional.of(person)
					.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
					.map(b -> b.getIfPresent(b::getName, Boat.Field.name))
					.orElse(null)
			);
			Assert.assertNotNull("person.boat.type should not be null",
				Optional.of(person)
					.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
					.map(b -> b.getIfPresent(b::getType, Boat.Field.type))
					.orElse(null)
			);
			Assert.assertNotNull("person.boat.homeport should not be null",
				Optional.of(person)
					.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.orElse(null)
			);
				Assert.assertNotNull("person.boat.homeport.name should not be null",
					Optional.of(person)
						.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
						.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
						.map(m -> m.getIfPresent(m::getName, Marina.Field.name))
						.orElse(null)
				);
				Assert.assertNotNull("person.boat.homeport.manager should not be null",
					Optional.of(person)
						.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
						.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
						.map(m -> m.getIfPresent(m::getManager, Marina.Field.manager))
						.orElse(null)
				);
					Assert.assertNotNull("person.boat.homeport.manager.name should not be null",
						Optional.of(person)
							.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
							.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
							.map(m -> m.getIfPresent(m::getManager, Marina.Field.manager))
							.map(p -> p.getIfPresent(p::getName, Person.Field.name))
							.orElse(null)
					);
			Assert.assertNull("person.boat.skipper should be null",
				Optional.of(person)
					.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.orElse(null)
			);
			Assert.assertFalse("person.boat.crew should not be empty",
				CollectionUtils.sizeIsEmpty(
					Optional.of(person)
						.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
						.map(b -> b.getIfPresent(b::getCrew, Boat.Field.crew))
						.orElse(null)
				)
			);
				for (Person member : person.getBoat().getCrew())
					Assert.assertNotNull("person.boat.crew.name should not be null", member.getIfPresent(member::getName, Person.Field.name));
		Assert.assertTrue("person.permissions should be null",
			CollectionUtils.sizeIsEmpty(
				Optional.of(person)
					.map(p -> p.getIfPresent(p::getPermissions, Person.Field.permissions))
					.orElse(null)
			)
		);
		
	}

	@Test
	public void testGetAsync() throws InterruptedException {

		final NarGraph<Person.Field> PersonView = NarGraph.of(Person.Field.name, Person.Field.boat);

		CountDownLatch lock = new CountDownLatch(1);
		
		AtomicReference<Person> personRef = new AtomicReference<>(null);
		
		/*FieldsRequest request = */personAsyncService.get(PersonDemoData.RonId, PersonView, person -> {
			personRef.set(person);
			lock.countDown();
		});

		Assert.assertNull("BEFORE wait person should be null", personRef.get());

		lock.await();

		Assert.assertNotNull("AFTER wait person should not be null", personRef.get());
		Assert.assertNotNull("name should not be null",
			Optional.of(personRef.get())
				.map(p -> p.getIfPresent(p::getName, Person.Field.name))
				.orElse(null)
		);
		Assert.assertNull("email should be null",
			Optional.of(personRef.get())
				.map(p -> p.getIfPresent(p::getEmail, Person.Field.email))
				.orElse(null)
		);
		Assert.assertNotNull("boat should not be null",
			Optional.of(personRef.get())
				.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
				.orElse(null)
		);
		Assert.assertTrue("boat fields should be empty", personRef.get().getBoat().getFields().isEmpty());

	}

	@Test
	public void testGetAsyncCancel() throws InterruptedException {

		final NarGraph<Person.Field> PersonView = NarGraph.of(Person.Field.name, Person.Field.boat);

		CountDownLatch lock = new CountDownLatch(1);
		
		AtomicReference<Person> personRef = new AtomicReference<>(null);
		AtomicBoolean isCancelled = new AtomicBoolean(false);
		AtomicBoolean isDestroyed = new AtomicBoolean(false);
		
		NarRequest request = personAsyncService.get(PersonDemoData.RonId, PersonView, new NarServiceHandler<Person>() {
			@Override
			public void onSuccess(Person person) {
				personRef.set(person);
			}
			@Override
			public void onCancel() {
				isCancelled.set(true);
			}
			@Override
			public void onDestroy() {
				isDestroyed.set(true);
				lock.countDown();
			}
		});

		// cancel immediately
		request.cancel();
		
		lock.await();

		Assert.assertNull("person should be null", personRef.get());
		Assert.assertTrue("isCancelled chould be set", isCancelled.get());
		Assert.assertTrue("isDestroyed chould be set", isDestroyed.get());

	}

	@Test
	public void testGetBatched() throws InterruptedException {

		Map<String, NarGraph<Person.Field>> retrievedFields = new HashMap<>();
		AtomicInteger requestedGetCount = new AtomicInteger(0);
		AtomicInteger realGetCount = new AtomicInteger(0);
		
		NarBatcher<String, Person, Person.Field> testPersonBatcher = new NarBatcher<String, Person, Person.Field>(new PersonDemoAsyncService() {
			@Override
			public NarRequest get(String id, NarGraph<Person.Field> graph, NarServiceHandler<Person> handler) {
				realGetCount.incrementAndGet();
				retrievedFields.put(id, graph);
				return super.get(id, graph, handler);
			}
		}) {
			@Override
			synchronized public NarRequest get(String id, NarGraph<Person.Field> graph, NarServiceHandler<Person> handler) {
				requestedGetCount.incrementAndGet();
				return super.get(id, graph, handler);
			}
		};
		
		final NarGraph<Person.Field> PersonShortView = NarGraph.of(Person.Field.name, Person.Field.boat);
		final NarGraph<Person.Field> PersonLongView = NarGraph.of(Person.Field.name, Person.Field.email, Person.Field.boat);

		CountDownLatch lock = new CountDownLatch(3);
		
		AtomicReference<Person> ronShortRef = new AtomicReference<>(null);
		/*FieldsRequest request = */testPersonBatcher.get(PersonDemoData.RonId, PersonShortView, person -> {
			ronShortRef.set(person);
			lock.countDown();
		});
		AtomicReference<Person> ronLongRef = new AtomicReference<>(null);
		/*FieldsRequest request = */testPersonBatcher.get(PersonDemoData.RonId, PersonLongView, person -> {
			ronLongRef.set(person);
			lock.countDown();
		});
		AtomicReference<Person> pirateRef = new AtomicReference<>(null);
		/*FieldsRequest request = */testPersonBatcher.get(PersonDemoData.PirateId, PersonShortView, person -> {
			pirateRef.set(person);
			lock.countDown();
		});

		Assert.assertNull("BEFORE wait Ron Short should be null", ronShortRef.get());
		Assert.assertNull("BEFORE wait Ron Long should be null", ronLongRef.get());
		Assert.assertNull("BEFORE wait Pirate should be null", pirateRef.get());

		testPersonBatcher.run();
		
		lock.await();

		Assert.assertEquals("There should be exactly 3 calls to testPersonBatcher.get", requestedGetCount.get(), 3);
		Assert.assertEquals("There should be exactly 2 calls to PersonService.get", realGetCount.get(), 2);
		Assert.assertEquals("Ron should be retrieved with fields " + PersonLongView, retrievedFields.get(PersonDemoData.RonId), PersonLongView);
		Assert.assertEquals("Pirate should be retrieved with fields " + PersonShortView, retrievedFields.get(PersonDemoData.PirateId), PersonShortView);

		Assert.assertNotNull("AFTER wait Ron Short should not be null", ronShortRef.get());
		Assert.assertNotNull("AFTER wait Ron Long should not be null", ronLongRef.get());
		Assert.assertNotNull("AFTER wait Pirate should not be null", pirateRef.get());
		Assert.assertNotNull("Ron Short's name should not be null",
			Optional.of(ronShortRef.get())
				.map(p -> p.getIfPresent(p::getName, Person.Field.name))
				.orElse(null)
		);
		Assert.assertTrue("Ron Short's permissions should be empty",
			CollectionUtils.sizeIsEmpty(
				Optional.of(ronShortRef.get())
					.map(p -> p.getIfPresent(p::getPermissions, Person.Field.permissions))
					.orElse(null)
			)
		);
		Assert.assertNotNull("Ron Short's boat should not be null",
			Optional.of(ronShortRef.get())
				.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
				.orElse(null)
		);
		Assert.assertTrue("Ron Short's boat fields should be empty", ronShortRef.get().getBoat().getFields().isEmpty());

		Assert.assertNotNull("Ron Longs's email should not be null",
			Optional.of(ronLongRef.get())
				.map(p -> p.getIfPresent(p::getEmail, Person.Field.email))
				.orElse(null)
		);

	}

	@Test
	public void testExtendWithService() {

		Person person;
		
		person = personService.get(PersonDemoData.RonId, NarGraph.of(Person.Field.name));

		Assert.assertNotNull("Empty name", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertNull("Email is unnecessarily resolved", person.getIfPresent(person::getEmail, Person.Field.email));
		Assert.assertTrue("Permissions are unnecessarily resolved",
			CollectionUtils.sizeIsEmpty(
				Optional.of(person)
					.map(p -> p.getIfPresent(p::getPermissions, Person.Field.permissions))
					.orElse(null)
			)
		);

		person.extend(NarGraph.of(Person.Field.email), personService);

		Assert.assertNotNull("Empty name after extend", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertNotNull("Email is not extended", person.getIfPresent(person::getEmail, Person.Field.email));
		Assert.assertTrue("Permissions are unnecessarily resolved after extend",
			CollectionUtils.sizeIsEmpty(
				Optional.of(person)
					.map(p -> p.getIfPresent(p::getPermissions, Person.Field.permissions))
					.orElse(null)
			)
		);

	}
	
	@Test
	public void testExtendHierarchyWithService() {

		NarGraph<Person.Field> BareView = NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.name)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.add(Boat.Field.crew, NarGraph.Builder.of(Person.Field.class)
					.add(Person.Field.name)
					.build()
				)
				.build()
			)
			.build()
		;

		NarGraph<Person.Field> ExtendedView = NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.name)
			.add(Person.Field.email)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.add(Boat.Field.type)
				.add(Boat.Field.homeport, NarGraph.Builder.of(Marina.Field.class)
					.add(Marina.Field.name)
					.add(Marina.Field.manager, NarGraph.Builder.of(Person.Field.class)
						.add(Person.Field.name)
						.build()
					)
					.build()
				)
				.add(Boat.Field.crew, NarGraph.Builder.of(Person.Field.class)
					.add(Person.Field.name)
					.add(Person.Field.email)
					.add(Person.Field.permissions)
					.build()
				)
				.build()
			)
			.build()
		;

		Person person = personService.get(PersonDemoData.RonId, BareView);

		// set debug names
		person.setName("New person name");
		person.getBoat().setName("New boat name");
		
		// delete every other crew member's names
		for (int i = 0; i < person.getBoat().getCrew().size(); ++i)
			if (i % 2 == 0) {
				Person member = person.getBoat().getCrew().get(i);
				member.setName(null);
				member.getFields().remove(Person.Field.name);
			}
				
		person.extend(ExtendedView, personService);

		Assert.assertNotNull("person.name should not be null", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertEquals("person.name should not be updated", person.getIfPresent(person::getName, Person.Field.name), "New person name");
		Assert.assertNotNull("person.email should not be null", person.getIfPresent(person::getEmail, Person.Field.email));
		Assert.assertNotNull("person.boat should not be null", person.getIfPresent(person::getBoat, Person.Field.boat));
			Assert.assertNotNull("person.boat.name should not be null",
				Optional.of(person)
					.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
					.map(b -> b.getIfPresent(b::getName, Boat.Field.name))
					.orElse(null)
			);
			Assert.assertEquals("person.boat.name should not be updated",
				Optional.of(person)
					.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
					.map(b -> b.getIfPresent(b::getName, Boat.Field.name))
					.orElse(null),
				"New boat name"
			);
			Assert.assertNotNull("person.boat.type should not be null",
				Optional.of(person)
					.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
					.map(b -> b.getIfPresent(b::getType, Boat.Field.type))
					.orElse(null)
			);
			Assert.assertNotNull("person.boat.homeport should not be null",
				Optional.of(person)
					.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
					.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
					.orElse(null)
			);
				Assert.assertNotNull("person.boat.homeport.name should not be null",
					Optional.of(person)
						.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
						.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
						.map(m -> m.getIfPresent(m::getName, Marina.Field.name))
						.orElse(null)
				);
				Assert.assertNotNull("person.boat.homeport.manager should not be null",
					Optional.of(person)
						.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
						.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
						.map(m -> m.getIfPresent(m::getManager, Marina.Field.manager))
						.orElse(null)
				);
					Assert.assertNotNull("person.boat.homeport.manager.name should not be null",
						Optional.of(person)
							.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
							.map(b -> b.getIfPresent(b::getHomeport, Boat.Field.homeport))
							.map(m -> m.getIfPresent(m::getManager, Marina.Field.manager))
							.map(p -> p.getIfPresent(p::getName, Person.Field.name))
							.orElse(null)
					);
			Assert.assertNull("person.boat.skipper should be null",
				Optional.of(person)
					.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
					.map(b -> b.getIfPresent(b::getSkipper, Boat.Field.skipper))
					.orElse(null)
			);
			Assert.assertFalse("person.boat.crew should not be empty",
				CollectionUtils.sizeIsEmpty(
					Optional.of(person)
						.map(p -> p.getIfPresent(p::getBoat, Person.Field.boat))
						.map(b -> b.getIfPresent(b::getCrew, Boat.Field.crew))
						.orElse(null)
				)
			);
				for (Person member : person.getBoat().getCrew()) {
					Assert.assertNotNull("person.boat.crew.name should not be null", member.getIfPresent(member::getName, Person.Field.name));
					Assert.assertNotNull("person.boat.crew.email should not be null", member.getIfPresent(member::getEmail, Person.Field.email));
					Assert.assertFalse("person.boat.crew.permissions should not be empty",
						CollectionUtils.sizeIsEmpty(
							Optional.of(member)
								.map(p -> p.getIfPresent(p::getPermissions, Person.Field.permissions))
								.orElse(null)
						)
					);
				}

	}
	
	/** Try to extend object that already has null field of type HasExtendableFields */ 
	@Test
	public void testExtendHierarchyWithServiceNullValue1() {

		NarGraph<Person.Field> View = NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.name)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.build()
			)
			.build()
		;

		Person person = personService.get(PersonDemoData.RonId, View);

		// clear boat (simulate iz was null when last time resolved)
		person.setBoat(null);
		
		// try to extend (shouldn't do anything, but shouldn't crash either)
		person.extend(View, personService);

		Assert.assertNotNull("person.name should not be null", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertNull("person.boat should still be null", person.getIfPresent(person::getBoat, Person.Field.boat));

	}
	
	/** Try to extend object with field of type HasExtendableFields that should be initialized to null */ 
	@Test
	public void testExtendHierarchyWithServiceNullValue2() {

		NarGraph<Person.Field> View = NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.name)
			.build()
		;

		NarGraph<Person.Field> ExtendedView = NarGraph.Builder.of(Person.Field.class)
			.add(View)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.build()
			)
			.build()
		;

		// Capo doesn't have a boat
		Person person = personService.get(PersonDemoData.CapoId, View);

		Assert.assertNotNull("person.name should not be null", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertFalse("person.boat should not be initialized", person.hasFields(Person.Field.boat));
		Assert.assertNull("person.boat should be null", person.getIfPresent(person::getBoat, Person.Field.boat));

		// try to extend
		person.extend(ExtendedView, personService);

		Assert.assertNotNull("person.name should not be null", person.getIfPresent(person::getName, Person.Field.name));
		Assert.assertTrue("person.boat should be initialized", person.hasFields(Person.Field.boat));
		Assert.assertNull("person.boat should still be null", person.getIfPresent(person::getBoat, Person.Field.boat));

	}
	
	private PersonService personService;
	private PersonAsyncService personAsyncService;
	
}
