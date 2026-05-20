FROM amazoncorretto:8-alpine

RUN mkdir -p /opt/app

ENV PROJECT_HOME=/opt/app

COPY target/spring-boot-mongo-1.0.jar $PROJECT_HOME/spring-boot-mongo.jar
COPY initScript.sh $PROJECT_HOME/initScript.sh

WORKDIR $PROJECT_HOME

RUN chmod +x initScript.sh

EXPOSE 8080

CMD ["sh", "initScript.sh"]
