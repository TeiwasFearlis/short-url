## 1. Docker run
FROM openjdk:11-jdk

COPY . /data_image

WORKDIR /data_image

RUN ./gradlew assemble

COPY build/libs/short-url-*-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]



## 2. Minikube run
minikube start - this is command for minikube run.

skaffold run - this is command for create jar-image

kubectl port-forward svc/short-url-db 5432:5432 - this is command for 
connect port kubectl with database

skaffold delete - this is command for delete jar-image