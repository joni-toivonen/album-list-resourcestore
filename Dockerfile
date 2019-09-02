FROM clojure:lein-2.8.1
RUN mkdir -p /usr/src/app/src
WORKDIR /usr/src/app
COPY project.clj /usr/src/app/
RUN lein deps
COPY src/ /usr/src/app/src
RUN lein ring uberjar
EXPOSE 3000

FROM openjdk:8-alpine
RUN mkdir -p /app
WORKDIR /app
COPY --from=0 /usr/src/app/target/ /app/
CMD ["java", "-jar", "/app/server.jar"]
EXPOSE 3000