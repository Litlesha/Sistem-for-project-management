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

            if (!dropTarget) {
                container.appendChild(taskElement);
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
            kanbanColumn.appendChild(taskContainer.firstElementChild);
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
document.addEventListener('DOMContentLoaded', () => {
    const projectId = getProjectIdFromUrl();
    if (projectId) {
        loadActiveSprint(projectId);
    }
});