/* DUET Meal Admin Panel Frontend Controller */
const API_BASE = (window.location.pathname.includes('/duetmealapi') ? '/duetmealapi' : '') + '/admin/api';

const state = {
    stats: null,
    settings: null,
    users: [],
    rechargeRequests: [],
    cashData: null,
    notices: [],
    menu: null,
    currentTab: 'dashboard',
    selectedCashDate: new Date().toISOString().split('T')[0]
};

// DOM ready
document.addEventListener('DOMContentLoaded', () => {
    lucide.createIcons();
    initApp();
    setupEventListeners();
});

async function apiCall(endpoint, method = 'GET', body = null) {
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        }
    };
    if (body) {
        options.body = JSON.stringify(body);
    }

    try {
        const response = await fetch(`${API_BASE}${endpoint}`, options);
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || data.error || 'Request failed');
        }
        return data.data !== undefined ? data.data : data;
    } catch (err) {
        console.error('API Error:', err);
        showToast(err.message, 'error');
        throw err;
    }
}

function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    const isError = type === 'error';
    toast.className = `flex items-center gap-3 px-4 py-3 rounded-2xl shadow-xl text-sm font-medium transition-all duration-300 transform translate-y-2 opacity-0 ${
        isError ? 'bg-rose-900 text-rose-100 border border-rose-700' : 'bg-[#14582B] text-white border border-emerald-600'
    }`;
    toast.innerHTML = `
        <i data-lucide="${isError ? 'alert-triangle' : 'check-circle-2'}" class="w-5 h-5 flex-shrink-0"></i>
        <span>${message}</span>
    `;

    container.appendChild(toast);
    lucide.createIcons();

    setTimeout(() => {
        toast.classList.remove('translate-y-2', 'opacity-0');
    }, 10);

    setTimeout(() => {
        toast.classList.add('opacity-0', 'translate-y-2');
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

async function initApp() {
    await loadSettings();
    await loadDashboardStats();
    startPeriodicRefresh();
}

function startPeriodicRefresh() {
    // Refresh stats every 30s
    setInterval(() => {
        if (state.currentTab === 'dashboard') {
            loadDashboardStats(false);
        }
    }, 30000);
}

function setupEventListeners() {
    // Navigation Tabs
    document.querySelectorAll('[data-tab-target]').forEach(btn => {
        btn.addEventListener('click', () => {
            const target = btn.getAttribute('data-tab-target');
            switchTab(target);
        });
    });

    // Booking Master Switch Toggle
    const bookingToggle = document.getElementById('bookingSwitch');
    if (bookingToggle) {
        bookingToggle.addEventListener('change', async (e) => {
            const enabled = e.target.checked;
            try {
                const res = await apiCall('/settings/booking-toggle', 'POST', { enabled });
                showToast(res.message);
                updateBookingUI(enabled);
            } catch (err) {
                e.target.checked = !enabled;
            }
        });
    }

    // Cash Date Picker
    const cashDatePicker = document.getElementById('cashDatePicker');
    if (cashDatePicker) {
        cashDatePicker.value = state.selectedCashDate;
        cashDatePicker.addEventListener('change', (e) => {
            state.selectedCashDate = e.target.value;
            loadDailyCash(state.selectedCashDate);
        });
    }

    // User Search Input
    const userSearchInput = document.getElementById('userSearchInput');
    if (userSearchInput) {
        let debounceTimer;
        userSearchInput.addEventListener('input', (e) => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                loadUsers(e.target.value);
            }, 300);
        });
    }
}

function switchTab(tabId) {
    state.currentTab = tabId;

    // Update Nav Buttons
    document.querySelectorAll('[data-tab-target]').forEach(btn => {
        const target = btn.getAttribute('data-tab-target');
        if (target === tabId) {
            btn.classList.add('bg-[#14582B]', 'text-white', 'shadow-md');
            btn.classList.remove('text-[#6B7D69]', 'hover:bg-emerald-50', 'hover:text-[#111827]');
        } else {
            btn.classList.remove('bg-[#14582B]', 'text-white', 'shadow-md');
            btn.classList.add('text-[#6B7D69]', 'hover:bg-emerald-50', 'hover:text-[#111827]');
        }
    });

    // Show/Hide Sections
    document.querySelectorAll('.tab-content').forEach(section => {
        section.classList.add('hidden');
    });

    const activeSection = document.getElementById(`tab-${tabId}`);
    if (activeSection) {
        activeSection.classList.remove('hidden');
    }

    // Fetch tab data
    if (tabId === 'dashboard') loadDashboardStats();
    if (tabId === 'cash') loadDailyCash(state.selectedCashDate);
    if (tabId === 'users') loadUsers();
    if (tabId === 'recharges') loadRechargeRequests();
    if (tabId === 'menu') loadMenu();
    if (tabId === 'notices') loadNotices();
    if (tabId === 'settings') loadSettingsForm();
}

