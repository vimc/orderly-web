FROM orderly-web-build-environment

ARG git_id='UNKNOWN'
ARG git_branch='UNKNOWN'
ARG registry=vimc
ARG name=orderly-web-user-cli

ENV GIT_ID $git_id
ENV APP_DOCKER_TAG $registry/$name
ENV APP_DOCKER_COMMIT_TAG $registry/$name:$git_id
ENV APP_DOCKER_BRANCH_TAG $registry/$name:$git_branch

CMD ./gradlew :userCLI:test :userCLI:docker -i -Pdocker_version=$GIT_ID -Pdocker_name=$APP_DOCKER_TAG \
    && docker tag $APP_DOCKER_COMMIT_TAG $APP_DOCKER_BRANCH_TAG \
    && docker push $APP_DOCKER_BRANCH_TAG \
    && docker push $APP_DOCKER_COMMIT_TAG
