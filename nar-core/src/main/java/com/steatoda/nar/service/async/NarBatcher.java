package com.steatoda.nar.service.async;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.steatoda.nar.NarField;
import com.steatoda.nar.NarGraph;
import com.steatoda.nar.NarEntity;

/**
 * <p>Batches multiple {@link NarAsyncService#get} operations on one instance into <u>one</u> async service method invocation.</p>
 *
 * @param <I> ID type
 * @param <C> concrete implementation of class implementing {@link NarEntity}
 * @param <F> field type
 *
 * @see NarAsyncService
 */
public class NarBatcher<I, C extends NarEntity<I, C, F>, F extends Enum<F> & NarField> implements NarAsyncService<I, C, F> {

	// TODO borrow instances from extend-jobs for get-jobs?

	/**
	 * <p>Represents retrieval of one distinct {@link NarEntity}. There may be multiple instances of that same entity
	 * in which case union of missing fields will be fetched (once) and all handlers will be notified with that one entity.</p>
	 */
	private class Job {
		
		public Job(I id) {
			this.id = id;
		}
		
		public void queue(NarGraph<F> graph, NarServiceHandler<C> handler) {
			if (graphBuilder == null)
				graphBuilder = NarGraph.Builder.of(graph.getDeclaringClass());
			graphBuilder.add(graph);
			handlers.add(handler);
		}
		
		private final I id;
		private final Queue<NarServiceHandler<C>> handlers = new ArrayDeque<>();
		private NarGraph.Builder<F> graphBuilder = null;
		private NarRequest request = null;
		private boolean finished = false;
		
	}

	/**
	 * <p>Constructs new instance using {@code service} as backing service.</p>
	 *
	 * @param service backing service to delegate calls to
	 */
	public NarBatcher(NarAsyncService<I, C, F> service) {
		this.service = service;
	}

	@Override
	public C instance() {
		return service.instance();
	}
	
	@Override
	synchronized public NarRequest get(I id, NarGraph<F> graph, NarServiceHandler<C> handler) {
		
		Job job = Jobs.get(id);
		if (job == null)
			Jobs.put(id, job = new Job(id));

		job.queue(graph, handler);
		
		final Job finalJob = job;
		NarRequest request = new NarRequest() {
			@Override
			public void cancel() {
				// calling cancel() on finished job should not trigger another onCancel
				if (finalJob.finished)
					return;
				// can't cancel (cumulative) request, but we can remove handler from list of to-be-notified handlers
				if (!finalJob.handlers.remove(handler))
					return;	// not found, probably already cancelled
				handler.onCancel();
				handler.onDestroy();
				// if there are no more handlers, cancel (cumulative) request
				if (finalJob.handlers.isEmpty() && finalJob.request != null)
					finalJob.request.cancel();
			}
		};
		
		handler.onPreRequest(request);

		return request;

	}

	/** Executes queued operations. */
	public void run() {

		List<Job> batch = pullJobsBatch();

		for (Job job : batch) {
			
			if (job.handlers.isEmpty())
				continue;	// all handlers are cancelled
			
			NarGraph<F> graph = job.graphBuilder.build();

			job.request = service.get(job.id, graph, new NarServiceHandler<C>() {
				// NOTE: we already called onPreRequest(FieldsRequest) when queuing this instance, so don't call again
				@Override
				public void onPostRequest(NarRequest request) {
					for (NarServiceHandler<C> handler : job.handlers)
						handler.onPostRequest(request);
				}
				@Override
				public void onSuccess(C entity) {
					job.finished = true;	// from now on, calling cancel() should be no-op
					for (NarServiceHandler<C> handler : job.handlers)
						handler.onSuccess(entity);
				}
				@Override
				public void onCancel() {
					job.finished = true;	// from now on, calling cancel() should be no-op
					for (NarServiceHandler<C> handler : job.handlers)
						handler.onCancel();
				}
				@Override
				public void onFail() {
					job.finished = true;	// from now on, calling cancel() should be no-op
					for (NarServiceHandler<C> handler : job.handlers)
						handler.onFail();
				}
				@Override
				public void onFinish() {
					for (NarServiceHandler<C> handler : job.handlers)
						handler.onFinish();
				}
				@Override
				public void onDestroy() {
					for (NarServiceHandler<C> handler : job.handlers)
						handler.onDestroy();
				}
			});
				
		}
		
	}

	synchronized private List<Job> pullJobsBatch() {
		
		List<Job> batch = new ArrayList<>(Jobs.values());
		
		Jobs.clear();
		
		return batch;
		
	}

	private final NarAsyncService<I, C, F> service;

	private final Map<I, Job> Jobs = new HashMap<>();

}
