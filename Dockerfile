FROM bellsoft/liberica-runtime-container:jdk-all-25-musl AS builder

WORKDIR /app
COPY . /app/reminder-api

RUN cd reminder-api && ./mvnw -Dmaven.test.skip=true clean package

RUN export JAR_FILE=$(ls reminder-api/target/*.jar) && \
    java -Djarmode=tools -jar "$JAR_FILE" extract

RUN export JAR_FILE=$(ls reminder-api/target/*.jar | head -n1) && \
    export EXTRACTED_DIR=$(ls -d reminder-api-* | head -n1) && \
    jdeps --multi-release 25 \
        --class-path "$EXTRACTED_DIR/lib/*" \
        --ignore-missing-deps \
        --print-module-deps \
        "$EXTRACTED_DIR/$(basename $JAR_FILE)" > /app/modules.txt

RUN jlink --compress=2 --strip-debug --no-header-files --no-man-pages \
    --add-modules $(cat /app/modules.txt) \
    --output /app/jlink-runtime


FROM bellsoft/alpaquita-linux-base:musl

COPY --from=builder /app/jlink-runtime /jlink-runtime

COPY --from=builder /app/reminder-api/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["/jlink-runtime/bin/java", "-jar", "/app/app.jar"]