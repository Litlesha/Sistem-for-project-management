function getSessionInfo() {
    return fetch('/api/session/info')
        .then(response => response.json())
        .then(data => ({ username: data.username }))
        .catch(error => {
            console.error("Error fetching session info:", error);
            return { username: "Guest" };
        });
}
function fetchChatInfo(type, id) {
    return fetch(`/api/chat/info?type=${type}&id=${id}`)
        .then(response => {
            if (!response.ok) throw new Error("Ошибка при получении информации о чате");
            return response.json();
        })
        .catch(error => {
            console.error("Ошибка загрузки chat info:", error);
            return { title: "Неизвестный чат", membersCount: 0 };
        });
}
getSessionInfo().then(({ username }) => {
    const section = document.querySelector('section.section');
    const chatType = section.id;  // chatType (например, teamChat)
    const entityId = section.getAttribute('data-id');  // entityId (например, team ID)

    console.log("Chat type:", chatType);
    console.log("Entity ID:", entityId);
    fetchChatInfo(chatType, entityId).then(({ title, membersCount }) => {
        const chatTitleElement = document.getElementById('chat-title'); // Добавь такой элемент в HTML
        const chatInfoElement = document.getElementById('chat-info');   // И такой

        if (chatTitleElement) chatTitleElement.textContent = title;
        if (chatInfoElement) chatInfoElement.textContent = `Участников: ${membersCount}`;
    });
    // Создаем WebSocket подключение
    const socket = new WebSocket(`ws://localhost:8081/ws?type=${chatType}&id=${entityId}`);

    const chatInput = document.querySelector('.chat-input');
    const sendButton = document.querySelector('.send-button');

    sendButton.addEventListener('click', sendMessage);
    chatInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') sendMessage();
    });

    function sendMessage() {
        const message = chatInput.value.trim();
        if (!message) return;

        const timestamp = Date.now();
        const payload = JSON.stringify({ user: username, text: message, timestamp });
        socket.send(payload);
        chatInput.value = '';
    }

    socket.addEventListener('message', (event) => {
        try {
            const { user, text, timestamp } = JSON.parse(event.data);
            const type = user === username ? 'sent' : 'received';
            const messageDiv = document.createElement('div');
            messageDiv.classList.add('chat-message', type);
            if (user && type === 'received') {
                const userSpan = document.createElement('div');
                userSpan.classList.add('message-user');
                userSpan.textContent = user;
                messageDiv.appendChild(userSpan);
            }
            appendMessage(text, type,timestamp, user);
        } catch (err) {
            console.error("Ошибка при парсинге:", err);
        }
    });
    function appendMessage(text, type, timestamp = null, user = null) {
        const messagesContainer = document.getElementById('chat-messages');

        const messageDiv = document.createElement('div');
        messageDiv.classList.add('chat-message', type); // 'sent' или 'received'

        if (type === 'received') {
            // Автор (только для полученных сообщений)
            if (user) {
                const userSpan = document.createElement('div');
                userSpan.classList.add('message-user');
                userSpan.textContent = user;
                messageDiv.appendChild(userSpan);
            }

            // Обёртка для текста и времени
            const contentWrapper = document.createElement('div');
            contentWrapper.classList.add('received-message-wrapper');

            const textDiv = document.createElement('div');
            textDiv.classList.add('message-text');
            textDiv.textContent = text;
            contentWrapper.appendChild(textDiv);

            const timeDiv = document.createElement('div');
            timeDiv.classList.add('timestamp');
            if (timestamp) {
                const date = new Date(timestamp);
                const hours = date.getHours().toString().padStart(2, '0');
                const minutes = date.getMinutes().toString().padStart(2, '0');
                timeDiv.textContent = `${hours}:${minutes}`;
            }
            contentWrapper.appendChild(timeDiv);

            messageDiv.appendChild(contentWrapper);
        } else {
            // Для sent — без обёртки
            const textDiv = document.createElement('div');
            textDiv.classList.add('message-text');
            textDiv.textContent = text;
            messageDiv.appendChild(textDiv);

            const timeDiv = document.createElement('div');
            timeDiv.classList.add('timestamp');
            if (timestamp) {
                const date = new Date(timestamp);
                const hours = date.getHours().toString().padStart(2, '0');
                const minutes = date.getMinutes().toString().padStart(2, '0');
                timeDiv.textContent = `${hours}:${minutes}`;
            }
            messageDiv.appendChild(timeDiv);
        }

        messagesContainer.appendChild(messageDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
});
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.chatbox').forEach(box => {
        const type = box.dataset.type;
        const id = box.dataset.id;
        const span = box.querySelector('.last-chat-message');
        const dateSpan = box.querySelector('.last-chat-date');

        const formatDate = isoString => {
            const date = new Date(isoString);
            return date.toLocaleString([], {
                timeStyle: 'short'
            });
        };

        // Первый fetch для начального сообщения
        fetch(`http://localhost:8081/api/last_message?type=${type}&id=${id}`)
            .then(res => res.status === 204 ? null : res.json())
            .then(data => {
                if (data) {
                    const { user, text, timestamp } = data;
                    console.log("Получены данные:", data);
                    span.textContent = `${user}: ${text}`;
                    if (timestamp) {
                        dateSpan.textContent = ` ${formatDate(timestamp)}`;
                    }
                } else {
                    span.textContent = "Нет сообщений";
                    dateSpan.textContent = "";
                }
            })
            .catch(err => {
                console.error(`Ошибка загрузки сообщения для ${id}:`, err);
                span.textContent = "Ошибка";
                dateSpan.textContent = "";
            });

        // Подключение к WebSocket
        const socket = new WebSocket(`ws://localhost:8081/ws?type=${type}&id=${id}`);

        socket.addEventListener('message', event => {
            try {
                const data = JSON.parse(event.data);
                if (data && data.user && data.text) {
                    span.textContent = `${data.user}: ${data.text}`;
                    if (data.timestamp) {
                        dateSpan.textContent = ` ${formatDate(data.timestamp)}`;
                    }
                }
            } catch (e) {
                console.error("Ошибка разбора сообщения WebSocket:", e);
            }
        });

        socket.addEventListener('error', e => {
            console.error(`WebSocket ошибка для ${id}:`, e);
        });

        socket.addEventListener('close', () => {
            console.warn(`WebSocket закрыт для ${id}`);
        });
    });
});
