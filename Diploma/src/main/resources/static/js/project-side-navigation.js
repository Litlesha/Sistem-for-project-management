async function loadProjectTitle() {
    const { projectId } = getProjectIdAndSectionFromUrl();
    if (!projectId) {
        document.getElementById("project-title").textContent = "Проект не найден";
        return;
    }

    try {
        const response = await fetch(`/api/project/${projectId}/backlog`);
        if (!response.ok) throw new Error("Проект не найден");

        const project = await response.json();
        document.getElementById("project-title").textContent = project.name;
    } catch (err) {
        console.error(err);
        document.getElementById("project-title").textContent = "Ошибка загрузки проекта";
    }
}
function getProjectIdAndSectionFromUrl() {
    const params = new URLSearchParams(window.location.search);
    const projectId = params.get('id');
    const section = params.get('section');
    return { projectId, section };
}
document.addEventListener('DOMContentLoaded', () => {
    const navLinks = document.querySelectorAll('.nav-link');
    const { projectId, section } = getProjectIdAndSectionFromUrl();

    // Обновляем href и выделяем активную ссылку
    navLinks.forEach(link => {
        const linkSection = link.getAttribute('data-section');
        link.href = `/project_page?id=${projectId}&section=${linkSection}`;

        if (linkSection === section) {
            link.classList.add('active-link');
        } else {
            link.classList.remove('active-link');
        }
    });

    // Переключаем активную секцию на странице
    document.querySelectorAll('section.section').forEach(sec => {
        sec.classList.remove('active-section');
    });

    const activeSection = document.getElementById(section);
    if (activeSection) {
        activeSection.classList.add('active-section');
    }
});

window.addEventListener("DOMContentLoaded", loadProjectTitle);