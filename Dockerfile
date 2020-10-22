
FROM    clojure:lein
# Uses openjdk 11.0.6 and openjdk runtime environment 18.9

RUN     mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY    project.clj /usr/src/app/
RUN     lein deps
COPY    . /usr/src/app
RUN     mv "$(lein uberjar | sed -n 's/^Created \(.*standalone\.jar\)/\1/p')" app-standalone.jar

CMD rm "/tmp/signals/benchmarks-finished" \
    && echo "Signal file for finished benchmark creation deleted!"; \
    echo "Starting benchmarking at $(date)"; \
    java -jar app-standalone.jar -u "datahike:file:///tmp/output-db" -p "/tmp/plots" -b "dh-mem-hht" -t -i "2 2 2" -x "0 101 25" -y "0 101 25" \
    && touch "/tmp/signals/benchmarks-finished" \
    && echo "Signal for finished benchmark creation sent" \
    && echo "Finished benchmarking at $(date)";

# (-main "-p" "./plots" "-b" "dh-mem-hht" "-t" "-i" "2 2 2" "-x" "0 101 25" "-y" "0 101 25" "-f" "connection")
