#FROM openjdk:11-jdk
#ADD . /src
#WORKDIR /src
#RUN . /src
#//TODO make  jar file \
#   // copy jar file
#EXPOSE 8080
#ENTRYPOINT ["kotlin","-jar","target/microservices-gateway-1.0.0.jar"]