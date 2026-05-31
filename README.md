Павлюченко Елизавета Александровна
Б9124-09.03.03пикд4

Юнит-тесты: 8
Интеграционные тесты: 4
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
ShowRepositoryTest (2 теста) - Data‑слой - Repository + Fake API + Room – добавление и удаление избранного, отсутствие дублей при повторной вставке.
RetryIntegrationTest - UI (состояние) -	Сценарий ошибка → нажатие Retry → успешное состояние (используется Fake API + in‑memory Room + реальный NavGraph).
NavigationTest.after successful search show card is displayed -	UI (состояние)	Отображение корректного состояния экрана после загрузки данных – карточка шоу появляется на экране списка.

Тестирование Flow
Полная последовательность эмиссий – favouritesUiState при загрузке испускает Loading, затем Success.

Нетривиальное потоковое поведение – outdated search result is ignored демонстрирует отмену устаревшей корутины поиска.

Нетривиальное тесты:
retry after error calls repository again – проверяет, что retryLastSearch() действительно инициирует новый сетевой запрос.

outdated search result is ignored – проверяет отмену устаревшего поискового запроса (более ранний результат не перезаписывает более поздний).

empty search result emits NoResults – гарантирует, что пустой ответ API приводит к состоянию NoResults, а не Success(emptyList()).

toggleFavourite adds show then removes it – после записи в Room данные корректно читаются повторно.
