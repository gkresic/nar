package com.steatoda.nar.demo.model.person;

import com.steatoda.nar.service.crud.NarCRUDService;

public interface PersonService extends NarCRUDService<String, Person, Person.Field, Void> {

	@Override
	default Person instance() {
		return new Person();
	}

}
