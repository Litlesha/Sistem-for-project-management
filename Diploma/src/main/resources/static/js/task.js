// document.addEventListener('DOMContentLoaded', () => {
//     const urlParams = new URLSearchParams(window.location.search);
//     const taskKey = urlParams.get('key'); // достаем параметр key
//
//     if (!taskKey) {
//         alert('Не указан ключ задачи в URL');
//         return;
//     }
//
//     fetch(`/api/tasks/by-key/${encodeURIComponent(taskKey)}`)
//         .then(response => {
//             if (!response.ok) {
//                 throw new Error('Не удалось загрузить задачу');
//             }
//             return response.json();
//         })
//         .then(task => {
//             openTaskModal(task); // открыть модалку с задачей
//         })
//         .catch(error => {
//             console.error(error);
//             alert('Ошибка при загрузке задачи');
//         });
// });

function openTaskModal(task) {
    const template = document.getElementById('task-modal-template');
    const modal = template.content.cloneNode(true);
    const containerEl = document.getElementById('modal-container');

    // Сохраняем URL без task-параметра
    const lastUrl = window.location.href;
    containerEl.setAttribute('data-return-url', lastUrl);

    // Меняем адрес без перезагрузки
    const taskUrl = `/project_page?id=${task.project.id}&section=report&task=${task.id}`;
    history.pushState({ modalOpen: true }, '', taskUrl);

    containerEl.innerHTML = '';
    containerEl.appendChild(modal);
    containerEl.setAttribute('data-task-id', task.id);

    // Остальная логика как есть
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
    initializeTeamSelection(containerEl, task);
    initializeExecutorSelection(containerEl, task);
    initializeSprintSelection(containerEl, task);
    loadComments(task.id);
    loadTaskHistory(task.id);
    const submitBtn = containerEl.querySelector('#submit-comment-btn');
    if (submitBtn) {
        submitBtn.addEventListener('click', () => {
            const taskId = containerEl.getAttribute('data-task-id');
            submitComment(taskId);
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
    initializeFileUpload(containerEl, task.id);
}


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
    const closeBtn = containerEl.querySelector('.close-modal');
    if (!closeBtn) return;

    closeBtn.addEventListener('click', (event) => {
        event.preventDefault();
        console.log('Close modal clicked, preventing default.');

        if (typeof tinymce !== 'undefined') {
            tinymce.remove();
        }

        containerEl.innerHTML = '';
        containerEl.removeAttribute('data-task-id');

        // Откат истории — убираем `&task=...` из адреса
        if (history.state && history.state.modalOpen) {
            history.back();
        } else {
            // fallback: на случай, если нет состояния
            const returnUrl = containerEl.getAttribute('data-return-url');
            if (returnUrl) {
                window.location.href = returnUrl;
            }
        }
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
            loadTaskHistory(task.id);
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
            loadTaskHistory(task.id);
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
function initDifficultyEditor(containerEl, task) {
    const difficultyEl = containerEl.querySelector('.difficulty');
    const difficultyEditor = containerEl.querySelector('.difficulty-editor');
    const difficultyInput = containerEl.querySelector('.difficulty-input');
    const saveButton = containerEl.querySelector('.save-difficulty');
    const cancelButton = containerEl.querySelector('.cancel-difficulty');
    const tagsContainer = containerEl.querySelector('.tags-container');
    const actionsContainer = containerEl.querySelector('.difficulty-actions');
    const historySection = document.querySelector('.history-section');  // Контейнер для истории

    // Сброс состояния
    difficultyEditor.style.display = 'none';
    actionsContainer.style.display = 'none';
    difficultyEl.style.display = 'inline';

    // Клик по .tags-container (именно сложности)
    tagsContainer.addEventListener('click', function () {
        if (difficultyEditor.style.display === 'none') {
            difficultyInput.value = difficultyEl.textContent.trim(); // Устанавливаем текущее значение
            difficultyEl.style.display = 'none';
            difficultyEditor.style.display = 'flex';
            actionsContainer.style.display = 'flex';
            difficultyInput.focus();
        }
    });

    // Сохранить новый приоритет
    saveButton.addEventListener('click', function (event) {
        event.stopPropagation();

        const newPriority = difficultyInput.value.trim();

        // Проверка на пустое значение
        if (!newPriority) {
            alert('Приоритет не может быть пустым');
            return;
        }

        const oldPriority = difficultyEl.textContent.trim() || 'Нет'; // Старое значение, если пустое, то "Нет"

        // Выполняем запрос на сервер для обновления приоритета
        fetch(`/api/tasks/${task.id}/priority`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ priority: newPriority })
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Не удалось обновить приоритет');
                }
                return response.json();
            })
            .then(data => {
                difficultyEl.textContent = data.priority;  // Обновляем отображение
                difficultyEl.style.display = 'inline';
                difficultyEditor.style.display = 'none';
                actionsContainer.style.display = 'none';

                // Добавляем запись в историю
                loadTaskHistory(data.id);
            })
            .catch(error => {
                console.error('Ошибка при обновлении приоритета:', error);
                alert('Не удалось обновить приоритет');
            });
    });

    // Отмена изменений
    cancelButton.addEventListener('click', function (event) {
        event.stopPropagation();
        difficultyEditor.style.display = 'none';
        actionsContainer.style.display = 'none';
        difficultyEl.style.display = 'inline';
    });

    // Запрещаем закрытие редактора при клике внутри
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
        appendComment(comment);
        loadTaskHistory(taskId);
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
        loadTaskHistory(taskId);
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
    loadTaskHistory(taskId);
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

    tagContainer.innerHTML = ''; // Очищаем контейнер перед загрузкой

    try {
        const response = await fetch(`/api/tasks/${taskId}/tags`);
        if (response.ok) {
            const tags = await response.json();
            if (tags.length === 0) {
                const noTagsEl = document.createElement('span');
                noTagsEl.textContent = 'Нет';
                noTagsEl.style.color = '#888'; // Серый цвет
                noTagsEl.style.fontSize = '12px'; // Меньший шрифт
                tagContainer.appendChild(noTagsEl);
                return;
            }
            tags.forEach(tag => {
                const tagElement = createTagElement(tag);
                tagContainer.appendChild(tagElement);
            });
        } else {
            console.error('Ошибка при получении меток');
            tagContainer.textContent = 'Ошибка загрузки';
        }
    } catch (error) {
        console.error('Ошибка при загрузке меток:', error);
        tagContainer.textContent = 'Ошибка загрузки';
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
function initializeTeamSelection(containerEl, task) {
    const teamContainer = containerEl.querySelector('.team-tags-container');
    const teamSearchResults = containerEl.querySelector('.team-search-results');
    const taskId = document.getElementById('modal-container')?.getAttribute('data-task-id');

    if (!taskId) {
        console.error('Ошибка: taskId не найден');
        return;
    }

    // ВСТАВЛЯЕМ НАЗВАНИЕ ТЕКУЩЕЙ КОМАНДЫ ЕСЛИ ЕСТЬ
    if (task.team && task.team.team_name) {  // добавлена проверка на существование task.team
        teamContainer.textContent = task.team.team_name;
    }

    const urlParams = new URLSearchParams(window.location.search);
    const projectId = urlParams.get('id');
    if (!projectId) {
        console.error('Ошибка: projectId не найден в URL');
        return;
    }

    teamContainer.addEventListener('click', async () => {
        if (teamSearchResults.style.display === 'flex') {
            teamSearchResults.style.display = 'none';
        } else {
            try {
                const response = await fetch(`/api/projects/${projectId}/teams`);
                const teams = await response.json();

                if (!Array.isArray(teams)) {
                    console.error('Ошибка: данные о командах не являются массивом');
                    return;
                }

                teamSearchResults.innerHTML = '';

                teams.forEach(team => {
                    const teamDiv = document.createElement('div');
                    teamDiv.textContent = team.team_name;
                    teamDiv.classList.add('search-result-item');

                    teamDiv.addEventListener('click', async () => {
                        // Отображаем сразу выбранную команду
                        teamContainer.textContent = team.team_name;
                        teamSearchResults.style.display = 'none';

                        // Отправляем PUT-запрос для привязки команды
                        const assignResponse = await fetch(`/api/tasks/${taskId}/team/${team.id_team}`, {
                            method: 'PUT'
                        });

                        if (!assignResponse.ok) {
                            console.error('Ошибка при прикреплении команды');
                        } else {
                            // Обновляем локальную переменную task
                            task.teamName = team.team_name;
                            loadTaskHistory(taskId);
                        }
                    });

                    teamSearchResults.appendChild(teamDiv);
                });

                teamSearchResults.style.display = 'block';
            } catch (error) {
                console.error('Ошибка загрузки команд:', error);
            }
        }
    });

    // Закрытие выпадающего списка при клике вне области
    document.addEventListener('click', (e) => {
        if (!teamContainer.contains(e.target)) {
            teamSearchResults.style.display = 'none';
        }
    });
}

//Прикрепить спринт
function initializeSprintSelection(containerEl, task) {
    const sprintContainer = containerEl.querySelector('.sprint-tags-container');
    const sprintSearchResults = containerEl.querySelector('.sprint-search-results');
    const taskId = document.getElementById('modal-container')?.getAttribute('data-task-id');

    if (!taskId) {
        console.error('Ошибка: taskId не найден');
        return;
    }

    // Показ текущего названия спринта
    if (task.sprint && task.sprint.sprintName) {
        sprintContainer.textContent = task.sprint.sprintName;
    }

    const urlParams = new URLSearchParams(window.location.search);
    const projectId = urlParams.get('id');
    if (!projectId) {
        console.error('Ошибка: projectId не найден в URL');
        return;
    }

    sprintContainer.addEventListener('click', async () => {
        if (sprintSearchResults.style.display === 'flex') {
            sprintSearchResults.style.display = 'none';
        } else {
            try {
                const response = await fetch(`/api/projects/${projectId}/sprints/active`);
                const sprints = await response.json();
                console.log(sprints)
                if (!Array.isArray(sprints)) {
                    console.error('Ошибка: данные о спринтах не являются массивом');
                    return;
                }

                sprintSearchResults.innerHTML = '';

                sprints.forEach(sprint => {
                    const sprintDiv = document.createElement('div');
                    sprintDiv.textContent = sprint.sprintName;
                    sprintDiv.classList.add('search-result-item');

                    sprintDiv.addEventListener('click', async () => {
                        sprintContainer.textContent = sprint.sprintName;
                        sprintSearchResults.style.display = 'none';

                        const assignResponse = await fetch(`/api/tasks/${taskId}/sprint/${sprint.id}`, {
                            method: 'PUT'
                        });

                        if (!assignResponse.ok) {
                            console.error('Ошибка при прикреплении спринта');
                        } else {
                            task.sprint = sprint;
                            loadTaskHistory(taskId);
                        }
                    });

                    sprintSearchResults.appendChild(sprintDiv);
                });

                sprintSearchResults.style.display = 'block';
            } catch (error) {
                console.error('Ошибка загрузки спринтов:', error);
            }
        }
    });

    document.addEventListener('click', (e) => {
        if (!sprintContainer.contains(e.target)) {
            sprintSearchResults.style.display = 'none';
        }
    });
}


// Прикрепить к задаче пользователя
function initializeExecutorSelection(containerEl, task) {
    const executorContainer = containerEl.querySelector('.executor-container');
    const executorSearchResults = containerEl.querySelector('.executor-search-results');
    const taskId = task.id;
    // const projectId = getProjectIdFromUrl()
    // Отображаем выбранного исполнителя при загрузке страницы
    const assignedUserDiv = containerEl.querySelector('.assigned-user');
    if (task.executorName) {
        assignedUserDiv.textContent = task.executorName; // Если исполнитель уже выбран, отображаем его
    } else {
        assignedUserDiv.textContent = 'Нет'; // Если исполнитель не выбран
    }
    console.log(task)
    executorContainer.addEventListener('click', async () => {
        if (executorSearchResults.style.display === 'block') {
            executorSearchResults.style.display = 'none';
        } else {
            try {
                const response = await fetch(`/api/teams/${task.project.id}/users`);
                const users = await response.json();

                if (!Array.isArray(users)) {
                    console.error('Ошибка: данные о пользователях не массив');
                    return;
                }

                executorSearchResults.innerHTML = '';

                users.forEach(user => {
                    const userDiv = document.createElement('div');
                    userDiv.textContent = user.email;
                    userDiv.classList.add('search-result-item');

                    userDiv.addEventListener('click', async () => {
                        // Отображаем выбранного пользователя
                        assignedUserDiv.textContent = `${user.email}`;
                        executorSearchResults.style.display = 'none';

                        // Назначаем исполнителя
                        const assignResponse = await fetch(`/api/tasks/${taskId}/executor/${user.id_user}`, {
                            method: 'PUT'
                        });

                        if (!assignResponse.ok) {
                            console.error('Ошибка при назначении исполнителя');
                        } else {
                            task.executorName = user.email; // Обновляем локальную переменную task
                            loadTaskHistory(taskId);
                        }
                    });

                    executorSearchResults.appendChild(userDiv);
                });

                executorSearchResults.style.display = 'block';
            } catch (error) {
                console.error('Ошибка загрузки исполнителей:', error);
            }
        }
    });

    document.addEventListener('click', (e) => {
        if (!executorContainer.contains(e.target)) {
            executorSearchResults.style.display = 'none';
        }
    });
}

// работа с файлами
function initializeFileUpload(container, taskId) {
    const addFileBtn = container.querySelector('.add-file');
    const fileListContainer = container.querySelector('.file-list');

    if (!addFileBtn || !fileListContainer) return;

    const fileInput = document.createElement('input');
    fileInput.type = 'file';
    fileInput.style.display = 'none';
    document.body.appendChild(fileInput);

    addFileBtn.addEventListener('click', () => {
        fileInput.click();
    });

    fileInput.addEventListener('change', () => {
        if (fileInput.files.length === 0) return;
        const file = fileInput.files[0];

        // Показываем прогресс-бар
        const progressBarContainer = document.querySelector('.file-upload-progress');
        progressBarContainer.style.display = 'block';

        const formData = new FormData();
        formData.append('file', file);

        // Создаем XMLHttpRequest для отслеживания прогресса
        const xhr = new XMLHttpRequest();
        xhr.open('POST', '/api/files/upload', true);

        // Обработчик прогресса
        xhr.upload.addEventListener('progress', function (e) {
            if (e.lengthComputable) {
                const percent = (e.loaded / e.total) * 100;
                document.getElementById('progressBar').style.width = `${percent}%`;
                document.getElementById('progressText').textContent = `${Math.round(percent)}%`;
            }
        });

        // Обработчик завершения загрузки
        xhr.onload = async function () {
            if (xhr.status === 200) {
                const fileData = JSON.parse(xhr.responseText);

                // Прикрепить файл к задаче
                const attachRes = await fetch(`/api/files/${taskId}/attach/${fileData.id}`, {
                    method: 'POST'
                });

                if (!attachRes.ok) {
                    alert('Ошибка при прикреплении');
                }

                renderFileItem(fileData, fileListContainer, taskId);
                loadTaskHistory(taskId);
            } else {
                alert('Ошибка при загрузке');
            }

            // Скрываем прогресс-бар после завершения загрузки
            progressBarContainer.style.display = 'none';
        };

        // Обработчик ошибок
        xhr.onerror = function () {
            alert('Ошибка при загрузке файла');
            progressBarContainer.style.display = 'none'; // скрыть прогресс-бар при ошибке
        };

        // Отправка данных
        xhr.send(formData);
    });

    // загрузить уже прикрепленные файлы
    fetch(`/api/files/task/${taskId}`)
        .then(res => res.json())
        .then(files => {
            fileListContainer.innerHTML = '';
            files.forEach(file => renderFileItem(file, fileListContainer, taskId));
        });
}

function renderFileItem(file, container, taskId) {
    const item = document.createElement('div');
    item.classList.add('file-item');

    // Проверка типа файла
    let fileContent = '';
    if (file.contentType.startsWith('image/')) {
        fileContent = `<img class="file-photo" src="/api/files/${file.id}/download" alt="${file.fileName}">`;
    } else {
        fileContent = ` 
            <div class="file-icon" style="width: 128.4px; height: 72.22px; display: flex; justify-content: center; align-items: center;">
                <img src="/icons/document.svg" style="max-width: 100%; max-height: 100%; object-fit: contain;">
            </div>
        `;
    }

    item.innerHTML = `
        ${fileContent}
        <span class="file-name">${file.fileName}</span>
        <div class="file-actions">
            <button class="download-btn" title="Скачать"><img src="/icons/download.svg"></button>
            <button class="delete-btn" title="Удалить"><img src="/icons/trash.svg"></button>
        </div>
    `;

    item.querySelector('.download-btn').addEventListener('click', () => {
        const link = document.createElement('a');
        link.href = `/api/files/${file.id}/download`;
        link.download = file.fileName; // Устанавливаем имя файла для скачивания
        link.click();
    });

    const deleteBtn = item.querySelector('.delete-btn');
    deleteBtn.addEventListener('click', () => {
        // Открытие модального окна
        openDeleteModal(file, taskId, item);
    });

    container.appendChild(item);
}

// Функция для открытия модального окна подтверждения удаления
function openDeleteModal(file, taskId, item) {
    const modal = document.getElementById('deleteModal');
    const closeBtn = modal.querySelector('.close'); // Используем 'close' для кнопки закрытия
    const confirmDeleteBtn = modal.querySelector('#confirmDelete');
    const cancelDeleteBtn = modal.querySelector('.cancel-btn'); // Используем 'cancel-btn' для кнопки отмены

    // Открытие модального окна
    modal.style.display = 'block';

    // Закрытие модального окна
    closeBtn.onclick = () => {
        modal.style.display = 'none';
    };

    cancelDeleteBtn.onclick = () => {
        modal.style.display = 'none';
    };

    // Подтверждение удаления
    confirmDeleteBtn.onclick = async () => {
        try {
            const res = await fetch(`/api/files/${taskId}/detach/${file.id}`, { method: 'DELETE' });
            if (res.ok) {
                item.remove();
                loadTaskHistory(taskId);
                modal.style.display = 'none'; // Закрыть модальное окно
            } else {
                alert('Не удалось удалить файл');
            }
        } catch (e) {
            console.error(e);
            alert('Ошибка при удалении файла');
        }
    };
}

//Работа с историей
// Форматирование даты
function formatDate(isoDate) {
    const date = new Date(isoDate);
    if (isNaN(date)) return 'Неверная дата';
    return date.toLocaleString('ru-RU', {
        day: 'numeric', month: 'long', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
    });
}
// Маппинг для полей
function mapField(field) {
    const map = {
        title: 'Название задачи',
        description: 'Описание',
        priority: 'Приоритет',
        task_type: 'Тип задачи',
        status: 'Статус',
        assignedUser: 'Назначенный пользователь',
        executor: 'Исполнитель',
        sprint: 'Спринт',
        project: 'Проект',
        tags: 'Метки',
        team: 'Команда',
        file: 'Прикреплённый файл',
        comments: 'Комментарий'
    };
    return map[field] || field;
}
function loadTaskHistory(taskId) {
    fetch(`/api/tasks/${taskId}/history`)
        .then(response => response.json())
        .then(historyEntries => {

            const historySection = document.querySelector('.history-section');
            historySection.innerHTML = ''; // Очищаем при загрузке

            historyEntries.forEach(entry => {
                const container = document.createElement('div');
                container.classList.add('grid-container-history');

                const beforeVal = entry.beforeValue || 'Нет';
                const afterVal = entry.afterValue || 'Нет';
                const authorName = entry.authorName || 'Неизвестный пользователь';
                const dateStr = formatDate(entry.createdAt); // используем createdAt
                const actionLabel = entry.actionTypeName || 'Неизвестное действие'; // используем actionTypeName для отображения действия

                container.innerHTML = `
                    <div class="section-photo-history">
                        <img src="icons/Group%205.svg">
                    </div>
                    <div class="section-action-history">
                        <div class="action-container">
                            <span>${authorName}</span>
                            <span>${actionLabel}</span> <!-- Динамически вставляем действие -->
                            <span class="bold">${mapField(entry.field)}</span>
                            <span>${dateStr}</span>
                        </div>
                    </div>
                    <div class="section-actin-show-history">
                        <div class="actions-show">
                            <span class="action-before">${beforeVal}</span>
                            <img src="/icons/arrow-right.svg">
                            <span class="action-after">${afterVal}</span>
                        </div>
                    </div>
                `;
                historySection.appendChild(container);
            });
        })
        .catch(error => {
            console.error('Ошибка при загрузке истории:', error);
        });
}