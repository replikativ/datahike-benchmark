
FROM    clojure:lein

# Uses openjdk 11.0.6 and openjdk runtime environment 18.9

# Required for using profiler to measure space requirements (but failing due to read-only filesystem):
# RUN echo 1 | tee /proc/sys/kernel/perf_event_paranoid
# RUN echo 0 | tee /proc/sys/kernel/kptr_restrict

RUN     mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY    project.clj /usr/src/app/
RUN     lein deps
COPY    . /usr/src/app
RUN     mv "$(lein uberjar | sed -n 's/^Created \(.*standalone\.jar\)/\1/p')" app-standalone.jar

CMD rm "/tmp/signals/benchmarks-finished" \
    && echo "Signal file for finished benchmark creation deleted!"; \
    echo "Starting benchmarking at $(date)"; \
    java -jar app-standalone.jar -u "datahike:file:///tmp/output-db" -p "/tmp/plots" \
    && touch "/tmp/signals/benchmarks-finished" \
    && echo "Signal for finished benchmark creation sent" \
    && echo "Finished benchmarking at $(date)";
