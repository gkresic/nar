package com.steatoda.nar;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.steatoda.nar.service.NarService;

/**
 * Interface that gives implementing classes basic fields operations.
 *
 * @param <C> class implementing this {@link NarObject}
 * @param <F> field type
 */
public interface NarObject<C extends NarObject<C, F>, F extends Enum<F> & NarField> {

	/**
	 * @return reference to this object (copy with no fields set, only ID (if object has it) and other properties not managed by fields)
	 */
	C ref();

	/**
	 * @return fields initialized within this object
	 */
	Set<F> getFields();

	/**
	 * Sets which fields are initialized within this object.
	 *
	 * @param fields fields initialized within this object
	 */
	void setFields(Set<F> fields);

	/**
	 * Checks if all requested fields are set.
	 *
	 * @param fields fields to check
	 *
	 * @return {@code true} if all requested fields are set, {@code false} otherwise.
	 */
	@SuppressWarnings("unchecked")
	default boolean hasFields(F... fields) {
		return hasFields(Arrays.asList(fields));
	}

	/**
	 * Checks if all requested fields are set.
	 *
	 * @param fields fields to check
	 *
	 * @return {@code true} if all requested fields are set, {@code false} otherwise.
	 */
	default boolean hasFields(Collection<F> fields) {
		return getFields().containsAll(fields);
	}

	/**
	 * Checks if all requested fields are set.
	 *
	 * @param fields fields to check (in string form, parsed using {@link #parseField(String)})
	 *
	 * @return {@code true} if all requested fields are set, {@code false} otherwise
	 *
	 * @throws IllegalArgumentException if any of specified fields can't be parsed as enum of type returned by {@link #getFieldsClass()}
	 */
	default boolean hasFieldsAsString(String... fields) {
		return hasFieldsAsString(Arrays.asList(fields));
	}

	/**
	 * Checks if all requested fields are set.
	 *
	 * @param fields fields to check (in string form, parsed using {@link #parseField(String)})
	 *
	 * @return {@code true} if all requested fields are set, {@code false} otherwise
	 *
	 * @throws IllegalArgumentException if any of specified fields can't be parsed as enum of type returned by {@link #getFieldsClass()}
	 */
	default boolean hasFieldsAsString(Collection<String> fields) {
		return hasFields(fields.stream().map(this::parseField).collect(Collectors.toSet()));
	}

	/**
	 * Checks if whole field graph is set.
	 *
	 * @param graph field graph to check
	 *
	 * @return {@code true} if all requested fields are set together with their subfields, {@code false} otherwise
	 */
	default boolean hasGraph(NarGraph<F> graph) {
		return getMissingGraph(graph).isEmpty();
	}

	/**
	 * Returns enum class used to represent fields.
	 *
	 * @return enum class used to represent fields
	 */
	Class<F> getFieldsClass();

	/**
	 * Parses {@code str} as field enum
	 *
	 * @param str string value to parse
	 *
	 * @return field of type F
	 *
	 * @throws IllegalArgumentException if specified value can't be parsed as enum of type returned by {@link #getFieldsClass()}
	 */
	default F parseField(String str) {
		return Enum.valueOf(getFieldsClass(), str);
	}

	/**
	 * <p>Retrieves value for requested field.</p>
	 * 
	 * @param field field for which value is requested
	 * 
	 * @return requested field's value.
	*/
	@SuppressWarnings("unchecked")
	default Object getFieldValue(F field) {
		// piggyback on pull (from self)
		return pull(field, (C) this, NarGraph.noneOf(getFieldsClass()));
	}

	/**
	 * <p>Retrieves value (via provided supplier) but only if all requested fields are set.
	 * Circumvents {@link FieldUnavailableException} for uninitialized fields.</p>
	 *
	 * @param supplier supplier for given value
	 * @param fields field(s) to check for presence
	 * @param <X> value's type
	 *
	 * @return requested field's value.
	 */
	@SuppressWarnings("unchecked")
	default <X> X getIfPresent(Supplier<X> supplier, F... fields) {
		return getIfPresent(supplier, Arrays.asList(fields));
	}
	
