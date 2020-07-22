
FROM    clojure:lein
RUN     mkdir -p /usr/src/app
WORKDIR /usr/src/app
#COPY    project.clj /usr/src/app/
#RUN     lein deps
#RUN     apt-get update && apt-get install -y curl
#RUN     curl -sL https://deb.nodesource.com/setup_8.x | bash -
#RUN     apt-get update && apt-get install -y nodejs
#RUN     npm -v
#RUN     npm install -g vega vega-lite vega-cli
COPY    . /usr/src/app

CMD while ! test -e "/tmp/signals/benchmarks-finished"; \
    do \
       echo "Waiting for benchmarking to finish..."; \
       sleep 30s; \
    done; \
    echo "Signal received! Starting creation of result presentation..."; \
    echo "Starting report creation at $(date)"; \
#    lein run "/tmp/presentation/" \
#    &&
    rm "/tmp/signals/benchmarks-finished" \
    && echo "Benchmarking signal file deleted!" \
    && touch "/tmp/signals/report-finished" \
    && echo "Signal for finished report creation sent"; \
    echo "Finished report creation at $(date)"