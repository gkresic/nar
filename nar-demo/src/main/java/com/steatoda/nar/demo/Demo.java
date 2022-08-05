package com.steatoda.nar.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.commons.text.TextStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.steatoda.nar.NarGraph;
import com.steatoda.nar.service.async.NarRequest;
import com.steatoda.nar.service.async.NarServiceHandler;
import com.steatoda.nar.jackson.NarPropertyFilter;
import com.steatoda.nar.jackson.NarSerializerModifier;
import com.steatoda.nar.demo.model.DemoEntity;
import com.steatoda.nar.demo.model.marina.MarinaDemoCachingService;
import com.steatoda.nar.demo.model.marina.MarinaDetailsDisplay;
import com.steatoda.nar.demo.model.marina.MarinaEditor;
import com.steatoda.nar.demo.model.marina.MarinaNameDisplay;
import com.steatoda.nar.demo.model.person.PersonAsyncService;
import com.steatoda.nar.demo.model.person.PersonBatcherBase;
import com.steatoda.nar.demo.model.person.PersonDemoAsyncService;
import com.steatoda.nar.demo.model.person.PersonDisplay;
import com.steatoda.nar.demo.model.berth.Berth;
import com.steatoda.nar.demo.model.berth.BerthDemoService;
import com.steatoda.nar.demo.model.berth.BerthService;
import com.steatoda.nar.demo.model.boat.Boat;
import com.steatoda.nar.demo.model.boat.BoatDemoData;
import com.steatoda.nar.demo.model.boat.BoatDemoService;
import com.steatoda.nar.demo.model.boat.BoatService;
import com.steatoda.nar.demo.model.marina.Marina;
import com.steatoda.nar.demo.model.marina.MarinaDemoData;
import com.steatoda.nar.demo.model.marina.MarinaDemoService;
import com.steatoda.nar.demo.model.marina.MarinaService;
import com.steatoda.nar.demo.model.person.Notifier;
import com.steatoda.nar.demo.model.person.Person;
import com.steatoda.nar.demo.model.person.PersonDemoData;
import com.steatoda.nar.demo.model.person.PersonDemoService;
import com.steatoda.nar.demo.model.person.PersonService;

public class Demo {

	public static void main(String[] args) throws IOException, InterruptedException {

		Log.info("Welcome to Nar demo!");

		Demo demo = new Demo();

		if (args.length == 0) {
			demo.personViews();
			demo.marinaViews();
			demo.boatViews();
			demo.notifier();
			demo.marinaDisplays();
			demo.marinaEditors();
			demo.async();
			demo.batcher();
			demo.cache();
		} else if ("personViews".equals(args[0])) {
			demo.personViews();
		} else if ("marinaViews".equals(args[0])) {
			demo.marinaViews();
		} else if ("boatViews".equals(args[0])) {
			demo.boatViews();
		} else if ("notifier".equals(args[0])) {
			demo.notifier();
		} else if ("marinaDisplays".equals(args[0])) {
			demo.marinaDisplays();
		} else if ("marinaEditors".equals(args[0])) {
			demo.marinaEditors();
		} else if ("async".equals(args[0])) {
			demo.async();
		} else if ("batcher".equals(args[0])) {
			demo.batcher();
		} else if ("cache".equals(args[0])) {
			demo.cache();
		} else {
			Log.error("Unrecognized demo '{}'", args[0]);
		}

		Log.info("Bye...");

	}

	public Demo() {

		personService = new PersonDemoService();
		personAsyncService = new PersonDemoAsyncService();
		marinaService = new MarinaDemoService();
		boatService = new BoatDemoService();
		berthService = new BerthDemoService();

		mapper = JsonMapper.builder()
			.configure(SerializationFeature.INDENT_OUTPUT, true)
			.disable(MapperFeature.AUTO_DETECT_CREATORS)
			.disable(MapperFeature.AUTO_DETECT_FIELDS)
			.disable(MapperFeature.AUTO_DETECT_GETTERS)
			.disable(MapperFeature.AUTO_DETECT_IS_GETTERS)
			.addModule(new SimpleModule()
				.setSerializerModifier(new NarSerializerModifier(DemoEntity.JsonPropertyId))
			)
			.filterProvider(new SimpleFilterProvider().addFilter(NarPropertyFilter.Name, new NarPropertyFilter().setIgnoreFieldUnavailableException(true)))
			.build();

	}

