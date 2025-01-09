FROM openjdk:17-jdk
WORKDIR /app
ENV METADATA_FILE_PATH="/app/datamart/Metadata Database/metadata.csv"
ENV TREE_DIRECTORY="/app/datamart/Inverted Index/Tree Data Structure"
ENV UNIQUE_FILE_PATH="/app/datamart/Inverted Index/Unique Data Structure/invertedIndex"
ENV HIERARCHICAL_DIRECTORY='/app/datamart/Inverted Index/Hierarchical Data Structure'
ENV BOOKS_FILE_PATH=/app/datalake/eventstore/gutenberg

COPY src/main/resources/public /app/public
COPY target/query_engine-1.0-SNAPSHOT.jar query_engine.jar

EXPOSE 54327
EXPOSE 8080
EXPOSE 5701
CMD ["java", "-Xms1g", "-Xmx6g", "-jar", "query_engine.jar"]
