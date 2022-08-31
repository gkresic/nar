package com.steatoda.nar.example.complex_key;

import com.steatoda.nar.FieldUnavailableException;
import com.steatoda.nar.NarEntityBase;
import com.steatoda.nar.NarField;
import com.steatoda.nar.NarGraph;
import com.steatoda.nar.example.first_object.City;
import com.steatoda.nar.example.first_object.Country;

public class Budget extends NarEntityBase<Budget.Key, Budget, Budget.Field> {

	public static class Key {

		public Key(String countryId, String cityId) {
			this.countryId = countryId;
			this.cityId = cityId;
		}

		public String getCountryId() { return countryId; }
		public String getCityId() { return cityId; }

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + countryId.hashCode();
			result = prime * result + cityId.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Key))
				return false;
			Key other = (Key) obj;
			return countryId.equals(other.countryId) && cityId.equals(other.cityId);
		}

		private final String countryId;
		private final String cityId;

	}

	public enum Field implements NarField {

		country		(Country.Field.class),
		city		(City.Field.class),
		amount;

		Field() { this(null); }
		<F extends Enum<F> & NarField> Field(Class<F> clazz) { this.clazz = clazz; }
		@Override
		@SuppressWarnings("unchecked")
		public <F extends Enum<F> & NarField> Class<F> getNarFieldClass() { return (Class<F>) clazz; }
		private final Class<?> clazz;

	}

	public static Budget ref(Key id) {
		return new Budget().setId(id);
	}

	public Budget() {
		super(Field.class);
	}

	public Country getCountry() { return fieldGet(Field.country, country); }
	public Budget setCountry(Country country) { this.country = fieldSet(Field.country, country); return this; }

	public City getCity() { return fieldGet(Field.city, city); }
	public Budget setCity(City city) { this.city = fieldSet(Field.city, city); return this; }

	public Integer getAmount() { return fieldGet(Field.amount, amount); }
	public Budget setAmount(Integer amount) { this.amount = fieldSet(Field.amount, amount); return this; }

	@Override
	public Object pull(Field field, Budget other, NarGraph<Field> graph) {
		switch (field) {
			case country:	return pull(other, other::getCountry,	this::setCountry,	value -> value.clone(field, graph));
			case city:		return pull(other, other::getCity,		this::setCity,		value -> value.clone(field, graph));
			case amount:	return pull(other, other::getAmount,	this::setAmount);
		}
		throw new FieldUnavailableException(field);
	}

	@Override
	public Budget ref() {
		return ref(getId());
	}

	private Country country;
	private City city;
	private Integer amount;

}
