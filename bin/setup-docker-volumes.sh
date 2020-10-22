#!/bin/bash

docker volume create --name=dh-benchmark-db
docker volume create --name=dh-benchmark-plots
docker volume create --name=dh-benchmark-errors
docker volume create --name=dh-benchmark-presentation
docker volume create --name=dh-benchmark-signals
