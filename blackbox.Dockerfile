FROM orderly-web-build-environment

ARG git_id='UNKNOWN'
ENV GIT_ID $git_id

WORKDIR /api/src

RUN ./gradlew :blackboxTests:compileKotlin

CMD ./gradlew :blackboxTests:test -i
