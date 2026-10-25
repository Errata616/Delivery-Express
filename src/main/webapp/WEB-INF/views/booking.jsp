<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="vi">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<title>Tạo đơn gửi hàng - UTEExpress</title>
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
				<a class="btn btn-outline-light btn-sm" href="${pageContext.request.contextPath}/my-orders">Đơn hàng của tôi</a>
				<span id="customer-name" class="text-white"></span>
				<div data-profile-actions class="d-flex align-items-center gap-2"></div>
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

		<section class="card shadow-sm mb-4" aria-labelledby="wallet-title">
			<div class="card-body p-4">
				<div class="d-flex flex-wrap justify-content-between align-items-start gap-3">
					<div>
						<h2 id="wallet-title" class="h5 mb-1">Ví ứng dụng</h2>
						<p class="text-muted mb-2">Số dư: <strong id="wallet-balance">Đang tải...</strong></p>
					</div>
					<div class="d-flex flex-wrap align-items-start gap-2">
						<div>
							<label class="visually-hidden" for="wallet-top-up-amount">Số tiền nạp (VND)</label>
							<input id="wallet-top-up-amount" class="form-control" type="number" min="1" max="50000000"
								step="1" placeholder="Số tiền nạp (VND)" aria-describedby="wallet-top-up-help">
							<div id="wallet-top-up-help" class="form-text">Tối đa 50.000.000 VND/lần</div>
						</div>
						<button id="wallet-top-up-button" class="btn btn-outline-primary" type="button">Nạp tiền</button>
					</div>
				</div>
				<div id="wallet-message" class="small mt-2" role="status" aria-live="polite"></div>
			</div>
		</section>

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
							<label class="form-label" for="pickupDistrict">Quận/huyện TP. Hồ Chí Minh</label>
							<input class="form-control" id="pickupDistrict" name="pickupDistrict" list="hcmDistrictList" maxlength="100" autocomplete="address-level2" required>
						</div>
						<div class="col-md-3">
							<label class="form-label" for="pickupProvince">Tỉnh/thành phố</label>
							<input class="form-control" id="pickupProvince" name="pickupProvince" value="TP. Hồ Chí Minh" maxlength="100" autocomplete="address-level1" readonly required>
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
							<label class="form-label" for="deliveryDistrict">Quận/huyện TP. Hồ Chí Minh</label>
							<input class="form-control" id="deliveryDistrict" name="deliveryDistrict" list="hcmDistrictList" maxlength="100" required>
						</div>
						<div class="col-md-3">
							<label class="form-label" for="deliveryProvince">Tỉnh/thành phố</label>
							<input class="form-control" id="deliveryProvince" name="deliveryProvince" value="TP. Hồ Chí Minh" maxlength="100" readonly required>
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

				<section class="mb-4">
					<h2 class="h5 mb-3">Phương thức thanh toán</h2>
					<div class="d-flex flex-column gap-2">
						<div class="form-check border rounded p-3 ps-5">
							<input class="form-check-input" type="radio" name="paymentMethod" id="payment-cash"
								value="CASH" checked>
							<label class="form-check-label fw-semibold" for="payment-cash">Tiền mặt</label>
							<div class="form-text">Thanh toán phí vận chuyển bằng tiền mặt.</div>
						</div>
						<div class="form-check border rounded p-3 ps-5">
							<input class="form-check-input" type="radio" name="paymentMethod" id="payment-wallet"
								value="WALLET" disabled>
							<label class="form-check-label fw-semibold" for="payment-wallet">Ví ứng dụng</label>
							<div id="wallet-payment-help" class="form-text">Tính phí vận chuyển để kiểm tra số dư ví.</div>
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
		<datalist id="hcmDistrictList">
			<option value="Quận 1">
			<option value="Quận 2">
			<option value="Quận 3">
			<option value="Quận 4">
			<option value="Quận 5">
			<option value="Quận 6">
			<option value="Quận 7">
			<option value="Quận 8">
			<option value="Quận 9">
			<option value="Quận 10">
			<option value="Quận 11">
			<option value="Quận 12">
			<option value="Thủ Đức">
			<option value="Gò Vấp">
			<option value="Bình Thạnh">
			<option value="Phú Nhuận">
			<option value="Tân Bình">
			<option value="Tân Phú">
			<option value="Bình Tân">
		</datalist>
	</main>

	<script>
		window.APP_CONTEXT_PATH = '<%= request.getContextPath() %>';
	</script>
	<script src="${pageContext.request.contextPath}/js/auth.js"></script>
	<script src="${pageContext.request.contextPath}/js/api.js"></script>
	<script src="${pageContext.request.contextPath}/js/profile.js"></script>
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
			var walletBalanceElement = document.getElementById("wallet-balance");
			var walletMessage = document.getElementById("wallet-message");
			var topUpAmount = document.getElementById("wallet-top-up-amount");
			var topUpButton = document.getElementById("wallet-top-up-button");
			var walletPayment = document.getElementById("payment-wallet");
			var walletPaymentHelp = document.getElementById("wallet-payment-help");
			var walletBalance = 0;
			var walletLoaded = false;
			var quotedFee = null;
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

			function updateWalletPaymentOption() {
				var canPayWithWallet = walletLoaded && (quotedFee === null || walletBalance >= quotedFee);
				walletPayment.disabled = !canPayWithWallet;
				if (quotedFee !== null && walletLoaded && canPayWithWallet) {
					walletPaymentHelp.textContent = "Số dư đủ để thanh toán phí vận chuyển "
						+ formatMoney(quotedFee) + " bằng ví.";
				} else if (quotedFee !== null && walletLoaded) {
					walletPaymentHelp.textContent = "Số dư không đủ cho phí "
						+ formatMoney(quotedFee) + ". Vui lòng nạp thêm hoặc chọn tiền mặt.";
					if (walletPayment.checked) {
						document.getElementById("payment-cash").checked = true;
					}
				} else if (walletLoaded) {
					walletPaymentHelp.textContent = "Bạn có thể chọn ví ngay; số dư sẽ được kiểm tra sau khi tính phí.";
				} else {
					walletPaymentHelp.textContent = "Đang tải số dư ví...";
				}
			}

			function loadWallet() {
				return window.apiData(window.Auth.url("/api/users/me/wallet"))
					.then(function (wallet) {
						walletBalance = Number(wallet.balance) || 0;
						walletLoaded = true;
						walletBalanceElement.textContent = formatMoney(walletBalance);
						updateWalletPaymentOption();
					});
			}

			topUpButton.addEventListener("click", async function () {
				var amount = Number(topUpAmount.value);
				if (!Number.isInteger(amount) || amount <= 0 || amount > 50000000) {
					walletMessage.className = "small mt-2 text-danger";
					walletMessage.textContent = "Nhập số tiền nguyên từ 1 đến 50.000.000 VND.";
					return;
				}

				topUpButton.disabled = true;
				walletMessage.className = "small mt-2 text-muted";
				walletMessage.textContent = "Đang nạp tiền...";
				try {
					var wallet = await window.apiData(window.Auth.url("/api/users/me/wallet/top-up"), {
						method: "POST",
						body: { amount: amount }
					});
					walletBalance = Number(wallet.balance) || 0;
					walletLoaded = true;
					walletBalanceElement.textContent = formatMoney(walletBalance);
					topUpAmount.value = "";
					walletMessage.className = "small mt-2 text-success";
					walletMessage.textContent = "Đã cộng tiền vào ví.";
					updateWalletPaymentOption();
				} catch (error) {
					walletMessage.className = "small mt-2 text-danger";
					walletMessage.textContent = error.message || "Không nạp được tiền vào ví.";
				} finally {
					topUpButton.disabled = false;
				}
			});

			loadWallet().catch(function (error) {
				walletBalanceElement.textContent = "Không tải được";
				walletMessage.className = "small mt-2 text-danger";
				walletMessage.textContent = error.message || "Không tải được số dư ví.";
			});

			form.querySelectorAll("input:not([name='paymentMethod']), select").forEach(function (field) {
				field.addEventListener("input", invalidateQuote);
				field.addEventListener("change", invalidateQuote);
			});

			function invalidateQuote() {
				quotedFee = null;
				updateWalletPaymentOption();
				quoteResult.classList.add("d-none");
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
					quotedFee = Number(quote.totalFee);
					updateWalletPaymentOption();
					quoteResult.textContent = "Phí dự kiến: " + formatMoney(quote.totalFee)
						+ " · Thời gian dự kiến: " + quote.minDays + "–" + quote.maxDays + " ngày";
					quoteResult.classList.remove("d-none");
				} catch (error) {
					quotedFee = null;
					updateWalletPaymentOption();
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