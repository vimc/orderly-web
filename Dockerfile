FROM openjdk:11

RUN apt-get update
RUN apt-get install -y build-essential

# Install docker
RUN apt-get update
RUN apt-get install -y \
        apt-transport-https \
        ca-certificates \
        curl \
        software-properties-common \
        dirmngr \
        apt-transport-https \
        lsb-release \
        gnupg \
        ca-certificates

RUN mkdir -p /etc/apt/keyrings
RUN curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
RUN echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian \
      $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
RUN apt-get update
RUN apt-get -y install docker-ce docker-ce-cli containerd.io docker-compose-plugin

RUN curl -sL https://deb.nodesource.com/setup_19.x | bash -
RUN apt-get -y install nodejs

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
COPY dist.Dockerfile /api/src/

RUN npm install -g npm

RUN npm ci --prefix=/api/src/app/static --force
RUN ./gradlew :app:compileFrontEnd

RUN npm run lint --prefix=/api/src/app/static -- --quiet
