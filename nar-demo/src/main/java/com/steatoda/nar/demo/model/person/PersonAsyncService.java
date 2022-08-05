package com.steatoda.nar.demo.model.person;

import com.steatoda.nar.service.async.NarAsyncService;

public interface PersonAsyncService extends NarAsyncService<String, Person, Person.Field> {

	@Override
	default Person instance() {
		return new Person();
	}

}
