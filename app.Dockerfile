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
    && ./gradlew :app:test --tests org.vaccineimpact.orderlyweb.tests.unit_tests.templates.VersionPageTests \
    && ./gradlew :app:test :app:distDocker -i -Pdocker_version=$GIT_ID -Pdocker_name=$APP_DOCKER_TAG \
    && docker tag $APP_DOCKER_COMMIT_TAG $APP_DOCKER_BRANCH_TAG \
    && docker push $APP_DOCKER_BRANCH_TAG \
    && docker push $APP_DOCKER_COMMIT_TAG \
    && docker push $APP_COMMIT_BRANCH_TAG \