/* ==========================================================================
   DASHBOARD
   ========================================================================== */
async function loadDashboardStats(showLoader = true) {
    try {
        const stats = await apiCall('/stats');
        state.stats = stats;
        renderDashboard(stats);
    } catch (err) {
        console.error(err);
    }
}

function renderDashboard(stats) {
    // 1. Booking Switch State
    const isBookingOn = stats.settings?.bookingServiceEnabled ?? true;
    const bookingSwitch = document.getElementById('bookingSwitch');
    if (bookingSwitch) bookingSwitch.checked = isBookingOn;
    updateBookingUI(isBookingOn);

    // 2. Metrics & KPI
    setText('statTodayMeals', `${stats.today.totalPortions} Portions`);
    setText('statLunchPortions', `${stats.today.lunchBooked} Booked · ${stats.today.lunchConsumed} Consumed`);
    setText('statDinnerPortions', `${stats.today.dinnerBooked} Booked · ${stats.today.dinnerConsumed} Consumed`);

    setText('statTodayCashInflow', `৳ ${formatNumber(stats.today.rechargesCash)}`);
    setText('statTodayMealDeductions', `৳ ${formatNumber(stats.today.mealDeductionsCash)}`);
    setText('statTodayNetShift', `৳ ${formatNumber(stats.today.netCashDifference)}`);

    setText('statTotalUsers', stats.users.total);
    setText('statTeachersCount', `${stats.users.teachers} Teachers`);
    setText('statOfficersCount', `${stats.users.officers} Officers`);
    
    setText('statPendingRechargesBadge', stats.users.pendingRecharges);
    const badgeEl = document.getElementById('rechargeQueueBadge');
    if (badgeEl) {
        badgeEl.textContent = stats.users.pendingRecharges;
        badgeEl.style.display = stats.users.pendingRecharges > 0 ? 'inline-flex' : 'none';
    }

    setText('statTotalWalletFunds', `৳ ${formatNumber(stats.finance.totalWalletFunds)}`);
    setText('statFlatRate', `৳ ${formatNumber(stats.settings.flatMealRate)}`);

    // 3. Recent Bookings Table
    const recentBookingsTbody = document.getElementById('recentBookingsTbody');
    if (recentBookingsTbody) {
        if (!stats.recentBookings || stats.recentBookings.length === 0) {
            recentBookingsTbody.innerHTML = `<tr><td colspan="5" class="py-6 text-center text-sm text-[#6B7D69]">No recent bookings found.</td></tr>`;
        } else {
            recentBookingsTbody.innerHTML = stats.recentBookings.map(b => `
                <tr class="border-b border-gray-100 hover:bg-emerald-50/30 transition-colors">
                    <td class="py-3 px-4">
                        <div class="font-medium text-[#111827] text-sm">${escapeHtml(b.full_name || b.user_id)}</div>
                        <div class="text-xs text-[#6B7D69]">${escapeHtml(b.email || '')}</div>
                    </td>
                    <td class="py-3 px-4 text-xs font-medium text-[#111827]">
                        ${b.start_date} ${b.start_date !== b.end_date ? '→ ' + b.end_date : ''}
                    </td>
                    <td class="py-3 px-4 text-xs">
                        <span class="inline-flex items-center gap-1 font-semibold ${b.include_lunch ? 'text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full' : 'text-gray-400'}">
                            ${b.include_lunch ? 'Lunch' : ''}
                        </span>
                        <span class="inline-flex items-center gap-1 font-semibold ${b.include_dinner ? 'text-indigo-700 bg-indigo-50 px-2 py-0.5 rounded-full' : 'text-gray-400'}">
                            ${b.include_dinner ? 'Dinner' : ''}
                        </span>
                        ${b.guest_count > 0 ? `<span class="text-amber-700 bg-amber-50 px-1.5 py-0.5 rounded-full text-[11px] font-medium">+${b.guest_count} Guest</span>` : ''}
                    </td>
                    <td class="py-3 px-4 text-xs font-semibold text-[#14582B]">৳ ${formatNumber(b.total_cost)}</td>
                    <td class="py-3 px-4">
                        <span class="text-[11px] font-semibold uppercase px-2 py-0.5 rounded-full ${
                            b.status === 'active' ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-800'
                        }">${b.status}</span>
                    </td>
                </tr>
            `).join('');
        }
    }

    // 4. Recent Transactions Table
    const recentTxTbody = document.getElementById('recentTxTbody');
    if (recentTxTbody) {
        if (!stats.recentTransactions || stats.recentTransactions.length === 0) {
            recentTxTbody.innerHTML = `<tr><td colspan="5" class="py-6 text-center text-sm text-[#6B7D69]">No transactions recorded today.</td></tr>`;
        } else {
            recentTxTbody.innerHTML = stats.recentTransactions.map(tx => `
                <tr class="border-b border-gray-100 hover:bg-emerald-50/30 transition-colors">
                    <td class="py-3 px-4">
                        <div class="font-medium text-[#111827] text-sm">${escapeHtml(tx.full_name || tx.user_id)}</div>
                        <div class="text-xs text-[#6B7D69]">${escapeHtml(tx.title)}</div>
                    </td>
                    <td class="py-3 px-4 text-xs font-medium uppercase text-[#6B7D69]">${tx.type.replace('_', ' ')}</td>
                    <td class="py-3 px-4 text-xs font-bold ${tx.sign === 'positive' ? 'text-emerald-600' : 'text-rose-600'}">
                        ${tx.sign === 'positive' ? '+' : '-'}৳ ${formatNumber(tx.amount)}
                    </td>
                    <td class="py-3 px-4 text-xs font-semibold text-[#111827]">৳ ${formatNumber(tx.balance_after)}</td>
                    <td class="py-3 px-4 text-xs text-[#6B7D69]">${formatDateTime(tx.timestamp)}</td>
                </tr>
            `).join('');
        }
    }

    lucide.createIcons();
}

