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
                const columns = document.querySelectorAll(".kanban-column");
                columns.forEach(column => {
                    const container = column.querySelector(".tusk-container");
                    container.querySelectorAll(".task-content").forEach(el => el.remove());
                });
                loadActiveSprint(projectId);
                return;
            }

            // Если введён текст — ищем
            const sprintId = await getActiveSprintId(projectId);
            if (!sprintId) return;

            try {
                const res = await fetch(`/api/sprint/${sprintId}/search?projectId=${projectId}&query=${encodeURIComponent(query)}`);
                if (!res.ok) {
                    console.error("Ошибка поиска задач");
                    return;
                }

                const tasks = await res.json();

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

// работа с задачей
document.addEventListener('click', (event) => {
    const container = event.target.closest('.task-content');
    if (!container) return;

    const taskId = container.getAttribute('data-task-id');
    if (!taskId) {
        console.error('ID задачи не найден в data-task-id');
        return;
    }

    // Загружаем задачу с сервера
    fetch(`/api/tasks/${taskId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Не удалось загрузить задачу');
            }
            return response.json();
        })
        .then(task => {
            // Открываем модалку с полученными данными
            openTaskModal(task);
        })
        .catch(error => {
            console.error(error);
            alert('Ошибка при загрузке задачи');
        });
});
// открытие окна задачи
function openTaskModal(task) {
    const template = document.getElementById('task-modal-template');
    const modal = template.content.cloneNode(true);
    const containerEl = document.getElementById('modal-container');
    containerEl.innerHTML = '';
    containerEl.appendChild(modal);
    containerEl.setAttribute('data-task-id', task.id);
    fillTaskModalData(containerEl, task);
    setupCloseModal(containerEl);
    const difficultyBlock = containerEl.querySelector('.difficulty-block');
    if (difficultyBlock) {
        initDifficultyEditor(difficultyBlock,task);
    }
    setupSectionSwitching(containerEl);
    setupTitleEdit(containerEl, task);
    initTinyMCEIfNeeded(containerEl);
    setupDescriptionEdit(containerEl, task);
    loadComments(task.id);
    const submitBtn = containerEl.querySelector('#submit-comment-btn');
    if (submitBtn) {
        submitBtn.addEventListener('click', () => {
            const taskId = containerEl.getAttribute('data-task-id');
            submitComment(taskId); // Отправка комментария при клике
        });
    }
    const commentsSection = containerEl.querySelector('.comments-section');
    const historySection = containerEl.querySelector('.history-section');
    if (commentsSection && historySection) {
        commentsSection.style.display = 'block';
        historySection.style.display = 'none';
    }

    const commentsBtn = containerEl.querySelector('button[data-target="comments-section"]');
    if (commentsBtn) commentsBtn.classList.add('active');
    initializeTags(containerEl);
    loadTagsForTask(task.id, containerEl);
}
//заполнение данных задачи из бд
function fillTaskModalData(containerEl, task) {
    containerEl.querySelector('.task-key-info span').textContent = task.taskKey;
    containerEl.querySelector('.task-title-modal').textContent = task.title;
    containerEl.querySelector('.description-container span').textContent = task.description || 'Добавьте описание';
    containerEl.querySelector('.difficulty').textContent = task.priority;
    containerEl.querySelector('.select-status').value = task.status;

    containerEl.querySelector('.createdAt span:nth-child(2)').textContent = new Date(task.createdAt).toLocaleString('ru-RU');
    containerEl.querySelector('.editedAt span:nth-child(2)').textContent = new Date(task.updatedAt).toLocaleString('ru-RU');

    const taskTypeIconEl = containerEl.querySelector('.task-type-icon');
    const iconMap = {
        task: 'icons/tusk.svg',
        story: 'icons/history.svg',
        bug: 'icons/bug.svg'
    };
    taskTypeIconEl.src = iconMap[task.taskType] || iconMap.task;

    const boardEl = containerEl.querySelector('.info-tag-inner:nth-child(4) .tags-container span');
    if (boardEl && task.sprintName)  boardEl.textContent = task.sprintName;
    const authorEl = containerEl.querySelector('.info-tag-inner:nth-child(6) .tags-container .user-div span');
    if (authorEl) {
        authorEl.textContent = task.authorName && task.authorName.trim() !== "Не назначено"
            ? task.authorName
            : task.authorEmail || "Неизвестен";
    }
}
// кнопка закрытия окна
function setupCloseModal(containerEl) {
    containerEl.querySelector('.close-modal').addEventListener('click', () => {
        tinymce.remove();
        containerEl.innerHTML = '';
    });
}
// переключение комментариев и истории
function setupSectionSwitching(containerEl) {
    containerEl.querySelectorAll('.action-nav button').forEach(button => {
        button.addEventListener('click', () => {
            const target = button.getAttribute('data-target');
            const taskAction = button.closest('.task-action');

            taskAction.querySelectorAll('.activity-content .section').forEach(section => {
                section.style.display = 'none';
            });

            const toShow = taskAction.querySelector(`.${target}`);
            if (toShow) {
                toShow.style.display = 'block';
                if (target === 'comments-section') {
                    initTinyMCEIfNeeded(containerEl);
                }
            }
        });
    });
}
// меняем название задачи
function setupTitleEdit(containerEl, task) {
    const titleSpan = containerEl.querySelector('.task-title-modal');
    const editControls = containerEl.querySelector('.edit-title-controls');
    const inputEl = editControls.querySelector('.edit-title-input');
    const saveBtn = editControls.querySelector('.save-title-btn');
    const cancelBtn = editControls.querySelector('.cancel-title-btn');

    titleSpan.addEventListener('click', () => {
        titleSpan.style.display = 'inline-block';
        requestAnimationFrame(() => {
            const spanRect = titleSpan.getBoundingClientRect();
            inputEl.style.width = spanRect.width + 'px';
            inputEl.value = titleSpan.textContent.trim();
            titleSpan.style.display = 'none';
            editControls.style.display = 'block';
            inputEl.focus();
        });
    });

    saveBtn.addEventListener('click', async () => {
        const newTitle = inputEl.value.trim();
        if (!newTitle || newTitle === task.title) {
            cancelEdit();
            return;
        }

        try {
            const response = await fetch(`/api/tasks/${task.id}/title`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ title: newTitle })
            });

            if (!response.ok) throw new Error('Ошибка при обновлении названия');
            titleSpan.textContent = newTitle;
            task.title = newTitle;
        } catch (err) {
            console.error(err);
            alert('Не удалось обновить название задачи');
        } finally {
            cancelEdit();
        }
    });

    cancelBtn.addEventListener('click', cancelEdit);

    function cancelEdit() {
        editControls.style.display = 'none';
        titleSpan.style.display = 'inline-block';
    }
}
// инициализация tinyMCE
function initTinyMCEIfNeeded(containerEl) {
    const textarea = containerEl.querySelector('#comments');
    const taskId = containerEl.getAttribute('data-task-id');

    if (textarea && !textarea.classList.contains('tinymce-initialized')) {
        tinymce.init({
            target: textarea, // безопаснее использовать target, чтобы не конфликтовать с другими
            menubar: false,
            plugins: 'lists link emoticons table image media',
            toolbar: 'undo redo | bold italic underline | forecolor backcolor | bullist numlist | link emoticons | alignleft aligncenter alignright | image table media',
            setup: editor => {
                editor.on('init', () => textarea.classList.add('tinymce-initialized'));

            },
            width: '100%',
            height: 150,
            resize: false,
            placeholder: 'Напишите ваш комментарий...',
        });
    }
}
// Редактирование описания
function setupDescriptionEdit(containerEl, task) {
    const descriptionSpan = containerEl.querySelector('.description-text');
    const editControls = containerEl.querySelector('.edit-description-controls');
    const textarea = editControls.querySelector('.edit-description-textarea');
    const saveBtn = editControls.querySelector('.save-description-btn');
    const cancelBtn = editControls.querySelector('.cancel-description-btn');

    // Переход в режим редактирования
    descriptionSpan.addEventListener('click', () => {
        textarea.value = task.description || '';
        descriptionSpan.style.display = 'none';
        editControls.style.display = 'block';
        textarea.focus();
    });

    // Сохранение описания
    saveBtn.addEventListener('click', async () => {
        const newDescription = textarea.value.trim();
        try {
            const response = await fetch(`/api/tasks/${task.id}/description`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ description: newDescription })
            });

            if (!response.ok) throw new Error('Ошибка при обновлении описания');

            task.description = newDescription;
            descriptionSpan.textContent = newDescription || 'Добавьте описание';
        } catch (err) {
            console.error(err);
            alert('Не удалось обновить описание задачи');
        } finally {
            editControls.style.display = 'none';
            descriptionSpan.style.display = 'inline-block';
        }
    });


    // Отмена редактирования
    cancelBtn.addEventListener('click', () => {
        editControls.style.display = 'none';
        descriptionSpan.style.display = 'inline-block';
    });
}
//Редактирование сложности
function initDifficultyEditor(containerEl,task) {
    const difficultyEl = containerEl.querySelector('.difficulty');
    const difficultyEditor = containerEl.querySelector('.difficulty-editor');
    const difficultyInput = containerEl.querySelector('.difficulty-input');
    const saveButton = containerEl.querySelector('.save-difficulty');
    const cancelButton = containerEl.querySelector('.cancel-difficulty');
    const tagsContainer = containerEl.querySelector('.tags-container');
    const actionsContainer = containerEl.querySelector('.difficulty-actions');

    // Сброс состояния
    difficultyEditor.style.display = 'none';
    actionsContainer.style.display = 'none';
    difficultyEl.style.display = 'inline';

    // Клик по .tags-container (именно сложности)
    tagsContainer.addEventListener('click', function () {
        if (difficultyEditor.style.display === 'none') {
            difficultyInput.value = difficultyEl.textContent.trim();
            difficultyEl.style.display = 'none';
            difficultyEditor.style.display = 'flex';
            actionsContainer.style.display = 'flex';
            difficultyInput.focus();
        }
    });

    saveButton.addEventListener('click', function (event) {
        event.stopPropagation();
        const newPriority = difficultyInput.value;

        fetch(`/api/tasks/${task.id}/priority`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ priority: newPriority })
        })
            .then(response => response.json())
            .then(data => {
                difficultyEl.textContent = data.priority;
                difficultyEl.style.display = 'inline';
                difficultyEditor.style.display = 'none';
                actionsContainer.style.display = 'none';
            })
            .catch(error => {
                console.error('Error updating priority:', error);
                alert('Не удалось обновить приоритет');
            });
    });

    cancelButton.addEventListener('click', function (event) {
        event.stopPropagation();
        difficultyEditor.style.display = 'none';
        actionsContainer.style.display = 'none';
        difficultyEl.style.display = 'inline';
    });

    difficultyInput.addEventListener('click', e => e.stopPropagation());
    difficultyEditor.addEventListener('click', e => e.stopPropagation());
}
//работа с комментариями
async function submitComment(taskId) {
    const editor = tinymce.get('comments');  // Получаем редактор TinyMCE
    const text = editor.getContent();  // Получаем содержимое редактора

    if (!text.trim()) return;  // Если комментарий пустой, не отправляем

    const response = await fetch(`/api/tasks/${taskId}/comments`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text })  // Отправляем только текст комментария
    });

    if (response.ok) {
        const comment = await response.json();
        appendComment(comment);  // Добавляем комментарий на страницу
        editor.setContent('');  // Очищаем редактор после отправки
    } else {
        alert('Ошибка при отправке комментария!');
    }
}

async function loadComments(taskId) {
    const response = await fetch(`/api/tasks/${taskId}/comments/active`);
    if (response.ok) {
        const comments = await response.json();
        const list = document.querySelector('.comments-section .comments-list');
        list.innerHTML = '';  // Очищаем комментарии
        comments.forEach(comment => {
            appendComment(comment);
        });
    } else {
        alert('Ошибка при загрузке комментариев!');
    }
}

function appendComment(comment) {
    const list = document.querySelector('.comments-section .comments-list');  // Теперь сюда вставляем
    if (!comment.author) {
        console.error("Author is missing in the comment:", comment);
        return;
    }


    // Преобразуем строку даты в объект Date
    const createdAt = new Date(comment.createdAt);

    // Форматируем дату и время для отображения
    const formattedDate = createdAt.toLocaleDateString('ru-RU', {
        day: 'numeric',
        month: 'long',
        year: 'numeric'
    });

    const formattedTime = createdAt.toLocaleTimeString('ru-RU', {
        hour: '2-digit',
        minute: '2-digit'
    });

    const commentBlock = document.createElement('div');
    commentBlock.classList.add('comment-container');

    commentBlock.innerHTML = `
        <div class="section-grid section-photo">
            <img src="icons/Group%205.svg" alt="Avatar">
        </div>
        <div class="section-grid section-void"></div>
        <div class="section-grid section-comment">
            <div class="sender-info">
                <span class="sender-name">${comment.author.username || comment.author.email}</span>
                <span>${formattedDate} в ${formattedTime}</span>
            </div>
            <div class="comment-message">
                ${comment.text}
            </div>
        </div>
        <div class="section-grid section-actions">
            <button class="comment-btn edit">Изменить</button>
            <button class="comment-btn delete">Удалить</button>
        </div>
    `;

    // Вставляем новый комментарий в начало списка
    list.insertBefore(commentBlock, list.firstChild);
}

//Работа с метками
function initializeTags(containerEl) {
    const tagsContainer = containerEl.querySelector('.task-tags-container');
    const viewContainer = tagsContainer.querySelector('.tags-tags');
    const editInputWrapper = tagsContainer.querySelector('.editing-tags-input');
    const editTagContainer = tagsContainer.querySelector('.editing-tags-wrapper');
    const tagInput = tagsContainer.querySelector('.tag-input');
    const tagSearchResults = tagsContainer.querySelector('.tag-search-results');

    tagsContainer.addEventListener('click', async () => {
        if (tagsContainer.classList.contains('editing')) return;

        tagsContainer.classList.add('editing');
        viewContainer.style.display = 'none';
        editInputWrapper.style.display = 'flex';
        tagInput.focus();

        const taskId = document.getElementById('modal-container').getAttribute('data-task-id');
        const response = await fetch(`/api/tasks/${taskId}/tags`);
        const tags = await response.json(); // используем json() вместо text() для прямого получения объекта
        tags.forEach(tag => {
            const tagEl = createEditableTagElement(tag, editTagContainer);
            editTagContainer.appendChild(tagEl);
        });
    });

    document.addEventListener('click', function (e) {
        if (!tagsContainer.contains(e.target)) {
            tagsContainer.classList.remove('editing');
            viewContainer.style.display = 'flex';
            editInputWrapper.style.display = 'none';
            tagSearchResults.style.display = 'none';
            tagInput.value = '';
            editTagContainer.innerHTML = '';
            updateTagsDisplay(); // Обновляем список меток, когда снимаем выделение
        }
    });

    tagInput.addEventListener('input', async function () {
        const searchTerm = tagInput.value.trim();
        if (searchTerm.length > 0) {
            const response = await fetch(`/api/tags/search?query=${searchTerm}`);
            const tags = await response.json();
            // Получаем уже выбранные метки
            const selectedTags = Array.from(editTagContainer.querySelectorAll('.tag-item'))
                .map(el => el.textContent.replace('×', '').trim());

            tagSearchResults.innerHTML = '';

            tags
                .filter(tag => !selectedTags.includes(tag.name)) // исключаем уже выбранные
                .forEach(tag => {
                    const tagDiv = document.createElement('div');
                    tagDiv.textContent = tag.name;
                    tagDiv.classList.add('search-result-item');
                    tagDiv.addEventListener('click', async () => {
                        await addTag(tag.name, editTagContainer);
                        tagInput.value = '';
                        tagSearchResults.style.display = 'none';
                        updateTagsDisplay();
                    });
                    tagSearchResults.appendChild(tagDiv);
                });

            tagSearchResults.style.display = 'block';
        } else {
            tagSearchResults.style.display = 'none';
        }
    });

    tagInput.addEventListener('keypress', async function (event) {
        if (event.key === 'Enter' && tagInput.value.trim() !== '') {
            await addTag(tagInput.value.trim(), editTagContainer);
            tagInput.value = '';
            updateTagsDisplay(); // Обновляем метки после добавления
        }
    });
}

function createEditableTagElement(tag) {
    const tagItem = document.createElement('span');
    tagItem.classList.add('tag-item');
    tagItem.textContent = tag.name;

    const removeButton = document.createElement('span');
    removeButton.textContent = '×';
    removeButton.classList.add('tag-remove-btn');

    removeButton.addEventListener('click', async (e) => {
        e.stopPropagation();
        await removeTag(tag);
        tagItem.remove();
        updateTagsDisplay(); // Обновляем список меток после удаления
    });

    tagItem.appendChild(removeButton);
    return tagItem;
}

async function addTag(tagName, tagContainer) {
    const taskId = document.getElementById('modal-container').getAttribute('data-task-id');

    // Проверка: уже есть такая метка в DOM?
    const existingTags = Array.from(tagContainer.querySelectorAll('.tag-item'));
    const tagAlreadyExists = existingTags.some(el => el.textContent.replace('×', '').trim() === tagName.trim());
    if (tagAlreadyExists) return; // Не добавляем дубликат

    const response = await fetch(`/api/tasks/${taskId}/tags`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: tagName })
    });
    if (response.ok) {
        const newTag = await response.json();
        const tagSpan = createTagElement(newTag);
        tagContainer.appendChild(tagSpan);
        updateTagsDisplay();
    } else {
        console.error('Не удалось добавить метку');
    }
}

function createTagElement(tag) {
    const tagItem = document.createElement('span');
    tagItem.classList.add('tag-item');
    tagItem.textContent = tag.name;

    const removeButton = document.createElement('span');
    removeButton.textContent = '×';
    removeButton.classList.add('tag-remove-btn');

    // Скрыт по умолчанию, только если родитель редактируется
    removeButton.style.display = 'none';
    tagItem.appendChild(removeButton);

    // Показывать крестик только если редактируем
    tagItem.addEventListener('mouseenter', () => {
        if (tagItem.closest('.task-tags-container')?.classList.contains('editing')) {
            removeButton.style.display = 'inline';
        }
    });
    tagItem.addEventListener('mouseleave', () => {
        removeButton.style.display = 'none';
    });

    removeButton.addEventListener('click', async (e) => {
        e.stopPropagation();
        await removeTag(tag);
        tagItem.remove();
        updateTagsDisplay(); // Обновляем список меток после удаления
    });

    return tagItem;
}

async function removeTag(tag) {
    const taskId = document.getElementById('modal-container').getAttribute('data-task-id');
    await fetch(`/api/tasks/${taskId}/tags/${tag.id}`, {
        method: 'DELETE'
    });
}

async function updateTagsDisplay() {
    const taskId = document.getElementById('modal-container')?.getAttribute('data-task-id');
    const tagsContainer = document.querySelector('.task-tags-container');
    if (!tagsContainer) return; // контейнер уже удалён

    const tagContainer = tagsContainer.querySelector('.tags-tags');
    if (!tagContainer) return;

    try {
        const response = await fetch(`/api/tasks/${taskId}/tags`);
        if (!response.ok) return;

        const tags = await response.json();
        tagContainer.innerHTML = '';

        tags.forEach(tag => {
            const tagElement = createTagElement(tag);
            tagContainer.appendChild(tagElement);
        });
    } catch (err) {
        console.error('Ошибка при обновлении меток:', err);
    }
}

async function loadTagsForTask(taskId, containerEl) {
    const tagContainer = containerEl.querySelector('.tags-tags');
    if (!tagContainer) return;

    try {
        const response = await fetch(`/api/tasks/${taskId}/tags`);
        if (response.ok) {
            const tags = await response.json(); // используем json(), чтобы сразу получить объект
            tags.forEach(tag => {
                const tagElement = createTagElement(tag);
                tagContainer.appendChild(tagElement);
            });
        } else {
            console.error('Ошибка при получении меток');
        }
    } catch (error) {
        console.error('Ошибка при загрузке меток:', error);
    }
}

//Добавление участников в проект
function openTeamModal() {
    document.getElementById("teamModal").style.display = "block";
}

function closeTeamModal() {
    document.getElementById("teamModal").style.display = "none";
}

async function addTeam(event) {
    event.preventDefault(); // Останавливаем отправку формы

    const teamName = document.getElementById("teamName").value;

    // Проверяем, что команда выбрана
    if (!teamName) {
        alert("Пожалуйста, выберите команду.");
        return;
    }

    // Получаем ID проекта из URL
    const urlParams = new URLSearchParams(window.location.search);
    const projectId = urlParams.get('id');

    if (!projectId) {
        alert("ID проекта не найден в URL.");
        return;
    }

    // Находим команду по имени
    const response = await fetch(`/api/teams/search?query=${teamName}`);
    const teams = await response.json();

    if (teams.length === 0) {
        alert("Команда не найдена.");
        return;
    }

    const teamId = teams[0].id_team; // Получаем ID выбранной команды
    console.log("Project ID:", projectId);
    console.log("Team ID:", teamId);
    // Добавляем команду в проект
    const addTeamResponse = await fetch("/api/projects/addTeamToProject", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            projectId: projectId,
            teamId: teamId
        })
    });
    if (addTeamResponse.ok) {
        closeTeamModal();
    } else {
        alert("Произошла ошибка при добавлении команды.");
    }
}
async function searchTeams() {
    const input = document.getElementById("teamName");
    const query = input.value.trim();

    const suggestionsContainer = document.getElementById("teamSuggestions");
    suggestionsContainer.innerHTML = ""; // Очищаем старые предложения

    if (!query) {
        suggestionsContainer.style.display = "none";
        return;
    }

    const projectId = getProjectIdFromUrl();  // Предположим, что функция для получения ID проекта есть

    try {
        // Получаем команды, уже добавленные в проект
        const projectTeamsResponse = await fetch(`/api/projects/${projectId}/teams`);
        if (!projectTeamsResponse.ok) {
            throw new Error("Ошибка при получении команд проекта");
        }

        const projectTeams = await projectTeamsResponse.json();
        const projectTeamIds = projectTeams.map(team => team.id_team);  // Получаем список ID команд проекта

        // Поиск команд по запросу
        const response = await fetch(`/api/teams/search?query=${encodeURIComponent(query)}`);
        if (!response.ok) {
            throw new Error("Ошибка при поиске команд");
        }

        const teams = await response.json();

        if (teams.length === 0) {
            suggestionsContainer.style.display = "none";
            return;
        }

        // Отфильтровываем команды, которые уже добавлены в проект
        const availableTeams = teams.filter(team => !projectTeamIds.includes(team.id_team));

        if (availableTeams.length === 0) {
            suggestionsContainer.style.display = "none";
            return;
        }

        // Отображаем оставшиеся команды
        availableTeams.forEach(team => {
            const div = document.createElement("div");
            div.className = "team-suggestion";
            div.textContent = team.team_name;
            div.addEventListener("click", () => {
                input.value = team.team_name;
                suggestionsContainer.style.display = "none";
            });
            suggestionsContainer.appendChild(div);
        });

        suggestionsContainer.style.display = "block";
    } catch (err) {
        console.error("Ошибка поиска команд:", err);
    }
}
function cancelTeamInput() {
    document.getElementById("teamName").value = "";
    document.getElementById("teamSuggestions").style.display = "none";
}










