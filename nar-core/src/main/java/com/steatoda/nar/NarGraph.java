package com.steatoda.nar;

import java.text.ParseException;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Iterators;
import com.steatoda.nar.internal.RecursiveStringMap;

/**
 * <p>Defines complete object's field graph with exact subfields for each fields-enabled subobject.</p>
 * <p>Implements {@link Set} interface for first-level fields.</p>
 *
 * @param <F> Field enum (first-level fields)
 */
public class NarGraph<F extends Enum<F> & NarField> extends AbstractSet<F> {

	// builder-style factories

	/** Builder for constructing {@link NarGraph}(s) */
	public static class Builder<F extends Enum<F> & NarField> {

		/**
		 * Constructs empty {@link NarGraph} builder using {@code clazz} as first-level field type.
		 * @param clazz first-level field class type
		 * @param <F> first-level field type
		 * @return Empty builder
		 */
		public static <F extends Enum<F> & NarField> Builder<F> of(Class<F> clazz) {
			return new Builder<>(clazz);
		}

		/**
		 * Constructs {@link NarGraph} builder using {@code graph} as initial value.
		 * @param graph Initial graph to initialize
		 * @param <F> first-level field type
		 * @return Builder pre-initialized to {@code graph}
		 */
		public static <F extends Enum<F> & NarField> Builder<F> of(NarGraph<F> graph) {
			return new Builder<>(graph.getDeclaringClass()).add(graph);
		}

		private Builder(Class<F> clazz) {
			this.clazz = clazz;
			data = new EnumMap<>(clazz);
		}

		/**
		 * Removes given field from graph together with its complete subgraph.
		 * @param field field to remove
		 * @return Builder with field removed
		 */
		public Builder<F> remove(F field) {
			data.remove(field);
			return this;
		}

		/**
		 * Adds field to graph. If field describes field-enabled object, it's subgraph will be empty.
		 * @param field field to add
		 * @return Builder with field added
		 */
		public Builder<F> add(F field) {
			return add(field, null);
		}

		/**
		 * Adds field describing fields-enabled subobject to graph and initializes its subgraph.
		 * @param field field to add
		 * @param subgraph field's subgraph
		 * @param <F2> subgraph's field type
		 * @return Builder with field added
		 * @throws IllegalArgumentException if {@code field} defines object with fields of type other than {@code F2}
		 */
		public <F2 extends Enum<F2> & NarField> Builder<F> add(F field, NarGraph<F2> subgraph) {
			if (subgraph != null && !subgraph.getDeclaringClass().equals(field.getNarFieldClass()))
				throw new IllegalArgumentException("Trying to add sub-graph for field " + field + " of type " + subgraph.getDeclaringClass() + " but field declares sub-graph of type " + field.getNarFieldClass());
			extend(data, field, subgraph);
			return this;
		}

		/** NOTE: setting field <b>overwrites</b> existing subfields if field was already initialized */
		<F2 extends Enum<F2> & NarField> Builder<F> set(F field, NarGraph<F2> subgraph) {
			if (subgraph != null && !subgraph.getDeclaringClass().equals(field.getNarFieldClass()))
				throw new IllegalArgumentException("Trying to set sub-graph for field " + field + " of type " + subgraph.getDeclaringClass() + " but field declares sub-graph of type " + field.getNarFieldClass());
			data.put(field, subgraph);
			return this;
		}

		/**
		 * Extends builder (using {@link #extend(Map, NarGraph)}) with given fields.
		 * @param extension fields to extend builder with
		 * @return Builder with all requested fields added
		 */
		public Builder<F> add(Set<F> extension) {
			if (!extension.isEmpty()) {
				if (extension instanceof NarGraph)
					extend(data, (NarGraph<F>) extension);
				else
					extend(data, NarGraph.of(extension));
			}
			return this;
		}

