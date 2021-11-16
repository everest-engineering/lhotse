# Welcome!

[![Build status](https://badge.buildkite.com/b44f55806fca0e349ecc8d470fe0fdcea8f49c1375f33e86d5.svg?branch=main)](https://buildkite.com/everest-engineering/lhotse) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=everest-engineering_lhotse&metric=alert_status)](https://sonarcloud.io/dashboard?id=everest-engineering_lhotse)

This is Lhotse, a starter kit for writing event sourced web applications following domain driven design principles.
It is based on [Spring Boot](https://spring.io/projects/spring-boot), [Axon](https://axoniq.io/) and
[Hazelcast](https://hazelcast.com/).

Whether you're starting a new project or refactoring an existing one, you should consider this project if you're seeking:

- horizontal scalability via self forming clusters with distributed command processing
- crypto-shredding support for your event log to address privacy regulations such as the GDPR
- Axon's awesome event sourcing and [CQRS](https://martinfowler.com/bliki/CQRS.html) features
- deduplicating filestore abstractions for a variety of backing stores such as S3 buckets and Mongo GridFS
- role based authorisation
- Spring Boot features such as OAuth ready authorisation and Prometheus integration

... without the time and effort involved in starting a new project from scratch.

The only end user functionality provided out of the box is basic support for creating organisations and users. The
sample code demonstrates end-to-end command handling and event processing flows from API endpoints down to projections.

# Table of Contents

- [Tooling](#tooling)
  - [IntelliJ configuration](#intellij-configuration)
  - [Building](#building)
  - [Semantic versioning](#semantic-versioning)
  - [Jupyter notebook](#jupyter-notebook)
  - [Swagger documentation](#swagger-documentation)
  - [Checkstyle and PMD configuration](#checkstyle-and-pmd-configuration)
- [Features](#features)
  - [Axon: DDD and event sourcing](#axon-ddd-and-event-sourcing)
  - [Distributed command handling](#distributed-command-handling)
  - [Command validation](#command-validation)
  - [Event processing](#event-processing)
  - [Event replays](#event-replays)
  - [Crypto shredding](#crypto-shredding)
  - [Security and access control](#security-and-access-control)
    - [User and Keycloak authentication token](#user-and-authentication-token)
    - [Endpoint access control](#endpoint-access-control)
  - [File support](#file-support)
    - [Configuring Mongo GridFS](#configuring-mongo-gridfs)
    - [Configuring AWS S3](#configuring-aws-s3)
  - [Media support](#media-support)
  - [ETag HTTP headers](#etag-http-headers)
- [Project Info](#project-info)
  - [Maintainers](#maintainers)
  - [Contributing](#contributing)
  - [License](#license)

# Tooling

This project uses [Java 11](https://openjdk.java.net/projects/jdk/11/).

The container system is [Docker](https://www.docker.com/).

[Project Lombok](https://projectlombok.org/) greatly reduces the need for hand cranking tedious boilerplate code.

The build system is [Gradle](https://gradle.org/) and it supports all the above.

## IntelliJ configuration

The [Lombok plugin](https://plugins.jetbrains.com/plugin/6317-lombok/) is required for IntelliJ, else the code generated
by the Lombok annotations will not be visible (and the project will be littered with red squiggle line errors).

## Building

To build the entire application, including running unit and functional tests:

`./gradlew startServer && ./gradlew build`

(Note that functional tests share the same port number for embedded database as for the containerised database, if
tests fail try running `./gradlew composeDown` first. Free-up port for the keycloak test server as well).

Create a `.env` file in the project root directory and do the env variables setup for keycloak:

```
KEYCLOAK_SERVER_PORT=8180
KEYCLOAK_USER=admin@everest.engineering
KEYCLOAK_PASSWORD=******
```

(Note: the above env variable values should match with the keycloak server details specified in the `application.properties` file and the `build.gradle` file present in the launcher module.
Keycloak test server also uses the same credentials for authentication.)

Start up containers for Postgres, MongoDB and Keycloak:

`docker-compose up`

This project uses keycloak for authentication and authorization. `docker-compose up` will run the keycloak container on the `KEYCLOAK_SERVER_PORT` which is specified in the `.env` file.

#### Client secret

We need to get the client secret from the keycloak server and configure it in the `application.properties` for `keycloak.credentials.secret` prop.

Open the http://localhost:8180/auth url and login to app using `KEYCLOAK_USER` and `KEYCLOAK_PASSWORD`.

Select the `default` realm and go to the `Clients` tab.

Select the `default-client` under Clients tab.

Go to the `Credentials` tab and regenerate the client secret if required.
Get the `secret` value and add it in the `application.properties` as mentioned above.

To run the application server using Gradle:

`./gradlew bootRun`

To create a docker image:

`./gradlew bootBuildImage`

To run the application server container with a TTY attached, allocating 2GiB memory and applying the `prod` Spring profile:

`docker run -t -m 2G --network host -e "SPRING_PROFILES_ACTIVE=prod" your.organisation.here/lhotse:$BUILD_VERSION`

To see all available Gradle tasks for this project:

`./gradlew tasks`

To run the OWASP dependency check plugin, which will generate a report at `build/reports/dependency-check-report.html`:

`./gradlew dependencyCheckAggregate`

To run the dependencies license plugin, which will generate a report at `build/reports/dependency-license/index.html`:

`./gradlew generateLicenseReport`

## Semantic versioning

[Semantic versioning](https://semver.org/) is automatically applied using git tags. Simply create a tag of, say, `1.2.0`
and Gradle will build a JAR package (or a docker container, if you wish) tagged with version `1.2.0-0` where `-0` is the
number of commits since the `1.2.0` tag was applied. Each subsequent commit will increment the version (`1.2.0-1`,
`1.2.0-2`, ... `1.2.0-N`) until the next release is tagged.

## Jupyter notebook

An initial [Jupyter notebook](https://jupyter.org/) can be found in 'doc/notebook'. It acts as an interactive
reference for the API endpoints and should be in your development workflow. Note that this requires
[IRuby](https://github.com/sciruby/iruby).

Jupyter notebook can be run as a Docker container:

`docker-compose -f doc/notebook/docker-compose.yml up`.

## Swagger documentation

Swagger API documentation is automatically generated by [Springfox](https://springfox.github.io/springfox/docs/current/).

API documentation is accessible when running the application locally by visiting
[Swagger UI](http://localhost:8080/swagger-ui/index.html). Default credentials for logging in as an administrator can
be found in `application.properties` along with the client ID and client secret.

Functional tests generate a Swagger JSON API definition at `./launcher/build/web-app-api.json`

## Checkstyle and PMD configuration

[PMD](https://pmd.github.io/) and [Checkstyle](https://checkstyle.org/) quality checks are automatically applied to all
sub-projects.

# Features

## Axon: DDD and event sourcing

Previously known as Axon Framework, [Axon](https://axoniq.io/) is a framework for implementing
[domain driven design](https://dddcommunity.org/learning-ddd/what_is_ddd/) using
[event sourced](https://martinfowler.com/eaaDev/EventSourcing.html)
[aggregates](https://www.martinfowler.com/bliki/DDD_Aggregate.html) and
[CQRS](https://www.martinfowler.com/bliki/CQRS.html).

DDD is, at its core, about _linguistics_. Establishing a ubiquitous language helps identify sources of overlap or
tension in conceptual understanding that _may be_ indicative of a separation of concern in a system. Rather than
attempting to model a domain in intricate detail inside a common model, DDD places great emphasis on identifying these
boundaries in order to define [bounded contexts](https://www.martinfowler.com/bliki/BoundedContext.html). These reduce
complexity of the system by avoiding [anemic domain models](https://www.martinfowler.com/bliki/AnemicDomainModel.html)
due to a slow migration of complex domain logic from within the domain model to helper classes on the periphery as the
system evolves.

Event sourcing captures the activities of a business in an event log, an append-only history of every important
_business action_ that has ever been taken by users or by the system itself. Events are mapped to an arbitrary number
of projections for use by the query side of the system. Being able to replay events offers several significant benefits:

- Projections can be optimised for reading by denormalising data
- Events can be _upcasted_. That is, events are marked with a revision that allows them to be transformed to an updated
  version of the same event. This protects developers from creating significant errors in users' data due to, for example,
  accidentally transposing two fields within a command or event;
- Projections can be updated with new information that was either captured by or derived from events. New business
  requirements can be met and projections generated such that historical user actions can be incorporated as new
  features are rolled out;

Axon provides the event sourcing framework. User actions of interest to a business (typically anything that modifies data)
are dispatched to command handlers that, after validation, emit events.

## Distributed command handling

Part of Axon's appeal is the ability to horizontally scale an application using Axon Server for command and event
dispatching, and event log persistence. While well suited to applications that require massive scale, deploying Axon
Server introduces additional maintenance and configuration overhead.

We have taken a different approach that provides a good starting point for small to medium applications while still
allowing migration to Axon Server in the future. Horizontal scalability is achieved via a
[command distribution extension](https://github.com/everest-engineering/axon-command-distribution-extension) that wraps
the standard Axon command gateway with a [Hazelcast](https://hazelcast.com/) based distributed command gateway.
An arbitrary number of application instances started together, either on the same network or within a Kubernetes cluster,
will be automatically discovered to form a cluster.

Commands dispatched through the Hazelcast command gateway are deterministically routed to a single application instance
which, based on the aggregate identifier, has ownership of an aggregate for as long as that instance remains a member of
the cluster. This clear aggregate ownership is vital to avoid a split brain scenario as aggregates are cached in memory
and are responsible for command validation. A split brain situation could arise if multiple unsynchronised copies were
to be distributed among cluster members.

Hazelcast will automatically reassign aggregate ownership if an application instance leaves the cluster due to a restart,
network disconnection or other failure.

Events emitted by an aggregate are passed to the application instance's local event bus. Events are persisted to the
event log by the instance handling the command. Subscribing event processing (the default in our configuration) guarantees
that the same instance will be performing the event handling.

## Command validation

Commands represent user actions that may be rejected by the system. Events, however, represent historical events and
cannot be rejected (though, they can be upcasted or ignored depending on circumstances). It is therefore vital that
robust command validation is performed to protect the integrity of the system.

If events are ever emitted in error then this creates a situation that should only be addressed by generating events
countering the erroneous ones. This, naturally, comes with a significant cost in terms of implementation and validation
overhead.

There is a philosophical argument for defining aggregates such that all information required to validate commands is
held by an aggregate in memory. In practice, however, more natural aggregates can be formed by allowing some validation
to be based on _projections_. We also know from experience that some validation will be shared among multiple aggregates.
The amount of testing required to verify all possible command failure situations tends to grow non-linearly as the number
of checks that are performed inside an aggregate grows.

We have addressed this in the `axon-support` and `command-validation-suport` modules through the introduction of marker
interfaces that map commands to dedicated command validators. Validators extract common checks or checks based on
projections and allow them to be tested independently. Aggregate tests just need to ensure that a failure in the validator
fails command validation. Since this design opens up the possibility of a validator to be missed, reflection is used to
detect validators at application start up and register them with a command interceptor that is triggered prior to the
command handler method being called. This significantly reduces testing effort and human error.

## Event processing

Axon provides two types of event processors,
[subscribing and tracking](https://docs.axoniq.io/reference-guide/configuring-infrastructure-components/event-processing/event-processors).

Subscribing processors execute on the same thread that is publishing the event. This allows command dispatching to wait
until the event has been both appended to the event store and all event handling is completed. Commands are queued for
processing on a FIFO basis. It is important, therefore, to not use them for long running tasks.

Tracking event processors (TEPs), in contrast, execute in their own thread, monitoring the event store for new events.
TEPs track their progress consuming events using tracking tokens persisted in the database. TEPs hold ownership of the
tokens, preventing multiple application instances from concurrently performing the same processing. Token ownership
passes to another application instance in the event that a token owner is shutdown or restarted.

TEPs introduce additional complexity by not guaranteeing that projections will be up to date when an API call has ended.
TEPs should, in our opinion, be only used for longer running processing, during replays and when preparing projections
for new feature releases.

Axon also introduces the concept of processing groups as a way of segmenting and orchestrating event processing, ensuring
that events are handled sequentially within a group. By default, Axon assigns each tracking event processor (TEP) to its
own processing group, aiming to parallelise event processing as much as possible. We take a more conservative approach to
make the system easier to reason about by defaulting to subscribing event processors and assigning them to a default
processing group unless explicitly assigned elsewhere.

## Event replays

Event replaying takes the system back to a previous point in time in order to apply a different interpretation of what
it means to process an event.

The simplest way of executing a replay is to wipe all projections and then reapply every event ever emitted to
rebuild using the latest logic. This is a valid approach for fledgling applications but may not be acceptable once the
system has scaled up. More advanced approaches are made possible by assigning event processors to different processing
groups and running a mixture of subscribing and tracking event processors. Advanced configuration opens up the
possibility of:

- Replaying events into a new projection database while continuing to project to an existing one, making the replay
  transparent to end users. The system can then be switched over to use the new projection while optionally continuing
  to maintain the old one.
- Tracking event processors can be used to generate projections for new features that are not yet released to users
  until the projections are ready for use.
- Processing groups allow replays to be limited to bounded contexts that are naturally isolated.

The starter kit comes with programmatic support for triggering replays. To perform a replay:

- disconnect the application from load balancers
- trigger a replay via a [Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/) call to `/actuator/replay`
- monitor the state of replay via a Spring actuator endpoint
- reconnect the application to load balancers

Behind the scenes, replays are being executed by:

- shutting down [tracking event processors](https://axoniq.io/blog-overview/tracking-event-processors) (TEP) processing;
- clearing the tracking tokens in the Axon database;
- placing a marker event into the event log that will end replays; and
- resstarting TEP processing.

## Crypto shredding

Crypto shredding is a technique for disabling access to sensitive information by discarding encryption keys. You might use
this on the behest of a user or when retention is no longer justified in order to comply with the European Union's General
Data Protection Regulation (GDPR) without compromising the append-only nature of your event log.

Documentation in the [crypto shredding](https://github.com/everest-engineering/axon-crypto-shredding-extension) repository
explains how it works, its limitations and an important caveat.

## Security and access control

We are using [Keycloak](https://www.keycloak.org/) to manage users' authentication, authorization, and session management.

Keycloak has the following three main concepts.

- _Realm_: A realm secures and manages security metadata for a set of users, applications, and registered auth clients. By default Keycloak will provide us with a `master` realm but using the `master` realm for the admin users is the best practice so we have created a new realm called `default` and all the app users will be managed from it.
- _Client_: Clients are entities that can request authentication of a user within a realm. By default, Keycloak will provide us with a few clients but using a separate client is the best practice. So we have created `default client` for the `default` realm. Using it we can access the Keycloak APIs from outside of its admin console.
- _Role_: Roles identify a type or category of user. Keycloak often assigns access and permissions to specific roles rather than individual users for fine-grained access control. Currently, we using the `ADMIN` role for admin users.

_Note: Our app has some internal roles for managing the user's access levels but those roles don't have any relation with Keycloak roles._

### User and Token

A user object in the business domain often requires more attributes than a user object from the Keycloak authentication token.
For an example, the starter kit's user object has an extra `organizationId` attribute, to authorize the user, we added this info in the Keycloak authentication token as other claims, and we can access these claims like remaining claims of a user object.

### Endpoint access control

Controller end-points are secured based on user roles, properties and entity permissions. Annotations are used to
configure the access control for each handler method. To reduce repetition and improve readability, a few
meta-annotations are created for common security configuration, e.g. `AdminOrAdminOfTargetOrganization`. There are
situations when user roles and properties are not sufficient to determine the access control. This is where the entity
permission check comes in. An entity in this case is a representation of domain object in the application layer. It
corresponds to at least one persistable object. For an example, one `Organization` entity corresponds to one
`PersistableOrganization`. To put it simply in the event sourcing context, it can be just considered as the projection.

The entity permission check is specified within the security annotation and takes the form of `hasPermission(#entityId, 'EntityClassName', 'permissionType')`. This expression is evaluated by `EntityPermissionEvaluator`, which in turn
delegates to corresponding permission check methods of an entity, where customized permission requirements can be
implemented. This workflow is made possible by: a) having all entity classes implementing the `Identifiable` interface
and b) having a `ReadService` for each `Identifable` entity. The `Identifiable` interface provides default _reject all_
permission checks which can be overridden by implementing entities. The `ReadService` provides a way to load an entity
by its simple class name. To help managing increasing number of `ReadService`, the starter kit provides a
`ReadServiceProvider` bean which collects all `ReadService` beans during start of the application context.

When adding new controllers and security configurations, it is important to refer to existing patterns and ensure
consistency. This also applies to tests where fixtures are provided to support the necessary _automagic_ behaviours.

## File support

The [storage](https://github.com/everest-engineering/lhotse-storage) module implements two file stores: one is referred
to as _permanent_, the other as the _ephemeral_ store. The permanent file store is for storing critical files that, such
as user uploads, cannot be recovered. The ephemeral store is for non-critical files that can be regenerated by the system
either dynamically or via an event replay.

Our file store implementation automatically deduplicates files. Storing a file whose contents matches
a previous file will return a (new) file identifier mapping to the original. The most recently stored file will then be
silently removed.

File stores need backing service such as a blob store or filesystem. This starter kit supports
[Mongo GridFS](https://docs.mongodb.com/manual/core/gridfs/) and AWS S3 at time of writing.

### Configuring Mongo GridFS

Set the application property:

```
application.filestore.backend=mongoGridFs
```

### Configuring AWS S3

Set the following application properties:

```
application.filestore.backend=awsS3
application.filestore.awsS3.buckets.permanent=sample-bucket-permanent
application.filestore.awsS3.buckets.ephemeral=sample-bucket-ephemeral
```

We rely on [DefaultAWSCredentialsProviderChain](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/DefaultAWSCredentialsProviderChain.html)
and [DefaultAwsRegionProviderChain](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/regions/DefaultAwsRegionProviderChain.html)
for fetching AWS credentials and the AWS region.

## Media support

The [media](https://github.com/everest-engineering/lhotse-media) module adds additional support for managing of image and
video updates. It generates thumbnail images on the fly, caching them in the ephemeral file store for subsequent requests.
Thumbnail sizes are limited to prevent the system from being overwhelmed by malicious requests.

## ETag HTTP headers

[ETag HTTP headers](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/ETag) are enabled by default for all API
endpoints via a shallow filter. The filter is unaware of any changes made to underlying data so while clients may use the
ETag to avoid unnecessary transfers and client side processing, there is no benefit to application server performance.

# Project Info

## Maintainers

[@sluehr](https://github.com/sluehr), [@ywangd](https://github.com/ywangd)

## Contributing

We appreciate your help!

[Open an issue](https://github.com/everest-engineering/lhotse/issues/new/choose) or submit a pull request for an enhancement.
You may want to view the [project board](https://github.com/orgs/everest-engineering/projects/1) or browse through the
[current open issues](https://github.com/everest-engineering/lhotse/issues).

## License

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

[![License: EverestEngineering](https://img.shields.io/badge/Copyright%20%C2%A9-EVERESTENGINEERING-blue)](https://everest.engineering)

> Talk to us `hi@everest.engineering`.
