# Logged

**Logged** is a lightweight Spring Boot library for method-level logging using Aspect-Oriented Programming (AOP). It provides an annotation-based mechanism to log method entry, return values, and exceptions using `@Logged`.

## Features

* Annotate methods with `@Logged` to automatically log:

  * Method entry with argument values
  * Method return values
  * Exceptions thrown
* Supports customization of log levels per operation (entry, return, exception)
* Designed to integrate seamlessly with Spring Boot projects via auto-configuration

## Technologies

* Java 21
* Spring Boot
* Spring AOP
* AspectJ (via Spring Aspects)
* Logback (or any SLF4J-compatible backend)

## Installation

Add the library to your Spring Boot project (e.g., via internal Maven repository or direct source inclusion).

```xml
<!-- Example: Replace with your own coordinates -->
<dependency>
    <groupId>com.example</groupId>
    <artifactId>logged</artifactId>
    <version>1.0.0</version>
</dependency>
```

No manual configuration is needed. Spring Boot will detect and wire all components automatically.

## Usage

Annotate any method with `@Logged`:

```java
@Logged
public String process(String input) {
    // your code
}
```

You can customize the logging levels:

```java
@Logged(entryLevel = LogLevel.DEBUG, returnLevel = LogLevel.INFO, exceptionLevel = LogLevel.ERROR)
public void compute() {
    // your code
}
```

> The library uses compile-time constants from `LogStrings` for consistent message formatting.

## Architecture

* Uses Spring AOP (`@Aspect`) to intercept methods annotated with `@Logged`
* Internally, delegates log construction to a reusable `LogStrings` utility
* No runtime reflection required outside of Spring proxy context

## License

This project is licensed under the MIT License.

## Contributing

Currently closed for external contributions.
