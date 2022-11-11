package com.steatoda.nar;

import java.text.ParseException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.steatoda.nar.demo.model.boat.Boat;
import com.steatoda.nar.demo.model.person.Person;

public class NarGraphTest {

	@Test
	public void testAllOf() {
		NarGraph<Person.Field> fields = NarGraph.allOf(Person.Field.class);
		Assert.assertEquals("fields should contain all person fields", EnumSet.allOf(Person.Field.class), top(fields));
		Assert.assertTrue("fields shouldn't contain any boat subfields", fields.getGraph(Person.Field.boat, Boat.Field.class).isEmpty());
	}
	
	@Test
	public void testOfCollection() {
		NarGraph<Person.Field> fields = NarGraph.of(Arrays.asList(Person.Field.name, Person.Field.boat));
		Assert.assertEquals("fields should contain only 'name' and 'boat' fields", EnumSet.of(Person.Field.name, Person.Field.boat), top(fields));
		Assert.assertTrue("fields shouldn't contain any boat subfields", fields.getGraph(Person.Field.boat, Boat.Field.class).isEmpty());
	}
	
	@Test
	public void testBuilder_Flat() {
		NarGraph<Person.Field> fields = NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.name)
			.add(Person.Field.boat)
			.build();
		Assert.assertEquals("fields should contain only 'name' and 'boat' fields", EnumSet.of(Person.Field.name, Person.Field.boat), top(fields));
		Assert.assertTrue("fields shouldn't contain any boat subfields", fields.getGraph(Person.Field.boat, Boat.Field.class).isEmpty());
	}
	
	@Test
	public void testBuilder_Hierarhical() {
		NarGraph<Person.Field> fields = NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.name)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.add(Boat.Field.type)
				.build()
			)
			.build();
		Assert.assertEquals("fields should contain only 'name' and 'boat' fields", EnumSet.of(Person.Field.name, Person.Field.boat), top(fields));
		Assert.assertEquals("fields should contain only boat subfield 'name' and 'type'", EnumSet.of(Boat.Field.name, Boat.Field.type), top(fields.getGraph(Person.Field.boat, Boat.Field.class)));
	}

	@Test
	public void testBuilder_Multiple() {
		NarGraph<Person.Field> fields = NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.name)
			.add(Person.Field.boat)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.build()
			)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.type)
				.build()
			)
			.build();
		Assert.assertEquals("fields should contain only 'name' and 'boat' fields", EnumSet.of(Person.Field.name, Person.Field.boat), top(fields));
		Assert.assertEquals("fields should contain only boat subfield 'name' and 'type'", EnumSet.of(Boat.Field.name, Boat.Field.type), top(fields.getGraph(Person.Field.boat, Boat.Field.class)));
	}

	@Test
	public void testBuilder_Extend() {
		NarGraph<Person.Field> extension = NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.email)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.add(Boat.Field.type)
				.build()
			)
			.build();
		NarGraph<Person.Field> fields = NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.name)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.build()
			)
			.add(extension)
			.build();
		Assert.assertEquals("fields should contain only 'name', 'email' and 'boat' fields", EnumSet.of(Person.Field.name, Person.Field.email, Person.Field.boat), top(fields));
		Assert.assertEquals("fields should contain only boat subfield 'name' and 'type'", EnumSet.of(Boat.Field.name, Boat.Field.type), top(fields.getGraph(Person.Field.boat, Boat.Field.class)));
	}

	@Test
	public void testBuilder_PreExtend() {
		NarGraph<Person.Field> extension = NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.email)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.add(Boat.Field.type)
				.build()
			)
			.build();
		NarGraph<Person.Field> fields = NarGraph.Builder.of(Person.Field.class)
			.add(extension)
			.add(Person.Field.name)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.build()
			)
			.build();
		Assert.assertEquals("fields should contain only 'name', 'email' and 'boat' fields", EnumSet.of(Person.Field.name, Person.Field.email, Person.Field.boat), top(fields));
		Assert.assertEquals("fields should contain only boat subfield 'name' and 'type'", EnumSet.of(Boat.Field.name, Boat.Field.type), top(fields.getGraph(Person.Field.boat, Boat.Field.class)));
	}

	@Test
	public void testBuilder_PreExtend_Constructor() {
		NarGraph<Person.Field> extension = NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.email)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.add(Boat.Field.type)
				.build()
			)
			.build();
		NarGraph<Person.Field> fields = NarGraph.Builder.of(extension)
			.add(Person.Field.name)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.add(Boat.Field.skipper)
				.build()
			)
			.build();
		// extended correctly?
		Assert.assertEquals("fields should contain only 'name', 'email' and 'boat' fields", EnumSet.of(Person.Field.name, Person.Field.email, Person.Field.boat), top(fields));
		Assert.assertEquals("fields should contain only boat subfield 'name', 'type' and 'skipper'", EnumSet.of(Boat.Field.name, Boat.Field.type, Boat.Field.skipper), top(fields.getGraph(Person.Field.boat, Boat.Field.class)));
		// extension should remain same
		Assert.assertEquals("extension should contain only 'email' and 'boat'", EnumSet.of(Person.Field.email, Person.Field.boat), top(extension));
		Assert.assertEquals("extension should contain only boat subfield 'name' and 'type'", EnumSet.of(Boat.Field.name, Boat.Field.type), top(extension.getGraph(Person.Field.boat, Boat.Field.class)));
	}

	@Test
	public void testBuilder_ExtendWithStringFieldSet() throws ParseException {
		NarGraph<Person.Field> extension = NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.email)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.add(Boat.Field.type)
				.build()
			)
			.build();
		NarGraph<Person.Field> fields = NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.name)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.build()
			)
			.add(NarGraph.of(extension.toString(), Person.Field.class))
			.build();
		Assert.assertEquals("fields should contain only 'name', 'email' and 'boat' fields", EnumSet.of(Person.Field.name, Person.Field.email, Person.Field.boat), top(fields));
		Assert.assertEquals("fields should contain only boat subfield 'name' and 'type'", EnumSet.of(Boat.Field.name, Boat.Field.type), top(fields.getGraph(Person.Field.boat, Boat.Field.class)));
	}

	@Test
	public void testBuilder_PreExtendWithStringFieldSet() throws ParseException {
		NarGraph<Person.Field> extension = NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.email)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.add(Boat.Field.type)
				.build()
			)
			.build();
		NarGraph<Person.Field> fields = NarGraph.Builder.of(Person.Field.class)
			.add(NarGraph.of(extension.toString(), Person.Field.class))
			.add(Person.Field.name)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.build()
			)
			.build();
		Assert.assertEquals("fields should contain only 'name', 'email' and 'boat' fields", EnumSet.of(Person.Field.name, Person.Field.email, Person.Field.boat), top(fields));
		Assert.assertEquals("fields should contain only boat subfield 'name' and 'type'", EnumSet.of(Boat.Field.name, Boat.Field.type), top(fields.getGraph(Person.Field.boat, Boat.Field.class)));
	}

	@Test
	public void testBuilder_remove() {
		NarGraph<Person.Field> fields = NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.name)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.build()
			)
			.remove(Person.Field.boat)
			.build();
		Assert.assertEquals("fields should contain only 'name' field", EnumSet.of(Person.Field.name), top(fields));
	}

	@Test
	public void testBuilder_remove2steps() {
		NarGraph<Person.Field> fields1 = NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.name)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.build()
			)
			.build();
		NarGraph<Person.Field> fields2 = NarGraph.Builder.of(fields1)
			.remove(Person.Field.boat)
			.build();
		Assert.assertEquals("fields1 should contain only 'name' and 'boat' fields", EnumSet.of(Person.Field.name, Person.Field.boat), top(fields1));
		Assert.assertEquals("fields2 should contain only 'name'", EnumSet.of(Person.Field.name), top(fields2));
	}

	@Test
	public void testToString_Empty() {
		String str = NarGraph.noneOf(Person.Field.class).toString();
		Assert.assertEquals("Empty FieldSet should serialize to empty string", "", str);
	}

	@Test
	public void testFromString_Null() throws ParseException {
		NarGraph<Person.Field> fields = NarGraph.of(null, Person.Field.class);
		Assert.assertEquals("fields should be empty", EnumSet.noneOf(Person.Field.class), top(fields));
	}

	@Test
	public void testFromString_Empty() throws ParseException {
		NarGraph<Person.Field> fields = NarGraph.of("", Person.Field.class);
		Assert.assertEquals("fields should be empty", EnumSet.noneOf(Person.Field.class), top(fields));
	}

	@Test
	public void testFromString_Blank() throws ParseException {
		NarGraph<Person.Field> fields = NarGraph.of("   ", Person.Field.class);
		Assert.assertEquals("fields should be empty", EnumSet.noneOf(Person.Field.class), top(fields));
	}

	@Test
	public void testFromString_Flat() throws ParseException {
		String str = "name,email,boat";
		NarGraph<Person.Field> fields = NarGraph.of(str, Person.Field.class);
		Assert.assertEquals("fields should contain only 'name', 'email' and 'boat' fields", EnumSet.of(Person.Field.name, Person.Field.email, Person.Field.boat), top(fields));
		Assert.assertTrue("fields shouldn't contain any boat subfields", fields.getGraph(Person.Field.boat, Boat.Field.class).isEmpty());
	}

	@Test
	public void testFromString_Hierarhical_First() throws ParseException {
		String str = "boat{name,type},name,email";
		NarGraph<Person.Field> fields = NarGraph.of(str, Person.Field.class);
		Assert.assertEquals("fields should contain only 'name', 'email' and 'boat' fields", EnumSet.of(Person.Field.name, Person.Field.email, Person.Field.boat), top(fields));
		Assert.assertEquals("fields should contain only boat subfield 'name' and 'type'", EnumSet.of(Boat.Field.name, Boat.Field.type), top(fields.getGraph(Person.Field.boat, Boat.Field.class)));
	}
	
	@Test
	public void testFromString_Hierarhical_Middle() throws ParseException {
		String str = "name,boat{name,type},email";
		NarGraph<Person.Field> fields = NarGraph.of(str, Person.Field.class);
		Assert.assertEquals("fields should contain only 'name', 'email' and 'boat' fields", EnumSet.of(Person.Field.name, Person.Field.email, Person.Field.boat), top(fields));
		Assert.assertEquals("fields should contain only boat subfield 'name' and 'type'", EnumSet.of(Boat.Field.name, Boat.Field.type), top(fields.getGraph(Person.Field.boat, Boat.Field.class)));
	}

	@Test
	public void testFromString_Hierarhical_Last() throws ParseException {
		String str = "name,email,boat{name,type}";
		NarGraph<Person.Field> fields = NarGraph.of(str, Person.Field.class);
		Assert.assertEquals("fields should contain only 'name', 'email' and 'boat' fields", EnumSet.of(Person.Field.name, Person.Field.email, Person.Field.boat), top(fields));
		Assert.assertEquals("fields should contain only boat subfield 'name' and 'type'", EnumSet.of(Boat.Field.name, Boat.Field.type), top(fields.getGraph(Person.Field.boat, Boat.Field.class)));
	}

	@Test
	public void testFromStringToString() throws ParseException {
		NarGraph<Person.Field> fields1 = NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.name)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.add(Boat.Field.type)
				.build()
			)
			.build();
		String str = fields1.toString();
		NarGraph<Person.Field> fields2 = NarGraph.of(str, Person.Field.class);
		Assert.assertEquals("fields should be equal", fields1, fields2);
		Assert.assertEquals("boat subfields should be equal", fields1.getGraph(Person.Field.boat, Boat.Field.class), fields2.getGraph(Person.Field.boat, Boat.Field.class));
	}

	@Test
	public void testFromString_Error_EmptyField_Start() {
		String str = ",name,boat{name,type},email";
		try {
			NarGraph.of(str, Person.Field.class);
			Assert.fail("Should have throw ParseException");
		} catch (ParseException ignored) {
		}
	}

	@Test
	public void testFromString_Error_EmptyField_Middle() {
		String str = "name,,boat{name,type},email";
		try {
			NarGraph.of(str, Person.Field.class);
			Assert.fail("Should have throw ParseException");
		} catch (ParseException ignored) {
		}
	}

	@Test
	public void testFromString_Error_EmptyField_End() {
		String str = "name,boat{name,type},email,";
		try {
			NarGraph.of(str, Person.Field.class);
			Assert.fail("Should have throw ParseException");
		} catch (ParseException ignored) {
		}
	}

	@Test
	public void testFromString_Error_DoubleStart() {
		String str = "name,boat{{name,type},email";
		try {
			NarGraph.of(str, Person.Field.class);
			Assert.fail("Should have throw ParseException");
		} catch (ParseException ignored) {
		}
	}

	@Test
	public void testFromString_Error_DoubleEnd() {
		String str = "name,boat{name,type}},email";
		try {
			NarGraph.of(str, Person.Field.class);
			Assert.fail("Should have throw ParseException");
		} catch (ParseException ignored) {
		}
	}

	@Test
	public void testFromString_Error_UnclosedStart() {
		String str = "name,boat{name,type},email{";
		try {
			NarGraph.of(str, Person.Field.class);
			Assert.fail("Should have throw ParseException");
		} catch (ParseException ignored) {
		}
	}

	@Test
	public void testFromString_Error_UnstartedEnd() {
		String str = "name,boat{name,type},email}";
		try {
			NarGraph.of(str, Person.Field.class);
			Assert.fail("Should have throw ParseException");
		} catch (ParseException ignored) {
		}
	}

	@Test
	public void testFromString_Error_InvalidType() {
		String str = "name,boat{name,email},email";
		try {
			NarGraph.of(str, Person.Field.class);
			Assert.fail("Should have throw UnknownFieldException");
		} catch (UnknownFieldException e) {
			Assert.assertEquals("UnknownFieldException reported wrong error value", "email", e.getValue());
			Assert.assertEquals("UnknownFieldException reported wrong class", Boat.Field.class, e.getClazz());
			Assert.assertEquals("UnknownFieldException reported wrong error offset", 15, e.getErrorOffset());
		} catch (ParseException e) {
			throw new AssertionError("Unknown ParseException throws", e);
		}
	}

	@Test
	public void testEquals() {
		NarGraph<Person.Field> fields1 = NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.email)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.add(Boat.Field.type)
				.build()
			)
			.build();
		NarGraph<Person.Field> fields2 = NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.email)
			.add(Person.Field.boat, NarGraph.Builder.of(Boat.Field.class)
				.add(Boat.Field.name)
				.add(Boat.Field.type)
				.build()
			)
			.build();
		NarGraph<Person.Field> fields3 = NarGraph.Builder.of(Person.Field.class)
			.add(Person.Field.email)
			.add(Person.Field.boat)
			.build();
		NarGraph<Person.Field> fields4 = NarGraph.of(Person.Field.email, Person.Field.boat);
		Assert.assertEquals("fields 1 and 2 should be equal", fields1, fields2);
		Assert.assertNotEquals("fields 1 and 3 should NOT be equal", fields1, fields3);
		Assert.assertEquals("fields 3 and 4 should be equal", fields3, fields4);
	}

	private <F extends Enum<F> & NarField> Set<F> top(NarGraph<F> fields) {
		return new HashSet<>(fields);
	}
	
}
