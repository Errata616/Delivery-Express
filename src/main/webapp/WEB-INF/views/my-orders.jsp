<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="vi">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<title>Đơn hàng của tôi - UTEExpress</title>
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
	<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
	<link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body class="bg-light" hidden>
	<nav class="navbar navbar-dark bg-primary">
		<div class="container">
			<a class="navbar-brand fw-bold d-flex align-items-center" href="${pageContext.request.contextPath}/">
				<span class="brand-mark"><i class="bi bi-box-seam-fill" aria-hidden="true"></i></span>
				UTEExpress
			</a>
			<div class="d-flex align-items-center gap-3">
				<a class="btn btn-outline-light btn-sm" href="${pageContext.request.contextPath}/about-us">Về chúng tôi</a>
				<a class="btn btn-outline-light btn-sm" href="${pageContext.request.contextPath}/booking">
					<i class="bi bi-plus-circle me-1" aria-hidden="true"></i>Tạo đơn mới
				</a>
				<%-- Notification bell (populated by notification.js) --%>
				<div id="notif-bell-container"></div>
				<span id="customer-name" class="text-white"></span>
				<div data-profile-actions class="d-flex align-items-center gap-2"></div>
				<button id="logout-button" class="btn btn-outline-light btn-sm" type="button">
					Đăng xuất
				</button>
			</div>
		</div>
	</nav>

	<main class="container py-5">
		<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
			<div>
				<h1 class="h2 mb-1">Đơn hàng của tôi</h1>
				<p class="text-muted mb-0">Theo dõi và thanh toán các vận đơn bạn đã tạo.</p>
			</div>
		</div>

		<div id="orders-message" class="alert d-none" role="status" aria-live="polite"></div>
		<div id="orders-summary" class="row g-3 mb-4 d-none" aria-label="Tổng quan đơn hàng">
			<div class="col-12 col-sm-4">
				<div class="card order-stat-card h-100">
					<div class="card-body d-flex align-items-center gap-3">
						<span class="order-stat-icon text-primary"><i class="bi bi-box-seam" aria-hidden="true"></i></span>
						<div><div class="small text-muted">Tổng đơn</div><div id="stat-total" class="fs-4 fw-bold">0</div></div>
					</div>
				</div>
			</div>
			<div class="col-12 col-sm-4">
				<div class="card order-stat-card h-100">
					<div class="card-body d-flex align-items-center gap-3">
						<span class="order-stat-icon text-warning"><i class="bi bi-truck" aria-hidden="true"></i></span>
						<div><div class="small text-muted">Đang giao</div><div id="stat-delivering" class="fs-4 fw-bold text-warning-emphasis">0</div></div>
					</div>
				</div>
			</div>
			<div class="col-12 col-sm-4">
				<div class="card order-stat-card h-100">
					<div class="card-body d-flex align-items-center gap-3">
						<span class="order-stat-icon text-success"><i class="bi bi-check2-circle" aria-hidden="true"></i></span>
						<div><div class="small text-muted">Đã giao</div><div id="stat-delivered" class="fs-4 fw-bold text-success">0</div></div>
					</div>
				</div>
			</div>
		</div>
		<div id="orders-empty" class="card d-none">
			<div class="card-body text-center py-5">
				<div class="orders-empty-icon mb-3" aria-hidden="true"><i class="bi bi-inbox"></i></div>
				<h2 class="h5">Bạn chưa có đơn hàng nào</h2>
				<p class="text-muted mb-3">Các vận đơn bạn tạo sẽ xuất hiện tại đây.</p>
				<a class="btn btn-primary" href="${pageContext.request.contextPath}/booking">
					<i class="bi bi-plus-circle me-1" aria-hidden="true"></i>Tạo đơn đầu tiên
				</a>
			</div>
		</div>
		<div id="orders-content" class="card orders-card d-none">
			<div class="table-responsive">
				<table class="table table-hover align-middle mb-0 orders-table">
					<thead class="table-light">
						<tr>
							<th scope="col">Mã vận đơn</th>
							<th scope="col">Người nhận</th>
							<th scope="col">Nơi nhận</th>
							<th scope="col">Trạng thái</th>
							<th scope="col">Phí vận chuyển</th>
							<th scope="col">Thanh toán</th>
							<th scope="col">Ngày tạo</th>
							<th scope="col" class="text-end">Thao tác</th>
						</tr>
					</thead>
					<tbody id="orders-list"></tbody>
				</table>
			</div>
		</div>
	</main>

	<div class="modal fade" id="trackingModal" tabindex="-1" aria-labelledby="trackingModalTitle" aria-hidden="true">
		<div class="modal-dialog modal-dialog-centered modal-lg modal-dialog-scrollable">
			<div class="modal-content border-0 shadow">
				<div class="modal-header">
					<div>
						<div class="small text-muted">Hành trình vận đơn</div>
						<h2 class="modal-title h5 mb-0" id="trackingModalTitle">Theo dõi đơn hàng</h2>
					</div>
					<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>
				</div>
				<div class="modal-body">
					<div id="tracking-content" aria-live="polite"></div>
				</div>
			</div>
		</div>
	</div>

	<!-- Payment modal -->
	<div class="modal fade" id="payModal" tabindex="-1" aria-labelledby="payModalTitle" aria-hidden="true">
		<div class="modal-dialog modal-dialog-centered">
			<div class="modal-content">
				<div class="modal-header">
					<h2 class="modal-title h5" id="payModalTitle">Thanh toán phí vận chuyển</h2>
					<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>
				</div>
				<div class="modal-body">
					<div id="pay-error" class="alert alert-danger d-none" role="alert"></div>
					<dl class="row mb-3">
						<dt class="col-5 text-muted fw-normal">Mã vận đơn</dt>
						<dd class="col-7 fw-semibold" id="pay-tracking">—</dd>
						<dt class="col-5 text-muted fw-normal">Số tiền</dt>
						<dd class="col-7 fw-bold fs-5 text-primary mb-0" id="pay-amount">—</dd>
					</dl>
					<div class="alert alert-info small mb-0">
						Đây là cổng thanh toán <strong>mô phỏng</strong> dùng cho đồ án. Bấm
						"Thanh toán ngay" sẽ ghi nhận thanh toán thành công, không trừ tiền thật.
					</div>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Để sau</button>
					<button type="button" class="btn btn-primary" id="pay-confirm">Thanh toán ngay</button>
				</div>
			</div>
		</div>
	</div>

	<script>
		window.APP_CONTEXT_PATH = "${pageContext.request.contextPath}";
	</script>
	<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
	<script src="${pageContext.request.contextPath}/js/auth.js"></script>
	<script src="${pageContext.request.contextPath}/js/api.js"></script>
	<script src="${pageContext.request.contextPath}/js/profile.js"></script>
	<%-- SockJS + STOMP + notification bell — must load before inline IIFE --%>
	<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
	<script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
	<script src="${pageContext.request.contextPath}/js/notification.js"></script>
	<script>
		(function () {
			"use strict";

			var user = window.Auth.requireRole(["USER"]);
			if (!user) { return; }

			document.body.hidden = false;
			document.getElementById("customer-name").textContent = user.username;
			document.getElementById("logout-button").addEventListener("click", function () {
				window.Auth.logout();
			});

			var message = document.getElementById("orders-message");
			var empty = document.getElementById("orders-empty");
			var content = document.getElementById("orders-content");
			var list = document.getElementById("orders-list");
			var summary = document.getElementById("orders-summary");

			var payModalEl = document.getElementById("payModal");
			var payModal = new bootstrap.Modal(payModalEl);
			var trackingModal = new bootstrap.Modal(document.getElementById("trackingModal"));
			var trackingContent = document.getElementById("tracking-content");
			var payError = document.getElementById("pay-error");
			var payConfirm = document.getElementById("pay-confirm");
			var payingShipment = null;

			var statuses = {
				READY: "Chờ xử lý",
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

			function showMessage(type, text) {
				message.textContent = text;
				message.className = "alert alert-" + type;
			}

			function addCell(row, value) {
				var cell = document.createElement("td");
				cell.textContent = value == null || value === "" ? "—" : value;
				row.appendChild(cell);
				return cell;
			}

			function formatDate(value) {
				if (!value) { return "—"; }
				var date = Array.isArray(value)
					? new Date(value[0], value[1] - 1, value[2], value[3] || 0, value[4] || 0)
					: new Date(value);
				return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString("vi-VN");
			}

			function formatFee(value) {
				if (value == null) { return "—"; }
				return new Intl.NumberFormat("vi-VN", {
					style: "currency",
					currency: "VND",
					maximumFractionDigits: 0
				}).format(value);
			}

			function statusBadge(status) {
				var badge = document.createElement("span");
				badge.className = "badge " + (statusClasses[status] || "text-bg-secondary");
				badge.textContent = statuses[status] || status || "Không rõ";
				return badge;
			}

			function renderSummary(shipments) {
				document.getElementById("stat-total").textContent = String(shipments.length);
				document.getElementById("stat-delivering").textContent = String(shipments.filter(function (shipment) {
					return shipment.status === "DELIVERING";
				}).length);
				document.getElementById("stat-delivered").textContent = String(shipments.filter(function (shipment) {
					return shipment.status === "DELIVERED";
				}).length);
				summary.classList.remove("d-none");
			}

			function openTracking(shipment) {
				trackingContent.replaceChildren();
				var loading = document.createElement("div");
				loading.className = "text-center text-muted py-4";
				loading.innerHTML = '<span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>Đang tải hành trình...';
				trackingContent.appendChild(loading);
				document.getElementById("trackingModalTitle").textContent = shipment.trackingCode;
				trackingModal.show();

				api("/api/shipments/tracking/" + encodeURIComponent(shipment.trackingCode))
					.then(function (tracking) {
						trackingContent.replaceChildren();

						var current = document.createElement("div");
						current.className = "d-flex flex-wrap align-items-center gap-2 mb-4";
						var currentLabel = document.createElement("span");
						currentLabel.className = "small text-muted";
						currentLabel.textContent = "Trạng thái hiện tại:";
						current.appendChild(currentLabel);
						current.appendChild(statusBadge(tracking.status));
						trackingContent.appendChild(current);

						var events = Array.isArray(tracking.events) ? tracking.events.slice().reverse() : [];
						if (!events.length) {
							var noEvents = document.createElement("p");
							noEvents.className = "text-muted mb-0";
							noEvents.textContent = "Chưa có cập nhật hành trình cho vận đơn này.";
							trackingContent.appendChild(noEvents);
							return;
						}

						var timeline = document.createElement("ol");
						timeline.className = "timeline";
						events.forEach(function (event, index) {
							var item = document.createElement("li");
							item.className = "tl-item" + (index === events.length - 1 ? " tl-current" : "");
							var dot = document.createElement("span");
							dot.className = "tl-dot";
							dot.setAttribute("aria-hidden", "true");
							item.appendChild(dot);

							var details = document.createElement("div");
							details.className = "pb-2";
							var title = document.createElement("div");
							title.className = "fw-semibold";
							title.textContent = event.statusText || statuses[event.status] || event.status || "Cập nhật trạng thái";
							details.appendChild(title);

							var meta = [event.location, formatDate(event.time)].filter(Boolean);
							if (meta.length) {
								var metaText = document.createElement("div");
								metaText.className = "small text-muted mt-1";
								metaText.textContent = meta.join(" · ");
								details.appendChild(metaText);
							}
							if (event.note) {
								var note = document.createElement("p");
								note.className = "small text-muted mb-0 mt-2";
								note.textContent = event.note;
								details.appendChild(note);
							}
							item.appendChild(details);
							timeline.appendChild(item);
						});
						trackingContent.appendChild(timeline);
					})
					.catch(function (error) {
						trackingContent.replaceChildren();
						var alert = document.createElement("div");
						alert.className = "alert alert-danger mb-0";
						alert.setAttribute("role", "alert");
						alert.textContent = error.message || "Không tải được hành trình. Vui lòng thử lại.";
						trackingContent.appendChild(alert);
					});
			}

			// shipmentId -> { paid: boolean, refunded: boolean, pending: payment | null }
			function indexPayments(payments) {
				var map = {};
				payments.forEach(function (p) {
					var entry = map[p.shipmentId] || (map[p.shipmentId] = {
						paid: false, refunded: false, pending: null
					});
					if (p.status === "PAID") {
						entry.paid = true;
					} else if (p.status === "REFUNDED") {
						entry.refunded = true;
					} else if (p.status === "PENDING" && !entry.pending) {
						entry.pending = p;
					}
				});
				return map;
			}

			function paymentBadge(shipment, info) {
				var badge = document.createElement("span");
				badge.className = "badge";

				if (info && info.refunded) {
					badge.classList.add("text-bg-info");
					badge.textContent = "Đã hoàn tiền";
				} else if (shipment.paymentStatus === "PAID" || (info && info.paid)) {
					badge.classList.add("text-bg-success");
					badge.textContent = shipment.paymentMethod === "WALLET"
						? "Đã trả bằng ví" : "Đã thanh toán";
				} else if (shipment.paymentMethod === "CASH") {
					badge.classList.add("text-bg-info");
					badge.textContent = "Tiền mặt";
				} else if (shipment.status === "READY") {
					badge.classList.add("text-bg-warning");
					badge.textContent = info && info.pending ? "Chờ thanh toán" : "Chưa thanh toán";
				} else {
					badge.classList.add("text-bg-light", "text-muted");
					badge.textContent = "—";
				}
				return badge;
			}

			function openPayModal(shipment) {
				payingShipment = shipment;
				payError.classList.add("d-none");
				payConfirm.disabled = false;
				payConfirm.textContent = "Thanh toán ngay";
				document.getElementById("pay-tracking").textContent = shipment.trackingCode;
				document.getElementById("pay-amount").textContent = formatFee(shipment.shippingFee);
				payModal.show();
			}

			payConfirm.addEventListener("click", async function () {
				if (!payingShipment) { return; }

				payError.classList.add("d-none");
				payConfirm.disabled = true;
				payConfirm.textContent = "Đang xử lý...";

				try {
					// 1) create (or reuse the pending) payment, 2) simulate the gateway callback
					var payment = await api("/api/payments", {
						method: "POST",
						body: { shipmentId: payingShipment.id }
					});
					await api("/api/payments/" + encodeURIComponent(payment.id) + "/simulate", {
						method: "POST"
					});

					payModal.hide();
					showMessage("success", "Thanh toán thành công cho vận đơn " + payingShipment.trackingCode
						+ ". Đơn đã sẵn sàng để quản lý phân công shipper.");
					payingShipment = null;
					await loadOrders(true);
				} catch (error) {
					payError.textContent = error.message || "Thanh toán thất bại. Vui lòng thử lại.";
					payError.classList.remove("d-none");
					payConfirm.disabled = false;
					payConfirm.textContent = "Thanh toán ngay";
				}
			});

			function renderRow(shipment, info) {
				var row = document.createElement("tr");
				var codeCell = addCell(row, shipment.trackingCode);
				codeCell.classList.add("order-tracking-code");
				addCell(row, shipment.receiverName);
				addCell(row, [shipment.deliveryDistrict, shipment.deliveryProvince].filter(Boolean).join(", "));
				var statusCell = document.createElement("td");
				statusCell.appendChild(statusBadge(shipment.status));
				row.appendChild(statusCell);
				addCell(row, formatFee(shipment.shippingFee));

				var payCell = document.createElement("td");
				payCell.appendChild(paymentBadge(shipment, info));
				row.appendChild(payCell);

				addCell(row, formatDate(shipment.createdAt));

				var actionCell = document.createElement("td");
				actionCell.className = "text-end text-nowrap";
				var trackButton = document.createElement("button");
				trackButton.type = "button";
				trackButton.className = "btn btn-sm btn-outline-primary";
				trackButton.innerHTML = '<i class="bi bi-geo-alt me-1" aria-hidden="true"></i>Theo dõi';
				trackButton.setAttribute("aria-label", "Theo dõi vận đơn " + shipment.trackingCode);
				trackButton.addEventListener("click", function () { openTracking(shipment); });
				actionCell.appendChild(trackButton);
				var needsPayment = shipment.status === "READY"
					&& !(info && info.paid)
					&& shipment.paymentStatus !== "PAID"
					&& shipment.paymentMethod !== "CASH"
					&& Number(shipment.shippingFee) > 0;
				if (needsPayment) {
					var button = document.createElement("button");
					button.type = "button";
					button.className = "btn btn-sm btn-primary ms-2";
					button.innerHTML = '<i class="bi bi-credit-card me-1" aria-hidden="true"></i>Thanh toán';
					button.addEventListener("click", function () { openPayModal(shipment); });
					actionCell.appendChild(button);
				}
				var hideButton = document.createElement("button");
				hideButton.type = "button";
				hideButton.className = "btn btn-sm btn-outline-secondary ms-2";
				hideButton.innerHTML = '<i class="bi bi-eye-slash me-1" aria-hidden="true"></i>Ẩn';
				hideButton.addEventListener("click", function () {
					if (!window.confirm("Ẩn vận đơn " + shipment.trackingCode + " khỏi danh sách của bạn?")) {
						return;
					}
					hideButton.disabled = true;
					api("/api/shipments/" + encodeURIComponent(shipment.id) + "/view", { method: "DELETE" })
						.then(function () {
							showMessage("success", "Đã ẩn vận đơn " + shipment.trackingCode + " khỏi danh sách của bạn.");
							return loadOrders(true);
						})
						.catch(function (error) {
							showMessage("danger", error.message || "Không thể ẩn vận đơn. Vui lòng thử lại.");
							hideButton.disabled = false;
						});
				});
				actionCell.appendChild(hideButton);
				row.appendChild(actionCell);

				return row;
			}

			async function loadOrders(keepMessage) {
				if (!keepMessage) { message.className = "alert d-none"; }
				list.innerHTML = "";
				empty.classList.add("d-none");
				content.classList.add("d-none");
				summary.classList.add("d-none");

				try {
					var results = await Promise.all([
						api("/api/shipments/my"),
						api("/api/payments/my")
					]);
					var shipments = results[0];
					var payments = results[1];

					if (!Array.isArray(shipments)) {
						throw new Error("Không thể đọc danh sách đơn hàng.");
					}
					renderSummary(shipments);
					if (shipments.length === 0) {
						empty.classList.remove("d-none");
						return;
					}

					var byShipment = indexPayments(Array.isArray(payments) ? payments : []);
					shipments.forEach(function (shipment) {
						list.appendChild(renderRow(shipment, byShipment[shipment.id]));
					});
					content.classList.remove("d-none");
				} catch (error) {
					showMessage("danger", error.message || "Không tải được đơn hàng. Vui lòng thử lại.");
				}
			}

			loadOrders(false);

			// ── WebSocket notifications ───────────────────────────
			window.Notifications.init(window.APP_CONTEXT_PATH || "");
		})();
	</script>
</body>
</html>