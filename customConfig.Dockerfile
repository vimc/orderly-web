FROM orderly-web-build-environment

WORKDIR /api/src

RUN ./gradlew :customConfigTests:compileKotlin

CMD ./gradlew :customConfigTests:test -i