		private static <F extends Enum<F> & NarField> void extend(Map<F, NarGraph<?>> data, NarGraph<F> extension) {
			for (F field : extension) {
				NarGraph<?> extensionSubset = extension.data.get(field);
				extend(data, field, extensionSubset);
			}
		}

		@SuppressWarnings("unchecked")
		private static <F extends Enum<F> & NarField, F2 extends Enum<F2> & NarField> void extend(Map<F, NarGraph<?>> data, F field, NarGraph<F2> subgraph) {
			if (subgraph != null && !subgraph.getDeclaringClass().equals(field.getNarFieldClass()))
				throw new IllegalArgumentException("Trying to extend sub-graph for field " + field + " with type " + subgraph.getDeclaringClass() + " but field declares sub-graph of type " + field.getNarFieldClass());
			NarGraph<F2> thisSubGraph = (NarGraph<F2>) data.get(field);
			NarGraph<F2> mergedSubGraph;
			if (thisSubGraph == null)
				mergedSubGraph = subgraph != null ? subgraph.clone() : null;
			else if (subgraph == null)
				mergedSubGraph = thisSubGraph;
			else {
				extend(thisSubGraph.data, subgraph);
				mergedSubGraph = thisSubGraph;
			}
			data.put(field, mergedSubGraph);
		}

		/**
		 * Constructes {@link NarGraph}.
		 * @return {@link NarGraph}
		 */
		public NarGraph<F> build() {
			return new NarGraph<>(clazz, data);
		}

		private final Class<F> clazz;
		private final Map<F, NarGraph<?>> data;

	}

	// enum-style factories

	/**
	 * Constructs empty {@link NarGraph} of {@code F} first-level fields.
	 * @param clazz class describing first-level field type
	 * @param <F> first-level field type
	 * @return empty {@link NarGraph}
	 */
	public static <F extends Enum<F> & NarField> NarGraph<F> noneOf(Class<F> clazz) {
		return new NarGraph<>(clazz);
	}

	/**
	 * Constructs {@link NarGraph} of {@code F} first-level fields with all fields initialized. If any field described
	 * field-enabled object, its subgraph will be empty.
	 * @param clazz class describing first-level field type
	 * @param <F> first-level field type
	 * @return {@link NarGraph} of type {@code F} with all first-level fields set
	 */
	public static <F extends Enum<F> & NarField> NarGraph<F> allOf(Class<F> clazz) {
		return of(EnumSet.allOf(clazz));
	}

	/**
	 * Constructs {@link NarGraph} of {@code F} first-level fields with fields initialized to complement of {@code fields}.
	 * If any field described field-enabled object, its subgraph will be empty.
	 * @param fields fields which <u>complement</u> to initialize
	 * @param <F> first-level field type
	 * @return {@link NarGraph} of type {@code F} with first-level fields initialized to complement of {@code fields}
	 */
	public static <F extends Enum<F> & NarField> NarGraph<F> complementOf(Collection<F> fields) {
		return of(EnumSet.complementOf(EnumSet.copyOf(fields)));
	}

	/**
	 * Constructs {@link NarGraph} of {@code F} first-level fields with given fields initialized.
	 * @param fields fields to initialize
	 * @param <F> first-level field type
	 * @return {@link NarGraph} of type {@code F} with first-level fields initialized to {@code fields}
	 */
	public static <F extends Enum<F> & NarField> NarGraph<F> of(Collection<F> fields) {
		if (fields.isEmpty())
			throw new IllegalArgumentException("fields param can not be empty");
		NarGraph<F> graph = null;
		for (F field : fields) {
			if (graph == null)
				graph = new NarGraph<>(field.getDeclaringClass());
			graph.data.put(field, null);
		}
		return graph;
	}

	/**
	 * Constructs {@link NarGraph} of {@code F} first-level fields with given fields initialized.
	 * @param first first field to initialize
	 * @param rest other fields to initialize
	 * @param <F> first-level field type
	 * @return {@link NarGraph} of type {@code F} with first-level fields initialized to {@code fields}
	 */
	public static <F extends Enum<F> & NarField> NarGraph<F> of(F first, Object... rest) {
		Class<F> clazz = first.getDeclaringClass();
		NarGraph<F> graph = new NarGraph<>(clazz);
		graph.data.put(first, null);
		add(clazz, graph, rest);
		return graph;
	}

