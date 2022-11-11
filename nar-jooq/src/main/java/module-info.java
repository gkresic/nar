/**
 * <p>This module provides base implementations of several Nar services using <a href="https://www.jooq.org/">jOOQ</a>.</p>
 */
module com.steatoda.nar.jooq {

	exports com.steatoda.nar.jooq.service;
	exports com.steatoda.nar.jooq.service.crud;

	requires org.jooq;
	requires com.google.common;
	requires com.steatoda.nar;

}