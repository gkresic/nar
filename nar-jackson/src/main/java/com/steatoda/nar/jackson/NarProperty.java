package com.steatoda.nar.jackson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to associate getters and setters with certain fields. See {@link NarPropertyFilter} for example usage.
 *
 * @see NarPropertyFilter
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface NarProperty {

	String value();

}