	// string-style factories

	/**
	 * Constructs {@link NarGraph} of {@code F} first-level fields by parsing its string representation. String representation
	 * follows thwse rules:
	 * <ul>
	 *     <li>fields are represented bay their enum names separated by commas: {@code foo,bar,baz}</li>
	 *     <li>each field's subfields are sourounded by curley braces (hierarchy may go as deep as needed): {@code foo,bar{b1,b2},baz{c1{c11,c12},c2}}</li>
	 * </ul>
	 * @param value string to parse
	 * @param clazz class representing first-level field type
	 * @param <F> first-level field type
	 * @return {@link NarGraph} of type {@code F} initialized with field graph parsed from {@code value}
	 * @throws ParseException is value cannot be parsed
	 */
	public static <F extends Enum<F> & NarField> NarGraph<F> of(String value, Class<F> clazz) throws ParseException {
		return parse(RecursiveStringMap.of(value), clazz);
	}

	@SuppressWarnings("unchecked")
	private static <F extends Enum<F> & NarField> void add(Class<F> clazz, NarGraph<F> graph, Object... objects) {
		for (Object object : objects) {
			if (!clazz.equals(object.getClass()))
				throw new IllegalArgumentException("Expected " + clazz + ", but got " + object.getClass());
			graph.data.put((F) object, null);
		}
	}

	// recursive
	@SuppressWarnings("unchecked")
	private static <F extends Enum<F> & NarField> NarGraph<F> parse(RecursiveStringMap raw, Class<F> clazz) throws UnknownFieldException {
		
		if (raw == null)
			return null;
		
		NarGraph<F> graph = new NarGraph<>(clazz);

		for (Map.Entry<RecursiveStringMap.Key, RecursiveStringMap> entry : raw.entrySet()) {
			F field;
			try {
				field = Enum.valueOf(clazz, entry.getKey().name);
			} catch (IllegalArgumentException e) {
				throw new UnknownFieldException(entry.getKey().name, clazz, entry.getKey().offset);
			}
			// NOTE this cast if WRONG, but Java complains otherwise (we need to recurse with parse using different type in each step)
			// Works OK due to type erasure, but beware...
			graph.data.put(field, parse(entry.getValue(), (Class<F>) field.getNarFieldClass()));
		}
		
		return graph;
		
	}
	
	private NarGraph(Class<F> clazz) {
		this(clazz, new EnumMap<>(clazz));
	}
	
	private NarGraph(Class<F> clazz, Map<F, NarGraph<?>> data) {
		this.clazz = clazz;
		this.data = data;
	}

