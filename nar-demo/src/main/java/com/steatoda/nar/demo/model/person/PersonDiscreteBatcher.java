package com.steatoda.nar.demo.model.person;

import java.util.Timer;
import java.util.TimerTask;

/** Demo batcher that performs its operation in discrete steps (every second) */
public class PersonDiscreteBatcher extends PersonBatcherBase {

	public static synchronized PersonDiscreteBatcher get() {
		
		if (instance == null)
			instance = new PersonDiscreteBatcher();
		
		if (!scheduled) {
			
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					instance.run();
				}
			};
			
			timer.schedule(task, 1000);	// delay execution for 1 second

			scheduled = true;
			
		}

		return instance;

	}

	public PersonDiscreteBatcher() {
		super(new PersonDemoAsyncService());
	}

	@Override
	public void run() {
		
		scheduled = false;
		
		super.run();
		
	}
	
	private static PersonDiscreteBatcher instance = null;

	private static boolean scheduled = false;

	private static final Timer timer = new Timer();

}