	/**
	 * <p>Retrieves value (via provided supplier) but only if all requested fields are set.
	 * Circumvents {@link FieldUnavailableException} for uninitialized fields.</p>
	 * 
	 * @param supplier supplier for given value
	 * @param fields fields to check for presence
	 * @param <X> value's type
	 *
	 * @return requested field's value.
	*/
	default <X> X getIfPresent(Supplier<X> supplier, Collection<F> fields) {
		return hasFields(fields) ? supplier.get() : null;
	}

	/**
	 * <p>Retrieves value (via provided supplier) as {@link Optional} but only if all requested fields are set.
	 * Circumvents {@link FieldUnavailableException} for uninitialized fields.</p>
	 *
	 * @param supplier supplier for given value
	 * @param fields field(s) to check for presence
	 * @param <X> value's type
	 *
	 * @return {@link Optional} holding requested field's value.
	 */
	@SuppressWarnings("unchecked")
	default <X> Optional<X> getOptional(Supplier<X> supplier, F... fields) {
		return Optional.ofNullable(getIfPresent(supplier, fields));
	}

	/**
	 * <p>Retrieves value (via provided supplier) as {@link Optional} but only if all requested fields are set.
	 * Circumvents {@link FieldUnavailableException} for uninitialized fields.</p>
	 *
	 * @param supplier supplier for given value
	 * @param fields field(s) to check for presence
	 * @param <X> value's type
	 *
	 * @return {@link Optional} holding requested field's value.
	 */
	default <X> Optional<X> getOptional(Supplier<X> supplier, Collection<F> fields) {
		return Optional.ofNullable(getIfPresent(supplier, fields));
	}

	/**
	 * <p>Helper method that throws {@link FieldUnavailableException} if field is not initialized.</p>
	 * 
	 * <p>Use in <i>getters</i> like:</p>
	 * 
	 * <blockquote><pre>
	 * public String getFoo() {
	 * 	return fieldGet(Field.foo, foo);
	 * }
	 * </pre></blockquote>
	 * 
	 * <p><b>WARNING:</b> Make sure such <i>getters</i> are not called by any reflection-based mechanism.</p>
	 * 
	 * @param field field to check
	 * @param value current value that represents field
	 * @param <V> value's type
	 *
	 * @return <code>value</code> if field is initialized
	 * 
	 * @throws FieldUnavailableException if field is not initialized
	 */
	default <V> V fieldGet(F field, V value) {
		if (!getFields().contains(field))
			throw new FieldUnavailableException(field);
		return value;
	}

	/**
	 * <p>Helper method that initializes fields set before returning {@code value}.</p>
	 *
	 * <p>Use in <i>setters</i> like:</p>
	 *
	 * <blockquote><pre>
	 * public void setFoo(String foo) {
	 * 	this.foo = fieldSet(Field.foo, foo);
	 * }
	 * </pre></blockquote>
	 *
	 * @param field field to initialize
	 * @param value return value
	 * @param <V> value's type
	 *
	 * @return {@code value}
	 */
	default <V> V fieldSet(F field, V value) {
		getFields().add(field);
		return value;
	}

	/**
	 * Clears fields and sets their corresponding values to {@code null}.
	 *
	 * @param fields fields to clear
	 *
	 * @return {@code true} if any field was cleared, {@code false} otherwise
	*/
	@SuppressWarnings("unchecked")
	default boolean clearFields(F... fields) {
		return clearFields(Arrays.asList(fields));
	}
	
	/**
	 * Clears fields and sets their corresponding values to {@code null}.
	 *
	 * @param fields fields to clear
	 *
	 * @return {@code true} if any field was cleared, {@code false} otherwise
	 */
	default boolean clearFields(Collection<F> fields) {
		// nullify by pulling from dummy
		C dummy = ref();
		dummy.getFields().addAll(fields);	// initialize fields so that possible checks in getter do not fail
		for (F field : fields)
			if (getFields().contains(field))
				pull(field, dummy, NarGraph.of(field));
		return getFields().removeAll(fields);
	}
	
