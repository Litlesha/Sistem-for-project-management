document.addEventListener("DOMContentLoaded", () => {
    const projectId = getProjectIdFromUrl();
    if (!projectId) return;

    loadFilters(projectId);
    setupEventListeners();

    document.querySelectorAll('.report-table').forEach(table => {
        table.addEventListener('click', event => {
            const cell = event.target.closest('.task-key-cell');
            if (cell) {
                const taskKey = cell.textContent.trim();

                // Загружаем задачу по ключу и открываем модальное окно
                fetch(`/api/tasks/by-key/${encodeURIComponent(taskKey)}`)
                    .then(res => {
                        if (!res.ok) {
                            throw new Error('Ошибка загрузки задачи');
                        }
                        return res.json();
                    })
                    .then(task => {
                        openTaskModal(task);
                    })
                    .catch(error => {
                        console.error('Не удалось открыть задачу:', error);
                    });
            }
        });
    });
});

function getProjectIdFromUrl() {
    const params = new URLSearchParams(window.location.search);
    return params.get("id");
}

/**
 * Загружает фильтры спринтов и пользователей с сервера и заполняет селект
 */
async function loadFilters(projectId) {
    try {
        const res = await fetch(`/api/report/filters/${projectId}`);
        if (!res.ok) throw new Error("Не удалось загрузить фильтры");
        const data = await res.json();

        fillFilterOptions(data.sprints, data.users);
    } catch (error) {
        console.error("Ошибка загрузки фильтров:", error);
    }
}

/**
 * Заполняет опции селекта для спринтов и пользователей
 */
function fillFilterOptions(sprints, users) {
    const sprintGroup = document.getElementById('sprint-options');
    const userGroup = document.getElementById('user-options');

    sprintGroup.innerHTML = '';
    userGroup.innerHTML = '';

    sprints.forEach(sprint => {
        const option = createOption(`sprint-${sprint.id}`, sprint.sprintName);
        sprintGroup.appendChild(option);
    });
    console.log(users)
    users.forEach(user => {
        const displayName = user.username?.trim() || user.email || "Без имени";
        const option = createOption(`user-${user.id}`, displayName);
        userGroup.appendChild(option);
    });
}

function createOption(value, text) {
    const option = document.createElement('option');
    option.value = value;
    option.textContent = text;
    return option;
}

/**
 * Устанавливает обработчики событий для фильтров
 */
function setupEventListeners() {
    const filterEntitySelect = document.getElementById('filter-entity');
    filterEntitySelect.addEventListener('change', onFilterEntityChange);
    document.getElementById('criteria').addEventListener('change', onCriteriaChange);
}
function onCriteriaChange() {
    const selected = document.getElementById('criteria').value;


    if (currentSprintData) {
        if (selected === 'tasks') {
            drawBurndownChart(currentSprintData);
        } else if (selected === 'complexity') {
            drawComplexityBurndownChart(currentSprintData);
        } else if (selected === 'taskTypes') {
            drawTaskTypeChart(currentSprintData);
        }
    } else if (currentUserData) {
        if (selected === 'tasks') {
            drawUserChart(currentUserData); // твой график по задачам
        } else if (selected === 'complexity') {
            drawUserPriorityChart(currentUserData); // если добавишь расчёт сложности
        } else if (selected === 'taskTypes') {
            drawUserTaskTypesChart(currentUserData); // например: распределение типов задач
        }
    }
}

let chart; // Глобальная переменная для Chart.js
let currentSprintData = null;
let currentUserData = null;
async function onFilterEntityChange(event) {
    const val = event.target.value;
    if (val.startsWith('sprint-')) {
        const sprintId = val.split('-')[1];
        await updateBurndown(sprintId);
    } else if (val.startsWith('user-')) {
        const userId = val.split('-')[1];  // <-- здесь исправлено с selected на val
        await updateUserReport(userId);
    } else {
        clearSprintDataAndChart();
    }
}

function clearSprintDataAndChart() {
    document.querySelector('.sprint-data').innerHTML = '';
    if (chart) {
        chart.destroy();
        chart = null;
    }
}

/**
 * Запрашивает данные спринта и обновляет burndown-диаграмму
 */
