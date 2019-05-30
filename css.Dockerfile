FROM node:8

RUN mkdir /static/ -p

COPY ./src/app/static /static

WORKDIR /static
RUN npm install
RUN npm install --global gulp-cli

CMD gulp sass
