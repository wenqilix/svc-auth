FROM openjdk:14-jdk-alpine
ARG JAR_FILE
ADD ${JAR_FILE} app.jar

ENTRYPOINT ["java", \
            "-Xmx1024m", \
            "-Xms128m", \
            "-Ddebug", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", \
            "/app.jar", \
            "--spring.config.location=file:/app/shared/application.yml" \
]
