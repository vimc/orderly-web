FROM openjdk:8u121

RUN mkdir /static/public -p

COPY ./src/app/static/public /static/public
COPY ./src/app/templates /templates
