fetch('/api/teams')
    .then(response => {
        if (!response.ok) throw new Error('Ошибка при загрузке команд');
        return response.json();
    })
    .then(teams => {
        const container = document.getElementById('teams-container');
        container.innerHTML = '';

        // Добавляем карточку "Создать команду"
        const createCard = document.createElement('div');
        createCard.className = 'create-team-card';
        createCard.innerHTML = `
            <div class="card-top">
              <img src="/icons/people.svg" alt="Иконка команды" class="card-icon">
            </div>
            <div class="card-bottom">
              <a  href="#create-team-modal" class="card-btn">Создать команду</a>
            </div>
          `;
        container.appendChild(createCard);

        if (teams.length === 0) {
            const message = document.createElement('p');
            message.textContent = 'Вы не участвуете ни в одной команде.';
            container.appendChild(message);
            return;
        }

        teams.forEach(team => {
            const card = document.createElement('div');
            card.className = 'card';
            card.setAttribute('data-url', `/api/team/${team.id_team}`);
            card.innerHTML = `
  <div class="card-top-active">
    <h3>${team.team_name}</h3>
    <div class="team-members">
      ${team.emails && team.emails.length > 0 ? team.emails.map(user => `
        <a href="/searchProfile?email=${encodeURIComponent(user.email)}" class="member-button" title="${user.email}">
          <span><img class="member-img" src="/icons/person.svg"></span>
        </a>
      `).join('') : '<p>У этой команды нет участников.</p>'}
    </div>
  </div>
  <div class="card-bottom-active">
    <span>Участников: ${team.emails ? team.emails.length : 0}</span>
    <span class="description-team">${team.description ? team.description : 'Без описания'}</span>
  </div>
`;

            card.addEventListener('click', () => {
                window.location.href = `/team_page?id=${team.id_team}`
            });

            container.appendChild(card);
            applyRandomColors();
        });
    })
    .catch(error => {
        console.error('Ошибка при загрузке команд:', error);
    });

const colors = ['#f28b82', '#fbbc04', '#fff475', '#ccff90', '#a7ffeb', '#cbf0f8', '#aecbfa', '#d7aefb', '#fdcfe8'];

let lastColor = null;

function applyRandomColors() {
    let lastColor = null;
    const members = document.querySelectorAll('.member-button');

    members.forEach(member => {
        let color;
        do {
            color = colors[Math.floor(Math.random() * colors.length)];
        } while (color === lastColor);

        lastColor = color;
        member.style.backgroundColor = color;
    });
}
