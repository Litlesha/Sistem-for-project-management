function connectGithub() {
    const currentUrl = window.location.pathname + window.location.search;
    window.location.href = "/connect/github?return_to=" + encodeURIComponent(currentUrl);
}

// Глобальная переменная для хранения данных задачи
let currentTaskKey = '';
let currentTaskTitle = '';

function openRepoModalWithAuthCheck() {
    fetch('/github/check-auth')
        .then(res => {
            if (res.status === 401) {
                // Не авторизован
                connectGithub();
            } else if (res.ok) {
                // Авторизован
                openRepoModalFromButton();
            } else {
                throw new Error("Ошибка при проверке авторизации GitHub");
            }
        })
        .catch(err => {
            console.error(err);
            alert("Произошла ошибка при проверке подключения к GitHub");
        });
}

function setTaskBranchData(taskKey, taskTitle) {
    currentTaskKey = taskKey;
    currentTaskTitle = taskTitle;

    console.log(currentTaskKey);
    console.log(currentTaskTitle);
}

function openRepoModalFromButton() {
    document.getElementById("repoModal").style.display = "block";
    loadRepositories();

    const repoSelect = document.getElementById('repo-select-modal');
    const branchSelect = document.getElementById('branch-select-modal');

    repoSelect.innerHTML = '<option disabled selected>Выберите репозиторий...</option>';
    branchSelect.innerHTML = '<option disabled selected>Сначала выберите репозиторий</option>';
    branchSelect.disabled = true;

    // Используем сохранённые глобальные переменные, чтобы установить имя ветки
    const branchName = generateBranchName(currentTaskKey, currentTaskTitle);
    document.getElementById("new-branch-name").value = branchName;

    repoSelect.onchange = function () {
        const [owner, repo] = this.value.split('/');
        loadBranches(owner, repo);
    };
    console.log("Branch name:", branchName);
    document.getElementById("new-branch-name").value = branchName;

}



function generateBranchName(issueKey, issueTitle) {
    if (!issueKey || !issueTitle) return '';

    const kebabTitle = issueTitle
        .toLowerCase()
        .replace(/[^\w\s]/g, '')
        .replace(/\s+/g, '-');

    const branchType = document.getElementById("branch-type")?.value || 'feature/';
    return `${branchType}${issueKey}-${kebabTitle}`;
}

document.getElementById("branch-type").addEventListener("change", () => {
    const branchName = generateBranchName(currentTaskKey, currentTaskTitle);
    document.getElementById("new-branch-name").value = branchName;
});



function closeRepoModal() {
    document.getElementById("repoModal").style.display = "none";
}


function loadRepositories() {
    fetch('/github/repos')
        .then(res => res.json())
        .then(data => {
            const select = document.getElementById('repo-select-modal');
            data.forEach(repo => {
                const option = document.createElement('option');
                option.value = `${repo.owner.login}/${repo.name}`;
                option.textContent = repo.name;
                select.appendChild(option);
            });
        });
}

function loadBranches(owner, repo) {
    fetch(`/github/${owner}/${repo}/branches`)
        .then(res => res.json())
        .then(branches => {
            const select = document.getElementById('branch-select-modal');
            select.innerHTML = '<option disabled selected>Выберите ветку...</option>';
            select.disabled = false;

            branches.forEach(branch => {
                const option = document.createElement('option');
                option.value = branch.name;
                option.textContent = branch.name;
                select.appendChild(option);
            });
        });
}

function submitRepoBranch(event) {
    event.preventDefault();

    const repoValue = document.getElementById('repo-select-modal').value;
    const [owner, repo] = repoValue.split('/');
    const baseBranch = document.getElementById('branch-select-modal').value;
    const newBranch = document.getElementById('new-branch-name').value;

    fetch('/github/create-branch', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            repoOwner: owner,
            repoName: repo,
            baseBranch: baseBranch,
            newBranch: newBranch
        })
    }).then(res => {
        if (res.ok) {
            alert("Ветка успешно создана");
            closeRepoModal();
        } else {
            alert("Ошибка при создании ветки");
        }
    });
}