async function updateBurndown(sprintId) {
    currentUserData = null; // очистить данные пользователя
    currentSprintData = null;
    try {
        const response = await fetch(`/api/report/sprint/${sprintId}`);
        if (!response.ok) throw new Error("Не удалось загрузить данные спринта");

        const sprint = await response.json();
        currentSprintData = sprint;
        updateSprintDateDisplay(sprint.startDate, sprint.endDate);
        drawBurndownChart(sprint);

        onCriteriaChange();
        renderSprintReport(sprint)
    } catch (error) {
        console.error("Ошибка при обновлении burndown:", error);
    }
}
async function updateUserReport(userId) {
    currentUserData = null; // очистить данные пользователя
    currentSprintData = null;
    try {
        const projectId = getProjectIdFromUrl();
        if (!projectId) throw new Error("projectId не найден в URL");

        const response = await fetch(`/api/report/user/${userId}/project/${projectId}`);
        if (!response.ok) throw new Error("Не удалось загрузить данные пользователя");

        const user = await response.json();

        currentUserData = user;

        drawUserChart(user);
        onCriteriaChange();
        renderUserReport(user);
    } catch (error) {
        console.error("Ошибка при обновлении отчёта пользователя:", error);
    }
}
function toISODate(date) {
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

function drawUserChart(user) {
    if (chart) {
        chart.destroy();
        chart = null;
    }

    const tasks = user.tasks || [];

    // Фильтруем выполненные задачи с датой обновления
    const completedTasks = tasks.filter(task =>
        task.status && task.status.toLowerCase() === 'выполнено' && task.updatedAt);

    // Группируем по дате (приведенной к ISO формату)
    const countsByDate = {};
    completedTasks.forEach(task => {
        const dateKey = toISODate(task.updatedAt);
        countsByDate[dateKey] = (countsByDate[dateKey] || 0) + 1;
    });

    // Сортируем даты по возрастанию
    const sortedDates = Object.keys(countsByDate).sort();

    // Количество задач по дням (без накопления)
    const dailyCounts = sortedDates.map(date => countsByDate[date]);

    const ctx = document.getElementById('report-chart').getContext('2d');

    chart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: sortedDates,
            datasets: [{
                label: 'Выполнено задач за день',
                data: dailyCounts,
                borderColor: '#4caf50',
                backgroundColor: 'rgba(76, 175, 80, 0.2)',
                fill: true,
                tension: 0.3,
                pointRadius: 4,
                pointHoverRadius: 6,
            }]
        },
        options: {
            responsive: true,
            scales: {
                x: {
                    type: 'time',
                    time: {
                        unit: 'day',
                        tooltipFormat: 'dd.MM.yyyy',
                        displayFormats: {
                            day: 'dd.MM'
                        }
                    },
                    title: {
                        display: true,
                        text: 'Дата'
                    }
                },
                y: {
                    beginAtZero: true,
                    precision: 0,
                    title: {
                        display: true,
                        text: 'Количество выполненных задач'
                    }
                }
            },
            plugins: {
                legend: {
                    display: true,
                    position: 'bottom'
                },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                    callbacks: {
                        label: ctx => `Выполнено: ${ctx.parsed.y}`
                    }
                }
            }
        }
    });

}

function drawUserPriorityChart(user) {
    if (chart) {
        chart.destroy();
        chart = null;
    }

    const tasks = user.tasks || [];

    // Фильтруем выполненные задачи с датой обновления и числовым priority
    const completedTasks = tasks.filter(task =>
        task.status && task.status.toLowerCase() === 'выполнено' &&
        task.updatedAt && task.priority != null
    );

    // Группируем по дате, суммируя priority
    const priorityByDate = {};
    completedTasks.forEach(task => {
        const dateKey = toISODate(task.updatedAt);
        if (!dateKey) return;

        // Приводим priority к целому числу
        const priorityNum = parseInt(task.priority, 10);
        if (isNaN(priorityNum)) return;

        priorityByDate[dateKey] = (priorityByDate[dateKey] || 0) + priorityNum;
    });

    // Сортируем даты
    const sortedDates = Object.keys(priorityByDate).sort();

    // Массив значений суммарного priority по дням
    const dailyPriority = sortedDates.map(date => priorityByDate[date]);

    const ctx = document.getElementById('report-chart').getContext('2d');
    console.log('completedTasks:', completedTasks);
    console.log('priorityByDate:', priorityByDate);
    console.log('sortedDates:', sortedDates);
    console.log('dailyPriority:', dailyPriority);
    chart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: sortedDates,
            datasets: [{
                label: 'Суммарный приоритет выполненных задач за день',
                data: dailyPriority,
                borderColor: '#2196f3',
                backgroundColor: 'rgba(33, 150, 243, 0.2)',
                fill: true,
                tension: 0.3,
                pointRadius: 4,
                pointHoverRadius: 6,
            }]
        },
        options: {
            responsive: true,
            scales: {
                x: {
                    type: 'time',
                    time: {
                        unit: 'day',
                        tooltipFormat: 'dd.MM.yyyy',
                        displayFormats: {
                            day: 'dd.MM'
                        }
                    },
                    title: {
                        display: true,
                        text: 'Дата'
                    }
                },
                y: {
                    beginAtZero: true,
                    precision: 0,
                    title: {
                        display: true,
                        text: 'Суммарный приоритет'
                    }
                }
            },
            plugins: {
                legend: {
                    display: true,
                    position: 'bottom'
                },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                    callbacks: {
                        label: ctx => `Сложность выполненных задач: ${ctx.parsed.y}`
                    }
                }
            }
        }
    });
}

