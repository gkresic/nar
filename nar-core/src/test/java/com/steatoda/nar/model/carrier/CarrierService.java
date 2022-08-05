package com.steatoda.nar.model.carrier;

import com.steatoda.nar.NarGraph;
import com.steatoda.nar.service.NarService;

public interface CarrierService extends NarService<String, Carrier, Carrier.Field> {

	@Override
	default Carrier instance() {
		return new Carrier();
	}

	@Override
	Carrier get(String id, NarGraph<Carrier.Field> fields);

}
