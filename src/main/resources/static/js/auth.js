// auth.js - Registration form handler
document.getElementById('register-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value.trim();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const errorMsg = document.getElementById('error-msg');
    const errorText = document.getElementById('error-text');

    errorMsg.style.display = 'none';

    if (password !== confirmPassword) {
        errorText.textContent = 'Passwords do not match';
        errorMsg.style.display = 'flex';
        return;
    }
    if (password.length < 6) {
        errorText.textContent = 'Password must be at least 6 characters';
        errorMsg.style.display = 'flex';
        return;
    }

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password })
        });
        const data = await response.json();
        if (response.ok) {
            window.location.href = '/login.html?registered=true';
        } else {
            errorText.textContent = data.error || 'Registration failed';
            errorMsg.style.display = 'flex';
        }
    } catch (error) {
        errorText.textContent = 'Server error. Please try again.';
        errorMsg.style.display = 'flex';
    }
});
