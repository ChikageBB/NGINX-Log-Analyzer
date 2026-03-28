# NGINX Log Analyzer

Консольная утилита для анализа лог-файлов NGINX. Читает локальные файлы или файлы по URL, собирает статистику и выводит результат в нужном формате.

---

## Быстрый старт

```shell
java -jar log-analyzer.jar \
  --path logs/*.log \
  --format markdown \
  --output report.md \
  --fromDate 2025-03-01
```

---

## Аргументы командной строки

| Параметр              | Обязателен | Описание                                              |
|-----------------------|:----------:|-------------------------------------------------------|
| `--path` / `-p`       | ✅          | Путь к лог-файлу(ам) — glob-паттерн или URL           |
| `--format` / `-f`     | ✅          | Формат вывода: `json`, `markdown`, `adoc`             |
| `--output` / `-o`     | ✅          | Путь к выходному файлу                                |
| `--fromDate`          | ❌          | Начало диапазона анализа (ISO 8601)                   |
| `--toDate`            | ❌          | Конец диапазона анализа (ISO 8601)                    |

### Примеры значений `--path`

```
/var/log/nginx/server.log
logs/2025*
https://raw.githubusercontent.com/elastic/examples/master/.../nginx_logs
```

Поддерживаемые расширения: `.log`, `.txt`

### Фильтрация по дате

| fromDate | toDate | Поведение                                      |
|----------|--------|------------------------------------------------|
| ✅        | ✅      | Анализ в диапазоне `[fromDate, toDate]`        |
| ✅        | —      | От `fromDate` до последней записи в логах      |
| —        | ✅      | От первой записи в логах до `toDate`           |
| —        | —      | Весь файл целиком                              |

---

## Коды возврата

| Код | Значение                                      |
|-----|-----------------------------------------------|
| `0` | Успешное завершение                            |
| `1` | Непредвиденная ошибка                          |
| `2` | Некорректные параметры / файл не найден        |

---

## Формат входных данных

Программа разбирает строки NGINX Combined Log Format:

```
$remote_addr - $remote_user [$time_local] "$request" $status $body_bytes_sent "$http_referer" "$http_user_agent"
```

Пример:
```
93.180.71.3 - - [17/May/2015:08:05:32 +0000] "GET /downloads/product_1 HTTP/1.1" 304 0 "-" "Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)"
```

Строки, не соответствующие формату, пропускаются с предупреждением (`WARN`).

---

## Собираемая статистика

- Общее количество запросов
- Средний, максимальный и 95-й перцентиль размера ответа (в байтах)
- Топ-10 наиболее запрашиваемых ресурсов
- Частота кодов HTTP-ответа
- Распределение запросов по датам *(+доп. баллы)*
- Уникальные протоколы передачи данных *(+доп. баллы)*

Точность всех числовых метрик — 2 знака после запятой.

---

## Форматы вывода

### `json`

Результат записывается в файл `.json` согласно схеме:

```json
{
    "files": ["access.log"],
    "totalRequestsCount": 10000,
    "responseSizeInBytes": {
        "average": 500.00,
        "max": 1000.00,
        "p95": 950.00
    },
    "resources": [
        { "resource": "/downloads/product_1", "totalRequestsCount": 1000 }
    ],
    "responseCodes": [
        { "code": 200, "totalResponsesCount": 8000 }
    ],
    "requestsPerDate": [
        { "date": "2024-03-01", "weekday": "Monday", "totalRequestsCount": 2981, "totalRequestsPercentage": 12.10 }
    ],
    "uniqueProtocols": ["HTTP/1.1", "HTTP/2.0"]
}
```

### `markdown`

Результат записывается в файл `.md` в виде таблиц с разделами: общая информация, ресурсы, коды ответа.

### `adoc`

Результат записывается в файл `.ad` в формате AsciiDoc.

---

## Технические требования

- Java (версия совместимая с проектом)
- Сборка через Maven / Gradle (в соответствии со структурой проекта)
- Логирование через `log4j` в `stdout`
- Файл не должен загружаться в память целиком — используется потоковая обработка
- Программа придерживается **fail-fast** поведения

---

## Полезные ссылки

- [Чтение больших файлов в Java](https://www.baeldung.com/java-read-lines-large-file)
- [Java HTTP Client](https://openjdk.org/groups/net/httpclient/recipes.html)
- [Jackson ObjectMapper](https://www.baeldung.com/jackson-object-mapper-tutorial)
- [HTTP статус коды](https://www.baeldung.com/linux/status-codes)
- [Что такое перцентили](https://habr.com/ru/companies/tochka/articles/690814/)
- [Пример датасета логов NGINX](https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/nginx_logs/nginx_logs)
