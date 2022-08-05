package com.steatoda.nar.demo.model.marina;

/** Fired when marina is deleted. <b>WARN</b>: enclosed object may be reference only. */
public class MarinaDeleteEvent extends MarinaEvent {

	public MarinaDeleteEvent(Marina marina, Object source) {
		super(marina, source);
	}
	
	private static final long serialVersionUID = 1L;

}
