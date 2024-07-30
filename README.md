# Поисковый движок по сайту
Проект представляет Spring-приложение, работающее с
локально установленной базой данных MySQL, которое имеет простой 
веб-интерфейс. Проект предназначен для индексации сайтов и поиска веб-страниц по запросу.

### Структура проекта
Проект состоит из четырех сервисов:
* **IndexingService** - сервис индексации, который
сохраняет в базу данных информацию о сайтах, производит
обход всех страниц сайта, добавляет их адреса, статусы и код в базу
данных и производит их индексацию.
* **IndexingPageService** - сервис индексации страницы, который
производит индексацию отдельной страницы.
* **SearchService** - сервис поиска, который осуществляет поиск страниц по запросу и
формирование сниппетов. Страницы возвращаются в виде списка с учетом релевантности.
* **StatisticsService** - сервис статистики, который рассчитывает
  статистику и возвращает её. В статистику входят данные о числе
сайтов, страниц и лемм в базе данных, а также о статусе индексации сайтов.

### Стек технологий
* Java 17
* Spring Boot 2.7.1 используется для создания веб-сервиса
* Jsoup 1.16.1 - используется для парсинга HTML-страниц
* MySQL - используется для хранения данных
* Lombok 1.18.24 - используется для сокращения кода

### Запуск проекта

#### 1. Клонирование репозитория
Для клонирования репозитория используется команда

`git clone <git@github.com:EM308/search-engine.git>`

#### 2. Сборка проекта через Maven
Для сборки проекта используется команда

`mvn clean install`

#### 3. Создание файла конфигурации
Необходимо создать конфигурационный файл
  application.yaml в корне проекта. Параметры username и password
должны быть заполнены для подключения к базе данных. Параметры url и name
обозначают сайт, который необходимо проиндексировать. 
Данные параметры могут повторяться, если необходима индексация нескольких 
сайтов.
```yaml
  server:
    port: 8080
  spring:
    datasource:
      username: <имя_пользователя>
      password: <пароль>
      url: jdbc:mysql://127.0.0.1:3306/search_engine?useSSL=false&requireSSL=false&allowPublicKeyRetrieval=true
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
    hibernate:
      ddl-auto: update
    show-sql: true
    liquibase:
      change-log: classpath:db/changelog/db.changelog-master.xml
    enabled: true

  indexing-settings:
    sites:
      - url: <url_сайта>
        name: <название_сайта>
```
#### 4. Запуск проекта
Для запуска проекта используется команда

`mvn spring-boot:run`

После запуска доступ к проекту может быть произведен через браузер по адресу
http://localhost:8080.