function drawUserTaskTypesChart(user) {
    if (chart) {
        chart.destroy();
        chart = null;
    }

    const tasks = user.tasks || [];

    // Фильтруем выполненные задачи
    const completedTasks = tasks.filter(task =>
        task.status && task.status.toLowerCase() === 'выполнено' && task.taskType);

    // Считаем количество по типам задач
    const countsByType = {};
    completedTasks.forEach(task => {
        const type = task.taskType.toLowerCase();
        countsByType[type] = (countsByType[type] || 0) + 1;
    });

    const types = Object.keys(countsByType);
    const counts = types.map(type => countsByType[type]);

    // Задаём цвета для типов
    const colorMap = {
        task: '#3290ec',    // синий
        bug: '#e53935',     // красный
        story: '#43a047',   // зелёный
    };

    const backgroundColors = types.map(type => colorMap[type] || '#9e9e9e'); // серый по умолчанию

    const ctx = document.getElementById('report-chart').getContext('2d');

    chart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: types,
            datasets: [{
                label: 'Выполнено задач по типам',
                data: counts,
                backgroundColor: backgroundColors,
            }]
        },
        options: {
            responsive: true,
            scales: {
                x: {
                    title: {
                        display: true,
                        text: 'Тип задачи'
                    }
                },
                y: {
                    beginAtZero: true,
                    precision: 0,
                    title: {
                        display: true,
                        text: 'Количество выполненных задач'
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: ctx => `Выполнено: ${ctx.parsed.y}`
                    }
                }
            }
        }
    });
}

function updateSprintDateDisplay(startDateStr, endDateStr) {
    const sprintDataDiv = document.querySelector('.sprint-data');
    const startDate = new Date(startDateStr);
    const endDate = new Date(endDateStr);

    sprintDataDiv.innerHTML = `
    <span><strong>Дата</strong></span>
    <span>${formatDate(startDate)}</span>
    <span> - </span>
    <span>${formatDate(endDate)}</span>
  `;
}

function formatDate(date) {
    return date.toLocaleDateString('ru-RU', {
        day: 'numeric',
        month: 'long',
        year: 'numeric',
    });
}

function getDatesArray(start, end) {
    const dates = [];
    let current = new Date(start);
    while (current <= end) {
        dates.push(new Date(current));
        current.setDate(current.getDate() + 1);
    }
    return dates;
}
function updateLegendTitles(datasetLabels, colors = []) {
    const linesContainer = document.querySelector('.lines');
    if (!linesContainer) return;

    linesContainer.innerHTML = '';

    datasetLabels.forEach((label, index) => {
        const lineDiv = document.createElement('div');
        lineDiv.className = 'line';
        lineDiv.style.display = 'flex';
        lineDiv.style.alignItems = 'center';
        lineDiv.style.marginBottom = '4px';

        const colorBox = document.createElement('span');
        colorBox.style.display = 'inline-block';
        colorBox.style.width = '20px';
        colorBox.style.height = '4px';
        colorBox.style.backgroundColor = colors[index] || 'black'; // fallback
        colorBox.style.marginRight = '8px';
        colorBox.style.borderRadius = '2px';

        const textNode = document.createTextNode(label);

        lineDiv.appendChild(colorBox);
        lineDiv.appendChild(textNode);

        linesContainer.appendChild(lineDiv);
    });
}
Chart.register(window['chartjs-plugin-annotation']);