	private void personViews() throws IOException {

		waitUser("Let's see different views on one Person object...");

		NarGraph<Person.Field> graph;
		String personId = PersonDemoData.RonId;
		Person person;

		graph = NarGraph.noneOf(Person.Field.class);
		person = personService.get(personId, graph);
		Log.info("This is object with only its (mandatory) ID field set ('reference' view):\n{}", mapper.writeValueAsString(person));

		waitUser();

		graph = NarGraph.of(Person.Field.name, Person.Field.email);
		person = personService.get(personId, graph);
		Log.info("Same object with name and email fields:\n{}", mapper.writeValueAsString(person));

		waitUser();

		graph = NarGraph.allOf(Person.Field.class);
		person = personService.get(personId, graph);
		Log.info("Same object with all fields set (with subobjects as refs only):\n{}", mapper.writeValueAsString(person));

		waitUser();

		graph = NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.name)
			.add(Person.Field.email)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.add(Boat.Field.homeport, NarGraph.Builder.of(Marina.Field.class)
					.build()
				)
				.build()
			)
			.build();
		person = personService.get(personId, graph);
		Log.info("Finally, same object, but with some fields initialized for 'boat' subobject :\n{}", mapper.writeValueAsString(person));

		waitUser();
		
	}

	private void marinaViews() throws IOException {

		waitUser("Ok, let's now see *real* use of views...");

		waitUser(
			"We can't resolve full Marina object because of cyclic references,",
			"but let's see just how big would payload be for Marina Kornati",
			"if only *first level* sub-objects were resolved with all their fields"
		);

		NarGraph<Marina.Field> graph = NarGraph.Builder.of(Marina.Field.class)
			.add(NarGraph.allOf(Marina.Field.class))
			.add(Marina.Field.manager, NarGraph.allOf(Person.Field.class))
			.add(Marina.Field.berths, NarGraph.allOf(Berth.Field.class))
			.build();

		Marina marina = marinaService.get(MarinaDemoData.KornatiId, graph);

		String marinaJson = mapper.writeValueAsString(marina);

		Log.info("This is such object:\n{}", marinaJson);

		waitUser();

		Log.info("It's ~{} bytes long :)", NumberFormat.getInstance().format(marinaJson.length()));

		waitUser();

		waitUser("Let's see what we'll get if we strip only 'depth' field");

		marina.clearFields(Marina.Field.depths);
		marinaJson = mapper.writeValueAsString(marina);

		Log.info("This is such object:\n{}", marinaJson);

		waitUser();

		Log.info("It's ~{} bytes long", NumberFormat.getInstance().format(marinaJson.length()));

		waitUser();

		waitUser("Better, but let's go even further and strip 'berths' field, too");

		marina.clearFields(Marina.Field.berths);
		marinaJson = mapper.writeValueAsString(marina);

		Log.info("This is such object:\n{}", marinaJson);

		waitUser();

		Log.info("This is now only ~{} bytes long", NumberFormat.getInstance().format(marinaJson.length()));

		waitUser();

		waitUser(
			"Let's go even further and resolve object again, without 'depth' and 'berths' fields",
			"but with sub-objects reduced to only fields needed for their identifying to user"
		);

		// NOTE re-adding fields to NarGraph.Builder appends graph, doesn't substitute it
		graph = NarGraph.Builder.of(Marina.Field.class)
			.add(NarGraph.complementOf(EnumSet.of(Marina.Field.berths, Marina.Field.depths)))
			.add(Marina.Field.manager, NarGraph.of(Person.Field.name, Person.Field.email))	// add manager again, with subgraph
			.build();
		marina = marinaService.get(MarinaDemoData.KornatiId, graph);
		marinaJson = mapper.writeValueAsString(marina);

		Log.info("This is such object:\n{}", marinaJson);

		waitUser();

		Log.info("This is now ~{} bytes long", NumberFormat.getInstance().format(marinaJson.length()));

		waitUser();

	}

	private void boatViews() throws IOException {

		waitUser(
			"If you consider marina sample a bit far fetched",
			"let's now see use of views on more \"real\" case: one boat..."
		);

		waitUser(
			"Again, we can't resolve full Boat object because of cyclic references,",
			"but let's see just how big would payload be for Alpha with only crew sub-objects fully resolved"
		);

		NarGraph<Boat.Field> graph = NarGraph.Builder.of(Boat.Field.class)
			.add(NarGraph.allOf(Boat.Field.class))
			.add(Boat.Field.crew, NarGraph.allOf(Person.Field.class))
			.build();

		Boat boat = boatService.get(BoatDemoData.AlphaId, graph);

		Log.info("This is such object:\n{}", mapper.writeValueAsString(boat));

		waitUser();

		Log.info("Even if we ignore size of this payload, note that we leaked user details");

		waitUser();

		waitUser(
			"Instead, what if we resolve skipper and crew sub-objects with only fields needed for their identifying to user",
			"and leave up to client to resolve additional fields if/when needed (with isolated security checks)?"
		);

		// lets say we are using PersonDisplay for displaying persons details to user, so resolve all fields it needs
		graph = NarGraph.Builder.of(Boat.Field.class)
			.add(NarGraph.allOf(Boat.Field.class))
			.add(Boat.Field.skipper, PersonDisplay.Graph)
			.add(Boat.Field.crew, PersonDisplay.Graph)
			.build();

		boat = boatService.get(BoatDemoData.AlphaId, graph);

		Log.info("This is such object:\n{}", mapper.writeValueAsString(boat));

		waitUser();

	}

	private void notifier() throws IOException {

		waitUser("Ok, now let's see some non-avoidable pain points...");

		waitUser(
			"Notifier requires 'email' field to be set in order to send email to some person.",
			"Makes sense."
		);

		waitUser("But what if all we have is person with only name set?");

		Person person = personService.get(PersonDemoData.RonId, NarGraph.of(Person.Field.name));
		Log.info("Like this:\n{}", mapper.writeValueAsString(person));

		waitUser("If we pass such an instance to Notifier, it should include some extra logic to fetch missing 'email' field:");

		Notifier notifier = new Notifier();

		notifier.sendEmail(person);

		waitUser();

		waitUser("Now, if we pass that same instance to notifier again, it will get already initialized email and notify person immediately:");

		notifier.sendEmail(person);

		waitUser();

		waitUser("'Pain' is in the fact that such logic should be implemented in *every* function that access *any* field-aware object");

	}

	private void marinaDisplays() throws IOException {

		waitUser("Let's see how different marina displays are reacting when value they are displaying is updated...");

		MarinaNameDisplay nameDisplay = new MarinaNameDisplay();
		MarinaDetailsDisplay detailsDisplay1 = new MarinaDetailsDisplay();
		MarinaDetailsDisplay detailsDisplay2 = new MarinaDetailsDisplay();

		Marina marina = marinaService.get(MarinaDemoData.KornatiId, NarGraph.of(Marina.Field.name));

		waitUser("We'll use partial object with only name initialized:");
		Log.info("This one:\n{}", mapper.writeValueAsString(marina));

		waitUser("We'll set that partial object in three displays:");

		waitUser("MarinaNameDisplay (works with '" + MarinaNameDisplay.Graph + "') should be satisfied with what it got");
		nameDisplay.setValue(marina);

		waitUser();

		waitUser("First MarinaDetailsDisplay (works with '" + MarinaDetailsDisplay.Graph + "') should extend value it gets");
		detailsDisplay1.setValue(marina);

		waitUser();

		waitUser(
			"However, if we use same value passed to first MarinaDetailsDisplay (which it extended) and pass it to second MarinaDetailsDisplay",
			"that second one should get already extended version so it should be satisfied immediately"
		);
		detailsDisplay2.setValue(marina);

		waitUser();

		waitUser("When marina's name is changed, all three displays should just use that new name immediately");
		marina.setName("Kornati, Biograd");
		marinaService.modify(marina, EnumSet.of(Marina.Field.name), NarGraph.noneOf(Marina.Field.class));

		waitUser();

		waitUser(
			"However, when marina's manager is changed using reference object,",
			"detail displays should react with extending manager with its name"
		);
		marina.setManager(Person.ref(PersonDemoData.RonId));
		// lets pretend we don't know wno is listening to events and not ask any fields to return
		marinaService.modify(marina, EnumSet.of(Marina.Field.manager), NarGraph.noneOf(Marina.Field.class));

		waitUser();

		nameDisplay.close();
		detailsDisplay1.close();
		detailsDisplay2.close();

	}

	private void marinaEditors() throws IOException {

		waitUser("Let's see how to safely edit partial objects using editors...");

		MarinaEditor editor = new MarinaEditor();
		MarinaNameDisplay display = new MarinaNameDisplay();

		Marina marina = marinaService.get(MarinaDemoData.KornatiId, MarinaNameDisplay.Graph);

		waitUser("We'll use partial object with initialized only fields MarinaNameDisplay needs:");
		display.setValue(marina);

		waitUser();

		waitUser("However, when we pass this value to MarinaEditor (which requires '" + MarinaEditor.Graph + "'), it will extend value it gets");
		editor.setValue(marina);

		waitUser();

		waitUser("Now we'll use editor's value, persist it and see how display reacts");
		marina = editor.getValue();
		marinaService.modify(marina, editor.getEditableGraph(), MarinaNameDisplay.Graph);

		waitUser();

		display.close();

	}

	private void async() throws IOException, InterruptedException {

		waitUser("Asynchronous services are common for remote requests, so let's see them in action...");

		NarGraph<Person.Field> graph = NarGraph.of(Person.Field.name, Person.Field.email);
		CountDownLatch lock = new CountDownLatch(1);

		Log.info("Fetching person's fields: {}", graph);
		/*FieldsRequest request = */personAsyncService.get(PersonDemoData.RonId, graph, new NarServiceHandler<Person>() {
			@Override
			public void onPreRequest(NarRequest request) {
				Log.info("Requesting (show loader in GUI or something)...");
				NarServiceHandler.super.onPreRequest(request);
			}
			@Override
			public void onSuccess(Person person) {
				Log.info("Got object:\n{}", writeValueAsString(person));
			}
			@Override
			public void onPostRequest(NarRequest request) {
				Log.info("Request finished (hide loader)");
				NarServiceHandler.super.onPostRequest(request);
			}
			@Override
			public void onFinish() {
				NarServiceHandler.super.onFinish();
				lock.countDown();
			}
		});

		// don't return until request simulation has finished
		lock.await();

	}

	private void batcher() throws IOException, InterruptedException {

		waitUser("Asynchronous services can be used with batchers to batch multiple get/extend requests into one");

		PersonAsyncService loggingAsyncService = new PersonAsyncService() {
			@Override
			public NarRequest get(String id, NarGraph<Person.Field> graph, NarServiceHandler<Person> handler) {
				Log.info("Fetching fields: {}", graph);
				return personAsyncService.get(id, graph, handler);
			}
		};

		PersonBatcherBase batcher = new PersonBatcherBase(loggingAsyncService);

		waitUser("Let's simulate two requests to retrieve user 'Ron' using batched async service...");

		NarGraph<Person.Field> graph1 = NarGraph.of(Person.Field.name);
		NarGraph<Person.Field> graph2 = NarGraph.of(Person.Field.name, Person.Field.email);

		String personId = PersonDemoData.RonId;
		CountDownLatch lock = new CountDownLatch(2);

		waitUser("First request will ask for fields: " + graph1);
		/*FieldsRequest request1 = */batcher.get(personId, graph1, new NarServiceHandler<Person>() {
			@Override
			public void onPreRequest(NarRequest request) {
				Log.info("(1) Requesting fields: {}", graph1);
				NarServiceHandler.super.onPreRequest(request);
			}
			@Override
			public void onSuccess(Person person) {
				Log.info("(1) Got object:\n{}", writeValueAsString(person));
			}
			@Override
			public void onPostRequest(NarRequest request) {
				Log.info("(1) Request finished");
				NarServiceHandler.super.onPostRequest(request);
			}
			@Override
			public void onFinish() {
				NarServiceHandler.super.onFinish();
				lock.countDown();
			}
		});

		waitUser();

		waitUser("Second request will ask for fields: " + graph2);
		/*FieldsRequest request2 = */batcher.get(personId, graph2, new NarServiceHandler<Person>() {
			@Override
			public void onPreRequest(NarRequest request) {
				Log.info("(2) Requesting fields: {}", graph2);
				NarServiceHandler.super.onPreRequest(request);
			}
			@Override
			public void onSuccess(Person person) {
				Log.info("(2) Got object:\n{}", writeValueAsString(person));
			}
			@Override
			public void onPostRequest(NarRequest request) {
				Log.info("(2) Request finished");
				NarServiceHandler.super.onPostRequest(request);
			}
			@Override
			public void onFinish() {
				NarServiceHandler.super.onFinish();
				lock.countDown();
			}
		});

		waitUser();

		waitUser("Now, lets trigger batched requests and see what will be *really* fetched and returned to callers...");

		// simulate batch event (end of event look, for example)
		batcher.run();

		// don't return until request simulation has finished
		lock.await();

	}

	private void cache() throws IOException {

		waitUser("Having the ability to hold only partially resolved entities drastically simplifies caching, too");

		NarGraph<Marina.Field> graph;
		String marinaId = MarinaDemoData.KornatiId;
		Marina marina;

		MarinaService loggingMarinaService = new MarinaDemoService() {
			@Override
			public Marina get(String id, NarGraph<Marina.Field> graph) {
				Log.info("Fetching from underlying service: {}", graph);
				return super.get(id, graph);
			}
		};

		try (MarinaDemoCachingService cachingService = new MarinaDemoCachingService(loggingMarinaService)) {

			graph = NarGraph.of(Marina.Field.name, Marina.Field.manager);

			waitUser("Let's first resolve marina Kornati using fields " + graph);

			marina = cachingService.get(marinaId, graph);

			Log.info("Got marina {} with fields {}", marina, marina.getFields());

			waitUser();

			waitUser("Let's resolve same marina again using same fields");

			marina = cachingService.get(marinaId, graph);

			Log.info("Got marina {} with fields {}", marina, marina.getFields());

			waitUser();

			waitUser("Notice that this time nothing was fetched from underlying service, because all requested fields were already available in cache?");

			graph = NarGraph.of(Marina.Field.name, Marina.Field.manager, Marina.Field.manager, Marina.Field.latitude, Marina.Field.longitude, Marina.Field.berths);

			waitUser("Now, lets fetch same marina, but using extended graph: " + graph);
			marina = cachingService.get(marinaId, graph);

			Log.info("Got marina {} with fields {}", marina, marina.getFields());

			waitUser();

			waitUser("Notice how this time only newly requested fields (latitude, longitude and berths) were fetched from underlying service?");

			waitUser("Lets resolve same marina again using same fields");

			marina = cachingService.get(marinaId, graph);

			Log.info("Got marina {} with fields {}", marina, marina.getFields());

			waitUser();

			waitUser("This time underlying service was needed only to provide non-cached fields (latitude and longitude)");

		}

	}

	private String writeValueAsString(Object value) {
		try {
			return mapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Unable to serialize " + value + " to JSON", e);
		}
	}

	private void waitUser() throws IOException {
		waitUser(new String[0]);
	}
	
	private void waitUser(String message) throws IOException {
		waitUser(new String[] { message } );
	}
	
	private void waitUser(String... messages) throws IOException {
		
		String message = new TextStringBuilder()
			.appendWithSeparators(messages, "\n")
			.appendSeparator("\n").append("[ENTER]")
			.toString();
		
		Log.info(message);

		new BufferedReader(new InputStreamReader(System.in)).readLine();
		
	}

	private static final Logger Log = LoggerFactory.getLogger(Demo.class);

	private final PersonService personService;
	private final PersonAsyncService personAsyncService;
	private final MarinaService marinaService;
	private final BoatService boatService;
	@SuppressWarnings({"unused", "FieldCanBeLocal"})
	private final BerthService berthService;
	private final JsonMapper mapper;

}
