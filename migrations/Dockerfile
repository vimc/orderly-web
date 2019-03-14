FROM boxfuse/flyway:5.2.4

COPY migrations/sql /flyway/sql/
COPY migrations/flyway.conf /flyway/conf/flyway.conf

# baselineOnMigrate required because the orderly db is presumed to pre-exist and be non-empty when we migrate
ENTRYPOINT ["flyway","-baselineOnMigrate=true", "migrate"]
