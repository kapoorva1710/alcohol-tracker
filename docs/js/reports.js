// reports.js - Weekly & Monthly charts + stats
const DAYS = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'];
const MONTHS = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];

document.addEventListener('DOMContentLoaded', () => {
    loadUsername();
    fetchAllDrinks();
});

async function loadUsername() {
    try {
        const res = await fetch('https://alcohol-tracker-mn9s.onrender.com/api/auth/current');
        if (res.ok) {
            const data = await res.json();
            document.getElementById('nav-username').innerHTML = '<i class="fa-solid fa-user"></i> ' + data.username;
        }
    } catch (e) {}
}

async function fetchAllDrinks() {
    try {
        const res = await fetch('https://alcohol-tracker-mn9s.onrender.com/api/drinks');
        const drinks = await res.json();
        renderStats(drinks);
        renderWeeklyChart(drinks);
        renderMonthlyChart(drinks);
    } catch (err) { console.error('Error:', err); }
}

function renderStats(drinks) {
    const totalDrinks = drinks.reduce((s, d) => s + d.quantity, 0);
    document.getElementById('total-all-time').textContent = totalDrinks.toFixed(1);

    // Average per day (based on date range)
    if (drinks.length > 0) {
        const dates = drinks.map(d => new Date(d.consumedAt));
        const minDate = new Date(Math.min(...dates));
        const maxDate = new Date(Math.max(...dates));
        const daysDiff = Math.max(1, Math.ceil((maxDate - minDate) / (1000 * 60 * 60 * 24)) + 1);
        document.getElementById('avg-per-day').textContent = (totalDrinks / daysDiff).toFixed(1);
    }

    // Most common beverage
    const freq = {};
    drinks.forEach(d => {
        const name = d.beverageName.toLowerCase();
        freq[name] = (freq[name] || 0) + 1;
    });
    const sorted = Object.entries(freq).sort((a, b) => b[1] - a[1]);
    document.getElementById('most-common').textContent = sorted.length > 0 ? sorted[0][0] : '-';

    // Heaviest day
    const dayTotals = {};
    drinks.forEach(d => {
        const day = DAYS[new Date(d.consumedAt).getDay()];
        dayTotals[day] = (dayTotals[day] || 0) + d.quantity;
    });
    const heaviest = Object.entries(dayTotals).sort((a, b) => b[1] - a[1]);
    document.getElementById('heaviest-day').textContent = heaviest.length > 0 ? heaviest[0][0] : '-';
}

function renderWeeklyChart(drinks) {
    const today = new Date();
    const startOfWeek = new Date(today);
    startOfWeek.setDate(today.getDate() - today.getDay());
    startOfWeek.setHours(0, 0, 0, 0);

    const dailyData = [0, 0, 0, 0, 0, 0, 0];
    drinks.forEach(d => {
        const dt = new Date(d.consumedAt);
        if (dt >= startOfWeek) {
            dailyData[dt.getDay()] += d.quantity;
        }
    });

    const ctx = document.getElementById('weeklyChart').getContext('2d');
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: DAYS,
            datasets: [{
                label: 'Drinks',
                data: dailyData,
                backgroundColor: '#00c49a',
                borderRadius: 8,
                borderSkipped: false,
                maxBarThickness: 40
            }]
        },
        options: {
            responsive: true, maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                y: { beginAtZero: true, ticks: { stepSize: 1 }, grid: { color: '#f1f5f9' } },
                x: { grid: { display: false } }
            }
        }
    });
}

function renderMonthlyChart(drinks) {
    const today = new Date();
    const year = today.getFullYear();
    const month = today.getMonth();
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    // Group by week of month
    const weeks = [0, 0, 0, 0, 0];
    drinks.forEach(d => {
        const dt = new Date(d.consumedAt);
        if (dt.getMonth() === month && dt.getFullYear() === year) {
            const weekIdx = Math.min(Math.floor((dt.getDate() - 1) / 7), 4);
            weeks[weekIdx] += d.quantity;
        }
    });

    const labels = ['Week 1', 'Week 2', 'Week 3', 'Week 4', 'Week 5'];
    const ctx = document.getElementById('monthlyChart').getContext('2d');
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Drinks',
                data: weeks,
                backgroundColor: '#6366f1',
                borderRadius: 8,
                borderSkipped: false,
                maxBarThickness: 50
            }]
        },
        options: {
            responsive: true, maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                y: { beginAtZero: true, ticks: { stepSize: 1 }, grid: { color: '#f1f5f9' } },
                x: { grid: { display: false } }
            }
        }
    });
}
