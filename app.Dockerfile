FROM orderly-web-build-environment

ARG git_id='UNKNOWN'
ARG git_branch='UNKNOWN'
ARG registry=vimc
ARG name=orderly-web

ENV GIT_ID $git_id
ENV APP_DOCKER_TAG $registry/$name
ENV APP_DOCKER_COMMIT_TAG $registry/$name:$git_id
ENV APP_DOCKER_BRANCH_TAG $registry/$name:$git_branch

RUN npm run test --prefix=/api/src/app/static

RUN mkdir -p /etc/orderly/web && touch /etc/orderly/web/go_signal

CMD docker build --file dist.Dockerfile --tag orderly-web-dist-base . \
    && ./gradlew :app:detektMain :app:test :app:jacocoTestReport :app:docker -Pdocker_version=$GIT_ID -Pdocker_name=$APP_DOCKER_TAG \
    && docker tag $APP_DOCKER_COMMIT_TAG $APP_DOCKER_BRANCH_TAG \
    && docker push $APP_DOCKER_BRANCH_TAG \
    && docker push $APP_DOCKER_COMMIT_TAG