	/** Strips sub-objects to references */
	default void flatten() {
		NarGraph<F> flatGraph = NarGraph.of(getFields());
		C flatClone = cloneFlat();	// have to clone, because pull doesn't change value if pulling from self
		for (F field : getFields())
			pull(field, flatClone, flatGraph);
	}
	
	/**
	 * <p>Copies value represented by given field from another object.</p>
	 *
	 * <p>Should obey following rules:</p>
	 * <ul>
	 * 	<li>if {@code other == this}, don't copy anything, just return current value</li>
	 * 	<li>if {@code graph == null}, pull whole sub-tree for every sub-object</li>
	 * </ul>
	 *
	 * <p>Implementations usually use {@link #pull(NarObject, Supplier, Consumer)} or {@link #pull(NarObject, Supplier, Consumer, Function)} when providing own functionality, like:</p>
	 * <blockquote><pre>
	 * &#64;Override
	 * public Object pull(Field field, Foo other, NarGraph&lt;Field&gt; graph) {
	 * 	switch (field) {
	 * 		case scalar:	return pull(other, other::getScalar,	this::setScalar);
	 * 		case list:		return pull(other, other::getList,		this::setList,		ArrayList::new);
	 * 		case object:	return pull(other, other::getObject,	this::setObject,	value -&gt; value.clone(field, graph));
	 * 	}
	 * 	throw new FieldUnavailableException(field);
	 * }
	 * </pre></blockquote>
	 *
	 * @param field field to pull from {@code other}
	 * @param other object to pull {@code field}'s value from
	 * @param graph field's graph of <u>this</u> object that may or may not hold subgraph to pull
	 *
	 * @return new field value
	 */
	Object pull(F field, C other, NarGraph<F> graph);

	/**
	 * <p>Utility method to implement {@link #pull(Enum, NarObject, NarGraph)}.</p>
	 * <p>NOTE: Doesn't invoke {@code consumer} if {@code other == this}.</p>

	 * @param other object to copy (pull) value from
	 * @param supplier supplier that extracts value to copy from {@code other}
	 * @param consumer consumer that sets value into this object
	 * @param <X> type of value copied

	 * @return copied value
	 */
	default <X> X pull(C other, Supplier<X> supplier, Consumer<X> consumer) {
		return pull(other, supplier, consumer, null);
	}

	/**
	 * <p>Utility method to implement {@link #pull(Enum, NarObject, NarGraph)}.</p>
	 * <p>NOTE: Doesn't invoke {@code consumer} if {@code other == this}.</p>

	 * @param other object to copy (pull) value from
	 * @param supplier supplier that extracts value to copy from {@code other}
	 * @param consumer consumer that sets value into this object
	 * @param mapper function that process value durins copying (e.g. clones it as new instance)
	 * @param <X> type of value copied

	 * @return copied value
	 */
	default <X> X pull(C other, Supplier<X> supplier, Consumer<X> consumer, Function<X, X> mapper) {
		X value = supplier.get();
		if (other != this) {
			if (mapper != null && value != null)
				value = mapper.apply(value);
			consumer.accept(value);
		}
		return value;
	}
	
	/**
	 * <p>(Deep)Pulls from {@code other} all fields it has initialized.</p>
	 * 
	 * <p><b>NOTE:</b> Sub-object's graphs may change if this object and {@code other} has fields in common, but with different sub-graphs resolved.
	 * If graph consistency is required, consider extending object using {@link NarEntity#extend} immediately after using this method.</p>
	 * 
	 * @param other object to pull fields from
	 * 
	 */
	default void pull(C other) {
		pull(other, other.getFields());
	}
	