function drawBurndownChart(sprint) {
    const ctx = document.getElementById('report-chart').getContext('2d');
    const startDate = new Date(sprint.startDate);
    const endDate = new Date(sprint.endDate);
    const dates = getDatesArray(startDate, endDate);
    const totalTasks = sprint.tasks.length;

    // Собираем даты завершения задач
    const completedDates = sprint.tasks
        .filter(task => (task.status || '').toLowerCase() === 'выполнено' && task.updatedAt)
        .map(task => new Date(task.updatedAt));

    const remainingTasks = dates.map(currentDate => {
        // Считаем, сколько задач уже выполнено к текущей дате включительно
        const doneCount = completedDates.filter(d => d <= currentDate).length;
        return totalTasks - doneCount; // оставшиеся задачи
    });

    // Оставляем идеальный график как есть
    const idealTasks = dates.map((_, i) =>
        totalTasks - (totalTasks * i) / (dates.length - 1)
    );

    const labels = dates.map(d =>
        d.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' })
    );

    const sprintEndDateStr = endDate.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' });
    const sprintEndIndex = labels.indexOf(sprintEndDateStr);

    if (chart) {
        chart.destroy();
        chart = null;
    }

    chart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Оставшаяся работа',
                    data: remainingTasks,
                    borderColor: 'red',
                    fill: false,
                    tension: 0.3,
                },
                {
                    label: 'Ориентир',
                    data: idealTasks,
                    borderColor: 'green',
                    borderDash: [5, 5],
                    fill: false,
                    tension: 0.3,
                },
            ],
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true,
                    stepSize: 1,
                    title: {
                        display: true,
                        text: 'Количество задач',
                    },
                },
                x: {
                    title: {
                        display: true,
                        text: 'Дата',
                    },
                },
            },
            plugins: {
                legend: {
                    display: false,
                },
                annotation: {
                    annotations: {
                        line1: {
                            type: 'line',
                            xMin: sprintEndIndex,
                            xMax: sprintEndIndex,
                            borderColor: 'blue',
                            borderWidth: 2,
                            label: {
                                content: 'Конец спринта',
                                enabled: true,
                                position: 'start',
                                backgroundColor: 'blue',
                                color: 'white',
                            }
                        }
                    }
                }
            }
        },
    });

    updateLegendTitles(chart.data.datasets.map(ds => ds.label), chart.data.datasets.map(ds => ds.borderColor));
}


function drawComplexityBurndownChart(sprint) {
    const ctx = document.getElementById('report-chart').getContext('2d');
    const startDate = new Date(sprint.startDate);
    const endDate = new Date(sprint.endDate);
    const dates = getDatesArray(startDate, endDate);

    const remainingComplexity = dates.map(date => {
        let total = 0;

        for (const task of sprint.tasks) {
            const status = (task.status || '').toLowerCase();
            const updatedAt = new Date(task.updatedAt);

            const isDone = status === 'выполнено';

            // Если задача завершена до этой даты — исключаем из подсчёта
            if (isDone && updatedAt <= date) continue;

            const priority = parseInt(task.priority || '0', 10);
            total += isNaN(priority) ? 0 : priority;
        }

        return total;
    });

    const totalComplexity = remainingComplexity[0];
    const idealComplexity = dates.map((_, i) =>
        totalComplexity - (totalComplexity * i) / (dates.length - 1)
    );

    const labels = dates.map(d =>
        d.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' })
    );

    const sprintEndDateStr = endDate.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' });
    const sprintEndIndex = labels.indexOf(sprintEndDateStr);
    if (chart) {
        chart.destroy();
        chart = null;
    }

    if (chart) {
        chart.data.labels = labels;
        chart.data.datasets[0].data = remainingComplexity;
        chart.data.datasets[1].data = idealComplexity;
        chart.data.datasets[0].label = 'Оставшаяся сложность';
        chart.data.datasets[1].label = 'Ориентир';

        // Пересоздаем объект заголовка оси Y, чтобы обновить текст
        chart.options.scales.y.title = {
            display: true,
            text: 'Суммарная сложность',
        };

        chart.options.plugins.annotation.annotations['line1'].xMin = sprintEndIndex;
        chart.options.plugins.annotation.annotations['line1'].xMax = sprintEndIndex;

        chart.update();
    } else {
        chart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Оставшаяся сложность',
                        data: remainingComplexity,
                        borderColor: 'red',
                        fill: false,
                        tension: 0.3,
                    },
                    {
                        label: 'Ориентир',
                        data: idealComplexity,
                        borderColor: 'green',
                        borderDash: [5, 5],
                        fill: false,
                        tension: 0.3,
                    },
                ],
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: 'Суммарная сложность',
                        },
                    },
                    x: {
                        title: {
                            display: true,
                            text: 'Дата',
                        },
                    },
                },
                plugins: {
                    legend: { display: false },
                    annotation: {
                        annotations: {
                            line1: {
                                type: 'line',
                                xMin: sprintEndIndex,
                                xMax: sprintEndIndex,
                                borderColor: 'blue',
                                borderWidth: 2,
                                label: {
                                    content: 'Конец спринта',
                                    enabled: true,
                                    position: 'start',
                                    backgroundColor: 'blue',
                                    color: 'white',
                                }
                            }
                        }
                    }
                }
            },
        });
    }

    updateLegendTitles(chart.data.datasets.map(ds => ds.label), chart.data.datasets.map(ds => ds.borderColor));
}

