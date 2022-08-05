module com.steatoda.nar.demo {

	requires com.steatoda.nar;
	requires com.steatoda.nar.jackson;

	requires org.slf4j;
	requires org.apache.commons.text;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.annotation;
	requires com.google.common;

	opens com.steatoda.nar.demo.model to com.fasterxml.jackson.databind;
	opens com.steatoda.nar.demo.model.berth to com.fasterxml.jackson.databind;
	opens com.steatoda.nar.demo.model.boat to com.fasterxml.jackson.databind;
	opens com.steatoda.nar.demo.model.marina to com.fasterxml.jackson.databind, com.google.common;
	opens com.steatoda.nar.demo.model.person to com.fasterxml.jackson.databind;

}