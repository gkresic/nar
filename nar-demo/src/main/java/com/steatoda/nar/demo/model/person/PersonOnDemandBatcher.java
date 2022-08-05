package com.steatoda.nar.demo.model.person;

/** Demo batched that performs its operation on demand */
public class PersonOnDemandBatcher extends PersonBatcherBase {

	public static PersonOnDemandBatcher get() {
		
		if (instance == null)
			instance = new PersonOnDemandBatcher();
		
		return instance;

	}

	public PersonOnDemandBatcher() {
		super(new PersonDemoAsyncService());
	}

	private static PersonOnDemandBatcher instance = null;

}
