/*
 * admin.js - dashboard logic shared by admin/dashboard.jsp and manager/dashboard.jsp.
 *  - loads statistics
 *  - lists unassigned shipments and assigns a shipper
 *  - lists shippers and (admin page only) creates SHIPPER / MANAGER accounts
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

	var state = { shippers: [], unassigned: [] };

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

	var ROLE_LABELS = { ADMIN: "Quản trị viên", MANAGER: "Quản lý" };

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
		setText("statShippers", String(stats.totalShippers || 0));
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
		return state.shippers.filter(function (s) { return s.enabled !== false; });
	}

	function renderShippers() {
		var body = $("shipperBody");
		if (!body) { return; }
		if (!state.shippers.length) {
			body.innerHTML = '<tr><td colspan="4" class="text-center text-muted">Chưa có shipper nào</td></tr>';
			return;
		}
		body.innerHTML = state.shippers.map(function (s) {
			return "<tr><td>" + esc(s.id) + "</td><td>" + esc(s.fullName || s.username) + "</td>" +
				"<td>" + esc(s.phone || "-") + "</td>" +
				"<td>" + (s.enabled === false
					? '<span class="badge bg-secondary">Đã khóa</span>'
					: '<span class="badge bg-success">Hoạt động</span>') + "</td></tr>";
		}).join("");
	}

	async function loadShippers() {
		try {
			state.shippers = (await api("/api/admin/shipments/shippers")) || [];
		} catch (error) {
			state.shippers = [];
			showAlert("danger", "Không tải được danh sách shipper: " + error.message);
		}
		renderShippers();
	}

	// ---------- unassigned shipments ----------

	function renderUnassigned() {
		var body = $("unassignedBody");
		if (!body) { return; }

		setText("unassignedCount", String(state.unassigned.length));

		if (!state.unassigned.length) {
			body.innerHTML = '<tr><td colspan="8" class="text-center text-muted">Không có vận đơn nào chờ phân công</td></tr>';
			return;
		}

		var shippers = activeShippers();
		var options = shippers.length
			? '<option value="">-- Chọn shipper --</option>' + shippers.map(function (s) {
				return '<option value="' + esc(s.id) + '">' + esc(s.fullName || s.username) + "</option>";
			}).join("")
			: '<option value="">Chưa có shipper</option>';

		body.innerHTML = state.unassigned.map(function (s) {
			var area = [s.deliveryDistrict, s.deliveryProvince].filter(Boolean).join(", ") || "-";
			return "<tr>" +
				"<td><strong>" + esc(s.trackingCode) + "</strong></td>" +
				"<td>" + esc(s.receiverName || "-") + "<br><small class=\"text-muted\">" + esc(s.receiverPhone || "") + "</small></td>" +
				"<td>" + esc(area) + "</td>" +
				"<td>" + esc(s.serviceType || "-") + "</td>" +
				"<td>" + money(s.shippingFee) + "</td>" +
				"<td>" + money(s.codAmount) + "</td>" +
				"<td>" + esc(fmtDate(s.createdAt)) + "</td>" +
				'<td class="text-nowrap"><div class="input-group input-group-sm">' +
				'<select class="form-select shipper-select"' + (shippers.length ? "" : " disabled") + ">" + options + "</select>" +
				'<button type="button" class="btn btn-primary" data-assign="' + esc(s.id) + '"' +
				(shippers.length ? "" : " disabled") + ">Phân công</button></div></td></tr>";
		}).join("");
	}

	async function loadUnassigned() {
		try {
			state.unassigned = (await api("/api/admin/shipments/unassigned")) || [];
		} catch (error) {
			state.unassigned = [];
			showAlert("danger", "Không tải được vận đơn chờ phân công: " + error.message);
		}
		renderUnassigned();
	}

	async function assignShipper(shipmentId, shipperId, button) {
		button.disabled = true;
		try {
			await api("/api/admin/shipments/" + encodeURIComponent(shipmentId) +
				"/assign/" + encodeURIComponent(shipperId), { method: "PUT" });
			showAlert("success", "Đã phân công shipper cho vận đơn #" + shipmentId);
			await Promise.all([loadUnassigned(), loadStats()]);
		} catch (error) {
			showAlert("danger", error.message);
			button.disabled = false;
		}
	}

	function bindAssign() {
		var body = $("unassignedBody");
		if (!body) { return; }
		body.addEventListener("click", function (event) {
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
				await Promise.all([loadShippers(), loadStats()]);
				renderUnassigned();
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
		await loadShippers();
		await Promise.all([loadUnassigned(), loadStats()]);
	}

	bindChrome();
	bindAssign();
	bindCreateUser();
	refreshAll();
})();