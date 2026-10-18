<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="vi">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<title>Đơn hàng của tôi - UTEExpress</title>
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light" hidden>
	<nav class="navbar navbar-dark bg-primary">
		<div class="container">
			<a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/">UTEExpress</a>
			<div class="d-flex align-items-center gap-3">
				<a class="btn btn-outline-light btn-sm" href="${pageContext.request.contextPath}/booking">Tạo đơn mới</a>
				<span id="customer-name" class="text-white"></span>
				<button id="logout-button" class="btn btn-outline-light btn-sm" type="button">Đăng xuất</button>
			</div>
		</div>
	</nav>

	<main class="container py-5">
		<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
			<div>
				<h1 class="h2 mb-1">Đơn hàng của tôi</h1>
				<p class="text-muted mb-0">Theo dõi và thanh toán các vận đơn bạn đã tạo.</p>
			</div>
			<a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/">Về trang chủ</a>
		</div>

		<div id="orders-message" class="alert d-none" role="status" aria-live="polite"></div>
		<div id="orders-empty" class="card d-none">
			<div class="card-body text-center py-5">
				<h2 class="h5">Bạn chưa có đơn hàng nào</h2>
				<p class="text-muted mb-0">Các vận đơn bạn tạo sẽ xuất hiện tại đây.</p>
			</div>
		</div>
		<div id="orders-content" class="card d-none">
			<div class="table-responsive">
				<table class="table table-hover align-middle mb-0">
					<thead class="table-light">
						<tr>
							<th scope="col">Mã vận đơn</th>
							<th scope="col">Người nhận</th>
							<th scope="col">Nơi nhận</th>
							<th scope="col">Trạng thái</th>
							<th scope="col">Phí vận chuyển</th>
							<th scope="col">Thanh toán</th>
							<th scope="col">Ngày tạo</th>
							<th scope="col"></th>
						</tr>
					</thead>
					<tbody id="orders-list"></tbody>
				</table>
			</div>
		</div>
	</main>

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

			var payModalEl = document.getElementById("payModal");
			var payModal = new bootstrap.Modal(payModalEl);
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

			// shipmentId -> { paid: boolean, pending: payment | null }
			function indexPayments(payments) {
				var map = {};
				payments.forEach(function (p) {
					var entry = map[p.shipmentId] || (map[p.shipmentId] = { paid: false, pending: null });
					if (p.status === "PAID") {
						entry.paid = true;
					} else if (p.status === "PENDING" && !entry.pending) {
						entry.pending = p;
					}
				});
				return map;
			}

			function paymentBadge(shipment, info) {
				var badge = document.createElement("span");
				badge.className = "badge";

				if (info && info.paid) {
					badge.classList.add("text-bg-success");
					badge.textContent = "Đã thanh toán";
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
				addCell(row, shipment.trackingCode);
				addCell(row, shipment.receiverName);
				addCell(row, [shipment.deliveryDistrict, shipment.deliveryProvince].filter(Boolean).join(", "));
				addCell(row, statuses[shipment.status] || shipment.status);
				addCell(row, formatFee(shipment.shippingFee));

				var payCell = document.createElement("td");
				payCell.appendChild(paymentBadge(shipment, info));
				row.appendChild(payCell);

				addCell(row, formatDate(shipment.createdAt));

				var actionCell = document.createElement("td");
				actionCell.className = "text-end";
				var needsPayment = shipment.status === "READY"
					&& !(info && info.paid)
					&& Number(shipment.shippingFee) > 0;
				if (needsPayment) {
					var button = document.createElement("button");
					button.type = "button";
					button.className = "btn btn-sm btn-primary";
					button.textContent = "Thanh toán";
					button.addEventListener("click", function () { openPayModal(shipment); });
					actionCell.appendChild(button);
				}
				row.appendChild(actionCell);

				return row;
			}

			async function loadOrders(keepMessage) {
				if (!keepMessage) { message.className = "alert d-none"; }
				list.innerHTML = "";
				empty.classList.add("d-none");
				content.classList.add("d-none");

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
		})();
	</script>
</body>
</html>