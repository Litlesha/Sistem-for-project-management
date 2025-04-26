document.addEventListener("DOMContentLoaded", function () {
    // Получаем данные о пользователе
    fetch('/me')
        .then(response => {
            if (!response.ok) throw new Error("Ошибка при загрузке профиля");
            return response.json();
        })
        .then(data => {
            document.getElementById("username").textContent = data.username;
            document.getElementById("position").value = data.position || '';
            document.getElementById("organization").value = data.organization || '';
            document.getElementById("email").value = data.email;
        })
        .catch(error => {
            console.error(error);
            document.getElementById("user-email").textContent = "Ошибка загрузки профиля";
        });

    // Загружаем команды пользователя
    fetch('/api/teams')
        .then(response => response.json())
        .then(teams => {
            const teamsList = document.getElementById("user-teams-list");
            teams.forEach(team => {
                const teamItem = document.createElement("div");
                teamItem.classList.add("team-item");
                teamItem.textContent = team.team_name; // Используем название команды
                teamItem.addEventListener("click", () => {
                    window.location.href = `/team_page?id=${team.id_team}`; // Переходим на страницу команды
                });
                teamsList.appendChild(teamItem);
            });
        })
        .catch(error => {
            console.error("Ошибка при загрузке команд:", error);
        });

    // Функция для обновления данных в базе данных
    function updateUserProfile(field, value) {
        fetch('/update-profile', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ [field]: value })
        })
            .then(response => {
                if (!response.ok) throw new Error("Ошибка при обновлении данных");
            })
            .catch(error => {
                console.error(error);
            });
    }

    // Обновление юзернейма
    const usernameElement = document.getElementById("username");
    usernameElement.addEventListener("click", function () {
        const currentUsername = usernameElement.textContent;

        const input = document.createElement("input");
        input.type = "text";
        input.value = currentUsername;
        usernameElement.innerHTML = '';
        usernameElement.appendChild(input);

        input.focus();

        input.addEventListener("keydown", function (e) {
            if (e.key === "Enter") {
                const newUsername = input.value;
                // Отправляем запрос на сервер для обновления юзернейма
                fetch('/update-profile', {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ username: newUsername })
                })
                    .then(response => response.json())
                    .then(data => {
                        // Обновляем отображаемое имя
                        usernameElement.textContent = data.username;
                    })
                    .catch(error => {
                        console.error("Ошибка при обновлении юзернейма:", error);
                        usernameElement.textContent = currentUsername; // В случае ошибки восстанавливаем старое имя
                    });
            }
        });

        input.addEventListener("keydown", function (e) {
            if (e.key === "Escape") {
                usernameElement.textContent = currentUsername; // В случае отмены восстанавливаем старое имя
            }
        });
    });

    // Обновление должности и организации при нажатии Enter
    document.getElementById("position").addEventListener("keydown", function (e) {
        if (e.key === "Enter") {
            const newPosition = e.target.value;
            updateUserProfile('position', newPosition);
        }
    });

    document.getElementById("organization").addEventListener("keydown", function (e) {
        if (e.key === "Enter") {
            const newOrganization = e.target.value;
            updateUserProfile('organization', newOrganization);
        }
    });
});
