FROM azul/zulu-openjdk-alpine:21-jre-headless AS base
WORKDIR /app

FROM base AS builder
COPY build/libs/sleep.jar ./
RUN java -Djarmode=tools -jar sleep.jar extract --layers

FROM base
EXPOSE 8080
COPY docker-cmd.sh ./
CMD ["/app/docker-cmd.sh"]

COPY --from=builder /app/sleep/dependencies/ ./
COPY --from=builder /app/sleep/spring-boot-loader/ ./
COPY --from=builder /app/sleep/snapshot-dependencies/ ./
COPY --from=builder /app/sleep/application/ ./
