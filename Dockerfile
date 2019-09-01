FROM clojure:lein-2.8.1
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY project.clj /usr/src/app/
RUN lein deps
COPY src/ /usr/src/app
RUN lein ring uberjar

FROM openjdk:8-alpine
RUN mkdir -p /app
WORKDIR /app
COPY --from=0 /usr/src/app/target/server.jar .
CMD ["java", "-jar", "server.jar"]
EXPOSE 3000