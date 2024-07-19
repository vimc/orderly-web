ARG GIT_ID="UNKNOWN"
FROM vimc/orderly-web-build-environment:$GIT_ID

RUN apt-get update && apt-get install -yq \
                default-jre \
                gconf-service \
                libasound2 \
                libatk1.0-0 \
                libatk-bridge2.0-0 \
                libc6 \
                libcairo2 \
                libcups2 \
                libdbus-1-3 \
                libexpat1 \
                libfontconfig1 \
                libgcc1 \
                libgconf-2-4 \
                libgdk-pixbuf2.0-0 \
                libglib2.0-0 \
                libgtk-3-0 \
                libnspr4 \
                libpango-1.0-0 \
                libpangocairo-1.0-0 \
                libstdc++6 \
                libx11-6 \
                libx11-xcb1 \
                libxcb1 \
                libxcomposite1 \
                libxcursor1 \
                libxdamage1 \
                libxext6 \
                libxfixes3 \
                libxi6 \
                libxrandr2 \
                libxrender1 \
                libxss1 \
                libxtst6 \
                ca-certificates \
                fonts-liberation \
                libgbm1 \
                libnss3 \
                lsb-release \
                xdg-utils \
                wget

RUN wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
RUN dpkg -i google-chrome-stable_current_amd64.deb; apt-get -fy install

RUN ../scripts/install-chromedriver.sh

RUN mkdir -p /etc/orderly/web
RUN touch /etc/orderly/web/go_signal

CMD ./gradlew :customConfigTests:compileTestKotlin :customConfigTests:test -i
