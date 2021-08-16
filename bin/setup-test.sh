#!/bin/bash

docker run --detach --publish 5440:5432 --env POSTGRES_DB=performance_psql --env POSTGRES_USER=datahike --env POSTGRES_PASSWORD=clojure postgres:12.4

docker run --detach --publish 3306:3306 --env MYSQL_RANDOM_ROOT_PASSWORD=true --env MYSQL_DATABASE=performance_msql --env MYSQL_USER=datahike --env MYSQL_PASSWORD=clojure mysql:8.0

docker run --detach --publish 4334-4336:4334-4336 --env ADMIN_PASSWORD="clojure" --env DATOMIC_PASSWORD="clojure" akiel/datomic-free:0.9.5703-3

