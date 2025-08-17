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

Maven:
```xml
<dependency>
    <groupId>com.darkona</groupId>
    <artifactId>logged</artifactId>
    <version>1.1.0</version>
</dependency>
```
Gradle:
```groovy
dependencies {
   implementation "com.darkona:logged:1.1.0" 
}
```
No manual configuration is needed. Spring Boot will detect and wire all components automatically.

## Usage

Annotate any public method with `@Logged`:

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

## 🧩 Message Formatting with Tokens

You can customize how your log messages look for method entry, exit, and exception cases using configurable templates.

Each template supports a specific set of tokens. These tokens will be dynamically replaced at runtime depending on the log context.

There are **three kinds of log messages**:

- **Entry**: before the method runs (may or may not have arguments).

- **Exit**: after successful execution (may or may not return a value).

- **Exception**: when a method throws an exception.

These customizations work both for the normal messages via configuration in your application.properties or application.yaml, and for custom messages for 
different methods.
---

### ✍️ Syntax

Use `{token}` to inject a runtime value. Example:

```yaml
entry: "{eI} → Entering {m}({a})"
exit:  "{xI} ← {m} returned {rV} in {d}ms"
exception: "{tI} !! {m} threw {ex}: {eM}"
```

---

## 📥 Entry Message Tokens (`format.entry`)

|Token|Description|
|---|---|
|`{m}`|Method name|
|`{c}`|Class name (simple name only)|
|`{a}`|Arguments passed to the method|
|`{eI}`|Entry icon (from config)|

---

## 📤 Exit Message Tokens (`format.exit`)

|Token|Description|
|---|---|
|`{m}`|Method name|
|`{c}`|Class name|
|`{rV}`|Return value (null-safe)|
|`{rC}`|Return value class name|
|`{d}`|Duration of method execution in milliseconds|
|`{xI}`|Exit icon (from config)|

---

## 💥 Exception Message Tokens (`format.exception`)

|Token|Description|
|---|---|
|`{m}`|Method name|
|`{c}`|Class name|
|`{ex}`|Exception class name|
|`{eM}`|Exception message|
|`{ec}`|Class where the exception originated|
|`{em}`|Method where the exception originated|
|`{f}`|File name (from the stack trace, if available)|
|`{L}`|Line number (from the stack trace, if available)|
|`{tI}`|Throw icon (from config)|




ℹ️ Notes

If a method has no arguments, {a} resolves to an empty string.

If a method returns void, {rV} is not evaluated.

Icons ({eI}, {xI}, {tI}) come from logging.decorated.icons in your config.
## Architecture

* Uses Spring AOP (`@Aspect`) to intercept methods annotated with `@Logged`
* Internally, delegates log construction to a reusable `LogStrings` utility
* No runtime reflection required outside of Spring proxy context

## License
Logged is licensed under the GNU Lesser General Public License v3.0 (LGPL-3.0).  
You may use it freely in commercial or open-source projects.  
If you modify the library itself and distribute those changes, you must publish them under the same license so the community benefits.

## Contributing

Currently closed for external contributions.
