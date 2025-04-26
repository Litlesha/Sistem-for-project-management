const formProject = document.getElementById("project-form");

formProject.addEventListener("submit", async (e) => {
    e.preventDefault();

    const name = document.getElementById("project-name").value;
    const key = document.getElementById("project-key").value;
    const description = document.getElementById("project-description").value;
    const keyErrorElement = document.getElementById("key-error");

    // Очистить предыдущее сообщение об ошибке
    keyErrorElement.textContent = "";

    // Проверка на ключ (4 заглавные латинские буквы)
    const keyPattern = /^[A-Z]{4}$/;
    if (!keyPattern.test(key)) {
        keyErrorElement.textContent = "Ключ должен состоять из 4 заглавных латинских букв.";
        keyErrorElement.style.color = "red";
        return;
    }

    const projectData = {
        name,
        key,
        description
    };

    try {
        const response = await fetch("/create_project", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(projectData)
        });

        if (!response.ok) {
            const text = await response.text();
            throw new Error("Ошибка создания проекта: " + text);
        }

        const result = await response.json();
        console.log("Проект создан:", result);

        window.location.href = `/project_page?id=${result.id_project}`;
    } catch (error) {
        console.error("Ошибка при отправке запроса:", error);
        alert("Ошибка при создании проекта: " + error.message);
    }
});
const form = document.getElementById("team-form");

form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const teamName = document.getElementById("team-name").value;
    const emails = document.getElementById("team-members").value.split(',').map(email => email.trim());

    const teamData = {
        team_name: teamName,
        emails: emails
    };

    try {
        const response = await fetch("/create_team", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(teamData)
        });

        if (!response.ok) {
            const text = await response.text();
            throw new Error("Ошибка создания команды: " + text);
        }

        const result = await response.json();
        console.log("Команда создана:", result);

        window.location.href = `/team_page?id=${result.id_team}`;  // Перенаправление на страницу команды
    } catch (error) {
        console.error("Ошибка при отправке запроса:", error);
        alert("Ошибка при создании команды: " + error.message);
    }
});

function goBack() {
    window.history.back();
}