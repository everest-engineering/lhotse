CREATE USER projections WITH PASSWORD 'projections' CREATEDB;
CREATE USER sessions WITH PASSWORD 'sessions' CREATEDB;
CREATE USER filemappings WITH PASSWORD 'filemappings' CREATEDB;
CREATE USER axon WITH PASSWORD 'axon' CREATEDB;
CREATE USER keycloak WITH PASSWORD 'keycloak' CREATEDB;

CREATE DATABASE axon;
CREATE DATABASE projections;
CREATE DATABASE filemappings;
CREATE DATABASE sessions;
CREATE DATABASE keycloak;

ALTER DATABASE axon owner to axon;
ALTER DATABASE filemappings owner to filemappings;
ALTER DATABASE projections owner to projections;
ALTER DATABASE sessions owner to sessions;
ALTER DATABASE keycloak owner to keycloak;
