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
        const kanbanTodoColumn = document.querySelector('.kanban .kanban-column:first-child .tusk-container');

        tasks.forEach(task => {
            const icon = iconMap[task.taskType] || 'icons/tusk.svg';

            const taskContentHTML = `
                <div class="task-content">
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
            kanbanTodoColumn.appendChild(taskContainer.firstElementChild);
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