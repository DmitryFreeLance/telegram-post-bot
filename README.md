# telegram-post-bot (Java + telegrambots 6.9.7.1 + SQLite + Docker)

Бот показывает кнопку в личке и по нажатию публикует подготовленный пост (фото + HTML-текст + inline-кнопка-ссылка) в группу.

## Переменные окружения

- `BOT_TOKEN` (обязательно) — токен бота от @BotFather
- `BOT_USERNAME` — username бота (без @), например `MyPostBot`
- `GROUP_CHAT_ID` — id группы, по умолчанию `-1003060928185`
- `DB_PATH` — путь к sqlite файлу, по умолчанию `/data/bot.db`
- `POST_IMAGE_PATH` — путь к картинке, по умолчанию `/app/1.jpg`
- `ALLOWED_USER_IDS` — список Telegram user_id через запятую, кто может нажимать «Опубликовать пост`.
  Если не задано — доступ есть у всех, кто пишет боту.

## Локальный запуск

```bash
export BOT_TOKEN="123:ABC..."
export BOT_USERNAME="MyPostBot"
export GROUP_CHAT_ID="-1003060928185"
export POST_IMAGE_PATH="./src/main/resources/1.jpg"
export DB_PATH="./bot.db"
# export ALLOWED_USER_IDS="111111111,222222222"

mvn -DskipTests package
java -jar target/telegram-post-bot-1.0.0-shaded.jar
```

## Docker

Сборка:

```bash
docker build -t telegram-post-bot:1 .
```

Запуск (пример):

```bash
docker run --rm \
  -e BOT_TOKEN="123:ABC..." \
  -e BOT_USERNAME="MyPostBot" \
  -e GROUP_CHAT_ID="-1003060928185" \
  -e ALLOWED_USER_IDS="111111111" \
  -v "$(pwd)/data:/data" \
  -v "$(pwd)/1.jpg:/app/1.jpg" \
  telegram-post-bot:1
```

> Важно: бот должен быть админом в группе, чтобы постить.

## Команды

- `/start` или `/panel` — панель с кнопкой публикации
- `/publish` — опубликовать (если пост уже публиковался — предупредит)
- `/force_publish` — опубликовать повторно, игнорируя защиту от дублей
