#!/bin/bash

set -e
set -u

function create_user_and_database() {
    local database=$1
    echo "  Creating database '$database'"
    psql -v ON_ERROR_STOP=1 --username "$MYSQL_USER" <<-EOSQL
        CREATE DATABASE $database;
EOSQL
}

if [ -n "$MYSQL_DATABASES" ]; then
    echo "Multiple database creation requested: $MYSQL_DATABASES"
    for db in $(echo "$MYSQL_DATABASES" | tr ',' ' '); do
        create_user_and_database "$db"
    done
    echo "Multiple databases created"
fi
