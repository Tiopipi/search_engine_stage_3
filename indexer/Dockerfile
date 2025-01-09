FROM openjdk:17-jdk
WORKDIR /app
COPY src/main/resources/Stop_words.txt /app/Stop_words.txt
COPY src/main/resources/Processed_books.txt /app/Processed_books.txt
ENV BOOKS_DIRECTORY=/app/datalake/eventstore/gutenberg
ENV INDEX_DIRECTORY_TREE="/app/datamart/Inverted Index/Tree Data Structure"
ENV INDEX_DIRECTORY_UNIQUE="/app/datamart/Inverted Index/Unique Data Structure"
ENV INDEX_DIRECTORY_HIERARCHICAL="/app/datamart/Inverted Index/Hierarchical Data Structure"
ENV METADATA_DIRECTORY="/app/datamart/Metadata Database"
ENV STOP_WORDS="/app/Stop_words.txt"
ENV PROCESSED_BOOKS_PATH="/app/Processed_books.txt"

COPY target/indexer-1.0-SNAPSHOT.jar indexer.jar
CMD ["java", "-Xms1g", "-Xmx6g", "-jar", "indexer.jar"]
