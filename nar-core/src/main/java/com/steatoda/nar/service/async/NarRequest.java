package com.steatoda.nar.service.async;

/** Represents one asynchronous operation. */
public interface NarRequest {

	/**
	 * <p>Cancels current operation.</p>
	 *
	 * <p>If operation was pending, {@link NarServiceHandler#onCancel()} is invoked.
	 * If operation already finished (either as success, failure or simply was cancelled before),
	 * then invocation is no-op.</p>
	 */
	void cancel();
	
}