	/**
	 * <p>(Deep)Pulls requested fields from another object.</p>
	 * 
	 * <p><b>NOTE:</b> Object graph may change if this object and {@code other} had fields in common, but with different sub-graphs.
	 * If graph consistency is required, consider {@link NarEntity#extend(NarGraph, NarService)}-ing object immediately
	 * after using this method.</p>
	 * 
	 * @param other object to pull values from
	 * 
	 * @param fields fields to pull
	 */
	default void pull(C other, Set<F> fields) {
		for (F field : fields)
			pull(field, other, null);
	}
	
	/**
	 * <p>Returns object's copy with exact fields as this object.</p>
	 * <p>NOTE better name would be simply {@code clone()}, but Java will complain (in Java, interfaces can't override methods from superclass).
	 * {@code clone()} is therefore implemented in {@link NarObjectBase}.</p>
	 *
	 * @return object's copy
	 */
	@SuppressWarnings("unchecked")
	default C cloneAll() {
		C clone = ref();
		clone.pull((C) this, getFields());
		return clone;
	}

	/**
	 * Returns object's copy with exact fields as this object, but with all sub-objects striped to references.
	 *
	 * @return object's copy
	 */
	default C cloneFlat() {
		return clone(NarGraph.of(getFields()));
	}

	/**
	 * Returns object's copy with only requested fields initialized.
	 *
	 * @param graph field graph to clone
	 *
	 * @return object's copy
	 *
	 * @throws FieldUnavailableException if object doesn't have all fields requested
	 */
	@SuppressWarnings("unchecked")
	default C clone(NarGraph<F> graph) {
		if (!getFields().containsAll(graph))
			throw new FieldUnavailableException(Sets.difference(graph, getFields()));
		C clone = ref();
		for (F field : graph)
			clone.pull(field, (C) this, graph);
		return clone;
	}

	/**
	 * Clones by picking graph from <u>parent's</u> graph. Usually used inside {@link #pull(Enum, NarObject, NarGraph)}
	 * implementations to pull subobjects.
	 * 
	 * @param field parent's field describing this object
	 * @param graph parent's graph from where own fields should be extracted (if {@code null} {@link #cloneAll()} is used)
	 * @param <F2> parent's graph field type
	 *
	 * @return object's copy
	 *
	 * @throws FieldUnavailableException if object doesn't have all fields requested
	 */
	default <F2 extends Enum<F2> & NarField> C clone(F2 field, NarGraph<F2> graph) {
		if (graph == null)
			return cloneAll();
		return clone(graph.getGraph(field, getFieldsClass())); 
	}

	/**
	 * <p>Extends this object with all values initialized in {@code extensionRaw}.</p>
	 * <p><b>INTERNAL</b></p>
	 *
	 * @param extensionRaw object to pull values from
	 * @param graphRaw fields graph from which to extract subgraph for pulled values
	 *
	 * @throws ClassCastException if {@code extensionRaw} is not instance of same class as this object or {@code graphRaw}'s
	 * 	first-level fields are not of same type as this object's fields
	 * @throws EntityUnavailableException if {@code extensionRaw} doesn't have any of fields specified by {@code graphRaw}
	 */
	@SuppressWarnings("unchecked")
	default void _extend(Object extensionRaw, NarGraph<?> graphRaw) throws EntityUnavailableException {
		
		// Java type erasure...
		C extension = (C) extensionRaw;
		NarGraph<F> graph = (NarGraph<F>) graphRaw;
		
		for (F field : extension.getFields())
			extend(field, extension, graph);
		
	}

	/**
	 * Recursively extends this objects from {@code extension}, but pulling only requested field's subgraph.
	 *
	 * @param field field to pull from {@code extension}
	 * @param extension object to pull {@code field} from
	 * @param graph fields graph from which to extract subgraph for requested field
	 *
	 * @throws EntityUnavailableException if {@code extension} doesn't have any of fields specified by {@code graph}
	 */
	@SuppressWarnings("unchecked")
	default void extend(F field, C extension, NarGraph<F> graph) throws EntityUnavailableException {

		if (!hasFields(field)) {
			// we are completely missing this field, pull it with whole sub-graph
			pull(field, extension, graph);
			return;
		}
		
		// we have this field...

		NarGraph<?> subGraphRaw = graph.getGraph(field);

		if (subGraphRaw == null)
			return;

		// we have this field, but sub-graph is requested - do we have it all?

		Object subObject = getFieldValue(field);
		Object subExtension = extension.getFieldValue(field);

		_extendRecursively(subObject, subExtension, field, graph);
		
	}

