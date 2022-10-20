FROM openjdk:18
EXPOSE 8080:8080
ADD build/libs/CloudRepository-0.0.1-SNAPSHOT.jar CloudRepository.jar
ENTRYPOINT ["java","-jar","/CloudRepository.jar"]