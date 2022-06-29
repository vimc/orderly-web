FROM openjdk:11

RUN mkdir /static/public -p

COPY ./app/static/public /static/public
COPY ./app/templates /templates
