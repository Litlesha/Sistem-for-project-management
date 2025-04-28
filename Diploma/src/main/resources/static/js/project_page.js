async function initTaskInputHandlers(scope = document) {
    scope.querySelectorAll('.task-title-bs').forEach(input => {
        if (input.dataset.handlerAttached === "true") return;

        input.dataset.handlerAttached = "true";

        input.addEventListener('keypress', async (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                const titleInput = e.target;
                const title = titleInput.value.trim();
                if (!title) return;

                const form = titleInput.closest('.task-form-container');
                const taskTypeInput = form.querySelector('input[name="taskType"]');
                const taskType = taskTypeInput ? taskTypeInput.value : 'task';

                const container = form.closest('.backlog-sprint-tusk-wrapper');
                const sprintId = container?.dataset?.sprintId ? parseInt(container.dataset.sprintId) : null;

                const urlParams = new URLSearchParams(window.location.search);
                const projectId = urlParams.get('id');

                const requestBody = {
                    title: title,
                    task_type: taskType,
                    sprintId: sprintId,
                    projectId: projectId ? parseInt(projectId) : null
                };

                try {
                    const response = await fetch('/create_task', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(requestBody)
                    });

                    if (!response.ok) throw new Error('Ошибка при создании задачи');
                    const task = await response.json();

                    // Передаем контейнер спринта для отображения задачи
                    if (sprintId) {
                        const sprintContainer = document.querySelector(`.backlog-sprint-tusk-wrapper[data-sprint-id="${sprintId}"]`);
                        renderTaskToSprint(task, sprintContainer); // рендерим задачу в спринт
                    } else {
                        renderTaskToBacklog(task); // если задачи без спринта
                    }

                    titleInput.value = ''; // очищаем input
                } catch (err) {
                    console.error('Ошибка:', err);
                }
            }
        });
    });
}
function renderTaskToSprint(task, sprintContainer) {
    const iconMap = {
        task: 'icons/tusk.svg',
        story: 'icons/history.svg',
        bug: 'icons/bug.svg'
    };
    const icon = iconMap[task.taskType] || 'icons/tusk.svg';

    const taskHTML = `
    <div class="task-wrap-container" draggable="true" data-task-id="${task.id}" data-sprint-id="${task.sprintId}">
        <div class="tusk-wrap">
            <div class="tusk-wrap-right">
                <img src="${icon}">
                <div class="key-and-name">
                    <span class="key">${task.taskKey}</span>
                    <span class="tusk-name">${task.title}</span>
                </div>
            </div>
            <div class="tusk-wrap-left">
                <select class="sprint-to-column">
                    <option>К выполнению</option>
                </select>
                <button><img class="performer" src="icons/performer.svg"></button>
                <button><img class="extra-menu" src="icons/extra-menu.svg"></button>
            </div>
        </div>
    </div>
`;

    const createBtnWrapper = sprintContainer.querySelector('.create-task-btn-wrapper');
    const emptyBacklog = sprintContainer.querySelector('.empty-backlog');
    if (emptyBacklog) emptyBacklog.remove();

    createBtnWrapper.insertAdjacentHTML('beforebegin', taskHTML);
    const taskElement = sprintContainer.querySelector(`div[data-task-id="${task.id}"]`);
    console.log(taskElement);
    taskElement.addEventListener("dragstart", handleDragStart);
    taskElement.addEventListener("dragover", handleDragOver);
    taskElement.addEventListener("drop", handleDrop);
}

