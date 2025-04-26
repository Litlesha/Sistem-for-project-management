// Получаем элементы формы
const form = document.getElementById('loginForm');
form.addEventListener('submit', async (event) => {
    event.preventDefault(); // Предотвращаем стандартное поведение формы

    // Получаем значения из формы
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    // Создаем объект данных для отправки
    const requestData = {
        email: email,
        password: password
    };

    try {
        // Отправляем POST-запрос на сервер для авторизации
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json' // Указываем тип контента как JSON
            },
            body: JSON.stringify(requestData) // Преобразуем данные в JSON
        });

        // Обработка ответа от сервера
        if (response.ok) {
            const data = await response.json();
            // Сохраняем токен в localStorage
            localStorage.setItem('token', data.token); // Сохраняем токен
            console.log('Token saved:', data.token);

            // Перенаправляем пользователя на защищенную страницу
            window.location.href = '/main.html';  // Перенаправление на защищенную страницу
        } else {
            console.error('Error:', response.statusText); // Обработка ошибок
        }
    } catch (error) {
        console.error('Error:', error); // Обработка исключений
    }
});

// Функция для добавления токена в заголовки запроса
async function fetchWithAuth(url) {
    const token = localStorage.getItem('token'); // Получаем токен из localStorage

    const response = await fetch(url, {
        method: 'GET', // Пример GET-запроса
        headers: {
            'Authorization': `Bearer ${token}` // Добавляем токен в заголовок
        }
    });

    if (response.ok) {
        // Обработка успешного ответа
        const data = await response.json();
        console.log('Data:', data);
    } else {
        // Обработка ошибок
        console.error('Error:', response.statusText);
    }
}

// Пример вызова защищенной страницы после логина
document.getElementById('goToMainButton').addEventListener('click', () => {
    fetchWithAuth('/main'); // Загружаем защищенную страницу
});