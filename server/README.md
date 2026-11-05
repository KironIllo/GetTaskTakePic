# GeTaTaPi backend

Backend проекта на Supabase: PostgreSQL, Auth, Storage и Edge Functions.

## Состав

- `supabase/migrations/20261102000100_initial_schema.sql` — схема БД, индексы, триггеры, RLS и политики Storage.
- `supabase/functions/api-v1/index.ts` — REST API `/api/v1`.
- `supabase/config.toml` — локальная конфигурация Supabase CLI.
- `supabase/migrations/20261105000100_api_rate_limit.sql` — ограничение частоты запросов на уровне PostgreSQL.
- `.env.example` — пример переменных окружения (без секретов).
- `postman/GeTaTaPi.postman_collection.json` — коллекция запросов Postman.

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

## Защита API

- Для авторизованного пользователя действует лимит 120 запросов за 60 секунд. Счётчик хранится в PostgreSQL, а не в памяти Edge Function.
- JSON-запросы ограничены 16 КБ.
- Для JSON проверяется `Content-Type: application/json`.
- `storagePath` проверяется по формату и принадлежности пользователя; перед созданием отправки проверяется наличие файла в приватном Storage.
- Ошибки возвращаются в едином формате `{ error: { code, message, details, requestId } }`.
- События ошибок записываются в структурированный лог без JWT и API-ключей.

## Важные ограничения

Проверка изображения ИИ пока представлена очередью (`queued`). Вызов внешней модели и фоновый worker не подключены. До их подключения запись не перейдёт автоматически в `completed` или `failed`.

Перед публикацией нужно дополнительно настроить production CORS (сейчас разрешён `*`), проверить все сценарии CRUD/Storage и провести интеграционный тест rate limit. `Idempotency-Key` уже обрабатывается, но конкурентное создание отправок следует вынести в одну транзакционную SQL-функцию до production.