function renderTaskToBacklog(task) {
    const iconMap = {
        task: 'icons/tusk.svg',
        story: 'icons/history.svg',
        bug: 'icons/bug.svg'
    };
    const icon = iconMap[task.taskType] || 'icons/tusk.svg';

    const taskHTML = `
        <div class="task-wrap-container" draggable="true" data-task-id="${task.id}" data-sprint-id="${task.sprintId}">
            <div class="tusk-wrap">
                <div class="tusk-wrap-right">
                    <img src="${icon}">
                    <div class="key-and-name">
                        <span class="key">${task.taskKey}</span>
                        <span class="tusk-name">${task.title}</span>
                    </div>
                </div>
                <div class="tusk-wrap-left">
                    <select class="sprint-to-column">
                        <option>К выполнению</option>
                    </select>
                    <button><img class="performer" src="icons/performer.svg"></button>
                    <button><img class="extra-menu" src="icons/extra-menu.svg"></button>
                </div>
            </div>
        </div>
    `;

    const backlogContainer = document.querySelector('.backlog-sprint-tusk-wrapper.backlog-section');
    if (!backlogContainer) return;

    const createBtnWrapper = backlogContainer.querySelector('.create-task-btn-wrapper');
    if (!createBtnWrapper) return;

    const emptyBacklog = backlogContainer.querySelector('.empty-backlog');
    if (emptyBacklog) emptyBacklog.remove();

    createBtnWrapper.insertAdjacentHTML('beforebegin', taskHTML);
    initTaskInputHandlers();
    const taskElement = backlogContainer.querySelector(`div[data-task-id="${task.id}"]`);
    console.log(taskElement);
    taskElement.addEventListener("dragstart", handleDragStart);
    taskElement.addEventListener("dragover", handleDragOver);
    taskElement.addEventListener("drop", handleDrop);
}
function handleDragStart(e) {
    // Храним ID задачи для использования при drop
    e.dataTransfer.setData("taskId", e.target.dataset.taskId);
}

function handleDragOver(e) {
    e.preventDefault(); // Нужно для разрешения drop
}

async function handleDrop(e) {
    e.preventDefault();

    const taskId = e.dataTransfer.getData("taskId");
    const taskElement = document.querySelector(`[data-task-id="${taskId}"]`);
    let dropTarget = e.target.closest('.task-wrap-container');
    let newContainer = e.target.closest('.backlog-sprint-tusk-wrapper');

    // Если дропнули на пустую надпись
    if (e.target.classList.contains('empty-backlog') || e.target.closest('.empty-backlog')) {
        newContainer = e.target.closest('.backlog-sprint-tusk-wrapper');
    }

    if (!newContainer) return;

    const sprintId = newContainer.dataset.sprintId || null;

    try {
        const response = await fetch('/update_task_location', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                taskId: taskId,
                sprintId: sprintId
            })
        });

        if (!response.ok) throw new Error('Ошибка при обновлении задачи');

        const createBtnWrapper = newContainer.querySelector('.create-task-btn-wrapper');

        if (dropTarget && dropTarget !== taskElement) {
            newContainer.insertBefore(taskElement, dropTarget);
        } else if (createBtnWrapper) {
            newContainer.insertBefore(taskElement, createBtnWrapper);
        } else {
            newContainer.appendChild(taskElement);
        }

        const emptyBacklog = newContainer.querySelector('.empty-backlog');
        if (emptyBacklog) emptyBacklog.remove();
    } catch (err) {
        console.error('Ошибка при перемещении задачи:', err);
    }
}

function initDragOverHandlers() {
    const containers = document.querySelectorAll('.backlog-sprint-tusk-wrapper');

    containers.forEach(container => {
        container.addEventListener('dragover', (e) => {
            e.preventDefault();
        });

        container.addEventListener('dragenter', (e) => {
            e.preventDefault();
        });

        container.addEventListener('drop', handleDrop);
    });
}


initDragOverHandlers();



// =======================================>//
function initCreateTaskButtons(scope = document) {
    scope.querySelectorAll(".create-task-btn-wrapper").forEach(wrapper => {
        const createBtn = wrapper.querySelector(".create-task-btn");
        const taskForm = wrapper.querySelector(".task-form-container");

        createBtn.addEventListener("click", () => {
            createBtn.style.display = "none";
            taskForm.style.display = "flex";
        });

        const customSelect = wrapper.querySelector(".custom-select");
        if (!customSelect) return;

        const selectedOption = customSelect.querySelector(".selected-option");
        const options = customSelect.querySelector(".optionssprint") || customSelect.querySelector(".optionsbacklog");
        const hiddenInput = customSelect.querySelector("input[type=hidden]");
        const arrow = selectedOption.querySelector(".arrow");
        const selectedText = selectedOption.querySelector(".selected-text");
        const allOptions = options.querySelectorAll(".option");

        const defaultValue = hiddenInput.value;
        allOptions.forEach(option => {
            option.style.display = option.getAttribute("data-value") === defaultValue ? "none" : "flex";
        });

        selectedOption.addEventListener("click", () => {
            const isOpen = options.style.display === "block";
            options.style.display = isOpen ? "none" : "block";
            arrow.classList.toggle("open", !isOpen);
        });

        allOptions.forEach(option => {
            option.addEventListener("click", () => {
                selectedText.textContent = option.querySelector(".option-text").textContent;
                selectedOption.querySelector(".content").innerHTML = option.innerHTML;
                hiddenInput.value = option.getAttribute("data-value");

                allOptions.forEach(opt => opt.style.display = "flex");
                option.style.display = "none";

                options.style.display = "none";
                arrow.classList.remove("open");
            });
        });

        document.addEventListener("click", (e) => {
            if (!customSelect.contains(e.target)) {
                options.style.display = "none";
                arrow.classList.remove("open");
            }
        });
    });

    document.addEventListener("click", (e) => {
        scope.querySelectorAll(".create-task-btn-wrapper").forEach(wrapper => {
            const form = wrapper.querySelector(".task-form-container");
            const btn = wrapper.querySelector(".create-task-btn");

            if (form && form.style.display !== "none" && !wrapper.contains(e.target)) {
                form.style.display = "none";
                btn.style.display = "flex";
            }
        });
    });
}

