<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DUET Dining — Admin Control Portal</title>
    
    <!-- Tailwind CSS -->
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        brand: {
                            primary: '#1F7A3B',
                            dark: '#14582B',
                            light: '#E8F4EA',
                            accent: '#D4AF37'
                        },
                        ink: '#111827',
                        muted: '#6B7D69',
                        surface: '#F3F7F1'
                    },
                    fontFamily: {
                        sans: ['Inter', 'sans-serif']
                    }
                }
            }
        }
    </script>
    
    <!-- Google Fonts & Lucide Icons -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>

    <style>
        body { font-family: 'Inter', sans-serif; -webkit-font-smoothing: antialiased; background-color: #F3F7F1; }
        .gradient-card { background: linear-gradient(135deg, #1F7A3B 0%, #14582B 100%); }
        .glass-card { background: rgba(255, 255, 255, 0.92); backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.9); }
        .tab-btn.active { background-color: #14582B; color: #FFFFFF; }
        .custom-switch { position: relative; display: inline-block; width: 52px; height: 28px; }
        .custom-switch input { opacity: 0; width: 0; height: 0; }
        .slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #cbd5e1; transition: .3s; border-radius: 28px; }
        .slider:before { position: absolute; content: ""; height: 22px; width: 22px; left: 3px; bottom: 3px; background-color: white; transition: .3s; border-radius: 50%; box-shadow: 0 2px 4px rgba(0,0,0,0.2); }
        input:checked + .slider { background-color: #14582B; }
        input:checked + .slider:before { transform: translateX(24px); }
        ::-webkit-scrollbar { width: 6px; height: 6px; }
        ::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 4px; }
        ::-webkit-scrollbar-thumb:hover { background: #94a3b8; }
    </style>
</head>
<body class="text-ink flex min-h-screen">

    <!-- Toast Notifications Container -->
    <div id="toastContainer" class="fixed top-5 right-5 z-50 flex flex-col gap-2 pointer-events-none"></div>

    <!-- Sidebar Navigation -->
    <aside class="w-64 bg-white border-r border-gray-200/80 flex flex-col justify-between flex-shrink-0 min-h-screen sticky top-0">
        <div>
            <!-- Brand Header -->
            <!-- Brand Header -->
<div class="p-6 pb-4 border-b border-gray-100 flex items-center gap-3">
    <div class="w-11 h-11 rounded-2xl bg-[#14582B] flex items-center justify-center shadow-md flex-shrink-0 overflow-hidden">
        <img src="assets/logo.png" alt="DUET Dining Logo" class="w-full h-full object-contain">
    </div>

    <div>
        <h1 class="font-bold text-base text-ink tracking-tight">DUET Dining</h1>
        <span class="text-[11px] font-semibold text-muted tracking-wider uppercase">
            Admin Portal
        </span>
    </div>
</div>

            <!-- Master Booking Switch Card -->
            <div class="px-4 py-3 mx-4 my-4 rounded-2xl bg-emerald-50/70 border border-emerald-200/70">
                <div class="flex items-center justify-between">
                    <div>
                        <div id="bookingSwitchLabel" class="text-xs font-bold text-[#14582B]">Booking Service</div>
                        <div class="text-[11px] text-muted mt-0.5">Toggle Live State</div>
                    </div>
                    <label class="custom-switch">
                        <input type="checkbox" id="bookingSwitch" checked>
                        <span class="slider"></span>
                    </label>
                </div>
            </div>

            <!-- Navigation Links -->
            <nav class="px-3 space-y-1">
                <button data-tab-target="dashboard" class="w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-medium text-sm transition-all bg-[#14582B] text-white shadow-md">
                    <i data-lucide="layout-dashboard" class="w-4 h-4"></i> Dashboard
                </button>
                <button data-tab-target="cash" class="w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-medium text-sm transition-all text-muted hover:bg-emerald-50 hover:text-ink">
                    <i data-lucide="wallet-cards" class="w-4 h-4"></i> Daily Cash & Audit
                </button>
                <button data-tab-target="users" class="w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-medium text-sm transition-all text-muted hover:bg-emerald-50 hover:text-ink">
                    <i data-lucide="users" class="w-4 h-4"></i> User Management
                </button>
                <button data-tab-target="recharges" class="w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl font-medium text-sm transition-all text-muted hover:bg-emerald-50 hover:text-ink">
                    <div class="flex items-center gap-3">
                        <i data-lucide="badge-dollar-sign" class="w-4 h-4"></i> Recharges
                    </div>
                    <span id="rechargeQueueBadge" class="hidden text-[10px] font-bold bg-[#14582B] text-white px-2 py-0.5 rounded-full">0</span>
                </button>
                <button data-tab-target="menu" class="w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-medium text-sm transition-all text-muted hover:bg-emerald-50 hover:text-ink">
                    <i data-lucide="utensils" class="w-4 h-4"></i> Menu & Meal Rates
                </button>
                <button data-tab-target="notices" class="w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-medium text-sm transition-all text-muted hover:bg-emerald-50 hover:text-ink">
                    <i data-lucide="megaphone" class="w-4 h-4"></i> Broadcast Notices
                </button>
                <button data-tab-target="settings" class="w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-medium text-sm transition-all text-muted hover:bg-emerald-50 hover:text-ink">
                    <i data-lucide="sliders-horizontal" class="w-4 h-4"></i> Dining Settings
                </button>
            </nav>
        </div>

        <!-- Admin Profile Footer -->
        <div class="p-4 m-4 rounded-2xl bg-gray-50 border border-gray-100 flex items-center justify-between">
            <div class="flex items-center gap-3">
                <div class="w-9 h-9 rounded-xl bg-[#14582B] text-[#D4AF37] font-bold text-xs flex items-center justify-center">
                    CA
                </div>
                <div>
                    <div class="font-semibold text-xs text-ink">Admin Desk</div>
                    <div class="text-[11px] text-muted">DUET Canteen</div>
                </div>
            </div>
            <a href="../test_api.php" target="_blank" title="Open API Tester" class="text-muted hover:text-[#14582B] p-1.5 rounded-lg hover:bg-white transition-colors">
                <i data-lucide="terminal" class="w-4 h-4"></i>
            </a>
        </div>
    </aside>

    <!-- Main Content Area -->
    <main class="flex-1 p-8 overflow-y-auto max-w-7xl mx-auto w-full">

        <!-- Top Header Bar -->
        <header class="flex items-center justify-between pb-6 mb-6 border-b border-gray-200/70">
            <div>
                <p class="text-xs font-semibold text-muted uppercase tracking-wider mb-1">Dhaka University of Engineering & Technology</p>
                <h2 class="text-2xl font-bold text-ink">Central Dining Administration</h2>
            </div>
            <div class="flex items-center gap-3">
                <button onclick="openDirectRechargeModal()" class="px-4 py-2 rounded-full bg-emerald-600 text-white text-xs font-semibold hover:bg-emerald-700 shadow-sm flex items-center gap-2">
                    <i data-lucide="badge-dollar-sign" class="w-3.5 h-3.5"></i> Direct Cash Recharge
                </button>
                <button onclick="openAddUserModal()" class="px-4 py-2 rounded-full bg-white border border-gray-200 text-ink text-xs font-semibold hover:bg-gray-50 shadow-sm flex items-center gap-2">
                    <i data-lucide="user-plus" class="w-3.5 h-3.5 text-[#14582B]"></i> Add User
                </button>
                <button onclick="openCreateNoticeModal()" class="px-4 py-2 rounded-full bg-[#14582B] text-white text-xs font-semibold hover:bg-emerald-900 shadow-sm flex items-center gap-2">
                    <i data-lucide="bell-plus" class="w-3.5 h-3.5"></i> Post Notice
                </button>
            </div>
        </header>

        <!-- ==================================================================== -->
        <!-- TAB 1: DASHBOARD -->
        <!-- ==================================================================== -->
        <section id="tab-dashboard" class="tab-content space-y-6">

            <!-- Live Status Alert Banner -->
            <div id="bookingStatusBanner" class="p-4 rounded-2xl bg-emerald-50 border border-emerald-200 text-emerald-900">
                <!-- Injected dynamically -->
            </div>

            <!-- Metric Cards Grid -->
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                
                <!-- Today's Meals Card (Gradient) -->
                <div class="gradient-card rounded-3xl p-5 text-white shadow-md relative overflow-hidden flex flex-col justify-between">
                    <div>
                        <div class="flex items-center justify-between mb-2">
                            <span class="text-xs font-medium text-white/80">Today's Meal Demand</span>
                            <i data-lucide="utensils" class="w-4 h-4 text-[#D4AF37]"></i>
                        </div>
                        <h3 id="statTodayMeals" class="text-2xl font-bold tracking-tight">0 Portions</h3>
                    </div>
                    <div class="pt-3 border-t border-white/10 text-[11px] text-white/80 space-y-0.5">
                        <div id="statLunchPortions">0 Lunch</div>
                        <div id="statDinnerPortions">0 Dinner</div>
                    </div>
                </div>

                <!-- Today's Cash Inflow -->
                <div class="bg-white border border-gray-100 rounded-3xl p-5 shadow-sm flex flex-col justify-between">
                    <div>
                        <div class="flex items-center justify-between mb-2">
                            <span class="text-xs font-semibold text-muted">Today's Cash Recharges</span>
                            <div class="w-7 h-7 rounded-lg bg-emerald-50 text-[#14582B] flex items-center justify-center">
                                <i data-lucide="arrow-down-left" class="w-4 h-4"></i>
                            </div>
                        </div>
                        <h3 id="statTodayCashInflow" class="text-2xl font-bold text-ink">৳ 0.00</h3>
                    </div>
                    <div class="pt-3 border-t border-gray-50 flex items-center justify-between text-xs">
                        <span class="text-muted">Meal Deductions:</span>
                        <span id="statTodayMealDeductions" class="font-semibold text-rose-600">৳ 0.00</span>
                    </div>
                </div>

                <!-- Total Registered Users -->
                <div class="bg-white border border-gray-100 rounded-3xl p-5 shadow-sm flex flex-col justify-between">
                    <div>
                        <div class="flex items-center justify-between mb-2">
                            <span class="text-xs font-semibold text-muted">Active DUET Staff</span>
                            <div class="w-7 h-7 rounded-lg bg-indigo-50 text-indigo-700 flex items-center justify-center">
                                <i data-lucide="users" class="w-4 h-4"></i>
                            </div>
                        </div>
                        <h3 id="statTotalUsers" class="text-2xl font-bold text-ink">0</h3>
                    </div>
                    <div class="pt-3 border-t border-gray-50 flex items-center justify-between text-xs text-muted">
                        <span id="statTeachersCount">0 Teachers</span>
                        <span>·</span>
                        <span id="statOfficersCount">0 Officers</span>
                    </div>
                </div>

                <!-- Wallet Liquidity / Flat Rate -->
                <div class="bg-white border border-gray-100 rounded-3xl p-5 shadow-sm flex flex-col justify-between">
                    <div>
                        <div class="flex items-center justify-between mb-2">
                            <span class="text-xs font-semibold text-muted">System Wallet Balance</span>
                            <div class="w-7 h-7 rounded-lg bg-amber-50 text-amber-700 flex items-center justify-center">
                                <i data-lucide="coins" class="w-4 h-4"></i>
                            </div>
                        </div>
                        <h3 id="statTotalWalletFunds" class="text-2xl font-bold text-[#14582B]">৳ 0.00</h3>
                    </div>
                    <div class="pt-3 border-t border-gray-50 flex items-center justify-between text-xs text-muted">
                        <span>Meal Rate: <b id="statFlatRate" class="text-ink">৳ 90.00</b></span>
                        <button onclick="switchTab('cash')" class="text-xs font-semibold text-[#14582B] hover:underline">View Cash →</button>
                    </div>
                </div>

            </div>

            <!-- Two Columns: Recent Bookings & Recent Cash Transactions -->
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">

                <!-- Recent Bookings Table Card -->
                <div class="bg-white border border-gray-100 rounded-3xl p-6 shadow-sm">
                    <div class="flex items-center justify-between mb-4">
                        <div class="flex items-center gap-2">
                            <span class="w-1.5 h-4 bg-[#14582B] rounded-full"></span>
                            <h3 class="font-bold text-base text-ink">Live Meal Bookings</h3>
                        </div>
                        <span class="text-xs text-muted">Auto-refreshed</span>
                    </div>
                    <div class="overflow-x-auto">
                        <table class="w-full text-left">
                            <thead>
                                <tr class="text-[11px] font-bold text-muted uppercase border-b border-gray-100 pb-2">
                                    <th class="py-2 px-4">User</th>
                                    <th class="py-2 px-4">Date</th>
                                    <th class="py-2 px-4">Meals</th>
                                    <th class="py-2 px-4">Amount</th>
                                    <th class="py-2 px-4">Status</th>
                                </tr>
                            </thead>
                            <tbody id="recentBookingsTbody">
                                <tr><td colspan="5" class="py-6 text-center text-sm text-muted">Loading bookings...</td></tr>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Recent Transactions Card -->
                <div class="bg-white border border-gray-100 rounded-3xl p-6 shadow-sm">
                    <div class="flex items-center justify-between mb-4">
                        <div class="flex items-center gap-2">
                            <span class="w-1.5 h-4 bg-[#D4AF37] rounded-full"></span>
                            <h3 class="font-bold text-base text-ink">Live Cash Ledger</h3>
                        </div>
                        <button onclick="switchTab('cash')" class="text-xs font-semibold text-[#14582B] hover:underline">Full Audit</button>
                    </div>
                    <div class="overflow-x-auto">
                        <table class="w-full text-left">
                            <thead>
                                <tr class="text-[11px] font-bold text-muted uppercase border-b border-gray-100 pb-2">
                                    <th class="py-2 px-4">User & Detail</th>
                                    <th class="py-2 px-4">Type</th>
                                    <th class="py-2 px-4">Amount</th>
                                    <th class="py-2 px-4">Balance</th>
                                    <th class="py-2 px-4">Time</th>
                                </tr>
                            </thead>
                            <tbody id="recentTxTbody">
                                <tr><td colspan="5" class="py-6 text-center text-sm text-muted">Loading transactions...</td></tr>
                            </tbody>
                        </table>
                    </div>
                </div>

            </div>

        </section>

        <!-- ==================================================================== -->
        <!-- TAB 2: DAILY CASH & AUDIT -->
        <!-- ==================================================================== -->
        <section id="tab-cash" class="tab-content hidden space-y-6">
            
            <!-- Cash Header & Date Picker -->
            <div class="flex flex-wrap items-center justify-between gap-4 bg-white border border-gray-100 rounded-3xl p-6 shadow-sm">
                <div>
                    <h3 class="text-lg font-bold text-ink">Daily Cash Log & Audit</h3>
                    <p class="text-xs text-muted mt-0.5">Audit every single BDT deposited, deducted for meals, or settled.</p>
                </div>
                <div class="flex items-center gap-3">
                    <label class="text-xs font-semibold text-muted">Audit Date:</label>
                    <input type="date" id="cashDatePicker" class="bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs font-semibold text-ink focus:outline-none focus:ring-2 focus:ring-[#14582B]">
                </div>
            </div>

            <!-- Daily Cash Summary Grid -->
            <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div class="bg-white border border-gray-100 rounded-3xl p-5 shadow-sm">
                    <span class="text-xs text-muted font-medium">Cash Collected (Recharges)</span>
                    <h4 id="cashTotalRecharges" class="text-2xl font-bold text-emerald-600 mt-1">৳ 0.00</h4>
                    <p class="text-[11px] text-muted mt-1">Cash deposited by Admin</p>
                </div>

                <div class="bg-white border border-gray-100 rounded-3xl p-5 shadow-sm">
                    <span class="text-xs text-muted font-medium">Meal Revenue Deductions</span>
                    <h4 id="cashTotalDeductions" class="text-2xl font-bold text-rose-600 mt-1">৳ 0.00</h4>
                    <p id="cashMealCount" class="text-[11px] text-muted mt-1">0 Portions</p>
                </div>

                <div class="bg-white border border-gray-100 rounded-3xl p-5 shadow-sm">
                    <span class="text-xs text-muted font-medium">Net Daily Balance Shift</span>
                    <h4 id="cashNetShift" class="text-2xl font-bold text-[#14582B] mt-1">৳ 0.00</h4>
                    <p class="text-[11px] text-muted mt-1">Inflow minus Meal Deductions</p>
                </div>

                <div class="bg-white border border-gray-100 rounded-3xl p-5 shadow-sm">
                    <span class="text-xs text-muted font-medium">Transaction Activity</span>
                    <h4 id="cashTxCount" class="text-2xl font-bold text-ink mt-1">0 tx</h4>
                    <p id="cashDateLabel" class="text-[11px] text-muted mt-1">—</p>
                </div>
            </div>

            <!-- Layout: Detailed Cash Table + 7 Days History Sidebar -->
            <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <!-- Transactions Table -->
                <div class="lg:col-span-2 bg-white border border-gray-100 rounded-3xl p-6 shadow-sm">
                    <div class="flex items-center justify-between mb-4">
                        <h4 class="font-bold text-base text-ink">Cash Movement Ledger</h4>
                        <span class="text-xs text-muted">Complete breakdown</span>
                    </div>
                    <div class="overflow-x-auto">
                        <table class="w-full text-left">
                            <thead>
                                <tr class="text-[11px] font-bold text-muted uppercase border-b border-gray-100 pb-2">
                                    <th class="py-2.5 px-4">User</th>
                                    <th class="py-2.5 px-4">Type</th>
                                    <th class="py-2.5 px-4">Description</th>
                                    <th class="py-2.5 px-4">Amount</th>
                                    <th class="py-2.5 px-4">Balance After</th>
                                    <th class="py-2.5 px-4">Time</th>
                                </tr>
                            </thead>
                            <tbody id="dailyCashTransactionsTbody">
                                <tr><td colspan="6" class="py-8 text-center text-sm text-muted">Loading daily cash ledger...</td></tr>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Past 7 Days Cash Trend -->
                <div class="bg-white border border-gray-100 rounded-3xl p-6 shadow-sm">
                    <h4 class="font-bold text-base text-ink mb-3">Past 7 Days Cash Summary</h4>
                    <p class="text-xs text-muted mb-4">Click any date below to inspect its full cash audit.</p>
                    <table class="w-full text-left">
                        <thead>
                            <tr class="text-[11px] font-bold text-muted uppercase border-b border-gray-100">
                                <th class="py-2 px-3">Date</th>
                                <th class="py-2 px-3">Inflow</th>
                                <th class="py-2 px-3">Meals</th>
                                <th class="py-2 px-3">Tx</th>
                            </tr>
                        </thead>
                        <tbody id="past7DaysCashTbody">
                            <!-- Injected -->
                        </tbody>
                    </table>
                </div>
            </div>

        </section>

        <!-- ==================================================================== -->
        <!-- TAB 3: USER MANAGEMENT -->
        <!-- ==================================================================== -->
        <section id="tab-users" class="tab-content hidden space-y-6">
            
            <!-- Users Control Header -->
            <div class="flex flex-wrap items-center justify-between gap-4 bg-white border border-gray-100 rounded-3xl p-6 shadow-sm">
                <div>
                    <h3 class="text-lg font-bold text-ink">University Staff Directory</h3>
                    <p class="text-xs text-muted mt-0.5">Manage teachers, officers, wallet balances and meal privileges.</p>
                </div>
                <div class="flex items-center gap-3">
                    <div class="relative">
                        <i data-lucide="search" class="w-4 h-4 text-muted absolute left-3 top-2.5"></i>
                        <input type="text" id="userSearchInput" placeholder="Search name, email, phone..." class="bg-gray-50 border border-gray-200 rounded-xl pl-9 pr-4 py-2 text-xs font-medium text-ink w-64 focus:outline-none focus:ring-2 focus:ring-[#14582B]">
                    </div>
                    <button onclick="openAddUserModal()" class="px-4 py-2 rounded-xl bg-[#14582B] text-white text-xs font-semibold hover:bg-emerald-900 shadow-sm flex items-center gap-2">
                        <i data-lucide="plus" class="w-3.5 h-3.5"></i> Add New User
                    </button>
                </div>
            </div>

            <!-- Users Table -->
            <div class="bg-white border border-gray-100 rounded-3xl p-6 shadow-sm">
                <div class="overflow-x-auto">
                    <table class="w-full text-left">
                        <thead>
                            <tr class="text-[11px] font-bold text-muted uppercase border-b border-gray-100 pb-2">
                                <th class="py-3 px-4">Staff Member</th>
                                <th class="py-3 px-4">Phone</th>
                                <th class="py-3 px-4">Role</th>
                                <th class="py-3 px-4">Resident Status</th>
                                <th class="py-3 px-4">Wallet Balance</th>
                                <th class="py-3 px-4">Meals Consumed</th>
                                <th class="py-3 px-4">Actions</th>
                            </tr>
                        </thead>
                        <tbody id="usersTableTbody">
                            <tr><td colspan="7" class="py-8 text-center text-sm text-muted">Loading user accounts...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>

        </section>

        <!-- ==================================================================== -->
        <!-- TAB 4: RECHARGE REQUESTS -->
        <!-- ==================================================================== -->
        <section id="tab-recharges" class="tab-content hidden space-y-6">
            
            <div class="bg-white border border-gray-100 rounded-3xl p-6 shadow-sm flex items-center justify-between">
                <div>
                    <h3 class="text-lg font-bold text-ink">User Recharge Queue</h3>
                    <p class="text-xs text-muted mt-0.5">Review and approve cash deposit requests submitted by teachers and officers.</p>
                </div>
                <button onclick="loadRechargeRequests()" class="px-3 py-1.5 rounded-xl border border-gray-200 text-xs font-semibold text-muted hover:text-ink flex items-center gap-1.5">
                    <i data-lucide="refresh-cw" class="w-3.5 h-3.5"></i> Refresh
                </button>
            </div>

            <div class="bg-white border border-gray-100 rounded-3xl p-6 shadow-sm">
                <div class="overflow-x-auto">
                    <table class="w-full text-left">
                        <thead>
                            <tr class="text-[11px] font-bold text-muted uppercase border-b border-gray-100 pb-2">
                                <th class="py-3 px-4">User</th>
                                <th class="py-3 px-4">Requested Amount</th>
                                <th class="py-3 px-4">Current Balance</th>
                                <th class="py-3 px-4">Admin Note / Slip</th>
                                <th class="py-3 px-4">Status</th>
                                <th class="py-3 px-4">Action</th>
                            </tr>
                        </thead>
                        <tbody id="rechargeRequestsTbody">
                            <tr><td colspan="6" class="py-8 text-center text-sm text-muted">Loading recharge requests...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>

        </section>

        <!-- ==================================================================== -->
        <!-- TAB 5: MENU & RATES -->
        <!-- ==================================================================== -->
        <section id="tab-menu" class="tab-content hidden space-y-6">
            
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">

                <!-- Daily Menu Planner -->
                <div class="bg-white border border-gray-100 rounded-3xl p-6 shadow-sm">
                    <div class="flex items-center gap-2 mb-4">
                        <span class="w-1.5 h-4 bg-[#14582B] rounded-full"></span>
                        <h3 class="font-bold text-base text-ink">Today's Canteen Menu</h3>
                    </div>
                    <form onsubmit="handleSaveMenu(event)" class="space-y-4">
                        <!-- Lunch -->
                        <div class="p-4 rounded-2xl bg-emerald-50/50 border border-emerald-100 space-y-3">
                            <h4 class="font-bold text-xs uppercase text-[#14582B] tracking-wider flex items-center gap-1.5">
                                <i data-lucide="sun" class="w-4 h-4"></i> Lunch Menu Configuration
                            </h4>
                            <div>
                                <label class="block text-xs font-semibold text-muted mb-1">Items (comma-separated)</label>
                                <input type="text" id="menuLunchItems" class="w-full bg-white border border-gray-200 rounded-xl px-3 py-2 text-xs font-medium text-ink focus:ring-2 focus:ring-[#14582B]" placeholder="e.g. Chicken Biryani, Salad, Borhani">
                            </div>
                            <div class="grid grid-cols-2 gap-3">
                                <div>
                                    <label class="block text-xs font-semibold text-muted mb-1">Venue</label>
                                    <input type="text" id="menuLunchVenue" class="w-full bg-white border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink" value="Main Canteen">
                                </div>
                                <div>
                                    <label class="block text-xs font-semibold text-muted mb-1">Lunch Cutoff Time</label>
                                    <input type="text" id="menuLunchCutoff" class="w-full bg-white border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink" value="12:00">
                                </div>
                            </div>
                            <div class="grid grid-cols-2 gap-3">
                                <div>
                                    <label class="block text-xs font-semibold text-muted mb-1">Start Time</label>
                                    <input type="text" id="menuLunchStart" class="w-full bg-white border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink" value="12:30">
                                </div>
                                <div>
                                    <label class="block text-xs font-semibold text-muted mb-1">End Time</label>
                                    <input type="text" id="menuLunchEnd" class="w-full bg-white border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink" value="14:00">
                                </div>
                            </div>
                        </div>

                        <!-- Dinner -->
                        <div class="p-4 rounded-2xl bg-indigo-50/50 border border-indigo-100 space-y-3">
                            <h4 class="font-bold text-xs uppercase text-indigo-800 tracking-wider flex items-center gap-1.5">
                                <i data-lucide="moon" class="w-4 h-4"></i> Dinner Menu Configuration
                            </h4>
                            <div>
                                <label class="block text-xs font-semibold text-muted mb-1">Items (comma-separated)</label>
                                <input type="text" id="menuDinnerItems" class="w-full bg-white border border-gray-200 rounded-xl px-3 py-2 text-xs font-medium text-ink focus:ring-2 focus:ring-[#14582B]" placeholder="e.g. Steamed Rice, Fish Curry, Dal">
                            </div>
                            <div class="grid grid-cols-2 gap-3">
                                <div>
                                    <label class="block text-xs font-semibold text-muted mb-1">Venue</label>
                                    <input type="text" id="menuDinnerVenue" class="w-full bg-white border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink" value="Main Canteen">
                                </div>
                                <div>
                                    <label class="block text-xs font-semibold text-muted mb-1">Guest Advance Hours</label>
                                    <input type="number" id="menuDinnerAdvance" class="w-full bg-white border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink" value="24">
                                </div>
                            </div>
                            <div class="grid grid-cols-2 gap-3">
                                <div>
                                    <label class="block text-xs font-semibold text-muted mb-1">Start Time</label>
                                    <input type="text" id="menuDinnerStart" class="w-full bg-white border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink" value="19:30">
                                </div>
                                <div>
                                    <label class="block text-xs font-semibold text-muted mb-1">End Time</label>
                                    <input type="text" id="menuDinnerEnd" class="w-full bg-white border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink" value="21:00">
                                </div>
                            </div>
                        </div>

                        <button type="submit" class="w-full py-2.5 rounded-xl bg-[#14582B] text-white text-xs font-semibold hover:bg-emerald-900 shadow-sm flex items-center justify-center gap-2">
                            <i data-lucide="save" class="w-4 h-4"></i> Save Today's Menu
                        </button>
                    </form>
                </div>

                <!-- Monthly Settlement & Meal Rate Finalization -->
                <div class="space-y-6">
                    <div class="bg-white border border-gray-100 rounded-3xl p-6 shadow-sm">
                        <div class="flex items-center gap-2 mb-4">
                            <span class="w-1.5 h-4 bg-[#D4AF37] rounded-full"></span>
                            <h3 class="font-bold text-base text-ink">Monthly Settlement & Rate Finalization</h3>
                        </div>
                        <p class="text-xs text-muted leading-relaxed mb-4">
                            At the end of each month, university meal costs are audited. Set the calculated rate (e.g. ৳88.10) and execute balance adjustment to credit refunds for all consumed meals automatically.
                        </p>
                        
                        <form onsubmit="handleSetMealRate(event)" class="space-y-4">
                            <div class="grid grid-cols-2 gap-3">
                                <div>
                                    <label class="block text-xs font-semibold text-muted mb-1">Target Month</label>
                                    <input type="month" id="settleMonthInput" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs font-semibold text-ink">
                                </div>
                                <div>
                                    <label class="block text-xs font-semibold text-muted mb-1">Finalized Rate (৳)</label>
                                    <input type="number" step="0.10" id="settleRateInput" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs font-bold text-[#14582B]" placeholder="88.10" required>
                                </div>
                            </div>
                            <div class="flex gap-3">
                                <button type="submit" class="flex-1 py-2 rounded-xl bg-white border border-gray-200 text-ink text-xs font-semibold hover:bg-gray-50">
                                    Save Final Rate
                                </button>
                                <button type="button" onclick="handleRunSettlement()" class="flex-1 py-2 rounded-xl bg-[#14582B] text-white text-xs font-semibold hover:bg-emerald-900 shadow-sm flex items-center justify-center gap-1.5">
                                    <i data-lucide="zap" class="w-3.5 h-3.5 text-[#D4AF37]"></i> Run Settlement
                                </button>
                            </div>
                        </form>
                    </div>

                    <!-- Meal Rate Info Alert -->
                    <div class="p-5 rounded-3xl bg-amber-50/70 border border-amber-200/70 text-amber-900 text-xs leading-relaxed">
                        <div class="font-bold mb-1 flex items-center gap-1.5 text-amber-950">
                            <i data-lucide="info" class="w-4 h-4"></i> Settlement Calculation Formula
                        </div>
                        <p class="text-amber-800">
                            Refund per Meal = <b>Standard Rate (৳90.00) − Final Rate</b>.<br>
                            If Final Rate is ৳88.10, each user receives a <b>৳1.90 refund</b> per portion directly into their Canteen Wallet.
                        </p>
                    </div>
                </div>

            </div>

        </section>

        <!-- ==================================================================== -->
        <!-- TAB 6: NOTICES -->
        <!-- ==================================================================== -->
        <section id="tab-notices" class="tab-content hidden space-y-6">
            
            <div class="flex items-center justify-between bg-white border border-gray-100 rounded-3xl p-6 shadow-sm">
                <div>
                    <h3 class="text-lg font-bold text-ink">Broadcast Notice Board</h3>
                    <p class="text-xs text-muted mt-0.5">Publish bulletins, rate finalization announcements, or maintenance notices to all users.</p>
                </div>
                <button onclick="openCreateNoticeModal()" class="px-4 py-2 rounded-xl bg-[#14582B] text-white text-xs font-semibold hover:bg-emerald-900 shadow-sm flex items-center gap-2">
                    <i data-lucide="plus" class="w-3.5 h-3.5"></i> Create Notice
                </button>
            </div>

            <!-- Notices Grid -->
            <div id="noticesGrid" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <!-- Injected dynamically -->
            </div>

        </section>

        <!-- ==================================================================== -->
        <!-- TAB 7: SETTINGS -->
        <!-- ==================================================================== -->
        <section id="tab-settings" class="tab-content hidden space-y-6">
            
            <div class="max-w-2xl bg-white border border-gray-100 rounded-3xl p-6 shadow-sm">
                <h3 class="text-lg font-bold text-ink mb-1">Global Canteen Dining Settings</h3>
                <p class="text-xs text-muted mb-6">Configure system-wide parameters and operational defaults.</p>

                <form onsubmit="handleSaveSettings(event)" class="space-y-4">
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs font-semibold text-muted mb-1">Flat Meal Rate (৳)</label>
                            <input type="number" step="0.50" id="setFlatRate" name="flatMealRate" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs font-bold text-[#14582B]" value="90.00" required>
                        </div>
                        <div>
                            <label class="block text-xs font-semibold text-muted mb-1">Fixed Meal Rate (৳)</label>
                            <input type="number" step="0.50" id="setFixedRate" name="fixedMealRate" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs font-bold text-[#14582B]" value="90.00" required>
                        </div>
                    </div>

                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs font-semibold text-muted mb-1">Lunch Cutoff Time</label>
                            <input type="text" id="setLunchCutoff" name="lunchCutoffTime" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink" value="12:00:00" required>
                        </div>
                        <div>
                            <label class="block text-xs font-semibold text-muted mb-1">Dinner Advance Hours</label>
                            <input type="number" id="setDinnerAdvance" name="dinnerAdvanceHours" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink" value="24" required>
                        </div>
                    </div>

                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs font-semibold text-muted mb-1">Canteen Facility Name</label>
                            <input type="text" id="setCanteenName" name="canteenName" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink" value="DUET Main Canteen">
                        </div>
                        <div>
                            <label class="block text-xs font-semibold text-muted mb-1">Helpdesk Contact</label>
                            <input type="text" id="setCanteenPhone" name="canteenPhone" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink" value="+880 1700 000000">
                        </div>
                    </div>

                    <div class="pt-4 border-t border-gray-100 flex justify-end">
                        <button type="submit" class="px-6 py-2.5 rounded-xl bg-[#14582B] text-white text-xs font-semibold hover:bg-emerald-900 shadow-sm flex items-center gap-2">
                            <i data-lucide="check" class="w-4 h-4"></i> Save Settings
                        </button>
                    </div>
                </form>
            </div>

        </section>

    </main>

    <!-- ==================================================================== -->
    <!-- MODAL: ADD USER -->
    <!-- ==================================================================== -->
    <div id="addUserModal" class="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm hidden items-center justify-center p-4">
        <div class="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl space-y-4 border border-gray-100">
            <div class="flex items-center justify-between pb-3 border-b border-gray-100">
                <h3 class="font-bold text-base text-ink">Add New Staff Member</h3>
                <button onclick="closeModal('addUserModal')" class="text-muted hover:text-ink p-1 rounded-lg">
                    <i data-lucide="x" class="w-4 h-4"></i>
                </button>
            </div>
            <form id="createUserForm" onsubmit="handleCreateUser(event)" class="space-y-3">
                <div>
                    <label class="block text-xs font-semibold text-muted mb-1">Full Name (e.g. Dr. Fazlul Hasan)</label>
                    <input type="text" name="fullName" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink focus:ring-2 focus:ring-[#14582B]" required>
                </div>
                <div>
                    <label class="block text-xs font-semibold text-muted mb-1">DUET Email</label>
                    <input type="email" name="email" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink focus:ring-2 focus:ring-[#14582B]" placeholder="name@duet.edu.bd" required>
                </div>
                <div class="grid grid-cols-2 gap-3">
                    <div>
                        <label class="block text-xs font-semibold text-muted mb-1">Password</label>
                        <input type="password" name="password" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink" placeholder="Default: password123">
                    </div>
                    <div>
                        <label class="block text-xs font-semibold text-muted mb-1">Phone Number</label>
                        <input type="text" name="phone" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink" placeholder="+880 1234...">
                    </div>
                </div>
                <div class="grid grid-cols-2 gap-3">
                    <div>
                        <label class="block text-xs font-semibold text-muted mb-1">User Role</label>
                        <select name="userType" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink">
                            <option value="teacher">Teacher</option>
                            <option value="officer">Officer</option>
                        </select>
                    </div>
                    <div>
                        <label class="block text-xs font-semibold text-muted mb-1">Resident Type</label>
                        <select name="residentType" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink">
                            <option value="outside">Outside Campus</option>
                            <option value="inside">Inside Campus</option>
                        </select>
                    </div>
                </div>
                <div>
                    <label class="block text-xs font-semibold text-muted mb-1">Initial Canteen Deposit (৳)</label>
                    <input type="number" step="10" name="initialBalance" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs font-bold text-[#14582B]" value="1000.00">
                </div>
                <div class="pt-3 border-t border-gray-100 flex justify-end gap-2">
                    <button type="button" onclick="closeModal('addUserModal')" class="px-4 py-2 rounded-xl text-xs font-semibold text-muted hover:bg-gray-100">Cancel</button>
                    <button type="submit" class="px-4 py-2 rounded-xl bg-[#14582B] text-white text-xs font-semibold hover:bg-emerald-900">Create Account</button>
                </div>
            </form>
        </div>
    </div>

    <!-- ==================================================================== -->
    <!-- MODAL: DIRECT CASH RECHARGE (ADD MONEY) -->
    <!-- ==================================================================== -->
    <div id="addMoneyModal" class="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm hidden items-center justify-center p-4">
        <div class="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl space-y-4 border border-gray-100">
            <div class="flex items-center justify-between pb-3 border-b border-gray-100">
                <div class="flex items-center gap-2">
                    <div class="w-8 h-8 rounded-xl bg-emerald-50 text-[#14582B] flex items-center justify-center">
                        <i data-lucide="badge-dollar-sign" class="w-4 h-4"></i>
                    </div>
                    <div>
                        <h3 class="font-bold text-base text-ink">Direct Cash Recharge</h3>
                        <p class="text-[11px] text-muted">Credit user wallet via manual cash collection</p>
                    </div>
                </div>
                <button onclick="closeModal('addMoneyModal')" class="text-muted hover:text-ink p-1 rounded-lg">
                    <i data-lucide="x" class="w-4 h-4"></i>
                </button>
            </div>
            <form onsubmit="handleAddMoney(event)" class="space-y-3">
                <input type="hidden" id="rechargeUserId">
                
                <!-- User Selector (shown when triggered from top bar) -->
                <div id="rechargeUserSelectContainer" class="hidden">
                    <label class="block text-xs font-semibold text-muted mb-1">Select Staff Member</label>
                    <select id="rechargeUserDropdown" onchange="onDirectRechargeUserChange(this.value)" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink focus:ring-2 focus:ring-[#14582B]">
                        <!-- Populated dynamically -->
                    </select>
                </div>

                <!-- Crediting Summary Card -->
                <div class="p-3.5 rounded-2xl bg-emerald-50/70 border border-emerald-100 text-xs">
                    <div class="flex items-center justify-between">
                        <span class="text-muted">Target User:</span>
                        <span class="inline-flex items-center gap-1 font-bold text-[11px] text-emerald-800 bg-emerald-100 px-2 py-0.5 rounded-full">
                            <i data-lucide="banknote" class="w-3 h-3"></i> CASH RECHARGE
                        </span>
                    </div>
                    <div id="rechargeUserName" class="font-bold text-sm text-[#14582B] mt-1">—</div>
                    <div class="text-muted mt-1">Available Spendable Balance: <b id="rechargeUserBalance" class="text-ink">৳ 0.00</b></div>
                </div>

                <div>
                    <label class="block text-xs font-semibold text-muted mb-1">Cash Amount to Credit (৳)</label>
                    <input type="number" step="10" id="rechargeAmountInput" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-base font-bold text-[#14582B] focus:ring-2 focus:ring-[#14582B]" placeholder="e.g. 500, 1000" required>
                </div>

                <div>
                    <label class="block text-xs font-semibold text-muted mb-1">Audit Note</label>
                    <input type="text" id="rechargeNoteInput" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink" value="Direct cash deposit at Canteen counter">
                </div>

                <div class="pt-3 border-t border-gray-100 flex justify-end gap-2">
                    <button type="button" onclick="closeModal('addMoneyModal')" class="px-4 py-2 rounded-xl text-xs font-semibold text-muted hover:bg-gray-100">Cancel</button>
                    <button type="submit" class="px-5 py-2 rounded-xl bg-[#14582B] text-white text-xs font-semibold hover:bg-emerald-900 shadow-sm flex items-center gap-1.5">
                        <i data-lucide="check" class="w-3.5 h-3.5"></i> Confirm Cash Deposit
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- ==================================================================== -->
    <!-- MODAL: EDIT USER -->
    <!-- ==================================================================== -->
    <div id="editUserModal" class="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm hidden items-center justify-center p-4">
        <div class="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl space-y-4 border border-gray-100">
            <div class="flex items-center justify-between pb-3 border-b border-gray-100">
                <h3 class="font-bold text-base text-ink">Edit Staff Profile</h3>
                <button onclick="closeModal('editUserModal')" class="text-muted hover:text-ink p-1 rounded-lg">
                    <i data-lucide="x" class="w-4 h-4"></i>
                </button>
            </div>
            <form onsubmit="handleEditUser(event)" class="space-y-3">
                <input type="hidden" id="editUserId">
                <div>
                    <label class="block text-xs font-semibold text-muted mb-1">Full Name</label>
                    <input type="text" id="editFullName" name="fullName" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink" required>
                </div>
                <div>
                    <label class="block text-xs font-semibold text-muted mb-1">Email</label>
                    <input type="email" id="editEmail" name="email" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink" required>
                </div>
                <div class="grid grid-cols-2 gap-3">
                    <div>
                        <label class="block text-xs font-semibold text-muted mb-1">Phone</label>
                        <input type="text" id="editPhone" name="phone" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink">
                    </div>
                    <div>
                        <label class="block text-xs font-semibold text-muted mb-1">New Password</label>
                        <input type="password" id="editPassword" name="password" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink" placeholder="Leave blank to keep">
                    </div>
                </div>
                <div class="grid grid-cols-2 gap-3">
                    <div>
                        <label class="block text-xs font-semibold text-muted mb-1">User Role</label>
                        <select id="editUserType" name="userType" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink">
                            <option value="teacher">Teacher</option>
                            <option value="officer">Officer</option>
                        </select>
                    </div>
                    <div>
                        <label class="block text-xs font-semibold text-muted mb-1">Resident Type</label>
                        <select id="editResidentType" name="residentType" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink">
                            <option value="outside">Outside Campus</option>
                            <option value="inside">Inside Campus</option>
                        </select>
                    </div>
                </div>
                <div class="pt-3 border-t border-gray-100 flex justify-end gap-2">
                    <button type="button" onclick="closeModal('editUserModal')" class="px-4 py-2 rounded-xl text-xs font-semibold text-muted hover:bg-gray-100">Cancel</button>
                    <button type="submit" class="px-4 py-2 rounded-xl bg-[#14582B] text-white text-xs font-semibold hover:bg-emerald-900">Save Changes</button>
                </div>
            </form>
        </div>
    </div>

    <!-- ==================================================================== -->
    <!-- MODAL: CREATE NOTICE -->
    <!-- ==================================================================== -->
    <div id="createNoticeModal" class="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm hidden items-center justify-center p-4">
        <div class="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl space-y-4 border border-gray-100">
            <div class="flex items-center justify-between pb-3 border-b border-gray-100">
                <h3 class="font-bold text-base text-ink">Publish Broadcast Bulletin</h3>
                <button onclick="closeModal('createNoticeModal')" class="text-muted hover:text-ink p-1 rounded-lg">
                    <i data-lucide="x" class="w-4 h-4"></i>
                </button>
            </div>
            <form id="createNoticeForm" onsubmit="handleCreateNotice(event)" class="space-y-3">
                <div class="grid grid-cols-2 gap-3">
                    <div>
                        <label class="block text-xs font-semibold text-muted mb-1">Category</label>
                        <select name="category" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink">
                            <option value="dining">Dining</option>
                            <option value="academic">Academic</option>
                            <option value="maintenance">Maintenance</option>
                        </select>
                    </div>
                    <div>
                        <label class="block text-xs font-semibold text-muted mb-1">Badge Tag</label>
                        <select name="tag" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs text-ink">
                            <option value="IMPORTANT">IMPORTANT</option>
                            <option value="NEW UPDATE">NEW UPDATE</option>
                            <option value="SYSTEM INFO">SYSTEM INFO</option>
                        </select>
                    </div>
                </div>
                <div>
                    <label class="block text-xs font-semibold text-muted mb-1">Notice Title</label>
                    <input type="text" name="title" class="w-full bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-xs font-semibold text-ink" placeholder="e.g. August Meal Rate Finalized" required>
                </div>
                <div>
                    <label class="block text-xs font-semibold text-muted mb-1">Notice Announcement Content</label>
                    <textarea name="content" rows="4" class="w-full bg-gray-50 border border-gray-200 rounded-xl p-3 text-xs text-ink focus:ring-2 focus:ring-[#14582B]" placeholder="Write announcement details..." required></textarea>
                </div>
                <div class="pt-3 border-t border-gray-100 flex justify-end gap-2">
                    <button type="button" onclick="closeModal('createNoticeModal')" class="px-4 py-2 rounded-xl text-xs font-semibold text-muted hover:bg-gray-100">Cancel</button>
                    <button type="submit" class="px-4 py-2 rounded-xl bg-[#14582B] text-white text-xs font-semibold hover:bg-emerald-900">Broadcast Notice</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Frontend Script -->
    <script src="assets/js/admin.js"></script>
</body>
</html>
