# Gatling Monitor

Мониторинг Gatling-генераторов через SSH.

## Главная идея

**Общий кэш статусов** — любой может открыть страницу и нажать «Обновить статусы».
Результат сохраняется в памяти сервиса и **виден всем** до следующего обновления.

## Настройка перед сборкой JAR

В `src/main/resources/application.yml` задайте SSH-креды и генераторы:

```yaml
monitor:
  ssh:
    username: loaduser
    password: your-password
    port: 22
  generators:
    - name: generator-01
      host: 10.0.0.1
```

Креды **нигде в UI не отображаются** — только в конфиге.

## Сборка и запуск

```bash
mvn clean package -DskipTests
java -jar target/gatling-monitor-1.0.0.jar
```

http://localhost:8080

## UI

- **Обновить статусы** — ручная проверка через SSH
- **Обновлять раз в 2 часа** — авто-обновление по тем же кредам из конфига

## Маскот

```
src/main/resources/static/img/mascot.png
```

## API

`GET /api/status` — JSON со статусами и флагом `updating`.
