FROM openjdk:8-jdk-alpine
ARG JAR_FILE
ADD ${JAR_FILE} app.jar

ENTRYPOINT ["java", \
            "-Xmx1024m", \
            "-Xms128m", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", \
            "/app.jar" \
]
