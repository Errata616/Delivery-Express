/*
 * admin.js - dashboard logic shared by admin/dashboard.jsp and manager/dashboard.jsp.
 *  - loads statistics
 *  - lists unassigned shipments and assigns a shipper
 *  - lists shippers and (admin page only) managers, and creates staff accounts
 * Allowed roles come from <body data-allowed-roles="...">.
 */
(function () {
	"use strict";

	var ctx = window.APP_CONTEXT_PATH || "";
	var allowed = (document.body.getAttribute("data-allowed-roles") || "ADMIN")
		.split(",").map(function (s) { return s.trim(); });

	var user = window.Auth.requireRole(allowed);
	if (!user) { return; }
	document.body.hidden = false;

	var state = { shippers: [], managers: [], currentShipments: [], deletable: [] };

	var STATUS_LABELS = {
		READY: "Chờ phân công",
		ASSIGNED: "Đã phân công",
		PICKED_UP: "Đã lấy hàng",
		DELIVERING: "Đang giao",
		DELIVERED: "Đã giao",
		FAILED: "Giao thất bại",
		CANCELLED: "Đã hủy"
	};

	var ORDER_LABELS = {
		PENDING: "Chờ xác nhận",
		CONFIRMED: "Đã xác nhận",
		SHIPPING: "Đang vận chuyển",
		DELIVERED: "Đã giao",
		CANCELLED: "Đã hủy",
		FAILED: "Thất bại"
	};

	var ROLE_LABELS = {
		ADMIN: "Quản trị viên",
		MANAGER: "Quản lý",
		SHIPPER: "Shipper",
		USER: "Người dùng",
		VENDOR: "Đối tác"
	};

	// ---------- helpers ----------

	function $(id) { return document.getElementById(id); }

	function esc(value) {
		return String(value === null || value === undefined ? "" : value)
			.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
			.replace(/"/g, "&quot;").replace(/'/g, "&#39;");
	}

	function money(value) {
		var number = Number(value || 0);
		return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(number);
	}

	function fmtDate(value) {
		if (!value) { return "-"; }
		var date = Array.isArray(value)
			? new Date(value[0], value[1] - 1, value[2], value[3] || 0, value[4] || 0)
			: new Date(value);
		return isNaN(date.getTime()) ? "-" : date.toLocaleString("vi-VN");
	}

	function api(path, options) {
		return window.apiData(ctx + path, options);
	}

	var alertTimer = null;

	function showAlert(type, message) {
		var box = $("alertBox");
		if (!box) { return; }
		box.innerHTML = "";
		var div = document.createElement("div");
		div.className = "alert alert-" + type + " alert-dismissible fade show";
		div.setAttribute("role", "alert");
		div.appendChild(document.createTextNode(message));
		var close = document.createElement("button");
		close.type = "button";
		close.className = "btn-close";
		close.setAttribute("data-bs-dismiss", "alert");
		close.setAttribute("aria-label", "Đóng");
		div.appendChild(close);
		box.appendChild(div);
		clearTimeout(alertTimer);
		alertTimer = setTimeout(function () { box.innerHTML = ""; }, 6000);
	}

	function setText(id, text) {
		var el = $(id);
		if (el) { el.textContent = text; }
	}

	// ---------- statistics ----------

	function renderStats(stats) {
		setText("statTotalShipments", String(stats.totalShipments || 0));
		setText("statDeliveredToday", String(stats.deliveredToday || 0));
		setText("statRevenue", money(stats.deliveredShippingRevenue));
		setText("statPendingCod", money(stats.pendingCodAmount));
		setText("statCustomers", String(stats.totalCustomers || 0));
		if (user.role !== "MANAGER") {
			setText("statShippers", String(stats.totalShippers || 0));
		}
		setText("statTotalOrders", String(stats.totalOrders || 0));

		var shipmentBox = $("shipmentStatusList");
		if (shipmentBox) {
			var byStatus = stats.shipmentsByStatus || {};
			shipmentBox.innerHTML = Object.keys(STATUS_LABELS).map(function (key) {
				return '<li class="list-group-item d-flex justify-content-between">' +
					"<span>" + esc(STATUS_LABELS[key]) + "</span>" +
					'<span class="badge bg-primary rounded-pill">' + esc(byStatus[key] || 0) + "</span></li>";
			}).join("");
		}

		var orderBox = $("orderStatusList");
		if (orderBox) {
			var byOrder = stats.ordersByStatus || {};
			orderBox.innerHTML = Object.keys(ORDER_LABELS).map(function (key) {
				return '<li class="list-group-item d-flex justify-content-between">' +
					"<span>" + esc(ORDER_LABELS[key]) + "</span>" +
					'<span class="badge bg-secondary rounded-pill">' + esc(byOrder[key] || 0) + "</span></li>";
			}).join("");
		}
	}

	async function loadStats() {
		try {
			renderStats(await api("/api/statistics"));
		} catch (error) {
			showAlert("danger", "Không tải được thống kê: " + error.message);
		}
	}

	// ---------- shippers ----------

	function activeShippers() {
		return state.shippers.filter(function (s) { return s.enabled !== false && s.available !== false; });
	}

	function renderShippers() {
		var body = $("shipperBody");
		if (!body) { return; }
		var isAdmin = user.role === "ADMIN";
		if (!state.shippers.length) {
			body.innerHTML = '<tr><td colspan="' + (isAdmin ? "6" : "4") +
				'" class="text-center text-muted">Chưa có shipper nào</td></tr>';
			return;
		}
		body.innerHTML = state.shippers.map(function (s) {
			var managerCell = "";
			if (isAdmin) {
				var options = '<option value="">-- Chưa phân công --</option>' + state.managers.map(function (manager) {
					return '<option value="' + esc(manager.id) + '"' +
						(manager.id === s.managerId ? " selected" : "") +
						(manager.enabled === false ? " disabled" : "") + ">" +
						esc(manager.fullName || manager.username) +
						(manager.enabled === false ? " (đã khóa)" : "") + "</option>";
				}).join("");
				managerCell = '<td><div class="input-group input-group-sm">' +
					'<select class="form-select manager-select" data-shipper-id="' + esc(s.id) + '">' +
					options + "</select>" +
					'<button type="button" class="btn btn-outline-primary" data-manager-save="' +
					esc(s.id) + '">Lưu</button></div></td>';
			}
			return "<tr><td>" + esc(s.id) + "</td><td>" + esc(s.fullName || s.username) + "</td>" +
				"<td>" + esc(s.phone || "-") + "</td>" +
				"<td>" + (s.enabled === false || s.available === false
					? '<span class="badge bg-secondary">Không hoạt động</span>'
					: '<span class="badge bg-success">Hoạt động</span>') + "</td>" + managerCell +
				(isAdmin
					? '<td class="text-end text-nowrap"><button type="button" class="btn btn-sm btn-outline-secondary me-1" data-shipper-account-enabled="' +
					esc(s.id) + '" data-enabled="' + (s.enabled === false ? "true" : "false") + '">' +
					(s.enabled === false ? "Mở khóa tài khoản" : "Khóa tài khoản") + '</button><button type="button" class="btn btn-sm btn-outline-danger" data-delete-user="' +
					esc(s.id) + '">Xóa</button></td>'
					: "") + "</tr>";
		}).join("");
	}

	async function loadShippers() {
		try {
			state.shippers = (await api("/api/admin/shipments/shippers")) || [];
			if (user.role === "MANAGER") {
				setText("statShippers", String(state.shippers.length));
			}
		} catch (error) {
			state.shippers = [];
			if (user.role === "MANAGER") {
				setText("statShippers", "-");
			}
			showAlert("danger", "Không tải được danh sách shipper: " + error.message);
		}
		renderShippers();
		renderManagers(state.managers);
	}

	// ---------- managers (admin page only) ----------

	function renderManagers(managers) {
		var body = $("managerBody");
		if (!body) { return; }
		if (!managers.length) {
			body.innerHTML = '<tr><td colspan="7" class="text-center text-muted">Chưa có manager nào</td></tr>';
			return;
		}
		body.innerHTML = managers.map(function (manager) {
			var shipperCount = state.shippers.filter(function (shipper) {
				return shipper.managerId === manager.id;
			}).length;
			return "<tr><td>" + esc(manager.id) + "</td>" +
				"<td>" + esc(manager.fullName || "-") + "</td>" +
				"<td>" + esc(manager.username || "-") + "</td>" +
				"<td>" + esc(manager.email || "-") + "</td>" +
				"<td>" + esc(manager.phone || "-") + "</td>" +
				"<td>" + shipperCount + "</td>" +
				'<td class="text-end"><button type="button" class="btn btn-sm btn-outline-danger" data-delete-user="' +
				esc(manager.id) + '">Xóa</button></td></tr>';
		}).join("");
	}

	function renderUserAccounts(accounts) {
		var body = $("userAccountBody");
		if (!body) { return; }
		if (!accounts.length) {
			body.innerHTML = '<tr><td colspan="7" class="text-center text-muted">Chưa có tài khoản người dùng hoặc đối tác</td></tr>';
			return;
		}
		body.innerHTML = accounts.map(function (account) {
			return "<tr><td>" + esc(account.id) + "</td>" +
				"<td>" + esc(account.fullName || "-") + "</td>" +
				"<td>" + esc(account.username || "-") + "</td>" +
				"<td>" + esc(account.email || "-") + "</td>" +
				"<td>" + esc(ROLE_LABELS[account.role] || account.role) + "</td>" +
				"<td>" + (account.enabled
					? '<span class="badge bg-success">Hoạt động</span>'
					: '<span class="badge bg-secondary">Đã khóa</span>') + "</td>" +
				'<td class="text-end"><button type="button" class="btn btn-sm btn-outline-danger" data-delete-user="' +
				esc(account.id) + '">Xóa</button></td></tr>';
		}).join("");
	}

	async function loadManagers() {
		var body = $("managerBody");
		if (!body) { return; }
		try {
			var users = (await api("/api/admin/users")) || [];
			state.managers = users.filter(function (account) { return account.role === "MANAGER"; });
			var otherAccounts = users.filter(function (account) {
				return account.role === "USER" || account.role === "VENDOR";
			});
			renderManagers(state.managers);
			renderUserAccounts(otherAccounts);
			renderShippers();
		} catch (error) {
			body.innerHTML = '<tr><td colspan="7" class="text-center text-danger">Không tải được danh sách manager</td></tr>';
			var accountsBody = $("userAccountBody");
			if (accountsBody) {
				accountsBody.innerHTML = '<tr><td colspan="7" class="text-center text-danger">Không tải được danh sách tài khoản</td></tr>';
			}
			showAlert("danger", "Không tải được danh sách manager: " + error.message);
		}
	}

	// ---------- current shipments ----------

	function renderCurrentShipments() {
		var body = $("currentShipmentsBody");
		if (!body) { return; }

		setText("currentShipmentCount", String(state.currentShipments.length));

		if (!state.currentShipments.length) {
			body.innerHTML = '<tr><td colspan="9" class="text-center text-muted">Không có vận đơn hiện tại</td></tr>';
			return;
		}

		var shippers = activeShippers();
		var options = shippers.length
			? '<option value="">-- Chọn shipper --</option>' + shippers.map(function (s) {
				return '<option value="' + esc(s.id) + '">' + esc(s.fullName || s.username) + "</option>";
			}).join("")
			: '<option value="">Chưa có shipper</option>';

		body.innerHTML = state.currentShipments.map(function (s) {
			var area = [s.deliveryDistrict, s.deliveryProvince].filter(Boolean).join(", ") || "-";
			var canAssign = s.status === "READY" && !s.shipper;
			var viewAction = user.role === "MANAGER"
				? '<button type="button" class="btn btn-sm btn-outline-secondary ms-2" data-hide="' +
				esc(s.id) + '">Ẩn</button>'
				: "";
			var assignment = canAssign
				? '<div class="input-group input-group-sm"><select class="form-select shipper-select"' +
					(shippers.length ? "" : " disabled") + ">" + options + "</select>" +
					'<button type="button" class="btn btn-primary" data-assign="' + esc(s.id) + '"' +
					(shippers.length ? "" : " disabled") + ">Phân công</button></div>" + viewAction
				: esc(s.shipper ? (s.shipper.fullName || s.shipper.username) : "-");
			return "<tr>" +
				"<td><strong>" + esc(s.trackingCode) + "</strong></td>" +
				"<td>" + esc(s.receiverName || "-") + "<br><small class=\"text-muted\">" + esc(s.receiverPhone || "") + "</small></td>" +
				"<td>" + esc(area) + "</td>" +
				"<td>" + esc(s.serviceType || "-") + "</td>" +
				"<td>" + money(s.shippingFee) + "</td>" +
				"<td>" + money(s.codAmount) + "</td>" +
				"<td>" + esc(fmtDate(s.createdAt)) + "</td>" +
				"<td>" + esc(STATUS_LABELS[s.status] || s.status || "-") + "</td>" +
				'<td class="text-nowrap">' + assignment + "</td></tr>";
		}).join("");
		applySuggestions();
	}

	function suggestionOptions(list) {
		var groups = { EXACT: [], NEARBY: [], NO_ZONE: [], UNKNOWN: [] };
		list.forEach(function (s) {
			(groups[s.match] || groups.UNKNOWN).push(s);
		});
		var labels = {
			EXACT: "Phụ trách khu vực này",
			NEARBY: "Khu vực lân cận",
			NO_ZONE: "Chưa được giao khu vực",
			UNKNOWN: "Shipper"
		};

		var html = '<option value="">-- Chọn shipper --</option>';
		Object.keys(labels).forEach(function (key) {
			if (!groups[key].length) { return; }
			html += '<optgroup label="' + esc(labels[key]) + '">' + groups[key].map(function (s) {
				var hint = key === "NEARBY" ? " (cách " + s.hops + " chặng)" : "";
				return '<option value="' + esc(s.id) + '">' + esc(s.fullName || ("#" + s.id)) + esc(hint) + "</option>";
			}).join("") + "</optgroup>";
		});
		return html;
	}

	async function applySuggestions() {
		var buttons = document.querySelectorAll("#currentShipmentsBody button[data-assign]");
		await Promise.all(Array.prototype.map.call(buttons, async function (button) {
			var row = button.closest("tr");
			var select = row && row.querySelector(".shipper-select");
			if (!select || select.disabled) { return; }

			try {
				var list = await api("/api/admin/shipments/" + encodeURIComponent(button.getAttribute("data-assign")) +
					"/suggested-shippers");
				if (!Array.isArray(list) || !list.length) { return; }

				select.innerHTML = suggestionOptions(list);
				var best = list.filter(function (s) { return s.match === "EXACT"; })[0];
				if (best) { select.value = String(best.id); }
			} catch (error) {
				// keep the plain shipper list if suggestions fail
			}
		}));
	}

	async function loadCurrentShipments() {
		try {
			state.currentShipments = (await api("/api/admin/shipments/current")) || [];
		} catch (error) {
			state.currentShipments = [];
			showAlert("danger", "Không tải được danh sách vận đơn hiện tại: " + error.message);
		}
		renderCurrentShipments();
	}

	async function assignShipper(shipmentId, shipperId, button) {
		button.disabled = true;
		try {
			await api("/api/admin/shipments/" + encodeURIComponent(shipmentId) +
				"/assign/" + encodeURIComponent(shipperId), { method: "PUT" });
			showAlert("success", "Đã phân công shipper cho vận đơn #" + shipmentId);
			await Promise.all([loadCurrentShipments(), loadStats()]);
		} catch (error) {
			showAlert("danger", error.message);
			button.disabled = false;
		}
	}

	async function hideShipment(shipmentId, button) {
		button.disabled = true;
		try {
			await api("/api/shipments/" + encodeURIComponent(shipmentId) + "/view", { method: "DELETE" });
			showAlert("success", "Đã ẩn vận đơn khỏi danh sách của bạn.");
			await loadCurrentShipments();
		} catch (error) {
			showAlert("danger", "Không thể ẩn vận đơn: " + error.message);
			button.disabled = false;
		}
	}

	async function loadDeletableShipments() {
		var card = $("deletableShipmentsCard");
		var body = $("deletableShipmentsBody");
		if (!card || !body || user.role !== "ADMIN") { return; }
		card.hidden = false;
		try {
			state.deletable = (await api("/api/admin/shipments/deletable")) || [];
			if (!state.deletable.length) {
				body.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Không có vận đơn đủ điều kiện xóa</td></tr>';
				return;
			}
			body.innerHTML = state.deletable.map(function (s) {
				return "<tr>" +
					"<td><strong>" + esc(s.trackingCode) + "</strong></td>" +
					"<td>" + esc(s.receiverName || "-") + "</td>" +
					"<td>" + esc(STATUS_LABELS[s.status] || s.status) + "</td>" +
					"<td>" + esc(fmtDate(s.createdAt)) + "</td>" +
					'<td class="text-end"><button type="button" class="btn btn-sm btn-outline-danger" data-delete="' +
					esc(s.id) + '">Xóa vĩnh viễn</button></td></tr>';
			}).join("");
		} catch (error) {
			body.innerHTML = '<tr><td colspan="5" class="text-center text-danger">Không tải được danh sách</td></tr>';
			showAlert("danger", "Không tải được vận đơn có thể xóa: " + error.message);
		}
	}

	async function deleteShipment(shipmentId, button) {
		if (!window.confirm("Xóa vĩnh viễn vận đơn này cùng thanh toán và lịch sử liên quan?")) { return; }
		button.disabled = true;
		try {
			await api("/api/admin/shipments/" + encodeURIComponent(shipmentId), { method: "DELETE" });
			showAlert("success", "Đã xóa vĩnh viễn vận đơn.");
			await Promise.all([loadDeletableShipments(), loadStats()]);
		} catch (error) {
			showAlert("danger", "Không thể xóa vận đơn: " + error.message);
			button.disabled = false;
		}
	}

	async function deleteAccount(userId, button) {
		if (!window.confirm("Xóa vĩnh viễn tài khoản này? Các đơn/vận đơn/review liên quan sẽ không bị xóa; tài khoản có dữ liệu khách hàng hoặc shipper còn vận đơn đang giao sẽ không thể xóa.")) {
			return;
		}
		button.disabled = true;
		try {
			await api("/api/admin/users/" + encodeURIComponent(userId), { method: "DELETE" });
			showAlert("success", "Đã xóa tài khoản.");
			await Promise.all([loadManagers(), loadShippers(), loadCurrentShipments(), loadStats()]);
		} catch (error) {
			showAlert("danger", "Không thể xóa tài khoản: " + error.message);
			button.disabled = false;
		}
	}

	function bindAccountDeletion() {
		["managerBody", "shipperBody", "userAccountBody"].forEach(function (id) {
			var body = $(id);
			if (!body) { return; }
			body.addEventListener("click", function (event) {
				var button = event.target.closest("button[data-delete-user]");
				if (button) {
					deleteAccount(button.getAttribute("data-delete-user"), button);
				}
			});
		});
	}

	function bindShipperStatus() {
		var body = $("shipperBody");
		if (!body || user.role !== "ADMIN") { return; }
		body.addEventListener("click", async function (event) {
			var button = event.target.closest("button[data-shipper-account-enabled]");
			if (!button) { return; }
			var enabled = button.getAttribute("data-enabled") === "true";
			var action = enabled ? "mở khóa" : "khóa";
			if (!window.confirm("Bạn có chắc muốn " + action + " tài khoản shipper này?")) { return; }

			button.disabled = true;
			try {
				await api("/api/admin/users/" + encodeURIComponent(button.getAttribute("data-shipper-account-enabled")) +
					"/enabled", { method: "PATCH", body: { enabled: enabled } });
				showAlert("success", enabled
					? "Đã mở khóa tài khoản shipper."
					: "Đã khóa tài khoản shipper.");
				await Promise.all([loadShippers(), loadManagers()]);
			} catch (error) {
				showAlert("danger", "Không thể cập nhật trạng thái shipper: " + error.message);
				button.disabled = false;
			}
		});
	}

	function bindAssign() {
		var body = $("currentShipmentsBody");
		if (!body) { return; }
		body.addEventListener("click", function (event) {
			var hideButton = event.target.closest("button[data-hide]");
			if (hideButton) {
				if (window.confirm("Ẩn vận đơn này khỏi danh sách của bạn?")) {
					hideShipment(hideButton.getAttribute("data-hide"), hideButton);
				}
				return;
			}
			var button = event.target.closest("button[data-assign]");
			if (!button) { return; }
			var row = button.closest("tr");
			var select = row.querySelector(".shipper-select");
			if (!select || !select.value) {
				showAlert("warning", "Hãy chọn shipper trước khi phân công");
				return;
			}
			assignShipper(button.getAttribute("data-assign"), select.value, button);
		});

		var deletableBody = $("deletableShipmentsBody");
		if (deletableBody) {
			deletableBody.addEventListener("click", function (event) {
				var button = event.target.closest("button[data-delete]");
				if (button) {
					deleteShipment(button.getAttribute("data-delete"), button);
				}
			});
		}
	}

	function bindManagerAssignment() {
		var body = $("shipperBody");
		if (!body || user.role !== "ADMIN") { return; }
		body.addEventListener("click", async function (event) {
			var button = event.target.closest("button[data-manager-save]");
			if (!button) { return; }
			var row = button.closest("tr");
			var select = row.querySelector(".manager-select");
			if (!select) { return; }
			button.disabled = true;
			try {
				var managerId = select.value ? Number(select.value) : null;
				await api("/api/admin/users/" + encodeURIComponent(button.getAttribute("data-manager-save")) +
					"/manager", { method: "PUT", body: { managerId: managerId } });
				showAlert("success", managerId
					? "Đã cập nhật manager phụ trách shipper."
					: "Đã bỏ phân công manager cho shipper.");
				await Promise.all([loadShippers(), loadManagers()]);
			} catch (error) {
				showAlert("danger", "Không thể cập nhật manager: " + error.message);
				button.disabled = false;
			}
		});
	}

	// ---------- create user (admin page only) ----------

	function bindCreateUser() {
		var form = $("createUserForm");
		if (!form) { return; }

		form.addEventListener("submit", async function (event) {
			event.preventDefault();
			var submit = form.querySelector("button[type=submit]");
			var phone = form.elements.phone.value.trim();
			var payload = {
				username: form.elements.username.value.trim(),
				email: form.elements.email.value.trim(),
				password: form.elements.password.value,
				fullName: form.elements.fullName.value.trim(),
				phone: phone || null,
				role: form.elements.role.value
			};

			submit.disabled = true;
			try {
				var created = await api("/api/admin/users", { method: "POST", body: payload });
				showAlert("success", "Đã tạo tài khoản " + (created && created.username ? created.username : payload.username) +
					" (" + payload.role + ")");
				form.reset();
				await Promise.all([loadShippers(), loadManagers(), loadStats()]);
				renderCurrentShipments();
			} catch (error) {
				showAlert("danger", error.message);
			} finally {
				submit.disabled = false;
			}
		});
	}

	// ---------- init ----------

	function bindChrome() {
		setText("whoami", user.username + " (" + (ROLE_LABELS[user.role] || user.role) + ")");

		var logoutBtn = $("logoutBtn");
		if (logoutBtn) {
			logoutBtn.addEventListener("click", function () { window.Auth.logout(); });
		}

		var refreshBtn = $("refreshBtn");
		if (refreshBtn) {
			refreshBtn.addEventListener("click", refreshAll);
		}
	}

	async function refreshAll() {
		await Promise.all([loadShippers(), loadManagers(), loadCurrentShipments(), loadStats(), loadDeletableShipments()]);
	}

	bindChrome();
	bindAssign();
	bindManagerAssignment();
	bindAccountDeletion();
	bindShipperStatus();
	bindCreateUser();
	refreshAll();
})();