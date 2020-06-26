package org.acme.getting.started;

import static org.acme.getting.started.CalculateService.log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

/**
 * Represents callable for running prime js logic with needed params.
 */
public class GraalVMIntegerCallable implements Callable<Integer> {

	private Context context;
	private final AtomicBoolean contextIsCanceled = new AtomicBoolean(false);

	private final String script;
	private final int arg;

	public GraalVMIntegerCallable(String script, int arg) {
		this.script = script;
		this.arg = arg;
	}

	@Override
	public Integer call() {
		var taskExecutor = getTaskExecutor();
		log("Thread start");
		context = Context
				.newBuilder("js")
				.allowHostAccess(HostAccess.EXPLICIT)
				.allowAllAccess(true)
				.allowExperimentalOptions(true)
				.option("engine.Compilation", "false")
				.out(System.out)
				.err(System.err)
				.build();
		//provide params and globals
		context.getBindings("js").putMember("graalArg", arg);
		context.getBindings("js").putMember("javaSolver", new PrimeNumberCalculator());

		//run script
		log("Process start");
		Future<Value> futureResult = taskExecutor.submit(() -> context.eval(Source.newBuilder("js", getScriptBody(), "test").build()));
		log("Process end");

		try {
			Value result = futureResult.get(5, TimeUnit.SECONDS);
			//print result
			System.out.println("Calculation Result = " + result.asInt());
			log("Thread end");
			return result.asInt();
		} catch (TimeoutException e) {
			futureResult.cancel(true);
			// Implicitly stop current context from inside the same object, since performing the same from Processor.java caused hang-outs
			//	and waiting for PolyglotException
			interrupt();
		}
		// After the execution got cancelled the executing thread stops by throwing a
		//	PolyglotException with the cancelled flag set.
		catch (ExecutionException e) {
			futureResult.cancel(true);
			PolyglotException polyglotException = (PolyglotException) e.getCause();
			if (!polyglotException.isCancelled()) {
				interrupt();
			}
			logPolyglotException(polyglotException);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!contextIsCanceled.getAndSet(true)) {
				log("Normally closing context ");
				context.close();
			}
			taskExecutor.shutdownNow();
		}
		return -1;
	}

	private void logPolyglotException(PolyglotException e) {
		log("PolyglotException while performing task:  \n" + Arrays
				.stream(e.getStackTrace())
				.map(Objects::toString)
				.collect(Collectors.joining("\n")));
		log("isGuestException: " + e.isGuestException());
		log("isHostException: " + e.isHostException());
		if (e.isHostException()) {
			log("asHostException: " + Arrays.stream(e.asHostException().getStackTrace()).map(Objects::toString).collect(Collectors.joining("\n")));
		}
		log("isCancelled: " + e.isCancelled());
		log("isExit: " + e.isExit());
	}

	private String getScriptBody() {
		return getScriptBody(script);
	}

	private String getScriptBody(String path) {
		ClassLoader cl = GraalVMIntegerCallable.class.getClassLoader() == null ?
				ClassLoader.getSystemClassLoader() :
				GraalVMIntegerCallable.class.getClassLoader();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(cl.getResourceAsStream(path), StandardCharsets.UTF_8))) {
			return br.lines().collect(Collectors.joining("\n"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public void interrupt() {
		if (context != null && !contextIsCanceled.getAndSet(true)) {
			log("Interrupting CallableTask and force closing context... ");
			var cancelExecutor = getTaskExecutor();
			try {
				cancelExecutor.submit(() -> context.close(true));
			} finally {
				cancelExecutor.shutdownNow();
			}
		}
	}

	private ExecutorService getTaskExecutor() {
		return Executors.newSingleThreadExecutor();
	}
}
