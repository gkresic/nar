package com.steatoda.nar.demo.model.berth;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import com.steatoda.nar.NarGraph;
import com.steatoda.nar.FieldUnavailableException;
import com.steatoda.nar.demo.model.berth.BerthDemoData.Record;
import com.steatoda.nar.demo.model.boat.Boat;
import com.steatoda.nar.demo.model.boat.BoatDemoService;
import com.steatoda.nar.demo.model.boat.BoatService;

public class BerthDemoService implements BerthService {

	@Override
	public Berth get(String id, NarGraph<Berth.Field> fields) {

		if (fields.isEmpty())
			return Berth.ref(id);
		
		// NOTE: if this was SQL service, here we would build SELECT statement with only selected columns

		Record rec = BerthDemoData.$().parallelStream().filter(r -> id.equals(r.id)).findFirst().orElse(null);
		
		if (rec == null)
			return null;
		
		return read(rec, fields);
		
	}

	@Override
	public void create(Berth berth) {
		
		Set<Berth.Field> missingFields = Sets.difference(berth.getFields(), EnumSet.allOf(Berth.Field.class));
		if (!missingFields.isEmpty())
			throw new FieldUnavailableException(missingFields);
		
		// NOTE: if this was SQL service, here we would build INSERT statement, executed it and read autogenerated columns
		berth.setId(UUID.randomUUID().toString());
		
		BerthDemoData.$().add(new Record(
			berth.getId(),
			Optional.ofNullable(berth.getBoat()).map(Boat::getId).orElse(null)
		));
		
		Log.debug("Created berth: {}", berth);
		
	}

	@Override
	public void modify(Berth berth, Set<Berth.Field> fields) {

		AtomicBoolean hasUpdates = new AtomicBoolean(false);
		
		// NOTE: if this was SQL service, here we would build UPDATE statement with only selected columns
		List<Consumer<Record>> dummyModifiers = fields.stream()
			.map(field -> {
				switch (field) {
					case boat:	return (Consumer<Record>) r -> r.boatId = Optional.ofNullable(berth.getBoat()).map(Boat::getId).orElse(null);
				}
				throw new UnsupportedOperationException("Unknown berth field: " + field);
			})
			.filter(Objects::nonNull)
			.filter(modifier -> { hasUpdates.set(true); return true; })
			.collect(Collectors.toList());
		
		// if no updatable fields were used, bail out
		if (!hasUpdates.get())
			return;

		// NOTE: if this was SQL service, here we would execute statement
		Record rec = BerthDemoData.$().parallelStream().filter(r -> berth.getId().equals(r.id)).findFirst().orElse(null);
		if (rec == null)
			throw new IllegalArgumentException("Berth " + berth + " doesn't exist!");
		dummyModifiers.forEach(modifier -> modifier.accept(rec));
		
		Log.debug("Modified {} field(s) in berth {}", dummyModifiers.size(), berth);
		
	}

	@Override
	public void delete(Berth berth) {

		BerthDemoData.$().removeIf(rec -> rec.id.equals(berth.getId()));
	
		Log.debug("Deleted berth: {}", berth);

	}

	private Berth read(Record record, NarGraph<Berth.Field> fields) {

		BoatService boatService = new BoatDemoService();
		
		// if this was SQL service, here we would (selectively) read java.sql.ResultSet
		
		Berth berth = new Berth();

		berth.setId(record.id);
		
		for (Berth.Field field : fields)
			switch (field) {
				case boat: berth.setBoat(Optional.ofNullable(record.boatId).map(id -> boatService.get(id, fields.getGraph(field, Boat.Field.class))).orElse(null)); break;
			}

		return berth;
		
	}

	private static final Logger Log = LoggerFactory.getLogger(BerthDemoService.class);

}
