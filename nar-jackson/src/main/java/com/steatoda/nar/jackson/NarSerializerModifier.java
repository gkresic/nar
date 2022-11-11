package com.steatoda.nar.jackson;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

/**
 * Serializer modifier that ensures {@code ID} property is serialized first and
 * {@code fields} property (if any) is serialized <u>last</u>.
 */
public class NarSerializerModifier extends BeanSerializerModifier {

	/**
	 * Constructs {@code FieldsSerializerModifier} using {@code propertyNameId} as name for property holding entity's ID.
	 *
	 * @param propertyNameId name for property holding entity's ID
	 */
	public NarSerializerModifier(String propertyNameId) {
		this(propertyNameId, null);
	}

	/**
	 * Constructs {@code FieldsSerializerModifier} using {@code propertyNameId} as name for property holding entity's ID
	 * and {@code propertyNameFields} as name for property holding entity's fields set.
	 *
	 * @param propertyNameId name for property holding entity's ID
	 * @param propertyNameFields name for property holding entity's fields set
	 */
	public NarSerializerModifier(String propertyNameId, String propertyNameFields) {
		this.comparator = new FieldsComparator(propertyNameId, propertyNameFields);
	}

	@Override
	public List<BeanPropertyWriter> orderProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
		beanProperties.sort(comparator);
		return super.orderProperties(config, beanDesc, beanProperties);
	}

	private final FieldsComparator comparator;

	private static class FieldsComparator implements Comparator<BeanPropertyWriter> {

		private FieldsComparator(String propertyNameId, String propertyNameFields) {
			order.put(propertyNameId, -1);
			if (propertyNameFields != null)
				order.put(propertyNameFields, Integer.MAX_VALUE);
		}

		@Override
		public int compare(BeanPropertyWriter p1, BeanPropertyWriter p2) {
			return Optional.ofNullable(order.get(p1.getName())).orElse(0).compareTo(Optional.ofNullable(order.get(p2.getName())).orElse(0));
		}

		/** Defines property ordering. Non-specified properties will have order of 0. */
		private final Map<String, Integer> order = new HashMap<>();

	}

}
