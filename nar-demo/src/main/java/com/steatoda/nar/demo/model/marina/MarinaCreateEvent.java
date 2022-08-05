package com.steatoda.nar.demo.model.marina;

/** Fired when marina is created. Enclosed object has all fields set. */
public class MarinaCreateEvent extends MarinaEvent {

	public MarinaCreateEvent(Marina marina, Object source) {
		super(marina, source);
	}
	
	private static final long serialVersionUID = 1L;

}
