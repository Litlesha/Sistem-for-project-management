document.addEventListener("DOMContentLoaded", function () {
    const userImageInput = document.getElementById('user-image-input');
    const photoContainer = document.getElementById('photo-container');
    let userId = null;
    let userImgPath = null;

    // Загрузка информации о пользователе
    async function loadCurrentUser() {
        try {
            const response = await fetch('/me');
            if (response.ok) {
                const user = await response.json();
                userId = user.id_user;
                userImgPath = user.userImgPath;

                if (user.username && user.username.trim() !== '') {
                    document.getElementById("username").textContent = user.username;
                } else {
                    renderUsernameInput();
                }

                document.getElementById("position").value = user.position || '';
                document.getElementById("organization").value = user.organization || '';
                document.getElementById("email").value = user.email;

                if (userImgPath && userImgPath.trim() !== '') {
                    renderUserImage(userImgPath);
                } else {
                    renderUploadButton();
                }
            } else {
                console.error('Ошибка получения пользователя');
                document.getElementById("user-email").textContent = "Ошибка загрузки профиля";
            }
        } catch (error) {
            console.error('Ошибка при загрузке профиля:', error);
        }
    }
    function renderUsernameInput() {
        const usernameElement = document.getElementById("username");
        usernameElement.innerHTML = '';

        const input = document.createElement("input");
        input.type = "text";
        input.placeholder = "Введите имя пользователя";
        input.classList.add('username-input');

        usernameElement.appendChild(input);
        input.focus();

        input.addEventListener("keydown", function (e) {
            if (e.key === "Enter") {
                const newUsername = input.value.trim();
                if (newUsername !== '') {
                    fetch('/update-profile', {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ username: newUsername })
                    })
                        .then(response => response.json())
                        .then(data => {
                            usernameElement.textContent = data.username;
                        })
                        .catch(error => {
                            console.error("Ошибка при обновлении юзернейма:", error);
                            usernameElement.textContent = "Ошибка";
                        });
                }
            } else if (e.key === "Escape") {
                usernameElement.textContent = "Нет имени";
            }
        });
    }

    loadCurrentUser();

    // Загрузка списка команд
    fetch('/api/teams')
        .then(response => response.json())
        .then(teams => {
            const teamsList = document.getElementById("user-teams-list");
            teams.forEach(team => {
                const teamItem = document.createElement("div");
                teamItem.classList.add("team-item");
                teamItem.textContent = team.team_name;
                teamItem.addEventListener("click", () => {
                    window.location.href = `/team_page?id=${team.id_team}`;
                });
                teamsList.appendChild(teamItem);
            });
        })
        .catch(error => {
            console.error("Ошибка при загрузке команд:", error);
        });

    // Функция для обновления профиля
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

    // Клик по username для редактирования
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
                fetch('/update-profile', {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username: newUsername })
                })
                    .then(response => response.json())
                    .then(data => {
                        usernameElement.textContent = data.username;
                    })
                    .catch(error => {
                        console.error("Ошибка при обновлении юзернейма:", error);
                        usernameElement.textContent = currentUsername;
                    });
            } else if (e.key === "Escape") {
                usernameElement.textContent = currentUsername;
            }
        });
    });

    // Обновление должности и организации
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

    // При выборе файла изображения
    userImageInput.addEventListener('change', function () {
        const file = this.files[0];
        if (!file || !userId) return;

        const formData = new FormData();
        formData.append('image', file);
        formData.append('userId', userId);

        fetch('/api/user/upload-photo', {
            method: 'POST',
            body: formData
        })
            .then(res => {
                if (!res.ok) throw new Error('Ошибка загрузки изображения');
                return res.json();
            })
            .then(data => {
                renderUserImage(data.imageUrl);
            })
            .catch(err => console.error('Ошибка загрузки изображения:', err));
    });

    // Отрисовка фото пользователя
    function renderUserImage(imageUrl) {
        photoContainer.innerHTML = '';
        const img = document.createElement('img');
        img.src = imageUrl;
        img.alt = "Фото пользователя";
        img.classList.add('user-photo-btn');
        img.style.opacity = '0';
        photoContainer.appendChild(img);

        img.onload = () => {
            img.style.transition = 'opacity 0.5s';
            img.style.opacity = '1';
        };

        img.addEventListener('click', () => {
            userImageInput.click();
        });
    }

    function renderUploadButton() {
        photoContainer.innerHTML = '';

        const button = document.createElement('button');
        button.classList.add('upload-photo-btn');

        // Создаём элемент для иконки
        const icon = document.createElement('img');
        icon.src = '/icons/load-photo.svg';  // Путь к иконке
        icon.alt = 'Загрузить фото';
        icon.style.width = '20px';
        icon.style.height = '20px';

        // Добавляем иконку в кнопку
        button.appendChild(icon);

        // Делаем контейнер (кнопку) кликабельным
        button.addEventListener('click', () => {
            userImageInput.click();  // Открытие выбора файла при клике на кнопку
        });

        // Добавляем кнопку в контейнер
        photoContainer.appendChild(button);
    }
    document.querySelector('.delete-btn').addEventListener('click', async function () {
        try {
            const response = await fetch('/logout', {
                method: 'POST'
            });
            if (response.ok) {
                window.location.href = '/login';
            } else {
                console.error('Ошибка выхода из аккаунта');
            }
        } catch (error) {
            console.error('Ошибка при выходе из аккаунта:', error);
        }
    });
});
