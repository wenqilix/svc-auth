FROM openjdk:15-jdk-alpine as build
ARG JAR_FILE
ADD ${JAR_FILE} app.jar
RUN jar -xf app.jar

FROM openjdk:15-jdk-alpine
RUN addgroup -g 1000 -S appuser && adduser -u 1000 -S appuser -G appuser
RUN mkdir /app && chown -R 1000:1000 /app

COPY --from=build /BOOT-INF/lib /app/lib
COPY --from=build /META-INF /app/META-INF
COPY --from=build /BOOT-INF/classes /app

COPY ./.version /app/static/.version

ENTRYPOINT ["java", \
            "-Xmx1024m", \
            "-Xms128m", \
            "-Ddebug", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-cp", \
            "app:app/lib/*", \
            "auth.ApplicationKt", \
            "--spring.config.location=file:/app/shared/application.yml" \
]
USER 1000
