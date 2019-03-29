FROM orderly-web-build-environment

WORKDIR /api/src

RUN ./gradlew :customConfigTests:compileKotlin
RUN ./gradlew :customConfigTests:copyStatic
RUN ./gradlew :customConfigTests:copyTemplates

RUN mkdir /etc/orderly/web -p
RUN touch /etc/orderly/web/go_signal

CMD ./gradlew :customConfigTests:test -i
