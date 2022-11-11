package com.steatoda.nar.demo.model.marina;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.steatoda.nar.demo.DemoEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.steatoda.nar.NarGraph;
import com.steatoda.nar.demo.model.berth.Berth;
import com.steatoda.nar.demo.model.berth.BerthDemoService;
import com.steatoda.nar.demo.model.berth.BerthService;
import com.steatoda.nar.demo.model.marina.MarinaDemoData.Record;
import com.steatoda.nar.demo.model.person.Person;
import com.steatoda.nar.demo.model.person.PersonDemoService;
import com.steatoda.nar.demo.model.person.PersonService;

/** Operates on sample marinas from memory store */
public class MarinaDemoService implements MarinaService {

	public MarinaDemoService() {
		this.validator = new MarinaValidator();
	}

	@Override
	public Marina get(String id, NarGraph<Marina.Field> graph) {

		// NOTE: if this was SQL service, here we would build SELECT statement with only selected columns

		Record rec = MarinaDemoData.$().parallelStream().filter(r -> id.equals(r.id)).findFirst().orElse(null);
		
		if (rec == null)
			return null;
		
		return read(rec, graph);
		
	}
	
	@Override
	public void create(Marina marina, NarGraph<Marina.Field> graph) {

		validator.validate(marina);

		// NOTE: if this was SQL service, here we would build INSERT statement, executed it and read autogenerated columns
		marina.setId(UUID.randomUUID().toString());
		
		MarinaDemoData.$().add(new Record(
			marina.getId(),
			marina.getName(),
			Optional.ofNullable(marina.getManager()).map(Person::getId).orElse(null),
			marina.getLatitude(),
			marina.getLongitude(),
			Optional.ofNullable(marina.getBerths()).map(berths -> berths.stream().map(Berth::getId).collect(Collectors.toList())).orElse(null),
			marina.getDepths()
		));

		DemoEventBus.get().post(new MarinaCreateEvent(marina, this));

		Log.debug("Created marina: {}", marina);

	}

	@Override
	public void modify(Marina marina, Marina patch, NarGraph<Marina.Field> graph) {

		// since patch can contain *any* set of fields, validation is multi-step process:
		// 1. extend base entity (marina) with all fields required for validation
		marina.extend(MarinaValidator.Graph, this);
		// 2. create mockup entity for validation
		Marina mockup = marina.clone();
		// 3. apply patch to mockup
		mockup.pull(patch);
		// 4. finally, validate mockup
		validator.validate(mockup);

		AtomicBoolean hasUpdates = new AtomicBoolean(false);
		
		// NOTE: if this was SQL service, here we would build UPDATE statement with only selected columns
		List<Consumer<Record>> modifiers = patch.getFields().stream()
			.map(field -> {
				switch (field) {
					case name:		return (Consumer<Record>) r -> r.name = marina.getName();
					case manager:	return (Consumer<Record>) r -> r.managerId = Optional.ofNullable(marina.getManager()).map(Person::getId).orElse(null);
					case latitude:	return (Consumer<Record>) r -> r.latitute = marina.getLatitude();
					case longitude:	return (Consumer<Record>) r -> r.longitude = marina.getLongitude();
					case berths:	return null;	// let's assume berths are not updatable
					case depths:	return null;	// let's assume depths are not updatable
				}
				throw new UnsupportedOperationException("Unknown person field: " + field);
			})
			.filter(Objects::nonNull)
			.filter(modifier -> { hasUpdates.set(true); return true; })
			.collect(Collectors.toList());
		
		// if no updatable fields were used, just extend with required graph and bail out
		if (!hasUpdates.get()) {
			marina.extend(graph, this);
			return;
		}

		// NOTE: if this was SQL service, here we would execute statement
		Record rec = MarinaDemoData.$().parallelStream().filter(r -> marina.getId().equals(r.id)).findFirst().orElse(null);
		if (rec == null)
			throw new IllegalArgumentException("Marina " + marina + " doesn't exist!");
		modifiers.forEach(modifier -> modifier.accept(rec));

		// lets assume modifiers didn't touch any fields outside of their own
		marina.clearFields(graph);
		marina.extend(graph, this);

		Log.debug("Modified {} field(s) in marina {}", modifiers.size(), marina);

		DemoEventBus.get().post(new MarinaModifyEvent(marina, this));

	}

	@Override
	public void delete(Marina marina) {
		
		MarinaDemoData.$().removeIf(rec -> rec.id.equals(marina.getId()));
		
		Log.debug("Deleted marina: {}", marina);

		DemoEventBus.get().post(new MarinaDeleteEvent(marina, this));

	}

	@Override
	public int count(Void selector) {
		throw new UnsupportedOperationException("Outside of scope of this demo");
	}

	@Override
	public Stream<Marina> query(Void selector, NarGraph<Marina.Field> graph) {
		throw new UnsupportedOperationException("Outside of scope of this demo");
	}

	@Override
	public Stream<Marina> queryAllFieldValues(Void selector, Set<Marina.Field> fields) {
		throw new UnsupportedOperationException("Outside of scope of this demo");
	}

	private Marina read(Record record, NarGraph<Marina.Field> graph) {

		BerthService berthService = new BerthDemoService();
		PersonService personService = new PersonDemoService();

		// if this was SQL service, here we would (selectively) read java.sql.ResultSet
		
		Marina marina = new Marina();

		marina.setId(record.id);
		
		for (Marina.Field field : graph)
			switch (field) {
				case name: marina.setName(record.name); break;
				case manager: marina.setManager(personService.get(record.managerId, graph.getGraph(field, Person.Field.class))); break;
				case latitude: marina.setLatitude(record.latitute); break;
				case longitude: marina.setLongitude(record.longitude); break;
				case berths: marina.setBerths(record.berthIds.stream().map(id -> berthService.get(id, graph.getGraph(field, Berth.Field.class))).collect(Collectors.toList())); break;
				case depths: marina.setDepths(Arrays.stream(record.depths).map(Integer[]::clone).toArray(Integer[][]::new)); break;
			}

		return marina;
		
	}

	private static final Logger Log = LoggerFactory.getLogger(MarinaDemoService.class);

	private final MarinaValidator validator;

}