document.addEventListener("DOMContentLoaded", () => {
    initCreateTaskButtons();
})

async function loadProjectData() {
    const projectId = getProjectIdFromUrl();
    if (!projectId) {
        document.getElementById("project-title").textContent = "Проект не найден";
        return;
    }

    try {
        const response = await fetch(`/api/project/${projectId}`);
        if (!response.ok) throw new Error("Проект не найден");

        const project = await response.json();
        document.getElementById("project-title").textContent = project.name;
    } catch (err) {
        console.error(err);
        document.getElementById("project-title").textContent = "Ошибка загрузки проекта";
    }
}

window.addEventListener("DOMContentLoaded", loadProjectData);

const navLinks = document.querySelectorAll('.nav-link');
const sections = document.querySelectorAll('.section');

navLinks.forEach(link => {
    link.addEventListener('click', (e) => {
        e.preventDefault();

        navLinks.forEach(l => l.classList.remove('active'));
        link.classList.add('active');

        const sectionId = link.dataset.section;
        sections.forEach(sec => sec.classList.remove('active-section'));
        document.getElementById(sectionId).classList.add('active-section');
    });
});

function openModal() {
    document.getElementById("sprintModal").style.display = "block";
    document.getElementById("sprintDuration").value = "1";
    updateDatesFromDuration();
}

function closeModal() {
    document.getElementById("sprintModal").style.display = "none";
}

function submitSprint() {
    const urlParams = new URLSearchParams(window.location.search);
    const projectId = urlParams.get('id');

    const durationValue = document.getElementById('sprintDuration').value;
    const duration = durationValue === "custom" ? null : parseInt(durationValue);

    const sprint = {
        sprintName: document.getElementById('sprintName').value,
        duration: duration,
        startDate: document.getElementById('startDate').value,
        endDate: document.getElementById('endDate').value,
        goal: document.getElementById('sprintGoal').value,
        projectId: projectId ? parseInt(projectId) : null
    };

    if (!sprint.projectId) {
        alert("ID проекта не найден. Невозможно создать спринт.");
        return;
    }

    fetch("/create_sprint", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(sprint)
    })
        .then(response => {
            if (!response.ok) throw new Error("Ошибка создания спринта");
            return response.json();
        })
        .then(data => {
            console.log("Спринт создан:", data);

            const container = document.querySelector('.scrollable-backlog-container');
            const backlogElement = container.querySelector('.backlog-sprint-tusk-wrapper:last-child');
            const sprintElement = renderSprint(data);

            if (backlogElement) {
                container.insertBefore(sprintElement, backlogElement);
            } else {
                container.appendChild(sprintElement);
            }

            initCreateTaskButtons(sprintElement); // ✅ инициализация кнопок
            initTaskInputHandlers(sprintElement);  // передаем только блок спринта
            closeModal();
        })
}

document.addEventListener("DOMContentLoaded", function () {
    const projectId = getProjectIdFromUrl(); // используем правильный способ

    if (!projectId) {
        console.error("Project ID не найден в URL");
        return;
    }

    // если есть скрытое поле
    const hiddenInput = document.getElementById("projectId");
    if (hiddenInput) {
        hiddenInput.value = projectId;
    }

    window.projectIdFromURL = projectId;
    loadSprints(projectId); // теперь projectId корректный
});

