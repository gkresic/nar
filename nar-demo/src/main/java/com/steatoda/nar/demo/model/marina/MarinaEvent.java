package com.steatoda.nar.demo.model.marina;

import java.util.EventObject;

public abstract class MarinaEvent extends EventObject {

	public MarinaEvent(Marina marina, Object source) {
		super(source);
		this.marina = marina;
	}
	
	public Marina getMarina() { return marina; }
	
	private static final long serialVersionUID = 1L;

	private final Marina marina;

}
