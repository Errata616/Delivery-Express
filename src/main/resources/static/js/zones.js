/*
 * zones.js - admin chooses which districts each shipper covers.
 * Needs the #zoneCard markup in admin/dashboard.jsp, plus auth.js and api.js.
 */
(function () {
	"use strict";

	var card = document.getElementById("zoneCard");
	if (!card || !window.Auth || !window.Auth.getAccessToken()) { return; }

	var ctx = window.APP_CONTEXT_PATH || "";
	var shipperSelect = document.getElementById("zoneShipper");
	var checks = document.getElementById("zoneChecks");
	var saveBtn = document.getElementById("zoneSave");
	var summary = document.getElementById("zoneSummary");
	var alertBox = document.getElementById("zoneAlert");

	var districts = [];
	var shippers = [];

	function api(path, options) {
		return window.apiData(ctx + path, options);
	}

	function esc(value) {
		return String(value === null || value === undefined ? "" : value)
			.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
			.replace(/"/g, "&quot;").replace(/'/g, "&#39;");
	}

	function flash(type, message) {
		alertBox.className = "alert alert-" + type + " py-2 small";
		alertBox.textContent = message;
	}

	function nameOf(code) {
		var found = districts.filter(function (d) { return d.code === code; })[0];
		return found ? found.name : code;
	}

	function currentShipper() {
		var id = Number(shipperSelect.value);
		return shippers.filter(function (s) { return s.shipperId === id; })[0];
	}

	function renderChecks() {
		var shipper = currentShipper();
		var selected = shipper ? shipper.districtCodes : [];
		checks.innerHTML = districts.map(function (d) {
			var id = "zone-" + d.code;
			return '<div class="col"><div class="form-check">' +
				'<input class="form-check-input zone-check" type="checkbox" id="' + esc(id) + '" value="' + esc(d.code) + '"' +
				(selected.indexOf(d.code) !== -1 ? " checked" : "") + ">" +
				'<label class="form-check-label" for="' + esc(id) + '">' + esc(d.name) + "</label></div></div>";
		}).join("");
		saveBtn.disabled = !shipper;
	}

	function renderSummary() {
		if (!shippers.length) {
			summary.innerHTML = '<tr><td colspan="2" class="text-center text-muted">Chưa có shipper nào</td></tr>';
			return;
		}
		summary.innerHTML = shippers.map(function (s) {
			var names = s.districtCodes.length
				? esc(s.districtCodes.map(nameOf).join(", "))
				: '<span class="text-muted">Chưa giao khu vực</span>';
			return '<tr role="button" data-id="' + esc(s.shipperId) + '"><td class="text-nowrap">' +
				esc(s.fullName || ("#" + s.shipperId)) + (s.enabled && s.available
					? ' <span class="badge bg-success">Hoạt động</span>'
					: ' <span class="badge bg-secondary">Không hoạt động</span>') +
				"</td><td>" + names + "</td></tr>";
		}).join("");
	}

	async function init() {
		try {
			var results = await Promise.all([
				api("/api/routes/districts"),
				api("/api/admin/shipments/shippers/zones")
			]);
			districts = results[0] || [];
			shippers = results[1] || [];

			shipperSelect.innerHTML = shippers.length
				? shippers.map(function (s) {
					return '<option value="' + esc(s.shipperId) + '">' + esc(s.fullName || ("#" + s.shipperId)) + "</option>";
				}).join("")
				: '<option value="">Chưa có shipper</option>';

			renderChecks();
			renderSummary();
		} catch (error) {
			flash("danger", "Không tải được dữ liệu khu vực: " + error.message);
		}
	}

	shipperSelect.addEventListener("change", renderChecks);

	document.getElementById("zoneAll").addEventListener("click", function () {
		checks.querySelectorAll(".zone-check").forEach(function (box) { box.checked = true; });
	});
	document.getElementById("zoneNone").addEventListener("click", function () {
		checks.querySelectorAll(".zone-check").forEach(function (box) { box.checked = false; });
	});

	summary.addEventListener("click", function (event) {
		var row = event.target.closest("tr[data-id]");
		if (!row) { return; }
		shipperSelect.value = row.getAttribute("data-id");
		renderChecks();
	});

	saveBtn.addEventListener("click", async function () {
		var shipper = currentShipper();
		if (!shipper) { return; }

		var codes = Array.prototype.map.call(checks.querySelectorAll(".zone-check:checked"), function (box) {
			return box.value;
		});

		saveBtn.disabled = true;
		try {
			var updated = await api("/api/admin/shipments/shippers/" + encodeURIComponent(shipper.shipperId) + "/zones",
				{ method: "PUT", body: { districtCodes: codes } });
			shipper.districtCodes = updated.districtCodes;
			renderSummary();
			flash("success", "Đã lưu khu vực cho " + (shipper.fullName || "shipper") + " (" + codes.length + " quận/huyện).");
		} catch (error) {
			flash("danger", error.message);
		} finally {
			saveBtn.disabled = false;
		}
	});

	init();
})();