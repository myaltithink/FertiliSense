FROM rasa/rasa:3.6.20
WORKDIR '/bot'
COPY . /bot
USER root
COPY ./data /bot/data
VOLUME /bot
VOLUME /bot/data
VOLUME /bot/models
EXPOSE 5005
CMD ["run","-m","./models","--enable-api","--cors","*","--debug" ,"--endpoints", "endpoints.yml", "--log-file", "out.log", "--debug"]
