const submitButttom = document.getElementById("submit");
const errorMessage = document.getElementById("error-message");
submitButttom.addEventListener('click',async () => {
    event.preventDefault();
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmpassword').value;
    const data = {
        email,
        password
    }
    errorMessage.style.display = 'none';
    errorMessage.textContent = '';
    // Проверка пароля на требования
    const passwordRequirements = [
        {regex: /.{8,}/, message: 'Минимум 8 символов'},
        {regex: /[a-z]/, message: 'Нужны маленькие буквы'},
        {regex: /[A-Z]/, message: 'Нужны большие буквы'},
        {regex: /\d/, message: 'Нужны цифры'}
    ];

    let passwordValid = true;
    passwordRequirements.forEach(requirement => {
        if (!requirement.regex.test(password)) {
            passwordValid = false;
            errorMessage.textContent += requirement.message + '\n';
        }
    });

    if (!passwordValid) {
        errorMessage.textContent = 'Пароль не соответствует требованиям'
        errorMessage.style.display = 'block';
        return;
    }
    if (password == confirmPassword) {
        const jsonData = JSON.stringify(data);
        const response = await fetch('register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if (response.ok) {
            window.location.href = '/login'; // ← Редирект после успешной регистрации
        }
    } else {
        // Показать сообщение об ошибке
        errorMessage.textContent = 'Пароли не совпадают';
        errorMessage.style.display = 'block';
    }

})

const passwordInput = document.querySelector('input[name="password"]');
const requirements = document.querySelector('.password-requirements');

passwordInput.addEventListener('focus', function () {
    requirements.style.display = 'block';
});

passwordInput.addEventListener('blur', function () {
    requirements.style.display = 'none';
});