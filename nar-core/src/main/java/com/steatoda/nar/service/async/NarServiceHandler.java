package com.steatoda.nar.service.async;

/**
 * Handler of asynchronous service invocations.
 *
 * @param <T> result type
 */
@FunctionalInterface
public interface NarServiceHandler<T> {

	/**
	 * <p>Called <b>before</b> service method invocation, iff one is about to be made.</p>
	 *
	 * <p>Example of an operation during which service method invocation won't be performed
	 * is extending an entity with graph that is already present in that entity.</p>
	 *
	 * @param request {@link NarRequest} describing this asynchronous operation
	 */
	default void onPreRequest(NarRequest request) { /* no-op */ }

	/**
	 * <p>Called <b>after</b> service method invocation, iff one was made.</p>
	 *
	 * <p>Example of an operation during which service method invocation won't be performed
	 * is extending an entity with graph that is already present in that entity.</p>
	 *
	 * @param request {@link NarRequest} describing this asynchronous operation
	 */
	default void onPostRequest(NarRequest request) { /* no-op */ }

	/**
	 * <p>Called after service method invocation is cancelled.</p>
	 * 
	 * <p>NOTE: this is final handler - no subsequent {@link #onFinish()} will be called.</p>
	 */
	default void onCancel() { /* no-op */ }

	/**
	 * Called after service method results are retrieved.
	 *
	 * @param value result from performed operation
	 */
	void onSuccess(T value);

	/**
	 * <p>Called when service method invocation fails, but NOT when it is cancelled (for cancellation, see {@link #onCancel()}).</p>
	 */
	default void onFail() { /* no-op */ }
	
	/**
	 * <p>Called after service method invocation succeeds or fails, but NOT after it is cancelled (for cancellation, see {@link #onCancel()}).</p>
	 */ 
	default void onFinish() { /* no-op */ }

	/**
	 * <p>Called after service method invocation should be disposed (it was either cancelled or finished via succeeding or failing).</p>
	 */ 
	default void onDestroy() { /* no-op */ }

}
