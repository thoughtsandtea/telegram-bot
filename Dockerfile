FROM eclipse-temurin:17-jre
WORKDIR /app
COPY build/install/thoughtsntea-bot /app
CMD ["bin/thoughtsntea-bot"]
