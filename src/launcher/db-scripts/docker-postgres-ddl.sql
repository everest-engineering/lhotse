CREATE USER lhotse WITH PASSWORD 'lhotse' CREATEDB;
CREATE USER keycloak WITH PASSWORD 'keycloak' CREATEDB;

CREATE DATABASE lhotse;
CREATE DATABASE keycloak;

ALTER DATABASE lhotse owner to lhotse;
ALTER DATABASE keycloak owner to keycloak;