	/**
	 * <p>Recursively extends {@code subObject} by pulling {@code field}'s value from {@code subExtension}. If {@code subObject}
	 * represents {@link Collection}, descends extension to all members. If {@code subObject} represents {@link Map},
	 * descends extension to all keys and values (if they implement {@link NarObject}).</p>
	 *
	 * <p><b>INTERNAL</b></p>
	 *
	 * @param subObject object to extend
	 * @param subExtension extension from which to pull values (may be instance of {@link NarObject}, but also {@link Collection}
	 *                     or {@link Map} holding instance of required {@link NarObject}
	 * @param field field describing value to pull
	 * @param graph fields graph from which to extract subgraph for requested field
	 *
	 * @throws IllegalStateException if {@code subObject} doesn't represent instance/collection/map of {@link NarObject}
	 * @throws EntityUnavailableException if {@code subExtension} doesn't equal to {@code subObject} or doesn't contain {@code subObject} (for collections and maps)
	 */
	default void _extendRecursively(Object subObject, Object subExtension, F field, NarGraph<F> graph) throws EntityUnavailableException {

		if (subObject instanceof NarObject) {

			// simple sub-object
			
			NarObject<?, ?> fieldSubObject = (NarObject<?, ?>) subObject;
			NarObject<?, ?> fieldSubExtension = _search(fieldSubObject, subExtension);
			
			if (fieldSubExtension == null)
				throw new EntityUnavailableException(fieldSubObject);
			
			fieldSubObject._extend(fieldSubExtension, graph.getGraph(field, fieldSubObject.getFieldsClass()));
			
		} else if (subObject instanceof Collection) {

			// BEWARE: O(n^m) (n - size of largest collection, m - levels of nested collections)

			Collection<?> subCollection = (Collection<?>) subObject;

			if (subCollection.isEmpty())
				return;

			// append missing sub-graph for *every* member
			for (Object subItem : subCollection)
				_extendRecursively(subItem, subExtension, field, graph);

		} else if (subObject instanceof Map) {

			// BEWARE: O(n^2m) (n - size of largest collection, m - levels of nested collections)

			Map<?, ?> subMap = (Map<?, ?>) subObject;

			if (subMap.isEmpty())
				return;

			// extend sub-graph for *every* entry, trying to recurse into both keys and values (ignoring IllegalStateException(s)),
			// assuming either one is legal field-object
			// throw IllegalStateException only if neither key nor value could be extended
			boolean processKey = true;
			boolean processValue = true;

			for (Map.Entry<?, ?> subEntry : subMap.entrySet()) {
				if (processKey)
					try {
						_extendRecursively(subEntry.getKey(), subExtension, field, graph);
					} catch (IllegalStateException e) {
						processKey = false;	// no need to try again
					}
				if (processValue)
					try {
						_extendRecursively(subEntry.getValue(), subExtension, field, graph);
					} catch (IllegalStateException e) {
						processValue = false;	// no need to try again
					}
				if (!processKey && !processValue)
					throw new IllegalStateException("Field " + field + " of class " + getClass().getName() + " descends into a map whose nor key nor value doesn't support field access");
			}

		} else {
			
			// TODO arrays

			throw new IllegalStateException("Field " + field + " of class " + getClass().getName() + " is of un-extendable type " + subObject.getClass().getName());

		}

	}

