# Стажировка <a href="https://github.com/JavaWebinar/topjava">Topjava</a>

## [Патчи занятия](https://drive.google.com/drive/u/1/folders/1ZsPX879m6x4Va0Wy3D1EQIBsnZUOOvao)

## ![hw](https://cloud.githubusercontent.com/assets/13649199/13672719/09593080-e6e7-11e5-81d1-5cb629c438ca.png) Финальные правки:

Один из вариантов сокрытия полей в примерах Swagger - сделать специальный TO класс. Но можно сделать проще через специальные аннотации: [Hide a Request Field in Swagger API](https://www.baeldung.com/spring-swagger-hide-field)
- Скрываем необязательные поле `id` при POST и PUT запросах через `@ApiModelProperty(hidden = true)` в примерах запроса Swagger. При этом передавать поле в запросе можно.
- `Meal.user` отсутствует в REST API, можно игнорировать: `@JsonIgnore`
- `User.meals` можно было сделать `JsonProperty.Access.READ_ONLY`, но при этом не пройдут тесты `getWithMeals` (maels не будет сериализоваться из ответа сервера для сравнения). Скрыл также через `@ApiModelProperty(hidden = true)`
- Также можно было скрыть нулевое поле `User.meals` при выводе через `@JsonInclude(JsonInclude.Include.NON_EMPTY)`. Но при этом поле исчезнет при запросе `getWithMeals` пользователя с пустым списком еды (например для Guest). Все зависит от бизнес-требований приложения (например насколько API публично и должно быть красивым). Можете попробовать самостоятельно скрыть это поле из вывода для запросов без еды через `View` (или отдельный TO).

#### Apply [11_16_HW_fix_swagger.patch](https://drive.google.com/file/d/1A76XXvZdZCKxeKnVjZ2VkrWAHEQ1iof2)

## Миграция на Spring Boot
За основу взят финальный код проекта BootJava с миграцией на Spring Boot 3.0 - это первый патч открытого урока курса [CloudJava](https://javaops.ru/view/cloudjava/lesson01),
ветка [_patched_](https://github.com/JavaOPs/cloudjava/tree/patched).
Вычекайте в отдельную папку (как отдельный проект) ветку `spring_boot` нашего проекта (так удобнее, не придется постоянно переключаться между ветками):
```
git clone --branch spring_boot --single-branch https://github.com/JavaWebinar/topjava.git topjava_boot
```  
Если будете его менять, [настройте `git remote`](https://javaops.ru/view/bootjava/lesson01#project)  
> Если захотите сами накатить патчи, сделайте ветку `spring_boot` от первого `initial` и в корне **создайте каталог `src\test`**  

----

#### Apply 12_1_init_boot_java
Оставил как в TopJava:
- название приложения  `Calories Management`
- имя базы `topjava`
- пользователей:  `user@yandex.ru`, `admin@gmail.com`, `guest@gmail.com`
- Swagger открывается из `localhost:8080` через конфигурацию `springdoc.swagger-ui.path: /`
- в `AppConfig` сохранение `objectMapper` должно проходить только после регистрации `Hibernate5Module` - объединил их в `configureAndStoreObjectMapper`
- для регистрации выделил отдельный контроллер `RegisterController`
####  Обновление на Spring Boot 3
- Spring 6, Hibernate 6.1, Hibernate Validator 7, R2DBC 1.0, JPA 3.1, Tomcat 10
- Java 17 – минимальная версия
- Генерация нативных образов GraalVM
- Улучшения в observability через Micrometer и Micrometer Tracing
- Переход от Java EE к Jakarta EE. Jakarta EE 9 – минимальная версия, поддержка Jakarta EE 10
- [Spring Boot 3.0 Goes GA](https://spring.io/blog/2022/11/24/spring-boot-3-0-goes-ga)
- [What's New in Spring Framework 6.x](https://github.com/spring-projects/spring-framework/wiki/What%27s-New-in-Spring-Framework-6.x/)
- [Spring Boot 3.0 Goes GA](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Release-Notes)
- [Get ready for Spring Boot 3.0](https://www.springcloud.io/post/2022-05/springboot-3-0)
- [The best way to do the Spring 6 migration](https://vladmihalcea.com/spring-6-migration/)

---------------------

-  В `pom.xml` обновляем версии Spring и Springdoc
-  [Мигрируем Springdoc на 2.x](https://github.com/springdoc/springdoc-openapi-demos/wiki/springdoc-openapi-2.x-migration-guide): меняется зависимости и пакет `GroupedOpenApi`.
-  Меняем зависимость `jackson-datatype-hibernate5` на `jackson-datatype-hibernate5-jakarta` и в `AppConfig` заменяем `Hibernate5Module` на `Hibernate5JakartaModule`
-  В security `antMatchers` меняется на `requestMatchers` и нужно явно исключать _swagger-ui_ из авторизации
-  `ResponseEntityExceptionHandler` теперь поддерживает спецификацию [RFC-7807](https://www.rfc-editor.org/rfc/rfc7807.html) - описание ошибок, перешел на нее в исключениях. В класс описания ошибок ProblemDetail можно добавлять свои поля: в `GlobalExceptionHandler#handleMethodArgumentNotValid()` добавил поле `invalid_params`. Однако при сериализации Jackson через поля, как у нас, в ответе это поле дублируется. Пришлось делать workaround: класс `AppConfig.MixIn`.
-  В `AdminUserControllerTest` не идут тесты на запросы со слешем в конце. Сделал отдельную переменную `REST_URL_SLASH`

#### Apply 12_2_add_calories_meals

Добавил из TopJava: 
- Еду, калории
- Таблицы назвал в единственном числе: `user_role, meal` (кроме `users`, _user_ зарезервированное слово)
- Общие вещи (пусть небольшие) вынес в сервис : `MealService`
- Проверку принадлежности еды делаю в `MealRepository.checkBelong` с исключением `DataConflictException` (не зависит от `org.springframework.web`)
- Вместо своих конверторов использую `@DateTimeFormat`
- Мигрировал все тесты контроллеров. В выпускном проекте столько тестов необязательно! Достаточно нескольких, на основные юзкейсы.
- Кэширование в выпускном желательно. 7 раз подумайте, что будете кэшировать! **Максимально просто, самые частые запросы, которые редко изменяются**.
- **Добавьте в свой выпускной OpenApi/Swagger - это будет большим плюсом и избавит от необходимости писать документацию**.

#### Apply 12_3_update_fix
- Обновил версии зависимостей
- Вернул регистрацию в `ProfileController`, возвращаемый при создании `Location` теперь верный. На курсе Spring Cloud разделение было логично, но пока этот функционал сыроват и требует осмысления.

### За основу выпускного предлагаю взять этот код миграции, сделав свой выпускной МАКСИМАЛЬНО в этом стиле.
### Успехов с выпускным проектом и в карьере! 
