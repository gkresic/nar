package com.steatoda.nar.demo.model.person;

import com.steatoda.nar.service.async.NarBatcher;

/** Base class for all person batchers */
public class PersonBatcherBase extends NarBatcher<String, Person, Person.Field> {

	public PersonBatcherBase(PersonAsyncService service) {
		super(service);
	}

}
