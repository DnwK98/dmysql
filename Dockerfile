FROM maven:3.6.0-jdk-11-slim AS build

# Install dependencies
COPY pom.xml /home/app/pom.xml
WORKDIR /home/app
RUN mvn dependency:resolve clean package

# Compile app
COPY src /home/app/src
RUN mvn -DskipTests clean package
RUN mkdir /home/lib && cp /home/app/target/distributed-mysql-1.0-jar-with-dependencies.jar /home/lib/app.jar

# Run app
CMD java -jar /home/lib/app.jar