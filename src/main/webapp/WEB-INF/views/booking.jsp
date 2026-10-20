<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="vi">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<title>Tạo đơn gửi hàng - UTEExpress</title>
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
	<link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body class="bg-light" hidden>
	<nav class="navbar navbar-dark bg-primary">
		<div class="container">
			<a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/">UTEExpress</a>
			<div class="d-flex align-items-center gap-3">
				<a class="btn btn-outline-light btn-sm" href="${pageContext.request.contextPath}/my-orders">Đơn hàng của tôi</a>
				<span id="customer-name" class="text-white"></span>
				<button id="logout-button" class="btn btn-outline-light btn-sm" type="button">Đăng xuất</button>
			</div>
		</div>
	</nav>

	<main class="container py-5">
		<div class="mb-4">
			<h1 class="h2 mb-1">Tạo đơn gửi hàng</h1>
			<p class="text-muted mb-0">Nhập thông tin người gửi, người nhận và kiện hàng.</p>
		</div>

		<div id="booking-message" class="alert d-none" role="status" aria-live="polite"></div>

		<form id="booking-form" class="card shadow-sm">
			<div class="card-body p-4">
				<section class="mb-4">
					<h2 class="h5 mb-3">Thông tin người gửi</h2>
					<div class="row g-3">
						<div class="col-md-6">
							<label class="form-label" for="senderName">Họ tên người gửi</label>
							<input class="form-control" id="senderName" name="senderName" maxlength="100" autocomplete="name" required>
						</div>
						<div class="col-md-6">
							<label class="form-label" for="senderPhone">Số điện thoại</label>
							<input class="form-control" id="senderPhone" name="senderPhone" type="tel" maxlength="20" pattern="(?:\+84|0)\d{9,10}" title="Nhập số điện thoại bắt đầu bằng 0 hoặc +84" autocomplete="tel" required>
						</div>
						<div class="col-md-6">
							<label class="form-label" for="pickupAddress">Địa chỉ lấy hàng</label>
							<input class="form-control" id="pickupAddress" name="pickupAddress" maxlength="255" autocomplete="street-address" required>
						</div>
						<div class="col-md-3">
							<label class="form-label" for="pickupDistrict">Quận/huyện</label>
							<input class="form-control" id="pickupDistrict" name="pickupDistrict" maxlength="100" autocomplete="address-level2">
						</div>
						<div class="col-md-3">
							<label class="form-label" for="pickupProvince">Tỉnh/thành phố</label>
							<input class="form-control" id="pickupProvince" name="pickupProvince" maxlength="100" autocomplete="address-level1" required>
						</div>
					</div>
				</section>

				<section class="mb-4">
					<h2 class="h5 mb-3">Thông tin người nhận</h2>
					<div class="row g-3">
						<div class="col-md-6">
							<label class="form-label" for="receiverName">Họ tên người nhận</label>
							<input class="form-control" id="receiverName" name="receiverName" maxlength="100" required>
						</div>
						<div class="col-md-6">
							<label class="form-label" for="receiverPhone">Số điện thoại</label>
							<input class="form-control" id="receiverPhone" name="receiverPhone" type="tel" maxlength="20" pattern="(?:\+84|0)\d{9,10}" title="Nhập số điện thoại bắt đầu bằng 0 hoặc +84" required>
						</div>
						<div class="col-md-6">
							<label class="form-label" for="deliveryAddress">Địa chỉ giao hàng</label>
							<input class="form-control" id="deliveryAddress" name="deliveryAddress" maxlength="255" required>
						</div>
						<div class="col-md-3">
							<label class="form-label" for="deliveryDistrict">Quận/huyện</label>
							<input class="form-control" id="deliveryDistrict" name="deliveryDistrict" maxlength="100">
						</div>
						<div class="col-md-3">
							<label class="form-label" for="deliveryProvince">Tỉnh/thành phố</label>
							<input class="form-control" id="deliveryProvince" name="deliveryProvince" maxlength="100" required>
						</div>
					</div>
				</section>

				<section class="mb-4">
					<h2 class="h5 mb-3">Thông tin kiện hàng</h2>
					<div class="row g-3">
						<div class="col-md-6">
							<label class="form-label" for="itemDescription">Mô tả hàng hóa</label>
							<input class="form-control" id="itemDescription" name="itemDescription" maxlength="255" required>
						</div>
						<div class="col-md-3">
							<label class="form-label" for="weightKg">Khối lượng (kg)</label>
							<input class="form-control" id="weightKg" name="weightKg" type="number" min="0.01" max="50" step="0.01" required>
						</div>
						<div class="col-md-3">
							<label class="form-label" for="serviceType">Dịch vụ</label>
							<select class="form-select" id="serviceType" name="serviceType" required>
								<option value="STANDARD">Tiêu chuẩn</option>
								<option value="EXPRESS">Hỏa tốc</option>
							</select>
						</div>
						<div class="col-md-4">
							<label class="form-label" for="lengthCm">Dài (cm) <span class="text-muted">(không bắt buộc)</span></label>
							<input class="form-control" id="lengthCm" name="lengthCm" type="number" min="1" step="1">
						</div>
						<div class="col-md-4">
							<label class="form-label" for="widthCm">Rộng (cm) <span class="text-muted">(không bắt buộc)</span></label>
							<input class="form-control" id="widthCm" name="widthCm" type="number" min="1" step="1">
						</div>
						<div class="col-md-4">
							<label class="form-label" for="heightCm">Cao (cm) <span class="text-muted">(không bắt buộc)</span></label>
							<input class="form-control" id="heightCm" name="heightCm" type="number" min="1" step="1">
						</div>
						<div class="col-md-6">
							<label class="form-label" for="codAmount">Tiền thu hộ (VND)</label>
							<input class="form-control" id="codAmount" name="codAmount" type="number" min="0" step="1000" value="0">
						</div>
						<div class="col-md-6">
							<label class="form-label" for="note">Ghi chú <span class="text-muted">(không bắt buộc)</span></label>
							<input class="form-control" id="note" name="note" maxlength="500">
						</div>
					</div>
				</section>

				<div id="quote-result" class="alert alert-info d-none" role="status" aria-live="polite"></div>
				<div class="d-flex flex-wrap gap-2">
					<button id="quote-button" class="btn btn-outline-primary" type="button">Tính phí vận chuyển</button>
					<button id="submit-button" class="btn btn-primary" type="submit">Tạo đơn gửi hàng</button>
				</div>
				<p class="form-text mt-3 mb-0">Phí sẽ được tính lại khi tạo đơn. Bạn có thể bổ sung kích thước kiện hàng để tính khối lượng quy đổi.</p>
			</div>
		</form>
	</main>

	<script>
		window.APP_CONTEXT_PATH = '<%= request.getContextPath() %>';
	</script>
	<script src="${pageContext.request.contextPath}/js/auth.js"></script>
	<script src="${pageContext.request.contextPath}/js/api.js"></script>
	<script>
		(function () {
			"use strict";

			var user = window.Auth.requireRole(["USER"]);
			if (!user) { return; }

			var form = document.getElementById("booking-form");
			var message = document.getElementById("booking-message");
			var quoteResult = document.getElementById("quote-result");
			var quoteButton = document.getElementById("quote-button");
			var submitButton = document.getElementById("submit-button");
			document.body.hidden = false;
			document.getElementById("customer-name").textContent = user.username;

			document.getElementById("logout-button").addEventListener("click", function () {
				window.Auth.logout();
			});

			function readForm() {
				var data = Object.fromEntries(new FormData(form).entries());
				["weightKg", "codAmount"].forEach(function (field) {
					data[field] = data[field] === "" ? null : Number(data[field]);
				});
				["lengthCm", "widthCm", "heightCm"].forEach(function (field) {
					data[field] = data[field] === "" ? null : Number(data[field]);
				});
				return data;
			}

			function showMessage(text, type) {
				message.textContent = text;
				message.className = "alert alert-" + type;
			}

			function formatMoney(value) {
				return new Intl.NumberFormat("vi-VN", {
					style: "currency",
					currency: "VND",
					maximumFractionDigits: 0
				}).format(value);
			}

			quoteButton.addEventListener("click", async function () {
				if (!form.reportValidity()) { return; }
				quoteButton.disabled = true;
				quoteButton.textContent = "Đang tính...";
				quoteResult.classList.add("d-none");
				try {
					var quote = await window.apiRequest(window.Auth.url("/api/shipments/quote"), {
						method: "POST",
						body: readForm()
					});
					quoteResult.textContent = "Phí dự kiến: " + formatMoney(quote.totalFee)
						+ " · Thời gian dự kiến: " + quote.minDays + "–" + quote.maxDays + " ngày";
					quoteResult.classList.remove("d-none");
				} catch (error) {
					showMessage(error.message || "Không tính được phí vận chuyển.", "danger");
				} finally {
					quoteButton.disabled = false;
					quoteButton.textContent = "Tính phí vận chuyển";
				}
			});

			form.addEventListener("submit", async function (event) {
				event.preventDefault();
				if (!form.reportValidity()) { return; }
				submitButton.disabled = true;
				quoteButton.disabled = true;
				submitButton.textContent = "Đang tạo đơn...";
				message.className = "alert d-none";
				try {
					var shipment = await window.apiRequest(window.Auth.url("/api/shipments/booking"), {
						method: "POST",
						body: readForm()
					});
					showMessage("Tạo đơn thành công. Mã vận đơn: "
						+ shipment.trackingCode + ". Đang chuyển đến danh sách đơn hàng...", "success");
					window.setTimeout(function () {
						window.location.href = window.Auth.url("/my-orders");
					}, 1200);
				} catch (error) {
					showMessage(error.message || "Không tạo được đơn hàng. Vui lòng kiểm tra lại thông tin.", "danger");
					submitButton.disabled = false;
					quoteButton.disabled = false;
					submitButton.textContent = "Tạo đơn gửi hàng";
				}
			});
		})();
	</script>
</body>
</html>