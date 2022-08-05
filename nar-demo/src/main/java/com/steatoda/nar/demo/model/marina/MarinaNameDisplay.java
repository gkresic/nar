package com.steatoda.nar.demo.model.marina;

import com.google.common.eventbus.Subscribe;
import com.steatoda.nar.NarGraph;
import com.steatoda.nar.demo.DemoEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Virtual presenter that displays marina names only.
 * Updates itself on detected change.
 */
public class MarinaNameDisplay implements AutoCloseable {

	public static final NarGraph<Marina.Field> Graph = NarGraph.Builder.of(Marina.Field.class)
		.add(Marina.Field.name)
		.build();

	public MarinaNameDisplay() {

		// service that can fetch missing fields
		marinaService = new MarinaDemoService();

		// register for MarinaModifyEvent<s>
		DemoEventBus.get().register(this);
		
	}

	public Marina getValue() {

		return marina;

	}
	
	public void setValue(Marina marina) {
	
		this.marina = marina;

		// for demo purposes, log missing graph
		NarGraph<Marina.Field> missingGraph = marina.getMissingGraph(Graph);
		if (!missingGraph.isEmpty())
			Log.info("Got value ({}) with some fields missing: {}", marina, missingGraph);

		marina.extend(Graph, marinaService);

		// now we are sure we have all fields we need

		Log.info("Got value with all fields I need: id={}; name={}", marina.getId(), marina.getName());
		
	}

	@Override
	public void close() {
		DemoEventBus.get().unregister(this);
	}
	
	@Subscribe
	void onMarinaModify(MarinaModifyEvent event) {

		if (marina == null)
			return;	// we are not initialized yet
		
		if (!marina.equals(event.getMarina()))
			return;	// we are displaying different object

		setValue(event.getMarina());

	}

	private static final Logger Log = LoggerFactory.getLogger(MarinaNameDisplay.class);

	private final MarinaService marinaService;
	
	private Marina marina;
	
}