function updateDatesFromDuration() {
    const duration = document.getElementById("sprintDuration").value;
    const startInput = document.getElementById("startDate");
    const endInput = document.getElementById("endDate");

    const now = new Date();

    const formatDateTimeLocal = (date) => {
        const pad = (n) => n.toString().padStart(2, '0');
        return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
    };

    if (duration === "custom") {
        startInput.readOnly = false;
        endInput.readOnly = false;
        return;
    }

    const durationWeeks = parseInt(duration);
    if (!durationWeeks || isNaN(durationWeeks)) return;

    const startDate = new Date();
    const endDate = new Date();
    endDate.setDate(endDate.getDate() + (durationWeeks * 7));

    startInput.value = formatDateTimeLocal(startDate);
    endInput.value = formatDateTimeLocal(endDate);

    startInput.readOnly = false;
    endInput.readOnly = true;
}
function renderSprint(sprint) {
    const container = document.createElement("div");
    container.className = "backlog-sprint-tusk-wrapper";
    container.setAttribute("data-sprint-id", sprint.id);

    container.innerHTML = `
    <div class="sprint-wrap-header">
      <div class="sprint-wrap-header-right">
        <button><img src="/icons/mingcute_down-line.svg"></button>
        <div class="sprint-spans">
          <span>Доска ${sprint.sprintName}</span>
          <span style="font-weight: lighter;font-size: 12px;">
            ${formatDate(sprint.startDate)} – ${formatDate(sprint.endDate)}
          </span>
        </div>
      </div>
      <div class="sprint-btns">
        <button class="edit-sprint-btn">
          <img src="/icons/pepicons-pencil_dots-x.svg">
        </button>
        <button class="start-project-btn" style="margin-right: 0px">Начать спринт</button>
      </div>
    </div>

    <div class="empty-backlog">
      <p>Чтобы добавить задачи в спринт нажмите на кнопку ниже или перетащите задачи из бэклога</p>
    </div>

    <div class="create-task-btn-wrapper">
      <button class="create-task-btn">
        <img src="/icons/plus.svg" alt="Добавить" class="plus-icon">
        Создать задачу
      </button>

      <div class="task-form-container task-form" style="display: none;">
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
            <div class="optionssprint">
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
        <input type="text" class="task-title-bs" placeholder="Название задачи" />
      </div>
    </div>
  `;
    return container;
}

async function loadSprints(projectId) {
    try {
        const response = await fetch(`/api/project/${projectId}/sprints`);
        if (!response.ok) throw new Error("Ошибка загрузки спринтов");

        const sprints = await response.json();
        const container = document.querySelector('.scrollable-backlog-container');

        // Находим элемент бэклога (последний .backlog-sprint-tusk-wrapper в контейнере)
        const backlogElement = container.querySelector('.backlog-sprint-tusk-wrapper:last-child');

        sprints.forEach(sprint => {
            const sprintElement = renderSprint(sprint);

            // Вставляем перед бэклогом
            if (backlogElement) {
                container.insertBefore(sprintElement, backlogElement);
            } else {
                container.appendChild(sprintElement); // fallback
            }
        });

        initCreateTaskButtons(container);
        initTaskInputHandlers()

    } catch (err) {
        console.error("Ошибка при загрузке спринтов:", err);
    }
}
function getProjectIdFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('id');
}

document.addEventListener('DOMContentLoaded', async () => {
    try {
        const projectId = getProjectIdFromUrl();
        const response = await fetch(`/api/project/${projectId}/backlog_tasks`);
        if (!response.ok) throw new Error('Ошибка при получении задач');

        const tasks = await response.json();
        tasks.forEach(renderTaskToBacklog); // отрисовываем только в бэклоге
    } catch (err) {
        console.error('Ошибка при загрузке задач из бэклога:', err);
    }
    try {
        const sprintElements = document.querySelectorAll('.backlog-sprint-tusk-wrapper[data-sprint-id]');
        for (const sprintElement of sprintElements) {
            const sprintId = sprintElement.getAttribute('data-sprint-id');
            const response = await fetch(`/sprint_tasks/${sprintId}`);
            if (!response.ok) throw new Error(`Ошибка при получении задач для спринта ${sprintId}`);
            const tasks = await response.json();
            tasks.forEach(task => renderTaskToSprint(task, sprintElement));
        }
    } catch (err) {
        console.error('Ошибка загрузки задач для спринтов:', err);
    }
});

const formatDate = (dateStr) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('ru-RU', {
        day: '2-digit',
        month: 'short',
    });
};
const backlog = document.querySelector('.scrollable-backlog-container');
initCreateTaskButtons(backlog);