	/**
	 * <p>Recursively searches for instance of {@code object} inside {@code tree} (using {@link Object#equals}). If {@code tree}
	 * represents {@link Collection}, descends search to all members. If {@code tree} represents {@link Map},
	 * descends search to all keys and values (if they implement {@link NarObject}).</p>
	 *
	 * <p>Performs DFS, but it will stop descending deeper after it encounters {@link NarObject} instance.
	 * Since all instances in tree at same level contain same fields, it doesn't matter which instance is found.</p>
	 *
	 * <p>TODO use BFS</p>
	 *
	 * <p><b>INTERNAL</b></p>

	 * @param object object to search for
	 * @param tree {@link NarObject}, {@link Collection} or {@link Map} where to search for {@code object}

	 * @return found {@code object}
	 *
	 * @throws IllegalStateException if {@code subObject} doesn't represent instance/collection/map of {@link NarObject}
	 */
	default NarObject<?, ?> _search(NarObject<?, ?> object, Object tree) {
		
		if (tree == null)
			return null;
		
		if (tree instanceof NarObject) {
			
			NarObject<?, ?> fieldTree = (NarObject<?, ?>) tree;
			
			if (object.equals(fieldTree))
				return fieldTree;
			
			return null;
		
		} else if (tree instanceof Collection) {

			Collection<?> treeCollection = (Collection<?>) tree;

			if (treeCollection.isEmpty())
				return null;

			for (Object treeItem : treeCollection) {
				NarObject<?, ?> item = _search(object, treeItem);
				if (item != null)
					return item;
			}
			
			return null;
				
		} else if (tree instanceof Map) {

			Map<?, ?> treeMap = (Map<?, ?>) tree;

			if (treeMap.isEmpty())
				return null;

			// search both keys and values (ignoring IllegalStateException(s)),
			// assuming either one is legal field-object
			// throw IllegalStateException only if neither key nor value could be processed
			boolean processKey = true;
			boolean processValue = true;

			for (Map.Entry<?, ?> treeEntry : treeMap.entrySet()) {
				NarObject<?, ?> item;
				if (processKey)
					try {
						item = _search(object, treeEntry.getKey());
						if (item != null)
							return item;
					} catch (IllegalStateException e) {
						processKey = false;	// no need to try again
					}
				if (processValue)
					try {
						item = _search(object, treeEntry.getValue());
						if (item != null)
							return item;
					} catch (IllegalStateException e) {
						processValue = false;	// no need to try again
					}
				if (!processKey && !processValue)
					throw new IllegalStateException("Tree " + tree + " descends into a map whose nor key nor value doesn't support field access");
			}
			
			return null;
				
		} else {

			// TODO arrays
			
			throw new IllegalStateException("Tree " + tree + " is of un-searchable type " + tree.getClass().getName());

		}

	}

	/**
	 * Calculates fields graph that consists of all fields (from this object and all sub-objects) specified by {@code graph},
	 * but which are not present in current object.
	 *
	 * @param graph requested graph
	 *
	 * @return field graph missing from current object
	 */
	default NarGraph<F> getMissingGraph(NarGraph<F> graph) {
		NarGraph.Builder<F> missingGraphBuilder = NarGraph.Builder.of(getFieldsClass());
		_appendMissingGraph(graph, missingGraphBuilder);
		return missingGraphBuilder.build();
	}

	/**
	 * <p>Appends (to {@code missingGraphBuilderRaw}) all fields present in {@code requestedGraphRaw} and missing in current object.</p>
	 *
	 * <p><b>INTERNAL</b></p>
	 *
	 * @param requestedGraphRaw (unchecked) field graph requested
	 * @param missingGraphBuilderRaw (unchecked) builder to append missing fields to
	 *
	 * @throws ClassCastException if {@code requestedGraphRaw}'s or {@code missingGraphBuilderRaw}'s first-level fields
	 * are not of same type as this object's fields
	 */
	@SuppressWarnings("unchecked")
	default void _appendMissingGraph(NarGraph<?> requestedGraphRaw, NarGraph.Builder<?> missingGraphBuilderRaw) {
		
		// Java type erasure...
		NarGraph<F> requestedGraph = (NarGraph<F>) requestedGraphRaw;
		NarGraph.Builder<F> missingGraphBuilder = (NarGraph.Builder<F>) missingGraphBuilderRaw;
		
		for (F field : requestedGraph)
			appendMissingGraph(field, requestedGraph, missingGraphBuilder);

	}
	
