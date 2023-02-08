FROM eclipse-temurin:17.0.6_10-jre-jammy@sha256:592f2d372afeb13bc3c5a28e49c7ca6b5e5688e767cb0b9e21a70caaacfc4cec
WORKDIR /opt/app
RUN addgroup --gid 1000 javauser && adduser javauser --shell /usr/sbin/nologin --gid 1000
COPY dependencies/ ./
COPY spring-boot-loader/ ./
COPY snapshot-dependencies/ ./
COPY application/ ./
RUN chown -R javauser:javauser .
USER javauser
HEALTHCHECK --interval=30s --timeout=3s --retries=1 CMD wget -qO- http://localhost:8080/actuator/health/ | grep UP || exit 1
EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]