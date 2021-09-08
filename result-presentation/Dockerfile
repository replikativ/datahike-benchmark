FROM    clojure:tools-deps-slim-buster

ENV     TAOENSSO_TIMBRE_MIN_LEVEL_EDN=':warn'
ENV     DEBIAN_FRONTEND=noninteractive
RUN     mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY    . /usr/src/app
RUN     apt-get update && apt-get install -y --no-install-recommends apt-utils
RUN     apt-get update && apt-get install -y curl
RUN     curl -sL https://deb.nodesource.com/setup_16.x | bash -
RUN     apt-get update && apt-get install -y nodejs
RUN     npm -v
RUN     npm install -g npm@7.22.0 
RUN     npm install vega vega-lite vega-cli --unsafe

CMD while ! test -e "/tmp/signals/benchmarks-finished"; \
    do \
       echo "Waiting for benchmarking to finish..."; \
       sleep 30s; \
    done; \
    echo "Signal received! Starting creation of result presentation..."; \
    echo "Starting report creation at $(date)"; \
    clj -M:run "/tmp/presentation/" \
    && rm "/tmp/signals/benchmarks-finished" \
    && echo "Benchmarking signal file deleted!" \
    && touch "/tmp/signals/report-finished" \
    && echo "Signal for finished report creation sent"; \
    echo "Finished report creation at $(date)"
