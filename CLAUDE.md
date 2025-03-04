# TeaClub Telegram Bot - Developer Guide

## Build & Run Commands

## Gradle Commands
```
# Build the project
./gradlew build

# Run the application
./gradlew run

# Create distribution package
./gradlew installDist
```

## Nix Commands
```
# Enter development environment
nix develop

# Build the project
nix build

# Run the application
nix run

# Check flake
nix flake check

# Build container image
nix build .#dockerImage

# Load the image into Docker
docker load < result
```

## Docker Commands
```
# Build Docker image
docker build -t thoughtsntea-bot .

# Run in Docker
docker run -e THOUGHTSNTEA_TELEGRAM_BOT_TOKEN=token -e THOUGHTSNTEA_TELEGRAM_CHAT_ID=chatid thoughtsntea-bot
```

## Code Style Guidelines
- **Package Structure**: Use `dev.teaguild.thoughtsntea` namespace
- **Imports**: Order by standard library, third-party libraries, then project imports
- **Formatting**: Follow Kotlin official style guide with 4-space indentation
- **Error Handling**: Use `@Throws` annotations; prefer safe handling with defaults
- **Naming Conventions**: 
  - Classes: PascalCase
  - Functions/Variables: camelCase
  - Constants: UPPER_SNAKE_CASE
- **Coroutines**: Properly handle dispatchers and cancellation
- **Extension Functions**: Prefer for utility methods
- **Comments**: Use KDoc for public functions/classes