# <p align="center">Nar</p>

<p align="center">
	<img alt="GitHub Actions status badge" src="https://github.com/gkresic/nar/actions/workflows/build.yaml/badge.svg"/>
	<img alt="License badge" src="https://img.shields.io/github/license/gkresic/nar"/>
</p>

*Nar* (eng. pomegranate) because I never manage to consume these as a whole...

Nar is a design pattern for working with partially initialized objects.
Think GraphQL, but much smaller in scope and at the same time more versatile.
Although this repository represents a Java library, there is nothing Java-specific
in Nar and it can be easily ported to any language that supports concepts like enums,
sets and interfaces with default methods (or any similar concept).
Also, it's [GWT](http://www.gwtproject.org/) compatible, so it can be used
on both backend and frontend.

See [Wiki](https://github.com/gkresic/nar/wiki) for details, especially
[Motivation](https://github.com/gkresic/nar/wiki/Motivation) and
[Getting started](https://github.com/gkresic/nar/wiki/Getting_started).

## Components

### Nar Core

Core Nar interfaces and some generic implementations.

```
<dependency>
	<groupId>com.steatoda.nar</groupId>
	<artifactId>nar-core</artifactId>
	<version>1.0.0</version>
</dependency>
```

### Nar Jackson

Useful utils for (de)serializing Nar objects to/from JSON using [Jackson](https://github.com/FasterXML/jackson).

```
<dependency>
	<groupId>com.steatoda.nar</groupId>
	<artifactId>nar-jackson</artifactId>
	<version>1.0.0</version>
</dependency>
```

### Nar jOOQ

Base implementations of `NarService` and `NarCRUDService` backed by relational database using
[jOOQ](https://www.jooq.org/).

```
<dependency>
	<groupId>com.steatoda.nar</groupId>
	<artifactId>nar-jooq</artifactId>
	<version>1.0.0</version>
</dependency>
```

## Demo

Run demo with:

```
./gradlew :nar-demo:run --console=plain
```

Java 11 is required on path or in JAVA_HOME.
