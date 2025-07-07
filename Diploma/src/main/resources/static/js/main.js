document.addEventListener('DOMContentLoaded', () => {
    fetch('/api/project/recent')
        .then(res => {
            if (!res.ok) throw new Error('Ошибка при загрузке проектов');
            return res.json();
        })
        .then(projects => {
            console.log(projects)
            const container = document.getElementById('recent-projects-list');
            container.innerHTML = ''; // очистить перед добавлением
            if (projects.length === 0) {
                container.textContent = 'Нет недавних проектов';
                return;
            }
            projects.forEach(project => {
                const card = document.createElement('div');
                card.className = 'project-card';
                card.innerHTML = `
                    <a href="/project_page?id=${project.id}&section=backlog" class="project-link">
                     <span class="link-header">${project.name}</span>
                    </a>
                    <p class="tasks-margin">Открытых задач: ${project.openTasksCount}</p>
                    <p class="tasks-margin">Активных спринтов: ${project.activeSprintsCount}</p>
                `;
                container.appendChild(card);
            });
        })
        .catch(err => {
            console.error(err);
            document.getElementById('recent-projects-list').textContent = 'Ошибка загрузки проектов';
        });
});