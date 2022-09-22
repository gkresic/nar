---
title: Nar
subtitle: Java library for working with partially initialized objects
layout: page
hero_height: is-small
menubar: menu
---

# Nar

*Nar* (eng. *pomegranate*), because I never manage to consume these as a whole...

Nar is a design pattern for working with partially initialized objects.
Think GraphQL, but much smaller in scope and at the same time more versatile.
Although this repository represents a Java library, there is nothing Java-specific
in Nar and it can be easily ported to any language that supports concepts like enums,
sets and interfaces with default methods (or any similar concept).
Also, it's [GWT](http://www.gwtproject.org/) compatible, so it can be used
on both backend and frontend.

Sources are available on [GitHub](https://github.com/gkresic/nar).

## Components

### Nar Core

Core Nar interfaces and some generic implementations.

```
<dependency>
	<groupId>com.steatoda.nar</groupId>
	<artifactId>nar-core</artifactId>
	<version>{{ site.nar_version }}</version>
</dependency>
```

### Nar Jackson

Useful utilities for (de)serializing Nar objects to/from JSON using [Jackson](https://github.com/FasterXML/jackson).

```
<dependency>
	<groupId>com.steatoda.nar</groupId>
	<artifactId>nar-jackson</artifactId>
	<version>{{ site.nar_version }}</version>
</dependency>
```

### Nar jOOQ

Base implementations of `NarService` and `NarCRUDService` backed by relational database using
[jOOQ](https://www.jooq.org/).

```
<dependency>
	<groupId>com.steatoda.nar</groupId>
	<artifactId>nar-jooq</artifactId>
	<version>{{ site.nar_version }}</version>
</dependency>
```

## Demo

Clone from GitHub and run demo:

```
git clone git@github.com:gkresic/nar.git

cd nar

./gradlew :nar-demo:run --console=plain
```

Java 11 is required on path or in JAVA_HOME.
