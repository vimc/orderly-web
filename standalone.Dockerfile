ARG GIT_ID="UNKNOWN"
FROM vimc/orderly-web:$GIT_ID

RUN mkdir -p /etc/orderly/web && touch /etc/orderly/web/go_signal && wget -qO- \
    https://download.red-gate.com/maven/release/org/flywaydb/enterprise/flyway-commandline/9.15.1/flyway-commandline-9.15.1-linux-x64.tar.gz \
    | tar -xvz && ln -s `pwd`/flyway-9.15.1/flyway /usr/local/bin

COPY src/config/users/standalone.properties /etc/orderly/web/config.properties
COPY migrations/sql /flyway/sql/
COPY migrations/flyway.conf /flyway/conf/flyway.conf

ENTRYPOINT flyway -baselineOnMigrate=true -configFiles=/flyway/conf/flyway.conf -locations=/flyway/sql \
migrate && bin/sh -c "/app/bin/app"
