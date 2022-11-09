ARG GIT_ID="UNKNOWN"
FROM vimc/orderly-web-build-environment:$GIT_ID

ARG GIT_ID="UNKNOWN"
ARG GIT_BRANCH='UNKNOWN'
ARG REGISTRY=vimc
ARG NAME=orderly-web

ENV GIT_ID $GIT_ID
ENV APP_DOCKER_TAG $REGISTRY/$NAME
ENV APP_DOCKER_COMMIT_TAG $REGISTRY/$NAME:$GIT_ID
ENV APP_DOCKER_BRANCH_TAG $REGISTRY/$NAME:$GIT_BRANCH

RUN npm run test --prefix=/api/src/app/static

RUN mkdir -p /etc/orderly/web && touch /etc/orderly/web/go_signal

CMD docker build --file dist.Dockerfile --tag orderly-web-dist-base . \
    && ./gradlew :app:detektMain :app:test :app:jacocoTestReport :app:docker -Pdocker_version=$GIT_ID -Pdocker_name=$APP_DOCKER_TAG \
    && docker tag $APP_DOCKER_COMMIT_TAG $APP_DOCKER_BRANCH_TAG \
    && docker push $APP_DOCKER_BRANCH_TAG \
    && docker push $APP_DOCKER_COMMIT_TAG
