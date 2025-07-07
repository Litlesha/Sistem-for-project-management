async function loadProjectTitle() {
    const { projectId } = getProjectIdAndSectionFromUrl();
    if (!projectId) {
        document.getElementById("project-title").textContent = "Проект не найден";
        return;
    }

    try {
        const response = await fetch(`/api/project/${projectId}/backlog`);

        if (response.status === 403) {
            window.location.href = "/access-project-denied";
            return;
        }

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

    checkRoleAndShowReportLink(projectId);

});

async function checkRoleAndShowReportLink(projectId) {
    try {
        const response = await fetch(`/api/roles/getUserRole/${projectId}`);
        if (!response.ok) throw new Error("Ошибка при получении роли");

        const role = await response.text();

        // Показываем ссылку на "Отчет", если пользователь — админ
        if (role === "PROJECT_ADMIN") {
            const reportLink = document.querySelector('.nav-link[data-section="report"]');
            if (reportLink) {
                reportLink.classList.remove("hidden");
            }
        }
    } catch (err) {
        console.error("Ошибка при проверке роли:", err);
    }
}

document.getElementById("report-button").addEventListener("click", async (e) => {
    e.preventDefault();
    const { projectId } = getProjectIdAndSectionFromUrl();

    try {
        const response = await fetch(`/project_page?id=${projectId}&section=report`, {
            method: "GET",
            headers: {
                "X-Requested-With": "XMLHttpRequest" // чтобы сервер мог понять, что это запрос, а не переход
            }
        });

        if (response.status === 403) {
            alert("У вас нет доступа к отчету");
            return;
        }

        // Если все хорошо — перейти
        window.location.href = `/project_page?id=${projectId}&section=report`;

    } catch (err) {
        alert("Ошибка при проверке доступа");
        console.error(err);
    }
});



window.addEventListener("DOMContentLoaded", loadProjectTitle);