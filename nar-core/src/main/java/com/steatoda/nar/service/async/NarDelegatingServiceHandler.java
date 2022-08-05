package com.steatoda.nar.service.async;

/**
 * Wraps {@link NarServiceHandler} and delegates every call to it. Override and provide decorated functionality.
 *
 * @param <T> result type
 *
 * @see NarServiceHandler
 */
public class NarDelegatingServiceHandler<T> implements NarServiceHandler<T> {

	/**
	 * Constructs {@link NarDelegatingServiceHandler} using {@code delegate} as delegate to forward method invocations to.
	 *
	 * @param delegate delegate to forward calls to
	 */
	public NarDelegatingServiceHandler(NarServiceHandler<T> delegate) {
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
		delegate.onSuccess(value);
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

	private final NarServiceHandler<T> delegate;
	
}
