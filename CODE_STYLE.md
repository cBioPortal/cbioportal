## Introduction

This document defines the coding standards for source files in the cBioPortal (Backend).

The structure of this document is based on the [Google Java Style](https://google.github.io/styleguide/javaguide.html) reference and is a _work in progress_.

## Source File Basics

### File encoding: UTF-8

Source files must be encoded using `UTF-8`.

### Indentation

* Indentation uses _tabs_ (not spaces)
* Unix (LF), not DOS (CRLF) line endings
* Eliminate all trailing whitespace
  * On Linux, Mac, etc.: `find . -type f -name "*.java" -exec perl -p -i -e "s/[ \t]$//g" {} \;`

## Source file structure

A source file consists of the following, in this exact order:

* License
* Package statement
* Import statements
* Exactly one top-level class

Exactly one blank line separates each of the above sections.

### License

Each source file must specify the following license at the very top of the file:

```java
/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
```

Always check the date range in the license header. For example, if you've modified a file in 2022 whose header still reads:

```
* Copyright 2002-2018 the original author or authors.
```

Then be sure to update it to 2023 accordingly:

```
* Copyright 2002-2023 the original author or authors.
```

### Import statements

The import statements are structured as follow:

* import `java.*`
* blank line
* import `javax.*`
* import `jakarta.*`
* import `org.springframework.*`
* blank line
* import all other imports
* blank line
* import `org.cbioportal.*`
* blank line
* import static all other imports

Static imports should not be used in production code, but they should be used in test code, especially for things like `import static org.assertj.core.api.Assertions.assertThat;`.

Although static imports are generally forbidden in production code, the following are use cases for which static imports are permissible.

* constants (including enum constants): such as those in `java.nio.charset.StandardCharsets` or `org.springframework.core.annotation.MergedAnnotations.SearchStrategy`
* static factory methods for third-party DSLs: such as the methods in `org.junit.platform.engine.discovery.DiscoverySelectors` when used with the JUnit Platform `Launcher` API

Wildcard imports such as `import java.util.*` or `import static org.assertj.core.api.Assertions.*` are forbidden, even in test code.

### Java source file organization

The following governs how the elements of a source file are organized:

1. static fields
1. normal fields
1. constructors
1. (private) methods called from constructors
1. static factory methods
1. JavaBean properties (i.e., getters and setters)
1. method implementations coming from interfaces
1. private or protected templates that get called from method implementations coming from interfaces
1. other methods
1. `equals`, `hashCode`, and `toString`

Note that private or protected methods called from method implementations should be placed immediately below the methods where they're used. In other words if there 3 interface method implementations with 3 private methods (one used from each), then the order of methods should include 1 interface and 1 private method in sequence, not 3 interface and then 3 private methods at the bottom.

Above all, the organization of the code should feel _natural_.

## Formatting

### Braces

#### Block-like constructs: K&R style

Braces mostly follow the _Kernighan and Ritchie style_ (a.k.a., "Egyptian brackets") for nonempty blocks and block-like constructs:

* No line break before the opening brace but prefixed by a single space
* Line break after the opening brace
* Line break before the closing brace
* Line break after the closing brace if that brace terminates a statement or the body of a method, constructor, or named class
* Line break before else, catch and finally statements

Example:

```java
return new MyClass() {
 @Override
 public void method() {
  if (condition()) {
   something();
  }
  else {
   try {
    alternative();
   }
   catch (ProblemException ex) {
    recover();
   }
  }
 }
};
```

### Line wrapping

90 characters is the _preferred_ line length we aim for. In some cases the preferred length can be achieved by refactoring code slightly. In other cases it's  just not possible.

90 is not a hard limit. Lines between 90-105 are perfectly acceptable in many cases where it aids readability and where wrapping has the opposite effect of reducing readability. This is a judgement call and it's also important to seek consistency. Many times you can learn by looking how specific situations are handled in other parts of the code.

Lines between 105-120 are allowed but discouraged and should be few.

No lines should exceed 120 characters.

The one big exception to the above line wrapping rules is Javadoc where we aim to wrap around 80 characters for maximum readability in all kinds of contexts -- for example, reading on Github, on your phone, etc.

When wrapping a lengthy expression, 90 characters is the length at which we aim to wrap. Place separator symbols at the end of the current line rather than at the beginning of the next line. In this context, the following are considered separator symbols: `,`, `+`, `?`, `:`, `&&`, `||`. For example:

```java
if (thisLengthyMethodCall(param1, param2) && anotherCheck() &&
        yetAnotherCheck()) {

    // ....
}
```

### Blank Lines

Add two blank lines before the following elements:

* `static {}` block
* Fields
* Constructors
* Inner classes

Add one blank line after a method signature that is multiline, i.e.

```java
@Override
protected Object invoke(FooBarOperationContext context,
        AnotherSuperLongName name) {

    // code here
}
```

For inner-classes, extra blank lines around fields and constructors are typically not
added as the inner class is already separated by 2 lines, unless the inner class is
more substantial in which case the 2 extra lines could still help with readability.

## Class declaration

Try as much as possible to put the `implements`, `extends` section of a class declaration on the same line as the class itself.

Order the classes so that the most important comes first.

## Naming

### Constant names

Constant names use `CONSTANT_CASE`: all uppercase letters, with words separated by underscores.

Every constant is a `static final` field, but not all `static final` fields are constants. Constant case should therefore be chosen only if the field **is really** a constant.

Example:

```java
// Constants
private static final Object NULL_HOLDER = new NullHolder();
public static final int DEFAULT_PORT = -1;

// Not constants
private static final ThreadLocal<Executor> executorHolder = new ThreadLocal<Executor>();
private static final Set<String> internalAnnotationAttributes = new HashSet<String>();
```

### Variable names

Avoid using single characters as variable names. For instance prefer `Method method` to `Method m`.

## Programming Practices

### File history

* A file should look like it was crafted by a single author, not like a history of changes.
* Don't artificially spread things out that belong together.

### Organization of setter methods

Choose wisely where to add a new setter method; it should not be simply added at the end of the list. Perhaps the setter is related to another setter or relates to a group. In that case it should be placed near related methods.

* Setter order should reflect order of importance, not historical order.
* Ordering of _fields_ and _setters_ should be **consistent**.

### Ternary Operator

Wrap the ternary operator within parentheses -- for example, `return (foo != null ? foo : "default");`.

Also make sure that the _not null_ condition comes first.

### Null Checks

Use the `org.springframework.util.Assert.notNull` static method to check that a method argument is not `null` and throw an `IllegalArgumentException` otherwise. Format the exception message so that the name of the parameter comes first with its first character capitalized, followed by "_must not be null_". For instance:

```java
public void handle(Event event) {
    Assert.notNull(event, "Event must not be null");
    //...
}
```

For other use cases, use the `org.springframework.util.Assert.state` static method to ensure a variable is not `null` and throw an `IllegalStateException` otherwise. Format the exception message so that the identifier related to the variable (name, type or description) comes first with its first character capitalized, followed by "_must not be null_". For instance:

```java
//...
Event event = ...
Assert.state(event != null, "Event must not be null");
//...
```

### Null Safety of APIs and fields

The null-safety of Spring Framework APIs and fields must be specified using [related annotations](https://docs.spring.io/spring-framework/reference/core/null-safety.html), and consistency is enforced at build-time via [NullAway](https://github.com/uber/NullAway).

Packages are expected to define non-null as the default in `package-info.java`:

```java
@NonNullApi
@NonNullFields
package org.springframework.mypackage;
```

And nullable fields, return values and parameters are expected to be specified explicitly using `@Nullable`:

```java
import org.springframework.lang.Nullable;

public class MyClass {

 @Nullable
 private Class<?> nullableType;

 public void setNullableType(@Nullable Class<?> nullableType) {
  this.nullableType = nullableType;
 }

 @Nullable
 public Class<?> getNullableType() {
  return this.nullableType;
 }
}
```

[`org.springframework.lang.Contract` annotation](https://docs.spring.io/spring-framework/docs/6.2.0-SNAPSHOT/javadoc-api/org/springframework/lang/Contract.html) can be used to specify some aspects of the method behavior depending on the arguments that are then [taken in account by NullAway](https://github.com/uber/NullAway/wiki/Supported-Annotations#contracts). See for example how `@Contract` is used in Spring Framework codebase in the `Assert` class:

```java
public abstract class Assert {
 // ...
 @Contract("false, _ -> fail")
 public static void state(boolean expression, String message) {
  if (!expression) {
   throw new IllegalStateException(message);
  }
 }
 // ...
 @Contract("null, _ -> fail")
 public static void notNull(@Nullable Object object, String message) {
  if (object == null) {
   throw new IllegalArgumentException(message);
  }
 }
 // ...
}
```

Those contract allows NullAway static analysis to understand that after an invocation of `Assert.state(value != null, "Value must not be null")` or `Assert.notNull(value, "Value must not be null")`, a nullable `value` can be considered as non nullable.

Related guidelines:

* When overriding a method, null-safety annotations of the super method need to be specified on the overridden method unless you want to override null-safety.
* Use `@SuppressWarnings("NullAway")` when NullAway triggers irrelevant errors, which can happen due to [NullAway bugs or missing features](https://github.com/uber/NullAway/issues) or when the analysis is not able to prove null-safety (can happen with lambdas).

### Use of @Override

Always add `@Override` on methods overriding or implementing a method declared in a super type.

### Utility classes

A class that is only a collection of static utility methods must be named with a `Utils` suffix, must have a `private` default constructor, and must be `abstract`. Making the class `abstract` and providing a `private` _default_ constructor prevent anyone from instantiating it. For example:

```java
public abstract MyUtils {

    private MyUtils() {
        /* prevent instantiation */
    }

    // static utility methods
}
```

### Field and method references

A field of a class should _always_ be referenced using `this`. A method of a class, however, should never be referenced using `this`.

### Local variable type inference

The use of `var` for variable declarations (_local variable type inference_) is not permitted. Instead, declare variables using the concrete type or interface (where applicable).

## Javadoc

### Javadoc formatting

The following template summarizes typical Javadoc usage for a method.

```java
/**
 * Parse the specified {@link Element} and register the resulting
 * {@link BeanDefinition BeanDefinition(s)}.
 * <p>Implementations must return the primary {@link BeanDefinition} that results
 * from the parsing if they will ever be used in a nested fashion (for example as
 * an inner tag in a {@code <property/>} tag). Implementations may return
 * {@code null} if they will <strong>not</strong> be used in a nested fashion.
 * @param element the element that is to be parsed into one or more {@link BeanDefinition BeanDefinitions}
 * @param parserContext the object encapsulating the current state of the parsing process;
 * provides access to a {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
 * @return the primary {@link BeanDefinition}
 */
BeanDefinition parse(Element element, ParserContext parserContext);
```

In particular, please note:

* Use an imperative style (i.e. _Return_ and not _Returns_) for the first sentence.
* No blank lines between the description and the parameter descriptions.
* If the description is defined with multiple paragraphs, start each of them with `<p>`.
* If a parameter description needs to be wrapped, do not indent subsequent lines (see `parserContext`).

The Javadoc of a class has some extra rules that are illustrated by the sample below:

```java
/*
 * Interface used by the {@link DefaultBeanDefinitionDocumentReader} to handle custom,
 * top-level (directly under {@code <beans/>}) tags.
 *
 * <p>Implementations are free to turn the metadata in the custom tag into as many
 * {@link BeanDefinition BeanDefinitions} as required.
 *
 * <p>The parser locates a {@link BeanDefinitionParser} from the associated
 * {@link NamespaceHandler} for the namespace in which the custom tag resides.
 *
 * @author Rob Harrop
 * @since 2.0
 * @see NamespaceHandler
 * @see AbstractBeanDefinitionParser
 */
```

* The order of tags for type-level Javadoc is: `@author`, `@param`, `@see`, `@deprecated`.
* The order of tags for constructor-level, method-level, and field-level Javadoc is: `@param`, `@return`, `@throws`, `@see`, `@deprecated`.
* In contrast to constructor-level, method-level, and field-level Javadoc, the paragraphs of a class description _are_ separated by blank lines.

The following are additional general rules to apply when writing Javadoc:

* Use `{@code}` to wrap code statements or values such as `null`.
* If a type is only referenced by a `{@link}` element, use the fully qualified name in order to avoid an unnecessary `import` declaration.

## Tests

### Testing cBioPortal

Tests must be written using JUnit 4

Stretch Goal Upgrade tests to JUnit 5!!

### Naming

Each test class name must end with a `Tests` suffix.

### Assertions

Use AssertJ for assertions.

### Mocking

Use the BDD Mockito support.
