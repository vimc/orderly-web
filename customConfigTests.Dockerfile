FROM orderly-web-build-environment

RUN mkdir -p /etc/orderly/web
RUN touch /etc/orderly/web/go_signal

CMD ./gradlew :customConfigTests:compileTestKotlin :customConfigTests:test -i
