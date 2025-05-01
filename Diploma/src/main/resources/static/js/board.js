function handleDragStart(e) {
    // Сохраняем ID задачи для использования при drop
    e.dataTransfer.setData("taskId", e.target.dataset.taskId);
}

function initKanbanDragAndDrop() {
    const columns = document.querySelectorAll('.kanban-column .tusk-container');

    columns.forEach(container => { // container теперь будет правильно определён для каждой колонки
        container.addEventListener('dragover', (e) => {
            e.preventDefault();
            const taskElement = e.target.closest('.task-content');
            if (taskElement && taskElement !== e.target) {
                taskElement.classList.add('drag-over');
            }
        });

        container.addEventListener('dragleave', (e) => {
            const taskElement = e.target.closest('.task-content');
            if (taskElement) {
                taskElement.classList.remove('drag-over');
            }
        });

        container.addEventListener('drop', async (e) => {
            e.preventDefault();

            const taskId = e.dataTransfer.getData("taskId");
            const taskElement = document.querySelector(`[data-task-id="${taskId}"]`);
            const newStatus = container.closest('.kanban-column').querySelector('.kanban-column-name span').innerText;

            const dropTarget = e.target.closest('.task-content');

            if (!dropTarget || dropTarget.classList.contains('add-task-btn')) {
                const addButton = container.querySelector('.add-task-btn');
                container.insertBefore(taskElement, addButton); // вставка ПЕРЕД кнопкой
            } else if (dropTarget !== taskElement) {
                container.insertBefore(taskElement, dropTarget);
            }

            try {
                const response = await fetch('/update_status', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ taskId, status: newStatus })
                });

                if (!response.ok) throw new Error('Ошибка при обновлении статуса');
            } catch (err) {
                console.error(err);
            }

            taskElement.classList.remove('drag-over');
        });
    });
}


initKanbanDragAndDrop();



// Отрисовывание задач на доске
async function loadActiveSprint(projectId) {
    const iconMap = {
        task: 'icons/tusk.svg',
        story: 'icons/history.svg',
        bug: 'icons/bug.svg'
    };

    try {
        const response = await fetch(`/api/sprint/active/${projectId}`);
        if (!response.ok) {
            throw new Error('Не удалось загрузить активный спринт');
        }
        const sprintData = await response.json();
        const { tasks } = sprintData;

        // Колонки по статусу задачи
        const columnMap = {
            'К выполнению': '.kanban-column:first-child .tusk-container',
            'В работе': '.kanban-column:nth-child(2) .tusk-container',
            'Выполнено': '.kanban-column:nth-child(3) .tusk-container'
        };

        tasks.forEach(task => {
            const icon = iconMap[task.taskType] || 'icons/tusk.svg';
            const statusColumn = task.status; // Предполагаем, что статус хранится в поле task.status
            if (!task.status) return;
            const taskContentHTML = `
                <div class="task-content" draggable="true" data-task-id="${task.id}">
                    <div class="task-name">
                        <span class="task-title">${task.title}</span>
                    </div>
                    <div class="tusk-bottom">
                        <div class="tag-and-key">
                            <img class="tag" src="${icon}">
                            <span class="task-id">${task.taskKey}</span>
                        </div>
                        <button>
                            <img class="performer" src="/icons/Group%205.svg">
                        </button>
                    </div>
                </div>
            `;

            const taskContainer = document.createElement('div');
            taskContainer.innerHTML = taskContentHTML;

            // Находим колонку по статусу задачи
            const kanbanColumn = document.querySelector(columnMap[statusColumn]);
            const addButton = kanbanColumn.querySelector('.add-task-btn');
            kanbanColumn.insertBefore(taskContainer.firstElementChild, addButton);
            const taskElement = kanbanColumn.querySelector(`[data-task-id="${task.id}"]`);
            taskElement.addEventListener("dragstart", handleDragStart);
        });
    } catch (err) {
        console.error('Ошибка при загрузке активного спринта:', err);
    }
}


function getProjectIdFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('id');
}

