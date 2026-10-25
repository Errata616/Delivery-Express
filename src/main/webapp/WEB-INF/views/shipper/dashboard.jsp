<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="vi">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<title>Bảng điều khiển shipper - UTEExpress</title>
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
	<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
	<link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body class="bg-light" data-allowed-roles="SHIPPER" hidden>
	<nav class="navbar navbar-dark bg-primary">
		<div class="container">
			<a class="navbar-brand fw-bold d-flex align-items-center" href="<%= request.getContextPath() %>/">
				<span class="brand-mark"><i class="bi bi-box-seam-fill" aria-hidden="true"></i></span>
				UTEExpress
			</a>
			<span class="navbar-text text-white me-auto ms-3">Bảng điều khiển shipper</span>
			<div class="d-flex align-items-center gap-3">
				<span id="whoami" class="text-white small"></span>
				<div data-profile-actions class="d-flex align-items-center gap-2"></div>
				<%-- Notification bell --%>
				<div id="notif-bell-container"></div>
				<button id="logoutBtn" type="button" class="btn btn-sm btn-outline-light">Đăng xuất</button>
			</div>
		</div>
	</nav>

	<main class="container py-4">
		<div id="alertBox" aria-live="polite"></div>

		<section class="card shadow-sm mb-4">
			<div class="card-body d-flex flex-wrap justify-content-between align-items-center gap-3">
				<div>
					<div class="fw-semibold">Trạng thái nhận việc</div>
					<div id="availabilityMessage" class="small text-muted">Đang tải trạng thái...</div>
				</div>
				<div class="d-flex align-items-center gap-2">
					<span id="availabilityBadge" class="badge text-bg-secondary">Đang tải</span>
					<button id="availabilityToggle" type="button" class="btn btn-sm btn-outline-primary" disabled>
						Đổi trạng thái
					</button>
				</div>
			</div>
		</section>

		<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
			<div>
				<h1 class="h3 mb-1">Vận đơn được giao cho tôi</h1>
				<p class="text-muted mb-0">Cập nhật trạng thái khi nhận hàng và hoàn tất giao hàng.</p>
			</div>
			<button id="refreshBtn" type="button" class="btn btn-sm btn-outline-primary">
				Làm mới
			</button>
		</div>

		<div class="row g-3 mb-4 row-cols-2 row-cols-lg-5">
			<div class="col">
				<div class="card shadow-sm h-100"><div class="card-body">
					<div class="text-muted small">Tổng vận đơn</div>
					<div class="fs-3 fw-bold" id="statTotal">-</div>
				</div></div>
			</div>
			<div class="col">
				<div class="card shadow-sm h-100"><div class="card-body">
					<div class="text-muted small">Đang xử lý</div>
					<div class="fs-3 fw-bold text-primary" id="statActive">-</div>
				</div></div>
			</div>
			<div class="col">
				<div class="card shadow-sm h-100"><div class="card-body">
					<div class="text-muted small">Giao thành công</div>
					<div class="fs-3 fw-bold text-success" id="statDelivered">-</div>
				</div></div>
			</div>
			<div class="col">
				<div class="card shadow-sm h-100"><div class="card-body">
					<div class="text-muted small">Giao thất bại</div>
					<div class="fs-3 fw-bold text-danger" id="statFailed">-</div>
				</div></div>
			</div>
			<div class="col">
				<div class="card shadow-sm h-100"><div class="card-body">
					<div class="text-muted small">Đánh giá khách hàng</div>
					<div class="fs-3 fw-bold text-warning" id="statRating">-</div>
					<div class="small text-muted" id="statRatingCount">Đang tải...</div>
				</div></div>
			</div>
		</div>

		<section class="card shadow-sm">
			<div class="card-header bg-white d-flex flex-wrap justify-content-between align-items-center gap-2">
				<h2 class="h5 mb-0">Danh sách vận đơn</h2>
				<div class="btn-group btn-group-sm" role="group" aria-label="Lọc vận đơn">
					<button type="button" class="btn btn-primary filter-btn" data-filter="all">Tất cả</button>
					<button type="button" class="btn btn-outline-primary filter-btn" data-filter="active">Đang xử lý</button>
					<button type="button" class="btn btn-outline-primary filter-btn" data-filter="delivered">Đã giao</button>
					<button type="button" class="btn btn-outline-primary filter-btn" data-filter="failed">Thất bại</button>
				</div>
			</div>
			<div class="table-responsive">
				<table class="table table-hover align-middle mb-0">
					<thead class="table-light">
						<tr>
							<th scope="col">Mã vận đơn</th>
							<th scope="col">Lấy hàng</th>
							<th scope="col">Người nhận / Nơi giao</th>
							<th scope="col">COD</th>
							<th scope="col">Trạng thái</th>
							<th scope="col" class="text-end">Thao tác</th>
						</tr>
					</thead>
					<tbody id="shipmentList">
						<tr><td colspan="6" class="text-center text-muted py-4">Đang tải vận đơn...</td></tr>
					</tbody>
				</table>
			</div>
		</section>
	</main>

	<script>
		window.APP_CONTEXT_PATH = '<%= request.getContextPath() %>';
	</script>
	<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
	<script src="<%= request.getContextPath() %>/js/auth.js"></script>
	<script src="<%= request.getContextPath() %>/js/api.js"></script>
	<script src="<%= request.getContextPath() %>/js/profile.js"></script>
	<%-- WebSocket notification bell --%>
	<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
	<script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
	<script src="<%= request.getContextPath() %>/js/notification.js"></script>
	<script>
		(function () {
			"use strict";

			var user = window.Auth.requireRole(["SHIPPER"]);
			if (!user) { return; }
			document.body.hidden = false;

			// ── Init notification bell ─────────────────────────────
			if (window.Notifications) {
				window.Notifications.init(window.APP_CONTEXT_PATH || "");
			}

			var shipments = [];
			var currentFilter = "all";
			var availability = null;
			var list = document.getElementById("shipmentList");
			var alertBox = document.getElementById("alertBox");
			var refreshButton = document.getElementById("refreshBtn");
			var availabilityBadge = document.getElementById("availabilityBadge");
			var availabilityMessage = document.getElementById("availabilityMessage");
			var availabilityToggle = document.getElementById("availabilityToggle");
			var statusLabels = {
				READY: "Chờ phân công",
				ASSIGNED: "Đã phân công",
				PICKED_UP: "Đã lấy hàng",
				DELIVERING: "Đang giao",
				DELIVERED: "Đã giao",
				FAILED: "Giao thất bại",
				CANCELLED: "Đã hủy"
			};
			var statusClasses = {
				READY: "text-bg-secondary",
				ASSIGNED: "text-bg-info",
				PICKED_UP: "text-bg-primary",
				DELIVERING: "text-bg-warning",
				DELIVERED: "text-bg-success",
				FAILED: "text-bg-danger",
				CANCELLED: "text-bg-dark"
			};

			function api(path, options) {
				return window.apiData(window.Auth.url(path), options);
			}

			function text(value, fallback) {
				return value == null || value === "" ? (fallback || "—") : String(value);
			}

			function setText(id, value) {
				document.getElementById(id).textContent = String(value);
			}

			function formatMoney(value) {
				return new Intl.NumberFormat("vi-VN", {
					style: "currency",
					currency: "VND",
					maximumFractionDigits: 0
				}).format(Number(value || 0));
			}

			function formatDate(value) {
				if (!value) { return "—"; }
				var date = Array.isArray(value)
					? new Date(value[0], value[1] - 1, value[2], value[3] || 0, value[4] || 0, value[5] || 0)
					: new Date(value);
				return Number.isNaN(date.getTime()) ? "—" : date.toLocaleString("vi-VN");
			}

			function renderAvailability() {
				if (availability === null) {
					availabilityBadge.textContent = "Chưa rõ";
					availabilityBadge.className = "badge text-bg-secondary";
					availabilityMessage.textContent = "Không tải được trạng thái nhận việc.";
					availabilityToggle.textContent = "Thử tải lại";
					availabilityToggle.disabled = false;
					return;
				}
				availabilityBadge.textContent = availability ? "Hoạt động" : "Không hoạt động";
				availabilityBadge.className = "badge " +
					(availability ? "text-bg-success" : "text-bg-secondary");
				availabilityMessage.textContent = availability
					? "Bạn có thể được quản lý phân công vận đơn mới."
					: "Bạn sẽ không được phân công vận đơn mới. Các vận đơn đã nhận vẫn giữ nguyên.";
				availabilityToggle.textContent = availability ? "Tạm ngừng nhận việc" : "Bật nhận việc";
				availabilityToggle.disabled = false;
			}

			async function loadAvailability() {
				availabilityToggle.disabled = true;
				try {
					var result = await api("/api/shipper/me/availability");
					if (!result || typeof result.available !== "boolean") {
						throw new Error("Dữ liệu trạng thái nhận việc không hợp lệ.");
					}
					availability = result.available;
				} catch (error) {
					availability = null;
					showAlert("danger", "Không tải được trạng thái nhận việc: " + error.message);
				}
				renderAvailability();
			}

			async function toggleAvailability() {
				if (availability === null) {
					await loadAvailability();
					return;
				}
				availabilityToggle.disabled = true;
				try {
					var result = await api("/api/shipper/me/availability", {
						method: "PATCH",
						body: { available: !availability }
					});
					if (!result || typeof result.available !== "boolean") {
						throw new Error("Dữ liệu trạng thái nhận việc không hợp lệ.");
					}
					availability = result.available;
					renderAvailability();
					showAlert("success", availability
						? "Bạn đang hoạt động và có thể nhận phân công mới."
						: "Bạn đã chuyển sang trạng thái không hoạt động. Vận đơn đã nhận không bị thay đổi.");
				} catch (error) {
					availabilityToggle.disabled = false;
					showAlert("danger", "Không thể cập nhật trạng thái: " + error.message);
				}
			}

			function address(shipment, prefix) {
				var parts = [
					shipment[prefix + "Address"],
					shipment[prefix + "District"],
					shipment[prefix + "Province"]
				].filter(function (part) { return Boolean(part); });
				return parts.length ? parts.join(", ") : "Chưa có địa chỉ";
			}

			function isActive(shipment) {
				return ["ASSIGNED", "PICKED_UP", "DELIVERING"].indexOf(shipment.status) !== -1;
			}

			function matchesFilter(shipment) {
				if (currentFilter === "active") { return isActive(shipment); }
				if (currentFilter === "delivered") { return shipment.status === "DELIVERED"; }
				if (currentFilter === "failed") { return shipment.status === "FAILED"; }
				return true;
			}

			function nextAction(shipment) {
				var status = shipment.status;
				if (status === "ASSIGNED") { return { status: "PICKED_UP", label: "Đã lấy hàng" }; }
				var route = Array.isArray(shipment.plannedRouteDistricts) ? shipment.plannedRouteDistricts : [];
				var routeIndex = Number(shipment.currentRouteIndex);
				if (route.length && (status === "PICKED_UP" || status === "DELIVERING")) {
					if (routeIndex < route.length - 1) {
						return { district: route[routeIndex + 1], label: "Đã đến " + route[routeIndex + 1] };
					}
					if (status === "PICKED_UP") { return { status: "DELIVERING", label: "Bắt đầu giao" }; }
					return { status: "DELIVERED", label: "Giao thành công" };
				}
				if (status === "PICKED_UP") { return { status: "DELIVERING", label: "Bắt đầu giao" }; }
				if (status === "DELIVERING") { return { status: "DELIVERED", label: "Giao thành công" }; }
				return null;
			}

			function appendCell(row, className) {
				var cell = document.createElement("td");
				if (className) { cell.className = className; }
				row.appendChild(cell);
				return cell;
			}

			function addActionButton(container, shipment, status, label, className, confirmMessage) {
				var button = document.createElement("button");
				button.type = "button";
				button.className = "btn btn-sm " + className;
				button.textContent = label;
				button.setAttribute("data-shipment-id", shipment.id);
				button.setAttribute("data-next-status", status);
				if (confirmMessage) { button.setAttribute("data-confirm-message", confirmMessage); }
				container.appendChild(button);
			}

			function addDistrictButton(container, shipment, district) {
				var button = document.createElement("button");
				button.type = "button";
				button.className = "btn btn-sm btn-primary";
				button.textContent = "Đã đến " + district;
				button.setAttribute("data-shipment-id", shipment.id);
				button.setAttribute("data-next-district", district);
				container.appendChild(button);
			}

			function renderRow(shipment) {
				var row = document.createElement("tr");

				var codeCell = appendCell(row, "shipment-code");
				var code = document.createElement("strong");
				code.textContent = text(shipment.trackingCode);
				codeCell.appendChild(code);
				var created = document.createElement("div");
				created.className = "small text-muted mt-1";
				created.textContent = formatDate(shipment.createdAt);
				codeCell.appendChild(created);

				var pickupCell = appendCell(row, "shipment-address");
				var sender = document.createElement("div");
				sender.className = "fw-semibold";
				sender.textContent = text(shipment.senderName);
				pickupCell.appendChild(sender);
				var pickupAddress = document.createElement("div");
				pickupAddress.className = "small text-muted";
				pickupAddress.textContent = address(shipment, "pickup");
				pickupCell.appendChild(pickupAddress);
				if (shipment.senderPhone) {
					var senderPhone = document.createElement("a");
					senderPhone.className = "small";
					senderPhone.href = "tel:" + shipment.senderPhone;
					senderPhone.textContent = shipment.senderPhone;
					pickupCell.appendChild(senderPhone);
				}

				var receiverCell = appendCell(row, "shipment-address");
				var receiver = document.createElement("div");
				receiver.className = "fw-semibold";
				receiver.textContent = text(shipment.receiverName);
				receiverCell.appendChild(receiver);
				var deliveryAddress = document.createElement("div");
				deliveryAddress.className = "small text-muted";
				deliveryAddress.textContent = address(shipment, "delivery");
				receiverCell.appendChild(deliveryAddress);
				if (shipment.receiverPhone) {
					var receiverPhone = document.createElement("a");
					receiverPhone.className = "small";
					receiverPhone.href = "tel:" + shipment.receiverPhone;
					receiverPhone.textContent = shipment.receiverPhone;
					receiverCell.appendChild(receiverPhone);
				}

				appendCell(row).textContent = formatMoney(shipment.codAmount);

				var statusCell = appendCell(row);
				var badge = document.createElement("span");
				badge.className = "badge " + (statusClasses[shipment.status] || "text-bg-secondary");
				badge.textContent = statusLabels[shipment.status] || text(shipment.status);
				statusCell.appendChild(badge);
				if (shipment.deliveredAt) {
					var deliveredAt = document.createElement("div");
					deliveredAt.className = "small text-muted mt-1";
					deliveredAt.textContent = "Hoàn tất: " + formatDate(shipment.deliveredAt);
					statusCell.appendChild(deliveredAt);
				}
				if (Array.isArray(shipment.plannedRouteDistricts) && shipment.plannedRouteDistricts.length) {
					var route = document.createElement("div");
					route.className = "small text-muted mt-1";
					route.textContent = "Lộ trình: " + shipment.plannedRouteDistricts.join(" → ");
					statusCell.appendChild(route);
					if (shipment.currentDistrict) {
						var currentDistrict = document.createElement("div");
						currentDistrict.className = "small text-muted";
						currentDistrict.textContent = "Hiện tại: " + shipment.currentDistrict;
						statusCell.appendChild(currentDistrict);
					}
				}

				var actionCell = appendCell(row, "text-end");
				var action = nextAction(shipment);
				if (action) {
					if (action.district) {
						addDistrictButton(actionCell, shipment, action.district);
					} else {
						addActionButton(actionCell, shipment, action.status, action.label, "btn-primary");
					}
					if (!action.district) {
						addActionButton(actionCell, shipment, "FAILED", "Giao thất bại", "btn-outline-danger mt-1",
							"Xác nhận đánh dấu vận đơn này giao thất bại?");
					}
				} else {
					actionCell.textContent = "";
				}
				var hideButton = document.createElement("button");
				hideButton.type = "button";
				hideButton.className = "btn btn-sm btn-outline-secondary mt-1";
				hideButton.textContent = "Ẩn khỏi danh sách";
				hideButton.setAttribute("data-hide-shipment-id", shipment.id);
				actionCell.appendChild(hideButton);

				return row;
			}

			function renderShipments() {
				list.replaceChildren();
				var visible = shipments.filter(matchesFilter);
				if (!visible.length) {
					var row = document.createElement("tr");
					var cell = document.createElement("td");
					cell.colSpan = 6;
					cell.className = "text-center text-muted py-5";
					cell.textContent = shipments.length
						? "Không có vận đơn trong mục này."
						: "Bạn chưa được phân công vận đơn nào.";
					row.appendChild(cell);
					list.appendChild(row);
					return;
				}
				visible.forEach(function (shipment) { list.appendChild(renderRow(shipment)); });
			}

			function renderStats() {
				setText("statTotal", shipments.length);
				setText("statActive", shipments.filter(isActive).length);
				setText("statDelivered", shipments.filter(function (s) { return s.status === "DELIVERED"; }).length);
				setText("statFailed", shipments.filter(function (s) { return s.status === "FAILED"; }).length);
			}

			async function loadRatingSummary() {
				try {
					var summary = await api("/api/reviews/me/shipper-summary");
					if (!summary || typeof summary.average !== "number" || typeof summary.count !== "number") {
						throw new Error("Dữ liệu đánh giá không hợp lệ.");
					}
					setText("statRating", summary.count ? summary.average.toFixed(1) + " / 5" : "Chưa có");
					setText("statRatingCount", summary.count + (summary.count === 1 ? " đánh giá" : " đánh giá"));
				} catch (error) {
					setText("statRating", "—");
					setText("statRatingCount", "Không tải được: " + (error.message || "Lỗi không xác định"));
				}
			}

			function showAlert(type, message) {
				alertBox.replaceChildren();
				var alert = document.createElement("div");
				alert.className = "alert alert-" + type + " alert-dismissible fade show";
				alert.setAttribute("role", "alert");
				alert.appendChild(document.createTextNode(message));
				var close = document.createElement("button");
				close.type = "button";
				close.className = "btn-close";
				close.setAttribute("data-bs-dismiss", "alert");
				close.setAttribute("aria-label", "Đóng");
				alert.appendChild(close);
				alertBox.appendChild(alert);
			}

			async function loadShipments() {
				refreshButton.disabled = true;
				try {
					var result = await api("/api/shipper/me/shipments");
					if (!Array.isArray(result)) {
						throw new Error("Dữ liệu vận đơn trả về không hợp lệ.");
					}
					shipments = result;
					renderStats();
					renderShipments();
				} catch (error) {
					showAlert("danger", "Không tải được danh sách vận đơn: " + error.message);
					list.replaceChildren();
					var row = document.createElement("tr");
					var cell = document.createElement("td");
					cell.colSpan = 6;
					cell.className = "text-center text-danger py-4";
					cell.textContent = "Tải dữ liệu thất bại. Hãy thử làm mới.";
					row.appendChild(cell);
					list.appendChild(row);
				} finally {
					refreshButton.disabled = false;
				}
			}

			async function updateStatus(button) {
				var shipmentId = button.getAttribute("data-shipment-id");
				var nextStatus = button.getAttribute("data-next-status");
				var confirmMessage = button.getAttribute("data-confirm-message");
				if (confirmMessage && !window.confirm(confirmMessage)) { return; }

				button.disabled = true;
				try {
					await api("/api/shipments/" + encodeURIComponent(shipmentId) + "/status?status="
						+ encodeURIComponent(nextStatus), { method: "PUT" });
					showAlert("success", "Đã cập nhật trạng thái vận đơn.");
					await loadShipments();
				} catch (error) {
					showAlert("danger", "Không cập nhật được trạng thái: " + error.message);
					button.disabled = false;
				}
			}

			async function updateDistrict(button) {
				var shipmentId = button.getAttribute("data-shipment-id");
				var district = button.getAttribute("data-next-district");
				button.disabled = true;
				try {
					await api("/api/shipments/" + encodeURIComponent(shipmentId) + "/district?district="
						+ encodeURIComponent(district), { method: "PUT" });
					showAlert("success", "Đã cập nhật vị trí theo lộ trình đã phân công.");
					await loadShipments();
				} catch (error) {
					showAlert("danger", "Không cập nhật được quận hiện tại: " + error.message);
					button.disabled = false;
				}
			}

			document.getElementById("whoami").textContent = user.username;
			document.getElementById("logoutBtn").addEventListener("click", function () {
				window.Auth.logout();
			});
			refreshButton.addEventListener("click", loadShipments);
			availabilityToggle.addEventListener("click", toggleAvailability);
			document.querySelectorAll(".filter-btn").forEach(function (button) {
				button.addEventListener("click", function () {
					currentFilter = button.getAttribute("data-filter");
					document.querySelectorAll(".filter-btn").forEach(function (filterButton) {
						var selected = filterButton === button;
						filterButton.classList.toggle("btn-primary", selected);
						filterButton.classList.toggle("btn-outline-primary", !selected);
					});
					renderShipments();
				});
			});
			list.addEventListener("click", function (event) {
				var hideButton = event.target.closest("button[data-hide-shipment-id]");
				if (hideButton) {
					hideShipment(hideButton);
					return;
				}
				var districtButton = event.target.closest("button[data-next-district]");
				if (districtButton) {
					updateDistrict(districtButton);
					return;
				}
				var button = event.target.closest("button[data-shipment-id]");
				if (button) { updateStatus(button); }
			});

			async function hideShipment(button) {
				var shipmentId = button.getAttribute("data-hide-shipment-id");
				if (!window.confirm("Ẩn vận đơn này khỏi danh sách của bạn?")) { return; }
				button.disabled = true;
				try {
					await api("/api/shipments/" + encodeURIComponent(shipmentId) + "/view", { method: "DELETE" });
					showAlert("success", "Đã ẩn vận đơn khỏi danh sách của bạn.");
					await loadShipments();
				} catch (error) {
					showAlert("danger", "Không thể ẩn vận đơn: " + error.message);
					button.disabled = false;
				}
			}

			loadAvailability();
			loadShipments();
			loadRatingSummary();
		})();
	</script>
</body>
</html>
