FROM amazoncorretto:17-alpine-jdk
COPY target/*.jar review-service.jar
ENTRYPOINT ["java","-jar","/review-service.jar"]