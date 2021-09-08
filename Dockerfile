FROM    clojure:openjdk-11-tools-deps-1.10.3.943-buster

ENV     TAOENSSO_TIMBRE_MIN_LEVEL_EDN=':warn'
RUN     mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY    . /usr/src/app
RUN     clj -X:build

CMD rm "/tmp/signals/benchmarks-finished" \
    && echo "Signal file for finished benchmark creation deleted!"; \
    echo "Starting benchmarking at $(date)"; \
    java -jar datahike-benchmark.jar -e -u -t -p "/tmp/plots" -m "/tmp/errors" -i "2 2 2" -x "0 101 25" -y "0 11 5" -f "connection" \
    && touch "/tmp/signals/benchmarks-finished" \
    && echo "Signal for finished benchmark creation sent" \
    && echo "Finished benchmarking at $(date)";

# (-main "-e" "-b" "dh-mem-hht" "-t" "-i" "2 2 2" "-x" "0 101 25" "-y" "0 101 25" "-f" "connection")
# (-main "-e" "-t" "-i" "2 2 2" "-x" "0 101 25" "-y" "0 101 25" "-f" "connection")
# (-main "-e" "-t" "-i" "2 2 2" "-x" "0 101 25" "-y" "0 101 25" "-f" "transaction")
# (-main "-e" "-t" "-i" "2 2 2" "-x" "0 101 25" "-y" "0 101 25" "-f" "random-query")
# (-main "-e" "-t" "-i" "2 2 2" "-x" "0 101 25" "-y" "0 101 25")