#!/bin/bash

nohup docker-compose up postgres mysql &

clj -M:run -e -t -i "2 2 2" -x "0 3 1" -y "0 3 1" -f "connection"
clj -M:run -e -t -i "2 2 2" -x "0 3 1" -y "0 3 1" -f "transaction"
clj -M:run -e -t -i "2 2 2" -x "0 101 25" -y "0 101 25" -f "random-query"