// Загрузка данных спринта
async function loadSprintInfo(projectId) {
    try {
        const response = await fetch(`/api/sprint/active/${projectId}`);
        if (!response.ok) {
            throw new Error('Не удалось загрузить данные о спринте');
        }

        const sprintData = await response.json();
        const sprintName = sprintData.sprintName || "Спринт какой по списку был создан";
        const startDate = new Date(sprintData.startDate);
        const endDate = new Date(sprintData.endDate);
        const duration = Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24));

        // Отображаем название спринта
        document.getElementById('sprint-name').textContent = `${sprintName}`;

        // Запуск отсчета
        startSprintCountdownDisplay(sprintData.startDate, duration);

    } catch (error) {
        console.error(error);
        document.getElementById('sprint-name').textContent = "Спринт";
        document.getElementById('sprint-duration').textContent = "Длительность:";
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const projectId = getProjectIdFromUrl();
    if (projectId) {
        loadActiveSprint(projectId);
        loadSprintInfo(projectId);
    }

    const searchInput = document.querySelector(".search-input-board");
    let searchTimeout = null;

    searchInput.addEventListener("input", async (e) => {
        const query = e.target.value.trim();

        // Очищаем предыдущий таймер
        if (searchTimeout) clearTimeout(searchTimeout);

        searchTimeout = setTimeout(async () => {
            const projectId = getProjectIdFromUrl();

            // Если строка поиска пуста — перезагружаем все задачи
            if (!query) {
                console.log("Строка поиска пуста — загружаем все задачи снова");
                const columns = document.querySelectorAll(".kanban-column");
                columns.forEach(column => {
                    const container = column.querySelector(".tusk-container");
                    container.querySelectorAll(".task-content").forEach(el => el.remove());
                });
                loadActiveSprint(projectId);
                return;
            }

            // Если введён текст — ищем
            console.log("Поиск начался...");
            const sprintId = await getActiveSprintId(projectId);
            if (!sprintId) return;

            try {
                const res = await fetch(`/api/sprint/${sprintId}/search?projectId=${projectId}&query=${encodeURIComponent(query)}`);
                if (!res.ok) {
                    console.error("Ошибка поиска задач");
                    return;
                }

                const tasks = await res.json();
                console.log("Найденные задачи:", tasks);

                renderSearchResults(tasks);
            } catch (err) {
                console.error("Ошибка во время выполнения поиска:", err);
            }
        }, 400); // Задержка 400 мс
    });
});

// создание задачи на доске
function createTaskFormBoard() {
    const formContainer = document.createElement("div");
    formContainer.className = "task-content task-form";

    formContainer.innerHTML = `
        <input type="text" class="task-title-bs" placeholder="Введите что хотите сделать" />
        <div class="select-create-wrap-board">
        <div class="task-type-select">
            <div class="custom-select">
                <div class="selected-option">
                    <div class="content">
                        <img src="/icons/tusk.svg" alt="иконка задачи" />
                        <span class="selected-text">Задача</span>
                    </div>
                    <div class="arrow">
                        <img src="/icons/mingcute_down-line.svg" />
                    </div>
                </div>
                <div class="optionsbacklog" style="display: none;">
                    <div class="option" data-value="task">
                        <img src="/icons/tusk.svg" alt="иконка задачи" />
                        <span class="option-text">Задача</span>
                    </div>
                    <div class="option" data-value="story">
                        <img src="/icons/history.svg" alt="иконка истории" />
                        <span class="option-text">История</span>
                    </div>
                    <div class="option" data-value="bug">
                        <img src="/icons/bug.svg" alt="иконка бага" />
                        <span class="option-text">Баг</span>
                    </div>
                </div>
                <input type="hidden" name="taskType" class="task-type-input" value="task" />
            </div>
        </div>
              <button class="save-task-btn">
               <img src="/icons/enter.svg"> 
            </button>
        </div>
    `;

    return formContainer;
}

document.addEventListener("click", (e) => {
    const btn = e.target.closest(".add-task-btn");
    if (!btn) return;

    const column = btn.closest(".kanban-column");

    // Если форма уже есть — не добавляем
    if (column.querySelector(".task-form")) return;

    const form = createTaskFormBoard();
    column.querySelector(".tusk-container").insertBefore(form, btn);

    form.querySelector(".task-title-bs").focus();

    setupCustomSelect(form);
    form.querySelector(".save-task-btn").addEventListener("click", async () => {

        const title = form.querySelector(".task-title-bs").value.trim();
        const taskType = form.querySelector(".task-type-input").value;

        if (!title) {
            alert("Введите название задачи");
            return;
        }

        const status = column.querySelector(".kanban-column-name").textContent.trim();
        const projectId = getProjectIdFromUrl();

        const sprintRes = await fetch(`/api/sprint/active/${projectId}`);
        if (!sprintRes.ok) {
            alert("Ошибка загрузки активного спринта");
            return;
        }

        const sprint = await sprintRes.json();

        const newTask = {
            title: title,
            task_type: taskType,
            sprintId: sprint.id,
            projectId: projectId,
            status: status
        };

        const createRes = await fetch("/create_task", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(newTask)
        });

        if (!createRes.ok) {
            alert("Ошибка создания задачи");
            return;
        }

        const taskData = await createRes.json();

        const iconMap = {
            task: 'icons/tusk.svg',
            story: 'icons/history.svg',
            bug: 'icons/bug.svg'
        };

        const taskContentHTML = `
        <div class="task-content" draggable="true" data-task-id="${taskData.id}">
            <div class="task-name">
                <span class="task-title">${taskData.title}</span>
            </div>
            <div class="tusk-bottom">
                <div class="tag-and-key">
                    <img class="tag" src="${iconMap[taskData.taskType] || 'icons/tusk.svg'}">
                    <span class="task-id">${taskData.taskKey}</span>
                </div>
                <button>
                    <img class="performer" src="/icons/Group%205.svg">
                </button>
            </div>
        </div>
    `;

        const taskContainer = document.createElement("div");
        taskContainer.innerHTML = taskContentHTML;

        const addButton = column.querySelector(".add-task-btn");
        column.querySelector(".tusk-container").insertBefore(taskContainer.firstElementChild, addButton);

        const taskElement = column.querySelector(`[data-task-id="${taskData.id}"]`);
        taskElement.addEventListener("dragstart", handleDragStart);

        // Убираем форму
        form.remove();
        addButton.style.display = "flex";
    });


    btn.style.display = "none";

    // Отключаем hover эффекты для кнопок внутри колонки

    // Удаление формы при клике вне
    document.addEventListener("click", function handler(event) {
        if (!form.contains(event.target) && event.target !== btn) {
            form.remove();
            btn.style.display = "flex";

            // Восстанавливаем hover эффекты для кнопок

            document.removeEventListener("click", handler);
        }
    });

});

