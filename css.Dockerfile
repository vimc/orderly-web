FROM node:19

RUN mkdir /static/ -p

COPY ./src/app/static /static

WORKDIR /static
RUN npm install -g npm
RUN npm install

CMD npm run sass-prod
