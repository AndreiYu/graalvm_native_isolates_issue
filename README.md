# Getting started with Quarkus

This is a sample code for checking isolates killing when background task is performed in js context

## Requirements

## Building the application

Launch the Maven build on the checked out sources of this demo:

> ./mvnw install

### Run Quarkus in JVM mode

When you're done iterating in developer mode, you can run the application as a
conventional jar file.

First compile it:

> ./mvnw install

Then run it:

> java -jar ./target/getting-started-1.0-SNAPSHOT-runner.jar

Have a look at how fast it boots, or measure the total native memory consumption.

### Run Quarkus as a native executable

You can also create a native executable from this application without making any
source code changes. A native executable removes the dependency on the JVM:
everything needed to run the application on the target platform is included in
the executable, allowing the application to run with minimal resource overhead.

Compiling a native executable takes a bit longer, as GraalVM performs additional
steps to remove unnecessary codepaths. Use the  `native` profile to compile a
native executable:

> ./mvnw package -Dquarkus.package.type=native -Dquarkus.native.additional-build-args=--language:js,-H:IncludeResources='.*\.js',-H:+StackTrace


After getting a cup of coffee, you'll be able to run this executable directly:

> ./target/getting-started-1.0-SNAPSHOT-runner

There is a timeout set to kill isolates running longer than 5 sec.

To reproduce the bug you can call  http://0.0.0.0:8080/calculate?n=100000000    or any  huge Int value to calculate
normally if calling http://0.0.0.0:8080/calculate?n=100 - the task has enough time to complete
