# GeTaTaPi backend

Backend проекта на Supabase: PostgreSQL, Auth, Storage и Edge Functions.

## Состав

- `supabase/migrations/20261102000100_initial_schema.sql` — схема БД, индексы, триггеры, RLS и политики Storage.
- `supabase/functions/api-v1/index.ts` — REST API `/api/v1`.
- `supabase/config.toml` — локальная конфигурация Supabase CLI.
- `.env.example` — переменные для локального тестирования.

## Требования

- Docker Desktop
- Supabase CLI
- Deno (необязательно для запуска через Supabase CLI)

Установи Supabase CLI по официальной инструкции: https://supabase.com/docs/guides/cli

## Локальный запуск

Из корня репозитория:

```bash
supabase start
supabase db reset
supabase functions serve api-v1 --no-verify-jwt
```

`--no-verify-jwt` используется потому, что функция сама проверяет JWT через Supabase Auth (`auth.getUser`). Не публикуй функцию с отключённой внешней проверкой JWT без понимания этой настройки; в production можно включить штатную проверку JWT.

После `supabase start` CLI покажет локальные URL и ключи. Используй `anon key` как `SUPABASE_ANON_KEY` и URL проекта как `SUPABASE_URL`.

В другом терминале:

```bash
export SUPABASE_URL="http://127.0.0.1:54321"
export SUPABASE_ANON_KEY="<локальный anon key>"
```

Локальный API:
`http://127.0.0.1:54321/functions/v1/api-v1`

## Создание пользователя и получение токена

Зарегистрируй пользователя через Supabase Auth (локальный Studio или Auth API), затем передавай access token в запросах:

```http
Authorization: Bearer <access_token>
```

Все защищённые запросы выполняются от имени пользователя. `user_id` берётся из проверенного JWT, а не из тела запроса. Запросы к таблицам используют клиент с anon key и пользовательским Authorization header, поэтому действуют RLS-политики.

## Основные маршруты

| Метод | Путь | Назначение |
|---|---|---|
| GET | `/topics` | Список активных тем |
| POST | `/tasks` | Создать задание |
| GET | `/tasks/{taskId}` | Получить своё задание |
| POST | `/submissions` | Создать отправку для проверки |
| GET | `/submissions` | История отправок |
| GET | `/submissions/{submissionId}` | Получить отправку и результат |
| GET | `/statistics` | Статистика пользователя |

Endpoint `POST /submissions` принимает JSON `{ "taskId": "...", "storagePath": "..." }` и обязательный заголовок `Idempotency-Key`. Фотография предварительно загружается в приватный Storage bucket `submissions`. Endpoint возвращает `202` и статус `queued`.

## Пример запросов

```bash
curl "$SUPABASE_URL/functions/v1/api-v1/topics" \
  -H "apikey: $SUPABASE_ANON_KEY" \
  -H "Authorization: Bearer <access_token>"

curl -X POST "$SUPABASE_URL/functions/v1/api-v1/tasks" \
  -H "apikey: $SUPABASE_ANON_KEY" \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"topicId":"<topic-uuid>","difficulty":"easy"}'
```

## Проверка

1. `supabase db reset` завершается без ошибок.
2. В Supabase Studio доступны таблицы `profiles`, `topics`, `tasks`, `submissions`, `user_statistics`.
3. Неавторизованный запрос к API возвращает `401`.
4. Авторизованный пользователь видит активные темы и может создать задание.
5. Пользователь не может прочитать или изменить чужие задания, отправки и файлы благодаря RLS.
6. Для `POST /submissions` повтор с тем же `Idempotency-Key` не создаёт новую запись.

## Ограничение текущей реализации

Проверка изображения ИИ пока представлена очередью (`queued`). Вызов Gemini/VK Cloud Vision и фоновый worker должны быть подключены отдельно, после выбора провайдера и получения ключа. До подключения worker запись не перейдёт автоматически в `completed` или `failed`. Это явно отделяет работающие CRUD/Auth/RLS от ещё не подключённой интеграции ИИ.
