FROM openjdk:11-jdk
COPY . /data_image
WORKDIR /data_image
RUN ./gradlew assemble
COPY build/libs/short-url-*-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]