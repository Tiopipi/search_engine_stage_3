FROM openjdk:17-jdk
WORKDIR /app
ENV REPOSITORY_DOCUMENTS=/app/datalake/eventstore/gutenberg
COPY target/crawler-1.0-SNAPSHOT.jar crawler.jar
CMD ["java", "-jar", "crawler.jar"]