	// (Immutable)Set interface
	@Override
	public int size() { return data.size(); }
	@Override
	public boolean isEmpty() { return data.isEmpty(); }
	@Override
	public boolean contains(Object o) { return data.containsKey(o); }
	@Override
	public Iterator<F> iterator() { return Iterators.unmodifiableIterator(data.keySet().iterator()); }
	@Override
	public Object[] toArray() { return data.keySet().toArray(); }
	@Override
	public <X> X[] toArray(X[] a) { return data.keySet().toArray(a); }
	@Override
	public boolean add(F field) { throw new UnsupportedOperationException(); }
	@Override
	public boolean remove(Object o) { throw new UnsupportedOperationException(); }
	@Override
	public boolean containsAll(Collection<?> c) { return data.keySet().containsAll(c); }
	@Override
	public boolean addAll(Collection<? extends F> c) { throw new UnsupportedOperationException(); }
	@Override
	public boolean retainAll(Collection<?> c) { throw new UnsupportedOperationException(); }
	@Override
	public boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException(); }
	@Override
	public void clear() { throw new UnsupportedOperationException(); }

	/**
	 * Clones this field graph with all subgraphs.
	 * @return new {@link NarGraph} clone
	 */
	//@Override	// GWT complains
	public NarGraph<F> clone() {
		return new NarGraph<>(clazz, data.isEmpty() ? new EnumMap<>(clazz) : new EnumMap<>(data));
	}

	/**
	 * (deep) Compares this {@link NarGraph} to another one.
	 * @param obj another {@link NarGraph} to which to compare this one
	 * @return {@code true} if given field graph contains exactly the same fields initialized as this one, {@code false} otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NarGraph<?> other = (NarGraph<?>) obj;
		if (clazz != other.clazz)
			return false;
		if (data.size() != other.data.size())
			return false;
		for (Map.Entry<F, NarGraph<?>> entry : data.entrySet()) {
			if (!other.data.containsKey(entry.getKey()))
				return false;
			if (!Objects.equals(
				Optional.ofNullable(entry.getValue()).filter(subfields -> !subfields.isEmpty()).orElse(null),
				Optional.ofNullable(other.data.get(entry.getKey())).filter(subfields -> !subfields.isEmpty()).orElse(null)
			))
				return false;	
		}
		return true;
	}

	/**
	 * Converts this field graph to its string representation.
	 * @return String representation of this field graph
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		toString(this, builder);
		return builder.toString();
	}
	
	// recursive
	private void toString(NarGraph<?> graph, StringBuilder builder) {
		boolean first = true;
		for (Map.Entry<?, NarGraph<?>> entry : graph.data.entrySet()) {
			if (first)
				first = false;
			else
				builder.append(',');
			builder.append(entry.getKey());
			if (entry.getValue() != null && !entry.getValue().isEmpty()) {
				builder.append('{');
				toString(entry.getValue(), builder);
				builder.append('}');
			}
		}
	}

	/**
	 * @return {@link NarField} field class which describes first-level fields
	 */
	public Class<F> getDeclaringClass() {
		return clazz;
	}

	/**
	 * <p>Retrieves (unchecked) subgraph associated with given field. If field doesn't have a subset, empty set is returned.</p>
	 * <p>For checked subgraph retrieval, see {@link #getGraph(Enum, Class)}.</p>
	 *
	 * @param field field for which to retrieve subgraph
	 *
	 * @return subgraph associated with given field
	 *
	 * @see #getGraph(Enum, Class)
	 */
	public NarGraph<?> getGraph(F field) {
		return data.get(field);
	}

	/**
	 * Retrieves subgraph associated with given field. If field doesn't have a subset, empty set is returned.
	 * 
	 * @param field field for which subset is requested
	 * @param clazz enum to which to cast returned subset
	 * @param <X> {@link NarField} describing expected first-level field type in retrieved graph
	 * 
	 * @return field's subset (possibly empty)
	 * 
	 * @throws IllegalArgumentException if subgraph defines fields of type other than {@code clazz} param
	 */
	@SuppressWarnings("unchecked")
	public <X extends Enum<X> & NarField> NarGraph<X> getGraph(F field, Class<X> clazz) {

		if (!clazz.equals(field.getNarFieldClass()))
			throw new IllegalArgumentException("Requested sub-graph for field " + field + " of type " + clazz + " but field declares sub-graph of type " + field.getNarFieldClass());

		NarGraph<?> subGraphRaw = getGraph(field);

		if (subGraphRaw == null)
			return new NarGraph<>(clazz);

		Class<?> subGraphDeclaringClass = subGraphRaw.getDeclaringClass();

		if (!subGraphDeclaringClass.equals(clazz))
			throw new IllegalArgumentException("Requested sub-graph for field " + field + " of type " + clazz + " but stored sub-graph was of type " + subGraphDeclaringClass);
		
		return (NarGraph<X>) subGraphRaw;

	}

	private final Class<F> clazz;
	private final Map<F, NarGraph<?>> data;

}
