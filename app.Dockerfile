FROM orderly-web-build-environment

ARG git_id='UNKNOWN'
ARG git_branch='UNKNOWN'
ARG registry=vimc
ARG name=orderly-web

ENV GIT_ID $git_id
ENV APP_DOCKER_TAG $registry/$name
ENV APP_DOCKER_COMMIT_TAG $registry/$name:$git_id
ENV APP_DOCKER_BRANCH_TAG $registry/$name:$git_branch

RUN mkdir -p /etc/orderly/web
RUN touch /etc/orderly/web/go_signal

CMD docker build --tag orderly-web-dist-base --file dist.Dockerfile . \
    && ./gradlew :app:detektMain :app:test :app:distDocker -i -Djava.util.logging.config.file=src/app/src/test/resources/logging.properties -Pdocker_version=$GIT_ID -Pdocker_name=$APP_DOCKER_TAG \
    && ./gradlew :app:jacocoTestReport && curl -s https://codecov.io/bash | bash -s -- -f src/app/coverage/test/*.xml \
    && docker tag $APP_DOCKER_COMMIT_TAG $APP_DOCKER_BRANCH_TAG \
    && docker push $APP_DOCKER_BRANCH_TAG \
    && docker push $APP_DOCKER_COMMIT_TAG \
