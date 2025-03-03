# TeaClub Telegram Bot

A Telegram bot designed to organize and manage regular tea tasting sessions for groups. Built with Kotlin and the TgBotApi library.

## Features

- 🍵 Schedule regular tea tasting sessions on specific days of the week
- 👥 Manage participant registration with maximum capacity
- ⏰ Send automated reminders before each session
- 🔒 Optional registration lockout before sessions begin
- ⚙️ Fully configurable schedule, reminders, and session parameters
- 🌐 Support for different time zones

## Getting Started

### Prerequisites

- JDK 23 or higher
- Gradle
- Telegram Bot Token (from [@BotFather](https://t.me/botfather))
- Target Chat ID for your group

### Environment Variables

The bot requires the following environment variables:

- `THOUGHTSNTEA_TELEGRAM_BOT_TOKEN`: Your Telegram bot token
- `THOUGHTSNTEA_TELEGRAM_CHAT_ID`: The chat ID where the bot will operate

### Installation

#### Using Gradle

1. Clone the repository:
   ```bash
   git clone https://github.com/TeaClub/telegram-bot.git
   cd telegram-bot
   ```

2. Build the project:
   ```bash
   ./gradlew build
   ```

3. Run the application:
   ```bash
   ./gradlew run
   ```

#### Using Nix (recommended)

1. Clone the repository:
   ```bash
   git clone https://github.com/TeaClub/telegram-bot.git
   cd telegram-bot
   ```

2. If you have direnv installed (recommended):
   ```bash
   direnv allow
   ```
   
   Or directly activate the Nix shell:
   ```bash
   nix develop
   ```

3. Build the project:
   ```bash
   nix build
   ```

4. Run the application (after setting up environment variables):
   ```bash
   nix run
   ```

### Docker

You can also run the bot using Docker:

```bash
# Build the Docker image
docker build -t thoughtsntea-bot .

# Run the container
docker run -e THOUGHTSNTEA_TELEGRAM_BOT_TOKEN=your_token -e THOUGHTSNTEA_TELEGRAM_CHAT_ID=your_chat_id thoughtsntea-bot
```

## Usage

### Admin Commands

- `/help` - Display all available commands
- `/showconfig` - Show current bot configuration
- `/setconfig` - Change bot settings

### User Commands

- `/join` - Join today's tea tasting session
- `/leave` - Leave today's tea tasting session
- `/start` - Get information about the bot

### Configuration Options

The bot can be configured using the `/setconfig` command with the following parameters:

- `daysOfWeek` - Days when tasting occurs (e.g., `monday,wednesday`)
- `askTime` - Time when bot asks for participants (HH:mm)
- `tastingTime` - Time when tasting starts (HH:mm)
- `maxParticipants` - Maximum number of participants allowed
- `reminders` - Reminder times in minutes before tasting (e.g., `30,10`)
- `lockoutBefore` - Minutes before tasting when registration locks
- `botActive` - Enable/disable bot (`true`/`false`)
- `timeZone` - Time zone for scheduling (e.g., `UTC`, `Europe/Berlin`)

## License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.