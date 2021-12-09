# Introduction

This directory contains the definition for the `default` realm and basic customisation of the self-registration screen. The default Keycloak
self registration screen asks users for a first and last name. Our preference is
to [be more flexible](https://www.kalzumeus.com/2010/06/17/falsehoods-programmers-believe-about-names/).

This sample project takes a minimalistic approach to configuration. Any pre-canned configuration is likely to be unsuitable for your
situation given the myriad of ways that Keycloak can be used.

Note that we _currently_ run Keycloak in both a container and directly from its distribution archive. The latter is required due to
limitations in our CI/CD pipeline. We hope to be able to standardise on the containerised approach in the future.

# Command line administration

Keycloak ships with a `kcadm.sh` script which launchers the administrative CLI jar. The CLI is _stateful_
in that it requires a configuration file containing configuration and credential information to execute tasks.

See the [official CLI documentation](https://www.keycloak.org/docs/latest/server_admin/#the-admin-cli) for full details.

## Running the CLI

If you have not installed Keycloak locally, you can run `kcadm.sh` directly within the Keycloak container:

```
docker exec keycloak /opt/jboss/keycloak/bin/kcadm.sh
```

Or via a locally cached copy created when running `./gradlew keycloakServer`:

```
/tmp/keycloak-$version/bin/kcadm.sh
```

## Creating CLI configuration

To create a configuration for the `master` realm (assuming that Keycloak has been configured to listen on port 8180) using the default
account created when the container is bootstrapped:

```
docker exec keycloak /opt/jboss/keycloak/bin/kcadm.sh config credentials \
    --server http://172.21.0.3:8180/auth \
    --realm master \
    --user admin@everest.engineering \
    --password ac0n3x72  \
    --config /tmp/master-admin.config
```

Keycloak only binds to the container's internal IP address and not localhost. You can find this address by inspecting the container:

```
docker container inspect keycloak | grep IPAddress
```
