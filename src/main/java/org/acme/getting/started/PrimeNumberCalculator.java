package org.acme.getting.started;

import org.graalvm.polyglot.HostAccess;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Represents prime calculator. It was created for showing the possibility of using java code from js inside GraalVM isolate.
 */
@RegisterForReflection
public class PrimeNumberCalculator {

	@HostAccess.Export
	public boolean isPrime(int n) {
		if (n == 1) return false;
		if (n < 4) return true;
		if (n % 2 == 0) return false;
		if (n < 9) return true;
		if (n % 3 == 0) return false;
		var r = Math.floor(Math.sqrt(n));
		var f = 5;
		while (f <= r) {
			if (n % f == 0) return false;
			if (n % (f + 2) == 0) return false;
			f += 6;
		}
		return true;
	}

	@HostAccess.Export
	public int solve(int n) {
		CalculateService.log("Starting to solve calculation.");
		if (n == 1) return 2;
		var count = 1;
		var candidate = 1;
		do {
			candidate += 2;
			if (isPrime(candidate)) count++;
		} while (count != n);
		return candidate;
	}
}
