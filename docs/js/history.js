// history.js - History page with filters, edit, delete
document.addEventListener('DOMContentLoaded', () => {
    loadUsername();
    fetchHistory();

    document.getElementById('filter-btn').addEventListener('click', fetchHistory);
    document.getElementById('clear-btn').addEventListener('click', () => {
        document.getElementById('filter-from').value = '';
        document.getElementById('filter-to').value = '';
        document.getElementById('filter-type').value = '';
        fetchHistory();
    });

    // Edit modal
    const editModal = document.getElementById('edit-modal');
    document.querySelector('.close-edit-btn').addEventListener('click', () => editModal.style.display = 'none');
    window.addEventListener('click', (e) => { if (e.target === editModal) editModal.style.display = 'none'; });

    document.getElementById('edit-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('edit-id').value;
        const payload = {
            beverageName: document.getElementById('edit-name').value,
            quantity: parseFloat(document.getElementById('edit-qty').value),
            notes: document.getElementById('edit-notes').value
        };
        try {
            const res = await fetch('https://alcohol-tracker-mn9s.onrender.com/api/drinks/' + id, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (res.ok) { editModal.style.display = 'none'; fetchHistory(); }
        } catch (err) { console.error('Error updating:', err); }
    });
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

async function fetchHistory() {
    const from = document.getElementById('filter-from').value;
    const to = document.getElementById('filter-to').value;
    const type = document.getElementById('filter-type').value;

    let url = 'https://alcohol-tracker-mn9s.onrender.com/api/drinks';
    if (from || to || type) {
        url = 'https://alcohol-tracker-mn9s.onrender.com/api/drinks/filter?';
        const params = [];
        if (from) params.push('from=' + from);
        if (to) params.push('to=' + to);
        if (type) params.push('type=' + type);
        url += params.join('&');
    }

    try {
        const res = await fetch(url);
        const drinks = await res.json();
        renderTable(drinks);
    } catch (err) { console.error('Error:', err); }
}

function renderTable(drinks) {
    const tbody = document.getElementById('history-tbody');
    const emptyEl = document.getElementById('empty-history');
    tbody.innerHTML = '';

    if (drinks.length === 0) {
        emptyEl.style.display = 'block';
        return;
    }
    emptyEl.style.display = 'none';

    drinks.forEach(d => {
        const tr = document.createElement('tr');
        const dt = new Date(d.consumedAt);
        const dateStr = dt.toLocaleDateString() + ' ' + dt.toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'});
        const sourceClass = 'source-' + (d.sourceType || 'MANUAL');

        tr.innerHTML =
            '<td>' + dateStr + '</td>' +
            '<td><strong>' + d.beverageName + '</strong></td>' +
            '<td>' + d.quantity + '</td>' +
            '<td><span class="source-badge ' + sourceClass + '">' + (d.sourceType || 'MANUAL') + '</span></td>' +
            '<td>' + (d.notes || '-') + '</td>' +
            '<td><div class="action-btns">' +
            '<button class="btn-edit" onclick="openEdit(' + d.id + ',\'' + d.beverageName.replace(/'/g, "\\'") + '\',' + d.quantity + ',\'' + (d.notes || '').replace(/'/g, "\\'") + '\')"><i class="fa-solid fa-pen"></i></button>' +
            '<button class="btn-delete" onclick="deleteDrink(' + d.id + ')"><i class="fa-solid fa-trash"></i></button>' +
            '</div></td>';
        tbody.appendChild(tr);
    });
}

function openEdit(id, name, qty, notes) {
    document.getElementById('edit-id').value = id;
    document.getElementById('edit-name').value = name;
    document.getElementById('edit-qty').value = qty;
    document.getElementById('edit-notes').value = notes;
    document.getElementById('edit-modal').style.display = 'block';
}

async function deleteDrink(id) {
    if (!confirm('Are you sure you want to delete this drink?')) return;
    try {
        const res = await fetch('https://alcohol-tracker-mn9s.onrender.com/api/drinks/' + id, { method: 'DELETE' });
        if (res.ok) fetchHistory();
    } catch (err) { console.error('Error deleting:', err); }
}