function updateBookingUI(isOn) {
    const banner = document.getElementById('bookingStatusBanner');
    const label = document.getElementById('bookingSwitchLabel');
    if (banner) {
        if (isOn) {
            banner.className = 'flex items-center justify-between p-4 rounded-2xl bg-emerald-50 border border-emerald-200 text-emerald-900 transition-all';
            banner.innerHTML = `
                <div class="flex items-center gap-3">
                    <div class="w-9 h-9 rounded-xl bg-[#14582B] text-white flex items-center justify-center flex-shrink-0">
                        <i data-lucide="check" class="w-5 h-5"></i>
                    </div>
                    <div>
                        <h4 class="font-semibold text-sm text-[#14582B]">Booking Service is Active</h4>
                        <p class="text-xs text-emerald-700">Teachers and officers can freely book daily lunch and dinner meals via the app.</p>
                    </div>
                </div>
                <span class="text-xs font-bold uppercase tracking-wider bg-emerald-200/60 text-[#14582B] px-3 py-1 rounded-full">ONLINE</span>
            `;
        } else {
            banner.className = 'flex items-center justify-between p-4 rounded-2xl bg-amber-50 border border-amber-300 text-amber-900 transition-all';
            banner.innerHTML = `
                <div class="flex items-center gap-3">
                    <div class="w-9 h-9 rounded-xl bg-amber-600 text-white flex items-center justify-center flex-shrink-0">
                        <i data-lucide="pause-circle" class="w-5 h-5"></i>
                    </div>
                    <div>
                        <h4 class="font-semibold text-sm text-amber-900">Booking Service is Temporarily PAUSED</h4>
                        <p class="text-xs text-amber-800">Users cannot submit new meal bookings until you turn this back on.</p>
                    </div>
                </div>
                <span class="text-xs font-bold uppercase tracking-wider bg-amber-200 text-amber-900 px-3 py-1 rounded-full">PAUSED</span>
            `;
        }
        lucide.createIcons();
    }
    if (label) {
        label.textContent = isOn ? 'Booking Service: Active' : 'Booking Service: Paused';
    }
}

/* ==========================================================================
   DAILY CASH & AUDITING
   ========================================================================== */
async function loadDailyCash(date) {
    try {
        const data = await apiCall(`/cash?date=${date}`);
        state.cashData = data;
        renderDailyCash(data);
    } catch (err) {
        console.error(err);
    }
}

