/**
 * <p>This module provides <a href="https://github.com/FasterXML/jackson">Jackson</a> integration for Nar entities.</p>
 */
module com.steatoda.nar.jackson {

	exports com.steatoda.nar.jackson;

	requires com.steatoda.nar;

	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;

}