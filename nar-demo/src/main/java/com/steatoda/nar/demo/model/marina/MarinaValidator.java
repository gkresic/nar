package com.steatoda.nar.demo.model.marina;

import org.apache.commons.lang3.StringUtils;

import com.steatoda.nar.NarGraph;

public class MarinaValidator {

	public static final NarGraph<Marina.Field> Graph = NarGraph.Builder.of(Marina.Field.class)
		.add(Marina.Field.name)
		.build();

	public void validate(Marina marina) {

		if (StringUtils.isBlank(marina.getIfPresent(marina::getName, Marina.Field.name)))
			throw new RuntimeException("Name is mandatory");
	
	}

}