	/**
	 * <p>Appends (to {@code missingGraphBuilder}) this field (possibly with subgraph) if it is missing from current object.
	 * Descends recursively to sub-objects that implement {@link NarObject} and collections containing them.</p>
	 * 
	 * <p>Override if field requires special processing logic.</p>
	 * 
	 * @param field field to process
	 * @param requestedGraph field graph requested
	 * @param missingGraphBuilder builder to append missing fields to
	 */
	@SuppressWarnings("unchecked")
	default void appendMissingGraph(F field, NarGraph<F> requestedGraph, NarGraph.Builder<F> missingGraphBuilder) {

		NarGraph<?> subGraphRaw = requestedGraph.getGraph(field);
		
		if (!hasFields(field)) {
			// object is completely missing this field, append it with all subfields (if any)
			missingGraphBuilder.set(field, subGraphRaw);
			return;
		}

		// object has this field...

		if (subGraphRaw == null)
			return;

		// object has this field, but subgraph is requested - do we have it completely?

		Object subObject = getFieldValue(field);
		
		_appendMissingGraphRecursively(subObject, subGraphRaw, field, missingGraphBuilder);

	}

	/**
	 * <p><b>INTERNAL</b></p>
	 *
	 * @param subObject fields object to inspect for missing fields
	 * @param requestedSubGraphRaw field graph requested
	 * @param field field to look for
	 * @param missingGraphBuilder builder to append missing fields to
	 */
	default void _appendMissingGraphRecursively(Object subObject, NarGraph<?> requestedSubGraphRaw, F field, NarGraph.Builder<F> missingGraphBuilder) {

		if (subObject == null)
			return;	// object has this field, but it's null, so treat all field as present
		
		if (subObject instanceof NarObject) {
			
			// found sub-object to extend (this sub-object is "base" value for this field)
			
			NarObject<?, ?> fieldObject = (NarObject<?, ?>) subObject;
			NarGraph.Builder<?> missingSubGraphBuilder = NarGraph.Builder.of(fieldObject.getFieldsClass());
			fieldObject._appendMissingGraph(requestedSubGraphRaw, missingSubGraphBuilder);
			NarGraph<?> missingSubGraph = missingSubGraphBuilder.build();
			if (!missingSubGraph.isEmpty())
				missingGraphBuilder.add(field, missingSubGraph);	// part of sub-graph is missing, fetch this field with only missing sub-graph

		} else if (subObject instanceof Collection) {

			Collection<?> subCollection = (Collection<?>) subObject;

			if (subCollection.isEmpty())
				return;
			
			// append missing sub-graph for *every* member
			for (Object subItem : subCollection)
				_appendMissingGraphRecursively(subItem, requestedSubGraphRaw, field, missingGraphBuilder);

		} else if (subObject instanceof Map) {

			Map<?, ?> subMap = (Map<?, ?>) subObject;

			if (subMap.isEmpty())
				return;
			
			// append missing sub-graph for *every* entry, trying to recurse into both keys and values (ignoring IllegalStateException(s)),
			// assuming either one is legal field-object
			// throw IllegalStateException only if neither key nor value could be processed
			boolean processKey = true;
			boolean processValue = true;

			for (Map.Entry<?, ?> subEntry : subMap.entrySet()) {
				if (processKey)
					try {
						_appendMissingGraphRecursively(subEntry.getKey(), requestedSubGraphRaw, field, missingGraphBuilder);
					} catch (IllegalStateException e) {
						processKey = false;	// no need to try again
					}
				if (processValue)
					try {
						_appendMissingGraphRecursively(subEntry.getValue(), requestedSubGraphRaw, field, missingGraphBuilder);
					} catch (IllegalStateException e) {
						processValue = false;	// no need to try again
					}
				if (!processKey && !processValue)
					throw new IllegalStateException("Field " + field + " of class " + getClass().getName() + " descends into a map whose nor key nor value doesn't support field access, but callee requested it's fields: " + requestedSubGraphRaw);
			}

		} else {
		
			// TODO arrays
			
			throw new IllegalStateException("Field " + field + " of class " + getClass().getName() + " doesn't support field access, but callee requested it's fields: " + requestedSubGraphRaw);
			
		}

	}
	
