FROM docker.montagu.dide.ic.ac.uk:5000/node-docker:master

# Install OpenJDK
RUN echo 'deb http://deb.debian.org/debian stretch-backports main' > /etc/apt/sources.list.d/stretch-backports.list
RUN apt-get update
RUN apt-get install -t stretch-backports -y \
    openjdk-8-jdk
RUN rm /etc/apt/sources.list.d/stretch-backports.list

# Setup gradle
COPY src/gradlew /api/src/
COPY src/gradle /api/src/gradle/
WORKDIR /api/src
RUN ./gradlew

# Pull in dependencies
COPY ./src/build.gradle /api/src/
COPY ./src/settings.gradle /api/src/
COPY ./src/config/ /api/src/config/
RUN echo 'docker' > config/current_user
RUN ./gradlew

# Copy source
COPY . /api

# Install front-end dependencies
RUN npm install --prefix=/api/src/app/static
RUN ./gradlew :app:compileFrontEnd

docker build --tag orderly-web-dist-base --file dist.Dockerfile .
