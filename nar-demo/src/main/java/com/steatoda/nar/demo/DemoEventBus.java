package com.steatoda.nar.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

public class DemoEventBus extends EventBus {

	public static DemoEventBus get() {
		if (instance == null)
			instance = new DemoEventBus();
		return instance;
	}
	
	private DemoEventBus() {
		super((exception, context) -> Log.error("Error in event handler ({})", context, exception));
	}

	private static final Logger Log = LoggerFactory.getLogger(DemoEventBus.class);

	private static DemoEventBus instance = null;
	
}
