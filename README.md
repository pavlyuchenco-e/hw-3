Павлюченко Елизавета Александровна
Б9124-09.03.03пикд4

Юнит-тесты: 8
Интеграционные тесты: 5
Нетривиальные тесты: 3
Тесты на Flow: 2 (полная последовательность эмиссий + нетривиальное потоковое поведение)

Покрытые сценарии
Юнит-тесты (ShowViewModelTest)
№	Название	Что проверяет
1	initial state is EmptyQuery	Начальное состояние экрана – EmptyQuery
2	searchShows emits Loading then Success	Успешная загрузка: Loading → Success с данными
3	searchShows emits Loading then Error on exception	Ошибка сети: Loading → Error с сообщением
4	retry after error calls repository again	Повторный вызов (Retry) после ошибки приводит к успеху
5	empty search result emits NoResults	Пустой результат – состояние NoResults, а не Success(emptyList())
6	ShowDto toDomain converts correctly	Корректное преобразование DTO → доменная модель (удаление HTML‑тегов, обработка null)
7	favouritesUiState emits Loading then Success on first load	Flow‑тест: последовательность эмиссий Loading → Success для избранного
8	outdated search result is ignored	Нетривиальное потоковое поведение: отмена устаревшего поискового запроса (более ранний результат не попадает в UI)
Интеграционные тесты
a) Data‑слой (ShowRepositoryTest)
toggleFavourite adds and removes from database
Проверка, что добавление в избранное сохраняется в Room, а удаление — удаляет запись.

adding same show twice does not create duplicate
Повторное добавление одного шоу не создаёт дубликат в таблице избранного (нетривиальный сценарий).

b) UI‑навигация (NavigationTest)
after successful search shows list is displayed
После успешного поиска список результатов отображается (используется мок‑репозиторий).

click on show card opens detail screen
Клик по карточке шоу открывает экран деталей (проверяется кнопка «Назад»).

c) Ошибка и Retry (RetryIntegrationTest)
error then retry shows success using mockk
При ошибке сети отображается кнопка «Повторить», после нажатия запрос повторяется, данные успешно загружаются и отображаются.

Тестирование Flow
Полная последовательность эмиссий:
Тест favouritesUiState emits Loading then Success on first load проверяет, что при загрузке избранного сначала приходит Loading, затем Success.

Нетривиальное потоковое поведение:
Тест outdated search result is ignored подтверждает, что при быстрой смене поискового запроса результат более раннего запроса не вытесняет более поздний (корректная отмена корутины через searchJob.cancel()).

