FROM eclipse-temurin:23-jre
WORKDIR /app
COPY build/install/thoughtsntea-bot /app
CMD ["bin/thoughtsntea-bot"]