	/**
	 * <p>(Deeply) intersects this object with {@code graph} ("strips" object from any fields not covered by given graph).</p>
	 * 
	 * @param graph graph to intersect this object with (may be either subset or superset of object's own graph)
	 */
	@SuppressWarnings("unchecked")
	default void intersect(NarGraph<F> graph) {
		
		if (getFields().isEmpty())
			return;	// nothing to intersect (need to bail out, so that following EnumSet.copyOf works)
		
		// iterate over a *copy* of fields, because inside the loop original graph may change
		for (F field : EnumSet.copyOf(getFields())) {
			
			if (!graph.contains(field)) {
				// strip this field
				clearFields(field);
				continue;
			}
			
			// ok, we'll keep this field

			if (field.getNarFieldClass() == null)
				continue;	// field represents regular (non-NarObject) value, no need to descend
			
			Object subObject = getFieldValue(field);

			if (subObject == null)
				continue;	// no need to descend any further

			_intersectRecursively(subObject, field, graph);
			
		}
		
	}

	/**
	 * <p><b>INTERNAL</b></p>
	 *
	 * @param subObject fields object to intersect
	 * @param field field describing subgraph inside {@code graph}
	 * @param graph field graph from which to extract subgraph
	 */
	default void _intersectRecursively(Object subObject, F field, NarGraph<F> graph) {

		if (subObject instanceof NarObject) {
			
			// simple sub-object
			NarObject<?, ?> fieldSubObject = (NarObject<?, ?>) subObject;
			fieldSubObject._intersectRaw(graph.getGraph(field));

		} else if (subObject instanceof Collection) {

			Collection<?> subCollection = (Collection<?>) subObject;

			// intersect *every* member
			for (Object subItem : subCollection)
				_intersectRecursively(subItem, field, graph);
			
		} else if (subObject instanceof Map) {

			Map<?, ?> subMap = (Map<?, ?>) subObject;

			// intersect *every* entry, trying to recurse into both keys and values (ignoring IllegalStateException(s)),
			// assuming either one is legal field-object
			// throw IllegalStateException only if neither key nor value could be intersected
			boolean processKey = true;
			boolean processValue = true;

			for (Map.Entry<?, ?> subEntry : subMap.entrySet()) {
				if (processKey)
					try {
						_intersectRecursively(subEntry.getKey(), field, graph);
					} catch (IllegalStateException e) {
						processKey = false;	// no need to try again
					}
				if (processValue)
					try {
						_intersectRecursively(subEntry.getValue(), field, graph);
					} catch (IllegalStateException e) {
						processValue = false;	// no need to try again
					}
				if (!processKey && !processValue)
					throw new IllegalStateException("Field " + field + " of class " + getClass().getName() + " descends into a map whose nor key nor value doesn't support field access");
			}
			
		} else {

			// TODO arrays
			
			throw new IllegalStateException("Field " + field + " of class " + getClass().getName() + " is of un-intersectable type " + subObject.getClass().getName());

		}

	}

	/**
	 * <p><b>INTERNAL</b></p>
	 *
	 * @param graphRaw field graph to intersect this object with
	 */
	@SuppressWarnings("unchecked")
	default void _intersectRaw(NarGraph<?> graphRaw) {
		// Java type erasure...
		NarGraph<F> graph = (NarGraph<F>) graphRaw;
		if (graph == null)
			graph = NarGraph.noneOf(getFieldsClass());
		intersect(graph);
	}

}