function drawTaskTypeChart(sprint) {
    const ctx = document.getElementById('report-chart').getContext('2d');

    // Считаем количество задач по типу
    const typeCounts = {};
    sprint.tasks.forEach(task => {
        const type = task.taskType || 'Не указан';
        typeCounts[type] = (typeCounts[type] || 0) + 1;
    });

    const labels = Object.keys(typeCounts);
    const data = Object.values(typeCounts);

    // Фиксированные цвета по типу задачи
    const colorMap = {
        'task': '#3290ec',  // синий
        'bug': '#FF6384',     // красный
        'story': '#43a047', // зелёный
        'Не указан': '#BDBDBD' // серый по умолчанию
    };

    // const defaultColors = ['#3290ec', '#FF6384', '#ADC68A']; // запасные цвета
    let colorIndex = 0;

    const colors = labels.map(label => {
        if (colorMap[label]) {
            return colorMap[label];
        } else {
            // если не задан цвет — берём из запасных
            return defaultColors[colorIndex++ % defaultColors.length];
        }
    });

    // Удаляем предыдущий график
    if (chart) {
        chart.destroy();
        chart = null;
    }

    chart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Количество задач по типам',
                data: data,
                backgroundColor: colors,
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Количество задач'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Тип задачи'
                    }
                }
            }
        }
    });

    updateLegendTitles(['Количество задач по типам'], [colors[0]]);
}

//Вывод задач после графика
function renderUserReport(user) {
    // Устанавливаем имя пользователя в заголовок
    document.getElementById('report-sprint-name').textContent = user.username || user.email || 'Без имени';

    const incompleteContainer = document.getElementById('incomplete-tasks');
    const completedContainer = document.getElementById('completed-tasks');

    // Очищаем старые задачи
    incompleteContainer.innerHTML = '';
    completedContainer.innerHTML = '';

    console.log(user.tasks);

    user.tasks.forEach(task => {
        const isDone = (task.status || '').toLowerCase() === 'выполнено';

        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="task-key-cell">${task.taskKey}</td>
            <td>${task.title}</td>
            <td>
                <div class="td-status">
                    <img src="${getTaskTypeIcon(task.taskType)}" alt="${task.taskType}"> 
                    <span>${capitalize(task.taskType)}</span>
                </div>
            </td>
            <td>${task.status}</td>
            <td>${task.executorName ? task.executorName : '—'}</td>
            <td>${task.priority ?? '—'}</td>
        `;

        (isDone ? completedContainer : incompleteContainer).appendChild(row);
    });
}

function renderSprintReport(sprint) {

    // Устанавливаем название спринта
    document.getElementById('report-sprint-name').textContent = sprint.name;

    const incompleteContainer = document.getElementById('incomplete-tasks');
    const completedContainer = document.getElementById('completed-tasks');

    // Очищаем текущие строки
    incompleteContainer.innerHTML = '';
    completedContainer.innerHTML = '';
    console.log(sprint.tasks)
    // Генерируем HTML для каждой задачи
    sprint.tasks.forEach(task => {
        const isDone = (task.status || '').toLowerCase() === 'выполнено';

        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="task-key-cell">${task.taskKey}</td>
            <td>${task.title}</td>
            <td>
                <div class="td-status">
                    <img src="${getTaskTypeIcon(task.taskType)}" alt="${task.taskType}"> 
                    <span>${capitalize(task.taskType)}</span>
                </div>
            </td>
            <td>${task.status}</td>
            <td>${task.executorName ? task.executorName : '—'}</td>
            <td>${task.priority ?? '—'}</td>
        `;

        (isDone ? completedContainer : incompleteContainer).appendChild(row);
    });
}

function getTaskTypeIcon(type) {
    if (!type || typeof type !== 'string') return 'icons/default.svg';

    const iconMap = {
        task: 'icons/tusk.svg',
        story: 'icons/history.svg',
        bug: 'icons/bug.svg'
    };
    return iconMap[type.toLowerCase()] || 'icons/default.svg';
}

function capitalize(str) {
    if (!str || typeof str !== 'string') return '';
    return str.charAt(0).toUpperCase() + str.slice(1);
}

