package com.steatoda.nar.service.async;

/**
 * <p>Converts {@link NarServiceHandler} from one type to another.</p>
 *
 * @param <T> type returned by performed operation
 * @param <R> converted type passed to delegate handler
 */
public abstract class NarServiceHandlerConvertor<T, R> implements NarServiceHandler<T> {

	/**
	 * Constructs {@link NarServiceHandlerConvertor} using {@code delegate} as handler to forward method invocations to.
	 *
	 * @param delegate handler to forward method invocations to
	 */
	public NarServiceHandlerConvertor(NarServiceHandler<R> delegate) {
		this.delegate = delegate;
	}

	@Override
	public void onPreRequest(NarRequest request) {
		delegate.onPreRequest(request);
	}
	
	@Override
	public void onPostRequest(NarRequest request) {
		delegate.onPostRequest(request);
	}

	@Override
	public void onCancel() {
		delegate.onCancel();
	}

	@Override
	public void onSuccess(T value) {
		delegate.onSuccess(convert(value));
	}

	@Override
	public void onFail() {
		delegate.onFail();
	}
	
	@Override
	public void onFinish() {
		delegate.onFinish();
	}

	@Override
	public void onDestroy() {
		delegate.onDestroy();
	}

	/**
	 * <p>Converts value from {@code T} (result from invoked operation) to {@code R} (handled by delegating handler).</p>
	 *
	 * @param value result from invoked operation
	 *
	 * @return converted value
	 */
	protected abstract R convert(T value);

	private final NarServiceHandler<R> delegate;
	
}
