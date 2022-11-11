package com.steatoda.nar.demo.model.marina;

import com.steatoda.nar.NarEditor;
import com.steatoda.nar.NarGraph;
import com.steatoda.nar.demo.model.person.Person;
import org.apache.commons.text.TextStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/** (Dummy, no-op) editor for editing marina names */
public class MarinaEditor implements NarEditor<Marina.Field> {

	// graph this editor only reads
	public static final NarGraph<Marina.Field> DisplayGraph = NarGraph.Builder.of(Marina.Field.class)
		.add(Marina.Field.manager, NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.name)
			.build()
		)
		.add(Marina.Field.latitude)
		.add(Marina.Field.longitude)
		.build();

	// graph this editor modifies
	public static final NarGraph<Marina.Field> EditableGraph = NarGraph.Builder.of(Marina.Field.class)
		.add(Marina.Field.name)
		.build();

	// cumulative graph this editor works with
	public static final NarGraph<Marina.Field> Graph = NarGraph.Builder.of(Marina.Field.class)
		.add(DisplayGraph)
		.add(EditableGraph)
		.build();

	public MarinaEditor() {
		
		// service that can fetch missing fields
		marinaService = new MarinaDemoService();

	}

	@Override
	public NarGraph<Marina.Field> getGraph() { return Graph; }

	@Override
	public NarGraph<Marina.Field> getEditableGraph() { return EditableGraph; }

	public Marina getValue() {

		// NOTE if this was real editor, here we would flush changes into 'marina' instance
		// instead, since we are dummy editor, just append current timestamp to name

		marina.setName(
			new TextStringBuilder(marina.getName())
				.appendSeparator(' ')
				.append("modified @ ")
				.append(DateFormatter.format(Instant.now()))
				.toString()
		);

		return marina;

	}
	
	public void setValue(Marina marina) {

		// since we'll *edit* this instance, make local copy
		marina = marina.clone();

		this.marina = marina;

		// for demo purposes, log missing graph
		NarGraph<Marina.Field> missingGraph = marina.getMissingGraph(Graph);
		if (!missingGraph.isEmpty())
			Log.info("Got value ({}) with some fields missing: {}", marina, missingGraph);

		marina.extend(Graph, marinaService);

		// now we are sure we have all fields we need

		Log.info("Got value with all fields I need: id={}; name={}; manager={}; latitute={}; longitude={}",
			marina.getId(),
			marina.getName(),
			marina.getManager(),
			marina.getLatitude(),
			marina.getLongitude()
		);

	}

	private static final Logger Log = LoggerFactory.getLogger(MarinaEditor.class);
	private static final DateTimeFormatter DateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	private final MarinaService marinaService;
	
	private Marina marina;
	
}
