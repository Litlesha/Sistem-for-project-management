document.addEventListener('DOMContentLoaded', function () {
    const sortSelect = document.getElementById('sort-options');

    // Функция для загрузки проектов
    function loadProjects(sortOrder = 'default') {
        fetch('/api/projects')  // Запрос на получение всех проектов
            .then(response => {
                if (!response.ok) {
                    throw new Error('Ошибка при загрузке проектов');
                }
                return response.json();
            })
            .then(projects => {
                const tbody = document.getElementById('projects-tbody');
                tbody.innerHTML = ''; // очищаем, если были заглушки

                if (projects.length === 0) {
                    const row = document.createElement('tr');
                    const cell = document.createElement('td');
                    cell.colSpan = 6;
                    cell.textContent = 'Проекты не найдены';
                    cell.style.textAlign = 'center';
                    row.appendChild(cell);
                    tbody.appendChild(row);
                    return;
                }

                // Сортировка проектов
                if (sortOrder === 'asc') {
                    projects.sort((a, b) => a.name.localeCompare(b.name));
                } else if (sortOrder === 'desc') {
                    projects.sort((a, b) => b.name.localeCompare(a.name));
                }

                // Добавление проектов в таблицу
                projects.forEach(project => {
                    const row = document.createElement('tr');
                    row.style.cursor = 'pointer';
                    row.onclick = () => window.location.href = `/project_page?id=${project.id}`;

                    // Ячейка с названием проекта
                    const nameCell = document.createElement('td');
                    nameCell.textContent = project.name;

                    // Ячейка с ключом проекта
                    const keyCell = document.createElement('td');
                    keyCell.textContent = project.key || '—';

                    // Ячейка с руководителем
                    const leaderCell = document.createElement('td');
                    leaderCell.textContent = project.owner || '—';

                    // Ячейка с описанием
                    const descCell = document.createElement('td');
                    descCell.textContent = project.description || 'Без описания';

                    // Ячейка со статусом
                    const statusCell = document.createElement('td');
                    statusCell.textContent = project.status || '—';

                    // Ячейка с действиями
                    const actionCell = document.createElement('td');
                    const viewBtn = document.createElement('button');
                    viewBtn.classList.add('action-btn');
                    viewBtn.innerHTML = '<img src="/icons/extramenu.svg">';
                    viewBtn.onclick = (e) => {
                        e.stopPropagation();
                        window.location.href = `/project_page?id=${project.id}`;
                    };
                    actionCell.appendChild(viewBtn);

                    row.appendChild(nameCell);
                    row.appendChild(keyCell);
                    row.appendChild(leaderCell);
                    row.appendChild(descCell);
                    row.appendChild(statusCell);
                    row.appendChild(actionCell);

                    tbody.appendChild(row);
                });
            })
            .catch(error => {
                console.error('Ошибка при загрузке проектов:', error);
            });
    }

    // Загрузка проектов при изменении выбора сортировки
    sortSelect.addEventListener('change', function () {
        const sortOrder = this.value;
        loadProjects(sortOrder);
    });

    // Инициализируем загрузку проектов при начальной загрузке страницы
    loadProjects();
});