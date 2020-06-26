package org.acme.getting.started;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;

import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.Isolates;
import org.graalvm.nativeimage.Isolates.CreateIsolateParameters;
import org.graalvm.nativeimage.RuntimeOptions;
import org.graalvm.nativeimage.c.function.CEntryPoint;

@ApplicationScoped
public class CalculateService {

	public int calculate(int n) {
		return processInNewIsolate(n);
	}

	private static int processInNewIsolate(int calculateNumber) {
		/* Create a new isolate for the function evaluation. */
		var params = CreateIsolateParameters.getDefault();
		var jsContext = Isolates.createIsolate(params);

		int result = isolateWrapper(jsContext, calculateNumber);

		Isolates.tearDownIsolate(jsContext);

		return result;
	}

	@CEntryPoint
	private static int isolateWrapper(
			@CEntryPoint.IsolateThreadContext IsolateThread jsContext, int calculateNumber
	) {
		//Set Xmx for Isolate
		RuntimeOptions.set("MaxHeapSize", 1024L * 1024L * 1024L);

		int result = 0;
		try {
			result = processInContext(calculateNumber);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return result;
	}

	private static int processInContext(int number) throws ExecutionException, InterruptedException {
		var script = "META-INF/resources/prime.js";
		var task = new GraalVMIntegerCallable(script, number);
		var mainExecutor = Executors.newSingleThreadExecutor();

		try {
			var result = mainExecutor.submit(task);
			return result.get();
		} finally {
			mainExecutor.shutdownNow();
		}
	}

	public static void log(String message) {
		var millisEnd = System.currentTimeMillis();
		var b = new StringBuilder();
		b.append(Thread.currentThread().getName());
		b.append(" - ");
		b.append(message);
		b.append(" time: ");
		b.append(millisEnd);
		System.out.println(b.toString());
	}

}
