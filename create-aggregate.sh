#!/bin/env bash

if [[ $# -eq 0 ]] ; then
    echo Creates the directory structure for a new aggregate.
    echo "Usage: $0 <aggregate-name>"
    exit 1
fi

mkdir -pv src/$1/src/main/java
mkdir -pv src/$1/src/test/java
mkdir -pv src/$1-api/src/main/java
mkdir -pv src/$1-api/src/test/java
mkdir -pv src/$1-persistence/src/main/java
mkdir -pv src/$1-persistence/src/test/java

cat <<EOT >> src/$1/build.gradle
apply plugin: 'jacoco'

dependencies {
    api project(':$1-api')
    api "org.axonframework:axon-spring:\${axonVersion}"

    implementation project(':axon-support')
    implementation project(':common')
    implementation project(':command-validation-support')
    implementation project(':i18n-support')
    implementation project(':$1-persistence')

    implementation "engineering.everest.axon:crypto-shredding-extension:\${axonCryptoShreddingVersion}"
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

    testImplementation project(':axon-support').sourceSets.test.output
    testImplementation "org.junit.jupiter:junit-jupiter:\${junitVersion}"
    testImplementation "org.axonframework:axon-test:\${axonVersion}"
    testImplementation "org.mockito:mockito-junit-jupiter:\${mockitoVersion}"
    testImplementation "org.hamcrest:hamcrest-library:\${hamcrestVersion}"
}

EOT
echo "Created src/$1/build.gradle"


cat <<EOT >> src/$1-api/build.gradle
dependencies {
    api project(':common')
    api project(':command-validation-api')

    implementation "engineering.everest.starterkit:storage:\${storageVersion}"
    implementation "engineering.everest.axon:crypto-shredding-extension:\${axonCryptoShreddingVersion}"
    implementation "org.axonframework:axon-modelling:\${axonVersion}"
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    testImplementation "org.junit.jupiter:junit-jupiter:\${junitVersion}"
    testImplementation "org.mockito:mockito-junit-jupiter:\${mockitoVersion}"
}

EOT
echo "Created src/$1-api/build.gradle"


cat <<EOT >> src/$1-persistence/build.gradle
apply plugin: 'jacoco'

dependencies {
    api project(':$1-api')

    implementation "engineering.everest.starterkit:storage:\${storageVersion}"
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation "org.liquibase:liquibase-core:\${liquibaseVersion}"

    testImplementation 'org.springframework.boot:spring-boot-test-autoconfigure'
    testImplementation 'org.springframework:spring-test'
    testImplementation "org.junit.jupiter:junit-jupiter:\${junitVersion}"
    testImplementation "org.mockito:mockito-junit-jupiter:\${mockitoVersion}"
    testImplementation "org.postgresql:postgresql:\${postgresDriverVersion}"
    testImplementation "io.zonky.test:embedded-database-spring-test:\${zonkyEmbeddedDbVersion}"
}
EOT
echo "Created src/$1-persistence/build.gradle"
