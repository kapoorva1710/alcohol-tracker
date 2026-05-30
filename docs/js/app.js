// app.js - Dashboard logic
let currentGoal = 14;
let consumptionChart;

document.addEventListener('DOMContentLoaded', () => {
    loadUsername();
    buildDateScroller();
    fetchSettingsAndDrinks();

    // Drink Form
    document.getElementById('drink-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const beverageName = document.getElementById('beverageName').value;
        const quantity = parseFloat(document.getElementById('quantity').value);
        const notes = document.getElementById('notes').value;
        try {
            const res = await fetch('https://alcohol-tracker-mn9s.onrender.com/api/drinks', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ beverageName, quantity, notes, sourceType: 'MANUAL' })
            });
            if (res.ok) {
                document.getElementById('drink-form').reset();
                fetchDrinks();
            }
        } catch (err) { console.error('Error:', err); }
    });

    // Settings Modal
    const modal = document.getElementById('settings-modal');
    const goalInput = document.getElementById('weeklyGoalInput');
    document.getElementById('settings-btn').addEventListener('click', () => {
        goalInput.value = currentGoal;
        modal.style.display = 'block';
    });
    document.querySelector('.close-btn').addEventListener('click', () => modal.style.display = 'none');
    window.addEventListener('click', (e) => { if (e.target === modal) modal.style.display = 'none'; });

    document.getElementById('settings-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const newGoal = parseFloat(goalInput.value);
        try {
            const res = await fetch('https://alcohol-tracker-mn9s.onrender.com/api/settings', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ weeklyGoal: newGoal })
            });
            if (res.ok) { currentGoal = newGoal; modal.style.display = 'none'; fetchDrinks(); }
        } catch (err) { console.error('Error:', err); }
    });
});

async function loadUsername() {
    try {
        const res = await fetch('https://alcohol-tracker-mn9s.onrender.com/api/auth/current');
        if (res.ok) {
            const data = await res.json();
            document.getElementById('nav-username').innerHTML =
                '<i class="fa-solid fa-user"></i> ' + data.username;
        }
    } catch (e) {}
}

function buildDateScroller() {
    const scroller = document.getElementById('date-scroller');
    const today = new Date();
    const days = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'];
    for (let i = -3; i <= 3; i++) {
        const d = new Date(today);
        d.setDate(today.getDate() + i);
        const div = document.createElement('div');
        div.className = 'date-item' + (i === 0 ? ' active' : '');
        div.innerHTML = '<span>' + days[d.getDay()] + '</span><strong>' + d.getDate() + '</strong>';
        scroller.appendChild(div);
    }
}

async function fetchSettingsAndDrinks() {
    try {
        const res = await fetch('https://alcohol-tracker-mn9s.onrender.com/api/settings');
        if (res.ok) { const s = await res.json(); currentGoal = s.weeklyGoal || 14; }
        await fetchDrinks();
    } catch (e) { await fetchDrinks(); }
}

async function fetchDrinks() {
    try {
        const res = await fetch('https://alcohol-tracker-mn9s.onrender.com/api/drinks');
        const drinks = await res.json();
        updateDashboard(drinks);
    } catch (e) { console.error('Error:', e); }
}

function updateDashboard(drinks) {
    const list = document.getElementById('log-list');
    const emptyEl = document.getElementById('empty-logs');
    list.innerHTML = '';
    let total = 0;
    drinks.sort((a, b) => new Date(b.consumedAt) - new Date(a.consumedAt));
    const recent = drinks.slice(0, 5);

    if (recent.length === 0) {
        emptyEl.style.display = 'block';
    } else {
        emptyEl.style.display = 'none';
        recent.forEach(d => {
            total += d.quantity;
            const li = document.createElement('li');
            const t = new Date(d.consumedAt).toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'});
            let icon = 'fa-martini-glass-empty';
            if (d.beverageName.toLowerCase().includes('beer')) icon = 'fa-beer-mug-empty';
            if (d.beverageName.toLowerCase().includes('wine')) icon = 'fa-wine-glass';
            li.innerHTML = '<div class="log-icon"><i class="fa-solid ' + icon + '"></i></div>' +
                '<div class="log-details"><span class="log-name">' + d.beverageName + '</span>' +
                '<span class="log-meta">' + t + ' &bull; ' + d.sourceType + '</span></div>' +
                '<div class="log-quantity">' + d.quantity + '</div>';
            list.appendChild(li);
        });
    }

    // Calculate total from ALL drinks not just recent
    total = drinks.reduce((sum, d) => sum + d.quantity, 0);
    document.getElementById('total-drinks').innerText = total.toFixed(1);

    const msg = document.getElementById('status-message');
    if (total >= currentGoal) {
        msg.textContent = 'You have exceeded your weekly goal!';
        msg.className = 'status-message danger';
    } else if (total >= currentGoal * 0.8) {
        msg.textContent = 'You are approaching your weekly goal.';
        msg.className = 'status-message warning';
    } else {
        msg.textContent = 'You are within your weekly goal.';
        msg.className = 'status-message';
    }
    updateChart(total);
}

function updateChart(totalQuantity) {
    const ctx = document.getElementById('consumptionChart').getContext('2d');
    const remaining = Math.max(currentGoal - totalQuantity, 0);
    if (consumptionChart) consumptionChart.destroy();
    consumptionChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Consumed', 'Remaining'],
            datasets: [{ data: [totalQuantity, remaining], backgroundColor: ['#00c49a', '#e2e8f0'], borderWidth: 0, cutout: '80%', borderRadius: [20, 0] }]
        },
        options: {
            responsive: true, maintainAspectRatio: false,
            plugins: { legend: { display: false }, tooltip: { enabled: false } },
            animation: { animateScale: true, animateRotate: true }
        }
    });
}
