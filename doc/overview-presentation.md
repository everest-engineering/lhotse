# Endoscale

An event sourced DDD starter kit

(Suggest a better name!)

---

## Feature list

Based on Spring Boot and the Axon framework, this starter kit offers you:
 
 * Horizontal scalability: self forming clusters with distributed command processing

 * OAuth ready authentication 

 * Role based authorisation

 * Event replays

----

* Prometheus integration

* Deduplicating filestore abstractions for permanent and ephemeral files

* On demand thumbnail generation (and caching)
 
---


### Axon: DDD and event sourcing simplified

Previously known as Axon Framework, [Axon](https://axoniq.io/) is a framework for implementing 
[domain driven design](https://dddcommunity.org/learning-ddd/what_is_ddd/) using 
[event sourced](https://martinfowler.com/eaaDev/EventSourcing.html) 
[aggregates](https://www.martinfowler.com/bliki/DDD_Aggregate.html) and 
[CQRS](https://www.martinfowler.com/bliki/CQRS.html).

---

### DDD

DDD is, at its core, about _linguistics_. 

Emphasis on identifying conceptual boundaries, build simpler models with rich domain logic

Define [bounded contexts](https://www.martinfowler.com/bliki/BoundedContext.html) to avoid [anemic domain models](https://www.martinfowler.com/bliki/AnemicDomainModel.html) 


---

### Event sourcing + CQRS


Captures the history of every important _business action_ in an append-only event log

Events are mapped to an arbitrary number of projections for use by the query side of the system


---

#### Events can be replayed! 

* Projections can be optimised for reading by denormalising data

* Events can be _upcasted_

----

* Projections can be updated with new information, including historical events

* Projection safety net. Don't stress if you forget something 
(as long as you can derive it from an event)

----

#### Commands & events

User actions of interest to a business represented as _commands_ dispatched to command handlers

Command handlers perform validation and emit events


---

 
### Distributed command handling

Horizontal scalability via distributed command handling & event processing


----

AxonIQ would like you to rely on a commerically licensed Axon Server

Adds maintenance and configuration overhead


----

We created our own distributed command gateway using [Hazelcast](https://hazelcast.com/)


----


Self forming clusters, either via broadcast packets or via Kubernetes service discovery


* Deterministic command routing
* Clear ownership of aggregates 
* Automatic reasssignment
* In-memory caching

----
 
#### Command validation


* Commands represent _requests_
* Events are historical
* Robust command validation is critical to protect system integrity

----

Impractical to define aggregates so that they hold all data for validation


More natural aggregates are formed by allowing validation using _projections_

----

We use marker interfaces to map commands to dedicated validators

_Greatly_ simplifies testing and reduces human error


---

### Event processing
Axon provides two types of event processors: 
_subscribing_ and _tracking_


----


#### Subscribing event processors

* Execute on the same thread that is publishing the event

* Calling code dispatching the command can wait

* Commands are processed FIFO

* Not good for processing intensive tasks

---

#### Tracking event processors

* Execute in their own thread

* Monitor the event store for new events

* Use _tracking tokens_ to mark their position tailing the event log

* Handle ownership of tokens to avoid double handling

----

Introduce additional complexity by not guaranteeing that projections will be up to date when an API call has ended


----

Prefer to use them for

* Longer running tasks

* Preparing projections for a new feature release

* Doing replays

---
 
### Replays

Learn from history _and_ repeat it!


----

* Derive new data from historical events

* Recover from software errors

* Optimise for new query models (e.g. mobile)


----


#### Just do it

 * Disconnect the application from load balancers

 * Trigger a replay via a Spring actuator call

 * Monitor the state of replay via a Spring actuator endpoint 

 * Reconnect the application to load balancers 


----

Add your own custom hooks for wiping projections


---

## Security support

Build ons [Spring Security OAuth](https://projects.spring.io/spring-security-oauth/docs/oauth2.html)

Sets up both an _authorisation server_ and a _resource server_ (that is, us!)

----

Facilitates authentication and authorisation workflows based on [OAuth2](https://oauth.net/2/)

Stateless sessions using [Jason Web Tokens](https://jwt.io/) (JWT) for easy microservice extraction

---

### Access control

Controller end-points secured based on:

* User roles & properties

* Entity permissions


----

##### User Roles

Annotation based - coarse grained

Reduce repetition and improve readability


----

##### Entities

Annotation based - finer grained

Application layer entity (between persistance object and domain model object)

Permission check logic is provided by entity

----

##### Example

```
@PreAuthorize("#requestingUser.id == #userId 
               or hasPermission(#userId, 'User', 'update')")
public void updateUser(User requestingUser, 
	                   @PathVariable UUID userId, 
	                   @RequestBody @Valid UpdateUserRequest request) {
 ...
}
```

---


## File support

Two conceptual file stores: 

_permanent_ & _ephemeral_


----

Permanent == critical by virtue of being unrecoverable

Don't tamper with the evidence!

----

Ephemeral == generated on the fly or by event processing

Space is reclaimable and files may be replaced (by the app)

----

### Backing Stores

Need backing service such as a blob store or filesystem

Only supports [Mongo GridFS](https://docs.mongodb.com/manual/core/gridfs/) (as of today)

----

#### File Deduplication

Opague to callers

Store and file and it returns an ID mapping to a backing file

(Don't worry about the rest)

---

## Media support

Only one feature so far....

----

#### Thumbnails

* Generated on the fly

* Cached in the ephemeral store

---

 
## ETag HTTP headers


Enabled by default for all API endpoints via a shallow filter

---

## Missing Features

* GDPR compliance: annotations and disposable decryption keys to protect PII

* API error code enhancements to make it easier for UIs to consume error responses

* Internationalisation support

----

* (Optional) out of the box authorisation server

* Out of the box support for common OAuth2 authentication providers

* API rate limiting

----

* Additional cloud provider file stores

* Programmatic clearing of aggregate snapshots

---

## Tooling

* Java 11

* Gradle

* Lombok

* Semantic versioning

* Swagger

---


## Demo

 