function setupCustomSelect(container) {
    const select = container.querySelector(".custom-select");
    const selected = select.querySelector(".selected-option");
    const options = select.querySelector(".optionsbacklog");
    const input = select.querySelector(".task-type-input");
    const textSpan = select.querySelector(".selected-text");
    const iconImg = select.querySelector(".selected-option .content img");

    selected.addEventListener("click", () => {
        const isVisible = options.style.display === "block";

        // Перед открытием: скрыть выбранную опцию
        const currentValue = input.value;
        options.querySelectorAll(".option").forEach(option => {
            option.style.display = option.dataset.value === currentValue ? "none" : "flex";
        });

        options.style.display = isVisible ? "none" : "block";
        selected.querySelector(".arrow").classList.toggle("open", !isVisible);
    });

    options.querySelectorAll(".option").forEach(option => {
        option.addEventListener("click", () => {
            const value = option.dataset.value;
            const text = option.querySelector(".option-text").textContent;
            const icon = option.querySelector("img").src;

            input.value = value;
            textSpan.textContent = text;
            iconImg.src = icon;

            options.style.display = "none";
            selected.querySelector(".arrow").classList.remove("open");
        });
    });
}
// Поиск по доске


async function getActiveSprintId(projectId) {
    const res = await fetch(`/api/sprint/active/${projectId}`);
    if (!res.ok) return null;
    const data = await res.json();
    return data.id;
}

function renderSearchResults(tasks) {
    const columns = document.querySelectorAll(".kanban-column");

    // Очистить все контейнеры задач
    columns.forEach(column => {
        const container = column.querySelector(".tusk-container");
        container.querySelectorAll(".task-content").forEach(el => el.remove());
    });

    const iconMap = {
        task: 'icons/tusk.svg',
        story: 'icons/history.svg',
        bug: 'icons/bug.svg'
    };

    const query = document.querySelector(".search-input-board").value.trim().toLowerCase();

    tasks.forEach(task => {
        const column = [...columns].find(col => {
            const name = col.querySelector(".kanban-column-name").textContent.trim();
            return name === task.status;
        });

        if (!column) return;

        const container = column.querySelector(".tusk-container");

        // Подсветка совпадений в названии задачи
        const titleLower = task.title.toLowerCase();
        const matchIndex = titleLower.indexOf(query);
        let highlightedTitle = task.title;

        if (matchIndex !== -1 && query.length > 0) {
            const before = task.title.slice(0, matchIndex);
            const match = task.title.slice(matchIndex, matchIndex + query.length);
            const after = task.title.slice(matchIndex + query.length);
            highlightedTitle = `${before}<span class="highlight">${match}</span>${after}`;
        }

        const html = `
            <div class="task-content" draggable="true" data-task-id="${task.id}">
                <div class="task-name">
                    <span class="task-title">${highlightedTitle}</span>
                </div>
                <div class="tusk-bottom">
                    <div class="tag-and-key">
                        <img class="tag" src="${iconMap[task.taskType] || 'icons/tusk.svg'}">
                        <span class="task-id">${task.taskKey}</span>
                    </div>
                    <button>
                        <img class="performer" src="/icons/Group%205.svg">
                    </button>
                </div>
            </div>
        `;

        const taskElement = document.createElement("div");
        taskElement.innerHTML = html;

        const addButton = column.querySelector(".add-task-btn");
        column.querySelector(".tusk-container").insertBefore(taskElement.firstElementChild, addButton);
    });
}

// Длительность спринта
function startSprintCountdownDisplay(startDateStr, durationInDays) {
    const startDate = new Date(startDateStr);
    const endDate = new Date(startDate.getTime() + durationInDays * 24 * 60 * 60 * 1000);

    function updateCountdown() {
        const now = new Date();
        const diff = endDate - now;

        const durationElement = document.getElementById('sprint-duration');

        const days = Math.floor(diff / (1000 * 60 * 60 * 24));
        const hours = Math.floor((diff / (1000 * 60 * 60)) % 24);
        const minutes = Math.floor((diff / (1000 * 60)) % 60);

        // Форматируем с ведущим нулём
        const formatted = `${days} дней ${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`;
        durationElement.textContent = formatted;

        setTimeout(updateCountdown, 60 * 1000); // обновляем каждую минуту
    }

    updateCountdown();
}







