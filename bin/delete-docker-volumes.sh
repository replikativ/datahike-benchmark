#!/bin/bash

docker container prune -f

docker volume rm dh-benchmark-db
docker volume rm dh-benchmark-plots
docker volume rm dh-benchmark-errors
docker volume rm dh-benchmark-presentation
docker volume rm dh-benchmark-signals