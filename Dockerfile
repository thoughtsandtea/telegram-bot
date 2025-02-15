FROM eclipse-temurin:23-jre
WORKDIR /app
COPY build/install/thoughtsntea-bot /app
VOLUME ["/app/thoughtsntea.json"]
CMD ["bin/thoughtsntea-bot"]
