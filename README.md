# datahike-benchmark
Commandline tool to run benchmarks and create visualizations for datahike backend performance comparisons.

## Prerequisites

Set up [PostgreSQL](https://www.postgresql.org/) and [mysql](https://www.mysql.com/) databases using [docker compose](https://docs.docker.com/compose/):
``` bash
docker-compose up postgres mysql
```

You can clean up the containers with:
``` bash
docker-compose down --volumes
```

### Time Measurements

No prerequisites.

Depending on your choice, this tool uses a macro similar to the built in [time](https://clojuredocs.org/clojure.core/time) function or [criterium](https://github.com/hugoduncan/criterium) for the time measurements, none of them require further installations.

### Space Measurements

The Java functions require no further installations.

The [clj-async-profiler](https://github.com/clojure-goes-fast/clj-async-profiler) based on a [JVM profiling tool](https://github.com/jvm-profiling-tools/async-profiler) used here to measure heap allocations requires HotSpot debug symbols. 

In the Oracle JDK they are already embedded, but for the Open JDK, the debug symbols have to be installed, e.g. by running

``` bash
sudo apt install openjdk-11-dbg
```

It might also be necessary to set
``` bash
echo 1 | sudo tee /proc/sys/kernel/perf_event_paranoid
echo 0 | sudo tee /proc/sys/kernel/kptr_restrict
```

If there are still problems occurring, please check the [profiler Github page](https://github.com/clojure-goes-fast/clj-async-profiler) and let us know, so we can update our instructions.


## Commandline Tool Usage 

``` bash
clj -M:run [options] 
```

Options:
 |                                | Description                                                                      | Default value                                |
 |--------------------------------|----------------------------------------------------------------------------------|----------------------------------------------|
 | -e, --crash-on-error           | Continue after error occurs                                                      | false                                        |
 | -D, --not-save-data            | Do not save raw benchmark output data                                            | false                                        |
 | -P, --not-save-plots           | Do not create plots                                                              | false                                        |
 | -a, --space-only               | Measure only heap allocations                                                    | false                                        |
 | -t, --time-only                | Measure only execution time                                                      | false                                        |
 | -c, --use-criterium            | Use criterium library for time measurements                                      | false                                        |
 | -j, --use-perf                 | Use perf events for space measurements                                           | false                                        |
 | -n, --data-dir                 | Data directory                                                                   | "./data"                                     |
 | -p, --plot-dir                 | Plot directory                                                                   | "./plots"                                    |
 | -m, --error-dir                | Error directory                                                                  | "./errors"                                   |
 | -u, --save-to-db URI           | Save results to datahike database with given URI instead of file                 | nil                                          |
 | -s, --seed SEED                | Initial seed for data creation                                                   | (rand-int)                                   |
 | -g, --time-step STEP           | Step size for measurements in ms. Used for measuring space with Java.            | 5                                            |
 | -d, --space-step STEP          | Step size for measurements in kB. Used for measuring space with Profiler.        | 5                                            |
 | -b, --only-database DBNAME     | Run benchmarks only for this database (library with backend); multi value        | #{}                                          |
 | -B, --except-database DBNAME   | Do not run benchmarks for this database (library with backend); multi value      | #{}                                          |
 | -l, --only-lib LIB             | Run benchmarks only for this library; multi value                                | #{}                                          |
 | -L, --except-lib LIB           | Do not run benchmarks for this library; multi value                              | #{}                                          |
 | -f, --only-function FUNCTION   | Function or database part to measure; multi value                                | #{}                                          |
 | -F, --except-function FUNCTION | Function or database part not to measure; multi value                            | #{}                                          |
 | -i, --iterations ITERATIONS    | Number of iterations of function measurements (ignored for criterium)            | {:connection 50, :transaction 10, :query 10} |
 | -x, --db-datom-count RANGE     | Range of numbers of datoms in database for which benchmarks should be run. Used in 'connection' and 'transaction'.   | :function-specific |
 | -y, --tx-datom-count RANGE     | Range of numbers of datoms in transaction for which benchmarks should be run. Used in 'transaction'.                 | :function-specific |
 | -z, --entity-count RANGE       | Range of numbers of entities in database for which benchmarks should be run. Used in 'random-query' and 'set-query'. | :function-specific |
 | -w, --ref-attr-count RANGE     | Range of numbers of attributes in entity for which benchmarks should be run. Used in 'random-query'.                 | :function-specific |
 | -h, --help                     |                                                                                   |                                             |

The indication 'multi value' indicated that this argument can be used multiple times. The values will be aggregated into a set. 

RANGE must be given as triple of integers 'start stop step' which are given as input for range function.
Example: 
``` bash
clj -M:run --db-datom-count "0 101 25" # (range 0 101 25) -> [0 25 50 75 100]
```

ITERATIONS must be given as string of space-separated integers of 
  1. connection 
  2. transaction and 
  3. query measurements
  
Example: 
``` bash
clj -M:run  --iterations "1 50 10" #  {:iterations {:connection 1, :transaction 50, :query 10}}
```

FUNCTION can be one of: 
- connection 
- transaction 
- random-query

LIB can be one of: 
- datahike 
- datalevin
- datascript
- hitchhiker

DBNAME can be one of:
 | id          | description                                          |
 |-------------|------------------------------------------------------|
 | dh-mem-hht  | datahike in-memory with hitchhiker-tree index        | 
 | dh-mem-set  | datahike in-memory with persistent set index         |
 | dh-file     | datahike with file backend and hitchhiker-tree index |
 | dh-psql     | datahike with Postgres and hitchhiker-tree index     |
 | dh-mysql    | datahike with MySQL and hitchhiker-tree index        |
 | dh-h2       | datahike with H2 in-memory and hitchhiker-tree index |
 | dh-level    | datahike with LevelDB and hitchhiker-tree index      |
 | hht-dat     | hitchhiker-tree directly using raw values            |
 | hht-val     | hitchhiker-tree directly using datoms                |
 | datascript  | datascript                                           |
 | datalevin   | datalevin                                            |

You can see the results as csv files in `./data` and as charts in `./plots`.

## Starting Full Run With Report creation

For full run renew volumes to get rid of old data, recreate the images and run docker-compose:

``` bash
cd bin
./recreate-docker-volumes.sh
docker-compose up --build --force-recreate
```

## Reproducing Errors

If an error occurs for a parameter configuration, on default the computations will not be stopped but skip the troubling configuration and create an error log.
The part of the computation where the error occurred can then be run again by using the exact options from the error log.
Most importantly, the seed has to be set like stated in the error log. Then, the data for testing will be exactly the same as in the faulty run so that you should be able to reproduce the error.


## Measuring Restrictions

connection
- The database *dat-mem* cannot be measured since reconnection after *db/release* is not possible. Therefore, connection on different database sizes cannot be compared.

random-query
- The *hitchhiker* library cannot be measured since queries are not applicable here.

## Commercial support

We are happy to provide commercial support with
[lambdaforge](https://lambdaforge.io). If you are interested in a particular
feature, please let us know.

## License

Copyright © 2020 Judith Massa

Licensed under Eclipse Public License (see [LICENSE](LICENSE)).
