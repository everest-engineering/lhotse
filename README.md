# Introduction
This project contains a starter kit for writing event sourced DDD application servers with CQRS. It's like using
JHipster but with less moustache.
 
The only end user functionality provided out of the box is basic support for creating organisations and users. The 
sample code is end-to-end, demonstrating the command handling and event processing flow from API endpoints down to 
projections.  

# Starter kit features

## Axon: DDD and event sourcing simplified (a little bit)
Previously known as Axon Framework, [Axon](https://axoniq.io/) is a framework for implementing 
[domain driven design](https://dddcommunity.org/learning-ddd/what_is_ddd/) using 
[event sourced])https://martinfowler.com/eaaDev/EventSourcing.html) 
[aggregates](https://www.martinfowler.com/bliki/DDD_Aggregate.html) and 
[CQRS](https://www.martinfowler.com/bliki/CQRS.html.

DDD is, at its core, about __linguistics__. Establishing a ubiquitous language helps identify sources of overlap or 
tension in conceptual understanding that __may be__ indicative of a separation of concern in a system. Rather than
attempting to model a domain in intricate detail inside a common model, DDD places great emphasis on identifying these
boundaries in order to define [bounded contexts](https://www.martinfowler.com/bliki/BoundedContext.html). These reduce
complexity of the system by avoiding [anemic domain models](https://www.martinfowler.com/bliki/AnemicDomainModel.html) 
due to a slow migration of complex domain logic from within the domain model to helper classes on the periphery as the 
system evolves.

Event sourcing captures the activities of a business in an event log, an append-only history of every important 
__business action__ that has ever been taken by users or by the system itself. Events are mapped to an arbitrary number 
of projections for use by the query side of the system. Being able to replay events offers several significant benefits:

* Projections can be optimised for reading by denormalising data
* Events can be __upcasted__. That is, events are marked with a revision that allows them to be transformed to an updated
  version of the same event. This protects developers from creating significant errors in users' data due to, for example,
  accidentally transposing two fields within a command or event;
* Projections can be updated with new information that was either captured by or derived from events. New business 
  requirements can be met and projections generated such that historical user actions can be incorporated as new 
  features are rolled out;

Axon provides the event sourcing framework. User actions of interest to a business (typically anything that modifies data)
are dispatched to command handlers that, after validation, emit events.
 
### Distributed command handling
Part of Axon's appeal is the ability to horizontally scale an application. A typical Axon deployment will rely on Axon 
Server to implement command and event dispatching, and event log persistence. A commercial license of Axon Server is 
needed, however, to fully support distributed command processing. Axon Server also adds maintenance and configuration 
overhead to any deployment.

We have avoided the dependency on Axon Server in the `axon-support` module by wrapping the standard Axon command gateway 
with a custom [Hazelcast](https://hazelcast.com/) based distributed command gateway. An arbitrary number of application 
instances started together, either on the same network or within a Kubernetes cluster, will be automatically discovered
to form a cluster. 

Commands dispatched through the Hazelcast command gateway are deterministically routed to a single application instance 
which, based on the aggregate identifier, has ownership of an aggregate for as long as that instance remains a member of
the cluster. This clear aggregate ownership is vital to avoid a split brain scenario as aggregates are cached in memory
and are responsible for command validation. A split brain situation could arise if multiple unsynchronised copies were 
to be distributed among cluster members. 

Hazelcast will automatically reassign aggregate ownership if an application instance leaves the cluster due to a restart,
network disconnection or other failure.

Events emitted by an aggregate are passed to the application instance's local event bus. Events are persisted to the 
event log by the instance handling the command. Subscribing event processing (the default in our configuration) also 
guarantees that the same instance will be performing the event handling. 
 
### Command validation
Commands represent user actions that may be rejected by the system. Events, however, represent historical events and can
not be rejected (though, they can be upcasted or ignored depending on circumstances). It is therefore vital that 
robust command validation be performed to protect the integrity of the system.
 
If events are ever emitted in error then this creates a situation that should only be addressed by generating events
countering the erroneous ones. This, naturally, comes with a significant cost in terms of implementation and validation
overhead.

There is an philosophical argument for defining aggregates such that all information required to validate commands is 
held by an aggregate in memory. In practice, however, more natural aggregates can be formed by allowing some validation 
to be based on __projected__ data. We also know from experience that some validation will shared among multiple 
aggregates. The amount of testing required to verify all possible command failure situations tends to grow non-linearly 
as the number of checks that are performed inside an aggregate grows. 

We have addressed this in the `axon-support` and `command-validation-suport` modules through the introduction of marker 
interfaces that map commands to dedicated command validators. Validators extract common checks or checks based on 
projections and allow them to be tested independently. Aggregate tests just need to ensure that a failure in the validator 
fails command validation. Since this design opens up the possibility of a validator to be missed, reflection is used to 
detect validators at application start up and register them with a command interceptor that is triggered prior to the 
command handler method being called. This significantly reduces testing effort and human error.

### Event processing
Axon provides two types of event processors: 
[subscribing and tracking](https://docs.axoniq.io/reference-guide/configuring-infrastructure-components/event-processing/event-processors).

Subscribing processors execute on the same thread that is publishing the event. This allow command dispatching to wait 
until the event has been both appended to the event store and all event handling as completed. Commands are queued for
processing on a FIFO basis. It is important, therefore, to not use them for long running tasks.  

Tracking event processors (TEPs), in contrast, execute in their own thread, monitoring the event store for new events. 
TEPs track their progress consuming events using tracking tokens persisted in the database. TEPs hold ownership of the 
tokens, preventing multiple application instances from concurrently performing the same processing. Token ownership 
passes to another application instance in the event that a token owner is shut down or restarted.  

TEPs introduce additional complexity by not guaranteeing that projections will be up to date when an API call has ended. 
TEPs should, in our opinion, be only used for longer running processing, during replays and when preparing projections 
for new feature releases. 

Axon also introduces the concept of processing groups as a way of segmenting and orchestrating event processing by 
ensuring that events are handled sequentially within a group. By default, Axon assigns each tracking event processor
(TEP) to its own processing group, aiming to parallelise event processing as much as possible. We take a more 
conservative approach to make the system easier to reason about by defaulting to subscribing event processing and 
assigning all event processors to a default processing group.  

### Event replays
Event replaying takes the system back to a previous point in time in order to apply a different interpretation of what
it means to process an event.  

The simplest way of executing an replay is to wipe all projections and then reapply every event ever emitted so as to 
rebuild projections using the latest logic. This is a valid approach for fledgling applications but may not be acceptable 
once the system has scaled up. More advanced approaches are made possible by assigning event processors to different 
processing groups and running a mixture of subscribing and tracking event processors. Advanced configuration opens up 
the possibility of:

 * Replaying events into a new projection database while continuing to project to an existing one, making the replay
   transparent to end users. The system can then be switched over to use the new projection while optionally continuing 
   to maintain the old one.
 * Tracking event processing can be used to generate projections for new features that are not yet released to users 
   until the projections are ready for use.
 * Processing groups allow replays to be limited to bounded contexts that are naturally isolated. 

The starter kit currently lacks programmatic support for triggering replays. Once this is implemented, the steps to 
perform a replay will be:
 
 * disconnect the application from load balancers
 * trigger a replay via a JMX or Spring actuator call
 * monitor the state of replay via a Spring actuator endpoint 
 * reconnect the application to load balancers 

However, as of right now we need to:

 * disconnect the application from load balancers
 * shut down the application nodes
 * change the event processing configuration from subscribing event processors to 
   [tracking event processors](https://axoniq.io/blog-overview/tracking-event-processors)
 * clear the tracking tokens (if present) in the Axon database
 * optionally clear aggregate snapshots from the Axon database
 * clear our projections
 * start up the application 
 * monitor the logs and the tracking tokens to determine when tracking event processors have caught up with
 * change the event processing configuration back to subscribing event processing
 * restart the application to apply the configuration change
 * reconnect the application to load balancers 


## Endpoint access control (TODO)
- annotations
- follow the pattern!
- "identifiable" entities (not aggregates, not projections) answer the actual question

## File storage support
The `file-service` module implements two file stores: one is referred to as _permanent_, the other as the _artefact_ store. 
The permanent file store is for storing critical files that, such as user uploads, cannot be recovered. The artefact store 
is for non-critical files that can be regenerated by the system either dynamically or via an event replay.   

File stores need backing service such as a blob store or filesystem. This starter kit only supports 
[Mongo GridFS](https://docs.mongodb.com/manual/core/gridfs/) at time of writing.

Our Mongo GridFS backed file store implementation automatically deduplicates files. Storing a file whose contents matches 
a previous file will return a (new) file identifier mapping to the original. The most recently stored file will then be
silently removed. 

## Thumbnail support
The `thumbnail-support` module generates thumbnail images on the fly, caching them in the artefact file store for 
subsequent requests. Thumbnail sizes are limited to prevent the system from being overwhelmed but no API rate limiting is 
applied yet. Consider adding it and contributing back to this repository!

## Security support (TODO)


## ETag HTTP headers
[ETag HTTP headers](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/ETag) are enabled by default for all API 
endpoints via a shallow filter. The filter is unaware of any changes made to underlying data so while clients may use the
ETag to avoid unnecessary transfers and client side processing, there is no benefit to application server performance.


# Tooling
This project uses [Java 11](https://openjdk.java.net/projects/jdk/11/).

The container system is [Docker](https://www.docker.com/).

[Project Lombok](https://projectlombok.org/) greatly reduces the need for hand cranking tedious boilerplate code.

The build system is [Gradle](https://gradle.org/) and it supports all of the above.

## IntelliJ configuration
The [Lombok plugin](https://plugins.jetbrains.com/plugin/6317-lombok/) is required for IntelliJ, else the code generated 
by the Lombok annotations will not be visible (and the project will be littered with red squiggle line errors). 

## Building
To build the entire application, including running unit and functional tests:

`./gradlew build`

(Note that functional tests share the same port number for embedded database as for the containerised database, if 
tests fail try running `./gradlew composeDown` first).
 
To run the application server, including starting up containers Postgres and MongoDB:

`./gradlew bootRun`

To create a docker image:

`./gradlew dockerBuild`

To see all available Gradle tasks for this project:

`./gradlew tasks`

## Semantic versioning
[Semantic versioning](https://semver.org/) is automatically applied using git tags. Simply create a tag of, say, `1.2.0` 
and Gradle will build a JAR package (or a docker container, if you wish) tagged with version `1.2.0-0` where `-0` is the 
number of commits since the `1.2.0` tag was applied. Each subsequent commit will increment the version (`1.2.0-1`, 
`1.2.0-2`, ... `1.2.0-N`) until the next release is tagged.   

## Jupyter notebook
An initial [Jupyter notebook](https://jupyter.org/) can be found in 'doc/notebook'. It acts as an interactive 
reference for the API endpoints and should be in your development workflow. A perfect replacement for Postman! Use it to 
try out the application, today!

(The notebook uses the [IRuby](https://github.com/sciruby/iruby) for Jupyter so go ahead and get that set up first).       

## Swagger documentation
Swagger API documentation is automatically generated by [Springfox](https://springfox.github.io/springfox/docs/current/).

API documentation is accessible when running the application locally by visiting 
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Functional tests generate a Swagger JSON API definition at `./launcher/build/web-app-api.json`

!!! TODO !!! swagger docs seem to be broken

## Checkstyle and PMD configuration
[PMD](https://pmd.github.io/) and [Checkstyle](https://checkstyle.org/) quality checks are automatically applied to all 
sub-projects.
