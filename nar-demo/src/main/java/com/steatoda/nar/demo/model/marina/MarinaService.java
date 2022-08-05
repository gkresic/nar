package com.steatoda.nar.demo.model.marina;

import com.steatoda.nar.service.crud.NarCRUDService;

public interface MarinaService extends NarCRUDService<String, Marina, Marina.Field, Void> {

	@Override
	default Marina instance() {
		return new Marina();
	}

}
