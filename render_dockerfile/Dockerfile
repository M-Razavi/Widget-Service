FROM maven:3.8.6-eclipse-temurin-17-alpine@sha256:6a53f197f7bdfdf56e50c03cdb9f82a339ca3ded4eae867834e4bcfa7276a832 AS builder
COPY . .
RUN mvn clean install -DskipTests
RUN mvn package -DskipTests
RUN pwd
RUN ls
RUN ls -la ./target
COPY target/widget-service-0.0.2-SNAPSHOT.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:17.0.6_10-jre-jammy@sha256:592f2d372afeb13bc3c5a28e49c7ca6b5e5688e767cb0b9e21a70caaacfc4cec
WORKDIR /opt/app
RUN addgroup --gid 1000 javauser && adduser javauser --shell /usr/sbin/nologin --gid 1000
COPY --from=builder ./dependencies/ ./
COPY --from=builder ./spring-boot-loader/ ./
COPY --from=builder ./snapshot-dependencies/ ./
COPY --from=builder ./application/ ./
RUN chown -R javauser:javauser .
USER javauser
HEALTHCHECK --interval=30s --timeout=3s --retries=1 CMD wget -qO- http://localhost:8080/actuator/health/ | grep UP || exit 1
EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]