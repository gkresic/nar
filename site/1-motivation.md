---
layout: page
title: Motivation
permalink: /motivation/
hero_height: is-small
menubar: menu
---

# Motivation

Working with complex object graphs in today's apps poses many challenges like bloat,
cyclic references and access control. If your objects are consumed by third parties
over a public API, add versioning to that list, too.

As an example, consider this simple data model:

```
/** Place where boats go to sleep */
Marina:
+ id : String
+ name : String
+ latitude : Double
+ longitude : Double
+ manager : Person
+ berths : List<Berth>
+ depths : Integer[][]
```

```
/** Docking berth at marina */
Berth:
+ id : String
+ boat : Boat
```

```
/** Any person in the app, may it be marina staff, skippers, boat crew, etc. */
Person:
+ id : String
+ name : String
+ email : String
+ permissions : Set<String>
+ boat : Boat
```

```
/** Any vessel in the app */
Boat:
+ id : String
+ name : String
+ type : String
+ homeport : Marina
+ skipper : Person
+ crew : List<Person>
```

Probably everyone's first impulse is to represent this model in a way so that every entity used contains fully
initialized sub-entities (all of them): every `Marina` instance will contain fully initialized `manager` and fully
initialized `berths` (note that `berths` represents an *array* so here we are talking about fully initialized
_every member_ of that array). Every `manager` will, in turn, contain fully initialized `boat` just like every berth
will have fully initialized `boat`. Boats will contain their sub-entities etc.

So called "full graph".

Working with full graphs is simple since everything your root entity references is already initialized. However, sooner
or later (and it somehow always ends up to be "sooner"), some inevitable problems pop out.

### Problems

#### Bloat

Sometimes entities contain either large, calculated-on-demand or somehow else 'expensive' properties which may be needed
only occasionally (in our demo case it would be the `depth` matrix in `Marina` entity). If we read/calculate and send
to clients such properties on *every* request, communication will be unnecessarily slow not to mention load on our
backend.

#### Cyclic references

Entity A as a member contains instance of entity B which, in turn, contains instance of entity A (graph theorists call
this a [cycle](https://en.wikipedia.org/wiki/Cycle_(graph_theory))). Cycles may not be as simple as former example,
but may arise in 3rd, 4th or even deeper references, making them increasingly difficult to spot and avoid.

Examples from our sample model:

* **Person** → `.boat` → **Boat** → `.skipper` → **Person**
* **Marina** → `.manager` → **Person** → `.boat` → **Boat** → `.homeport` → **Marina**
* **Marina** → `.berths` → **Berth** → `.boat` → **Boat** → `.homeport` → **Marina**

Of course, holding such entity graphs in memory is certainly possible, but (de)serializing them is always a challenge.

#### Access control

Often our model contains entities with properties that may need certain access rights to read. For example (and this is
a fairly common pattern) take `Person` entity that describe users in our app. Not every client working with `Boat`
should be able to read which permissions every crew member on that boat has (`boat.crew[].permissions`) nor is it
GDPR-compliant to expose their email addresses (`boat.crew[].email`).

#### Versioning

If project contains multiple components, like mobile apps that communicates with a backend or (much
worse) any 3rd party's client for your public API, usually it's not simple - if even possible - to synchronize upgrades
to every component in the ecosystem (for example, the long review process on app stores is something none of us can
control).

One common scenario is deprecating a property in one of your entities or replacing it with other, often of different
type. For example, let's say we need to upgrade our sample data model so that `Marina` can have more
than one manager. Just dropping `manager` property is not feasible, since existing clients still expect it, so we'll
probably decide to go with backward-compatible upgrade: keep `manager`, but introduce new property `managers` of type
`List<Person>`. For some transitional period, we'll initialize `manager` with *first* manager in our new data model
(so that existing clients have at least something to work with) and `managers` will hold all of them. Over the time,
clients will be upgraded to read `managers` instead of `manager` and we would be able to drop `manager` from our model.

Obvious question is: **when** it's safe to drop deprecated `manager` property? How do we know that all clients are
upgraded?

However, if we force clients to request each property *explicitly*, we'll be able to track which properties are being
requested. If client requests a deprecated property, we can send them a warning (for example, as an HTTP header in a
REST API response). When usage of that deprecated property drops to zero (or at least to some acceptable low values),
we can safely remove it from our responses altogether.

### Possible solutions

Of course, every mentioned problem (bloat, cycles, access control, versioning) could be solved with a more or less ugly
hack. But hacks are not what we should be striving for.

None of the mentioned problems are new, and certainly I'm not the only one who tried to solve them. And I tried many
things:

1. Work with a full graph. Obviously, this phase didn't last for long :).

2. Hold only IDs for every sub-entity and fetch their (fully resolved) instances on demand. And that's how I met set of
   problems commonly referred to as "waterfall effect".

4. Specialized DTO (data transfer entities) for every specific request type which led me to copy/paste hell.

5. [RequestFactory](https://www.gwtproject.org/doc/latest/DevGuideRequestFactory.html) was an interesting new concept in
   the mid 00s popularized by [GWT](https://www.gwtproject.org/). Although its abandoned now, it heavily influenced Nar.

6. Finally, [GraphQL](https://graphql.org/) is most recent take on this problem, by far best implemented, documented and
   supported. However, it's not a silver bullet:

    * Returned entities are for one-time use and are not meant to be passed between functions since it's not possible
      to track what has been initialized in them. In other words, only function that originally fetched an entity knows
      if some empty property really holds no value, or it's empty because it wasn't fetched from the backend.

    * Field declarations used for selecting specific properties in entities are strings which may lead to long-term
      problems when upgrading an entity graph, since it's difficult to isolate all the places where that changed
      properties were being used (this problem is very similar in nature with problems caused by misspelled variable
      names in programming languages that don't mandate explicit variable declarations)

    * Protocol is, [according to its own authors](https://graphql.org/learn/best-practices/#server-side-batching-caching),
      pretty "chatty" and requires various addons like [DataLoader](https://github.com/graphql/dataloader) to keep
      roundtrips to backing stores on sane levels.

    * Although it declares itself as "protocol agnostic", there are very few references on using it outside of HTTP
      scope. It probably can be done, but then you are on your own.

    * Finally, GraphQL (+extras) represent significant dependency, not that much in sheer size (it seems these days
      nobody cares for few MBs more or less) as much in terms of "cognitive load" on how much
      it takes to learn, master and debug projects based on such complicated 3rd party library. Not to mention that it
      can end up abandoned at any point in time, leaving you without support in the future
      ("That's not possible!", you say? Read my lips:
      [CORBA](https://en.wikipedia.org/wiki/Common_Object_Request_Broker_Architecture),
      [SOAP](https://en.wikipedia.org/wiki/SOAP),
      [RequestFactory](https://www.gwtproject.org/doc/latest/DevGuideRequestFactory.html),
      ...).

### Meet Nar

So, generic solutions are not easily achievable.

But, what if we don't need a "generic" solution? What if amount of own code required to accomplish nearly the same
functionality is equivalent to or possibly even smaller than what's required for integrating big external framework?

What if John Carmack was right, after all:

> _"Sometimes, the elegant implementation is just a function. Not a method. Not a class. Not a framework. Just a function."_

Nar is a bet that the solution to this problem can be rather simple if we do not try to make it generic.
If we commit ourselves to certain simple design patterns and introduce few simple lines of code in every class whose
properties we would like to selectively retrieve, we may easily end up with solution that both has *less boilerplate*
and *better performance* than any existing generic framework.

[Prove me wrong](https://github.com/gkresic/nar/issues).
