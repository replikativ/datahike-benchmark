#!/bin/bash

set -e
set -u

function create_user_and_database() {
    local database=$1
    echo "  Creating database $database"
    mysql -u "$MYSQL_USER" -e "create database $database"
}

if [ -n "$MYSQL_DATABASES" ]; then
    echo "Multiple database creation requested: $MYSQL_DATABASES"
    for db in $(echo "$MYSQL_DATABASES" | tr ',' ' '); do
        create_user_and_database "$db"
    done
    echo "Multiple databases created"
fi
