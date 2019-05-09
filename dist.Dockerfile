FROM openjdk:8u121

RUN mkdir /static/public -p

COPY /api/src/app/static /static/public
COPY /api/src/app/static /templates