function renderDailyCash(data) {
    const s = data.summary;
    setText('cashDateLabel', formatDate(data.queryDate));
    setText('cashTotalRecharges', `৳ ${formatNumber(s.totalCashInflow)}`);
    setText('cashTotalDeductions', `৳ ${formatNumber(s.totalMealDeductions)}`);
    setText('cashNetShift', `৳ ${formatNumber(s.netBalanceShift)}`);
    setText('cashMealCount', `${s.lunchPortions + s.dinnerPortions} Portions (L: ${s.lunchPortions} / D: ${s.dinnerPortions})`);
    setText('cashTxCount', `${s.transactionCount} transactions`);

    const tbody = document.getElementById('dailyCashTransactionsTbody');
    if (tbody) {
        if (!data.transactions || data.transactions.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="py-8 text-center text-sm text-[#6B7D69]">No cash transactions recorded on ${data.queryDate}.</td></tr>`;
        } else {
            tbody.innerHTML = data.transactions.map(tx => `
                <tr class="border-b border-gray-100 hover:bg-emerald-50/30 transition-colors">
                    <td class="py-3.5 px-4">
                        <div class="font-semibold text-sm text-[#111827]">${escapeHtml(tx.full_name || tx.user_id)}</div>
                        <div class="text-xs text-[#6B7D69]">${escapeHtml(tx.email || tx.phone || '')}</div>
                    </td>
                    <td class="py-3.5 px-4 text-xs font-medium uppercase text-[#6B7D69]">${tx.type.replace('_', ' ')}</td>
                    <td class="py-3.5 px-4 text-xs text-[#111827] font-medium">${escapeHtml(tx.title)}</td>
                    <td class="py-3.5 px-4 text-sm font-bold ${tx.sign === 'positive' ? 'text-emerald-600' : 'text-rose-600'}">
                        ${tx.sign === 'positive' ? '+' : '-'}৳ ${formatNumber(tx.amount)}
                    </td>
                    <td class="py-3.5 px-4 text-xs font-semibold text-[#111827]">৳ ${formatNumber(tx.balance_after)}</td>
                    <td class="py-3.5 px-4 text-xs text-[#6B7D69]">${formatDateTime(tx.timestamp)}</td>
                </tr>
            `).join('');
        }
    }

    // Past 7 Days Table
    const past7Tbody = document.getElementById('past7DaysCashTbody');
    if (past7Tbody && data.past7Days) {
        past7Tbody.innerHTML = data.past7Days.map(d => `
            <tr class="border-b border-gray-100 hover:bg-emerald-50/30 cursor-pointer" onclick="selectCashDate('${d.tx_date}')">
                <td class="py-2.5 px-3 text-xs font-semibold text-[#111827]">${formatDate(d.tx_date)}</td>
                <td class="py-2.5 px-3 text-xs font-bold text-emerald-600">+৳ ${formatNumber(d.recharges)}</td>
                <td class="py-2.5 px-3 text-xs font-bold text-rose-600">-৳ ${formatNumber(d.deductions)}</td>
                <td class="py-2.5 px-3 text-xs text-[#6B7D69]">${d.tx_count} tx</td>
            </tr>
        `).join('');
    }
}

function selectCashDate(dateStr) {
    const picker = document.getElementById('cashDatePicker');
    if (picker) picker.value = dateStr;
    state.selectedCashDate = dateStr;
    loadDailyCash(dateStr);
}

/* ==========================================================================
   USER MANAGEMENT
   ========================================================================== */
async function loadUsers(search = '') {
    try {
        const query = search ? `?search=${encodeURIComponent(search)}` : '';
        const res = await apiCall(`/users${query}`);
        state.users = res.users || [];
        renderUsersTable(state.users);
    } catch (err) {
        console.error(err);
    }
}

function renderUsersTable(users) {
    const tbody = document.getElementById('usersTableTbody');
    if (!tbody) return;

    if (!users || users.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="py-10 text-center text-sm text-[#6B7D69]">No users found. Click "Add User" to create one.</td></tr>`;
        return;
    }

    tbody.innerHTML = users.map(u => `
        <tr class="border-b border-gray-100 hover:bg-emerald-50/30 transition-colors">
            <td class="py-3.5 px-4">
                <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-xl bg-[#14582B] text-[#D4AF37] font-bold text-sm flex items-center justify-center flex-shrink-0 shadow-sm">
                        ${u.initials || 'DM'}
                    </div>
                    <div>
                        <div class="font-semibold text-sm text-[#111827]">${escapeHtml(u.fullName)}</div>
                        <div class="text-xs text-[#6B7D69]">${escapeHtml(u.email)}</div>
                    </div>
                </div>
            </td>
            <td class="py-3.5 px-4 text-xs font-medium text-[#111827]">${escapeHtml(u.phone || '—')}</td>
            <td class="py-3.5 px-4">
                <span class="text-xs font-semibold capitalize px-2.5 py-1 rounded-full ${
                    u.userType === 'teacher' ? 'bg-indigo-50 text-indigo-700' : 'bg-amber-50 text-amber-700'
                }">${u.userType}</span>
            </td>
            <td class="py-3.5 px-4">
                <span class="text-xs font-medium capitalize px-2 py-0.5 rounded-full ${
                    u.residentType === 'inside' ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-700'
                }">${u.residentType}</span>
            </td>
            <td class="py-3.5 px-4">
                <div class="font-bold text-sm text-[#14582B]">৳ ${formatNumber(u.wallet.availableBalance)}</div>
                <div class="text-[11px] text-[#6B7D69]">Deposited: ৳ ${formatNumber(u.wallet.totalBalance)}</div>
            </td>
            <td class="py-3.5 px-4 text-xs text-[#6B7D69]">
                ${u.stats?.consumedMeals || 0} meals
            </td>
            <td class="py-3.5 px-4">
                <div class="flex items-center gap-1.5">
                    <button onclick="openAddMoneyModal('${u.id}', '${escapeHtml(u.fullName)}', ${u.wallet.availableBalance})" class="px-2.5 py-1.5 rounded-xl bg-emerald-50 text-[#14582B] hover:bg-emerald-100 text-xs font-semibold flex items-center gap-1">
                        <i data-lucide="plus" class="w-3.5 h-3.5"></i> Add Money
                    </button>
                    <button onclick="openEditUserModal('${u.id}')" class="p-1.5 rounded-xl text-[#6B7D69] hover:bg-gray-100 hover:text-[#111827]">
                        <i data-lucide="edit-3" class="w-4 h-4"></i>
                    </button>
                    <button onclick="confirmDeleteUser('${u.id}', '${escapeHtml(u.fullName)}')" class="p-1.5 rounded-xl text-rose-500 hover:bg-rose-50">
                        <i data-lucide="trash-2" class="w-4 h-4"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');

    lucide.createIcons();
}

function openAddUserModal() {
    document.getElementById('createUserForm').reset();
    openModal('addUserModal');
}

async function handleCreateUser(e) {
    e.preventDefault();
    const formData = new FormData(e.target);
    const body = Object.fromEntries(formData.entries());

    try {
        const user = await apiCall('/users', 'POST', body);
        showToast(`User ${user.fullName} created successfully!`);
        closeModal('addUserModal');
        loadUsers();
        loadDashboardStats(false);
    } catch (err) {}
}

function openAddMoneyModal(userId, fullName, currentBalance) {
    document.getElementById('rechargeUserSelectContainer').classList.add('hidden');
    document.getElementById('rechargeUserId').value = userId;
    document.getElementById('rechargeUserName').textContent = fullName;
    document.getElementById('rechargeUserBalance').textContent = `৳ ${formatNumber(currentBalance)}`;
    document.getElementById('rechargeAmountInput').value = '';
    document.getElementById('rechargeNoteInput').value = 'Direct cash deposit at Canteen counter';
    openModal('addMoneyModal');
}

async function openDirectRechargeModal() {
    // Ensure users list is loaded
    if (!state.users || state.users.length === 0) {
        const res = await apiCall('/users?limit=100');
        state.users = res.users || [];
    }

    const dropdown = document.getElementById('rechargeUserDropdown');
    dropdown.innerHTML = state.users.map(u => `
        <option value="${u.id}" data-name="${escapeHtml(u.fullName)}" data-balance="${u.wallet.availableBalance}">
            ${escapeHtml(u.fullName)} (${u.email || u.phone}) — Current: ৳${formatNumber(u.wallet.availableBalance)}
        </option>
    `).join('');

    document.getElementById('rechargeUserSelectContainer').classList.remove('hidden');

    if (state.users.length > 0) {
        onDirectRechargeUserChange(state.users[0].id);
    }

    document.getElementById('rechargeAmountInput').value = '';
    document.getElementById('rechargeNoteInput').value = 'Direct cash deposit at Canteen counter';
    openModal('addMoneyModal');
}

function onDirectRechargeUserChange(userId) {
    const user = state.users.find(u => u.id === userId);
    if (user) {
        document.getElementById('rechargeUserId').value = user.id;
        document.getElementById('rechargeUserName').textContent = user.fullName;
        document.getElementById('rechargeUserBalance').textContent = `৳ ${formatNumber(user.wallet.availableBalance)}`;
    }
}

async function handleAddMoney(e) {
    e.preventDefault();
    const userId = document.getElementById('rechargeUserId').value;
    const amount = parseFloat(document.getElementById('rechargeAmountInput').value);
    const note = document.getElementById('rechargeNoteInput').value;

    if (!userId) {
        showToast("Please select a valid user.", "error");
        return;
    }

    try {
        const res = await apiCall('/wallet/recharge', 'POST', { userId, amount, note });
        showToast(`Successfully credited ৳${formatNumber(amount)} in cash to ${document.getElementById('rechargeUserName').textContent}'s wallet.`);
        closeModal('addMoneyModal');
        loadUsers();
        loadDashboardStats(false);
    } catch (err) {}
}

async function openEditUserModal(userId) {
    try {
        const user = await apiCall(`/users/${userId}`);
        document.getElementById('editUserId').value = user.id;
        document.getElementById('editFullName').value = user.fullName;
        document.getElementById('editEmail').value = user.email;
        document.getElementById('editPhone').value = user.phone || '';
        document.getElementById('editUserType').value = user.userType;
        document.getElementById('editResidentType').value = user.residentType;
        document.getElementById('editPassword').value = '';
        openModal('editUserModal');
    } catch (err) {}
}

async function handleEditUser(e) {
    e.preventDefault();
    const userId = document.getElementById('editUserId').value;
    const formData = new FormData(e.target);
    const body = Object.fromEntries(formData.entries());

    try {
        await apiCall(`/users/${userId}`, 'PUT', body);
        showToast(`User profile updated successfully.`);
        closeModal('editUserModal');
        loadUsers();
    } catch (err) {}
}

async function confirmDeleteUser(userId, name) {
    if (confirm(`Are you sure you want to permanently delete user "${name}"?\nThis will remove their wallet, meal history and bookings.`)) {
        try {
            await apiCall(`/users/${userId}`, 'DELETE');
            showToast(`User "${name}" deleted.`);
            loadUsers();
            loadDashboardStats(false);
        } catch (err) {}
    }
}

/* ==========================================================================
   RECHARGE REQUESTS
   ========================================================================== */
async function loadRechargeRequests() {
    try {
        const requests = await apiCall('/wallet/requests');
        state.rechargeRequests = requests || [];
        renderRechargeRequests(state.rechargeRequests);
    } catch (err) {
        console.error(err);
    }
}

function renderRechargeRequests(requests) {
    const tbody = document.getElementById('rechargeRequestsTbody');
    if (!tbody) return;

    if (!requests || requests.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="py-8 text-center text-sm text-[#6B7D69]">No recharge requests found.</td></tr>`;
        return;
    }

    tbody.innerHTML = requests.map(r => `
        <tr class="border-b border-gray-100 hover:bg-emerald-50/30 transition-colors">
            <td class="py-3.5 px-4">
                <div class="font-semibold text-sm text-[#111827]">${escapeHtml(r.full_name || r.user_id)}</div>
                <div class="text-xs text-[#6B7D69]">${escapeHtml(r.email || r.phone || '')}</div>
            </td>
            <td class="py-3.5 px-4 text-base font-bold text-[#14582B]">৳ ${formatNumber(r.amount)}</td>
            <td class="py-3.5 px-4 text-xs font-semibold text-[#111827]">৳ ${formatNumber(r.available_balance || 0)}</td>
            <td class="py-3.5 px-4 text-xs text-[#6B7D69]">${escapeHtml(r.admin_note || '—')}</td>
            <td class="py-3.5 px-4">
                <span class="text-xs font-bold uppercase px-2.5 py-1 rounded-full ${
                    r.status === 'pending' ? 'bg-amber-100 text-amber-800' : (r.status === 'approved' ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-800')
                }">${r.status}</span>
            </td>
            <td class="py-3.5 px-4">
                ${r.status === 'pending' ? `
                    <div class="flex items-center gap-2">
                        <button onclick="approveRechargeRequest('${r.id}', ${r.amount})" class="px-3 py-1 rounded-xl bg-[#14582B] text-white hover:bg-emerald-800 text-xs font-semibold">
                            Approve
                        </button>
                        <button onclick="rejectRechargeRequest('${r.id}')" class="px-3 py-1 rounded-xl bg-rose-50 text-rose-700 hover:bg-rose-100 text-xs font-semibold">
                            Reject
                        </button>
                    </div>
                ` : `<span class="text-xs text-[#6B7D69]">${formatDateTime(r.approved_at || r.created_at)}</span>`}
            </td>
        </tr>
    `).join('');
}

async function approveRechargeRequest(reqId, amount) {
    if (confirm(`Approve recharge request for ৳${formatNumber(amount)}?`)) {
        try {
            await apiCall(`/wallet/requests/${reqId}/approve`, 'POST');
            showToast(`Recharge approved and added to user balance.`);
            loadRechargeRequests();
            loadDashboardStats(false);
        } catch (err) {}
    }
}

async function rejectRechargeRequest(reqId) {
    const reason = prompt("Enter reason for rejection:", "Deposit slip not verified");
    if (reason !== null) {
        try {
            await apiCall(`/wallet/requests/${reqId}/reject`, 'POST', { reason });
            showToast(`Recharge request rejected.`);
            loadRechargeRequests();
            loadDashboardStats(false);
        } catch (err) {}
    }
}

/* ==========================================================================
   DAILY MENU & RATES
   ========================================================================== */
async function loadMenu() {
    try {
        const menu = await apiCall('/menu');
        state.menu = menu;
        renderMenuForm(menu);
    } catch (err) {
        console.error(err);
    }
}

function renderMenuForm(menu) {
    document.getElementById('menuLunchItems').value = (menu.lunch?.items || []).join(', ');
    document.getElementById('menuLunchVenue').value = menu.lunch?.venue || 'Main Canteen';
    document.getElementById('menuLunchStart').value = menu.lunch?.startTime || '12:30';
    document.getElementById('menuLunchEnd').value = menu.lunch?.endTime || '14:00';
    document.getElementById('menuLunchCutoff').value = menu.lunch?.cutoffTime || '12:00';

    document.getElementById('menuDinnerItems').value = (menu.dinner?.items || []).join(', ');
    document.getElementById('menuDinnerVenue').value = menu.dinner?.venue || 'Main Canteen';
    document.getElementById('menuDinnerStart').value = menu.dinner?.startTime || '19:30';
    document.getElementById('menuDinnerEnd').value = menu.dinner?.endTime || '21:00';
    document.getElementById('menuDinnerAdvance').value = menu.dinner?.cutoffHoursInAdvance || 24;
}

async function handleSaveMenu(e) {
    e.preventDefault();
    const lunchItems = document.getElementById('menuLunchItems').value.split(',').map(s => s.trim()).filter(Boolean);
    const dinnerItems = document.getElementById('menuDinnerItems').value.split(',').map(s => s.trim()).filter(Boolean);

    const body = {
        date: new Date().toISOString().split('T')[0],
        lunch: {
            venue: document.getElementById('menuLunchVenue').value,
            startTime: document.getElementById('menuLunchStart').value,
            endTime: document.getElementById('menuLunchEnd').value,
            cutoffTime: document.getElementById('menuLunchCutoff').value,
            items: lunchItems
        },
        dinner: {
            venue: document.getElementById('menuDinnerVenue').value,
            startTime: document.getElementById('menuDinnerStart').value,
            endTime: document.getElementById('menuDinnerEnd').value,
            cutoffHoursInAdvance: parseInt(document.getElementById('menuDinnerAdvance').value) || 24,
            items: dinnerItems
        }
    };

    try {
        await apiCall('/menu', 'POST', body);
        showToast("Today's dining menu updated successfully!");
    } catch (err) {}
}

async function handleSetMealRate(e) {
    e.preventDefault();
    const month = document.getElementById('settleMonthInput').value || new Date().toISOString().slice(0, 7);
    const rate = parseFloat(document.getElementById('settleRateInput').value);

    try {
        await apiCall('/meal-rate', 'POST', { month, rate });
        showToast(`Meal rate for ${month} finalized at ৳${formatNumber(rate)}.`);
    } catch (err) {}
}

async function handleRunSettlement() {
    const month = document.getElementById('settleMonthInput').value || new Date().toISOString().slice(0, 7);
    if (confirm(`Run automatic monthly balance settlement for ${month}?\nThis will calculate the difference between standard rate (৳90) and finalized rate, and credit/debit all users automatically.`)) {
        try {
            const res = await apiCall('/settlement/run', 'POST', { month });
            showToast(`Settlement executed! Settled ${res.settledUsers} user accounts.`);
            loadDashboardStats(false);
        } catch (err) {}
    }
}

/* ==========================================================================
   NOTICES
   ========================================================================== */
async function loadNotices() {
    try {
        const notices = await apiCall('/notices');
        state.notices = notices || [];
        renderNotices(state.notices);
    } catch (err) {
        console.error(err);
    }
}

function renderNotices(notices) {
    const container = document.getElementById('noticesGrid');
    if (!container) return;

    if (!notices || notices.length === 0) {
        container.innerHTML = `<div class="col-span-3 py-12 text-center text-sm text-[#6B7D69]">No broadcast notices posted yet.</div>`;
        return;
    }

    container.innerHTML = notices.map(n => `
        <div class="bg-white border border-gray-100 rounded-3xl p-5 shadow-sm hover:shadow-md transition-all flex flex-col justify-between">
            <div>
                <div class="flex items-center justify-between gap-2 mb-3">
                    <span class="text-[11px] font-bold uppercase tracking-wider px-2.5 py-1 rounded-full ${
                        n.tag === 'IMPORTANT' ? 'bg-rose-50 text-rose-700' : (n.tag === 'NEW UPDATE' ? 'bg-emerald-50 text-emerald-700' : 'bg-blue-50 text-blue-700')
                    }">${escapeHtml(n.tag)}</span>
                    <span class="text-xs text-[#6B7D69] capitalize">${escapeHtml(n.category)}</span>
                </div>
                <h4 class="font-bold text-base text-[#111827] mb-2">${escapeHtml(n.title)}</h4>
                <p class="text-sm text-[#6B7D69] leading-relaxed">${escapeHtml(n.content)}</p>
            </div>
            <div class="flex items-center justify-between pt-4 mt-4 border-t border-gray-100 text-xs text-[#6B7D69]">
                <span>${formatDateTime(n.createdAt)}</span>
                <button onclick="confirmDeleteNotice('${n.id}')" class="text-rose-600 hover:text-rose-800 font-semibold flex items-center gap-1">
                    <i data-lucide="trash-2" class="w-3.5 h-3.5"></i> Delete
                </button>
            </div>
        </div>
    `).join('');

    lucide.createIcons();
}

function openCreateNoticeModal() {
    document.getElementById('createNoticeForm').reset();
    openModal('createNoticeModal');
}

async function handleCreateNotice(e) {
    e.preventDefault();
    const formData = new FormData(e.target);
    const body = Object.fromEntries(formData.entries());

    try {
        await apiCall('/notices', 'POST', body);
        showToast("Broadcast notice published!");
        closeModal('createNoticeModal');
        loadNotices();
    } catch (err) {}
}

async function confirmDeleteNotice(noticeId) {
    if (confirm("Are you sure you want to remove this notice?")) {
        try {
            await apiCall(`/notices/${noticeId}`, 'DELETE');
            showToast("Notice deleted.");
            loadNotices();
        } catch (err) {}
    }
}

/* ==========================================================================
   SYSTEM SETTINGS
   ========================================================================== */
async function loadSettings() {
    try {
        const settings = await apiCall('/settings');
        state.settings = settings;
    } catch (err) {}
}

async function loadSettingsForm() {
    try {
        const settings = await apiCall('/settings');
        state.settings = settings;
        document.getElementById('setFlatRate').value = settings.flatMealRate;
        document.getElementById('setFixedRate').value = settings.fixedMealRate;
        document.getElementById('setLunchCutoff').value = settings.lunchCutoffTime;
        document.getElementById('setDinnerAdvance').value = settings.dinnerAdvanceHours;
        document.getElementById('setCanteenName').value = settings.canteenName;
        document.getElementById('setCanteenPhone').value = settings.canteenPhone;
    } catch (err) {}
}

async function handleSaveSettings(e) {
    e.preventDefault();
    const formData = new FormData(e.target);
    const body = Object.fromEntries(formData.entries());

    try {
        await apiCall('/settings', 'POST', body);
        showToast("System dining settings saved.");
        loadDashboardStats(false);
    } catch (err) {}
}

/* ==========================================================================
   MODAL & UTILITY HELPERS
   ========================================================================== */
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('hidden');
        modal.classList.add('flex');
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }
}

function formatNumber(num) {
    return (parseFloat(num) || 0).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
}

function formatDateTime(dtStr) {
    if (!dtStr) return '';
    const dt = new Date(dtStr.replace(' ', 'T'));
    return dt.toLocaleString('en-GB', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' });
}

function setText(elementId, text) {
    const el = document.getElementById(elementId);
    if (el) el.textContent = text;
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}
