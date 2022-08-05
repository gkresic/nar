package com.steatoda.nar.demo.model.marina;

import com.steatoda.nar.service.crud.NarCachingCRUDService;

public class MarinaDemoCachingService extends NarCachingCRUDService<String, Marina, Marina.Field, Void> implements MarinaService, AutoCloseable {

	public MarinaDemoCachingService() {
		this(new MarinaDemoService());
	}

	public MarinaDemoCachingService(MarinaService service) {
		super(Cache, service);
	}

	@Override
	public void close() {
		Cache.close();
	}

	private static final MarinaCache Cache = new MarinaCache();

}
