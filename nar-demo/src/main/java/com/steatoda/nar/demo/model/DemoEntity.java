package com.steatoda.nar.demo.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.steatoda.nar.NarField;
import com.steatoda.nar.NarEntityBase;
import com.steatoda.nar.jackson.NarPropertyFilter;

@JsonFilter(NarPropertyFilter.Name)
public abstract class DemoEntity<I, C extends DemoEntity<I, C, F>, F extends Enum<F> & NarField> extends NarEntityBase<I, C, F> {

	public static final String JsonPropertyId = "id";

	protected DemoEntity(Class<F> fieldsClass) {
		super(fieldsClass);
	}

	@Override
	@JsonProperty(JsonPropertyId)
	public I getId() {
		return super.getId();
	}

}
