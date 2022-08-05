package com.steatoda.nar;

/**
 * <p>Editors that work with {@link NarObject}s can implement this interface to provide caller with information
 * exactly which fields they operate on.</p>
 *
 * @param <F> Field type
 */
public interface NarEditor<F extends Enum<F> & NarField> {

	/**
	 * @return complete graph that this editor needs to operate properly
	 */
	NarGraph<F> getGraph();

	/**
	 * @return graph that editor modifies
	 */
	NarGraph<F> getEditableGraph();
	
}
