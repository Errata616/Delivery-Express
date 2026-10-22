<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>UTEExpress - Giao nhận nhanh chóng, tin cậy</title>

<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link
	href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&display=swap"
	rel="stylesheet">
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
	rel="stylesheet">
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css"
	rel="stylesheet">
<link
	href="${pageContext.request.contextPath}/css/style.css"
	rel="stylesheet">
</head>

<body>

	<!-- ===================== NAVBAR ===================== -->
	<nav class="navbar navbar-expand-lg navbar-dark site-nav sticky-top">
		<div class="container">
			<a class="navbar-brand fw-bold d-flex align-items-center"
				href="${pageContext.request.contextPath}/"> <span
				class="brand-mark"><i class="bi bi-box-seam-fill"></i></span>
				UTEExpress
			</a>
			<button class="navbar-toggler" type="button"
				data-bs-toggle="collapse" data-bs-target="#mainNav"
				aria-controls="mainNav" aria-expanded="false"
				aria-label="Mở menu">
				<span class="navbar-toggler-icon"></span>
			</button>

			<div class="collapse navbar-collapse" id="mainNav">
				<ul class="navbar-nav me-auto ms-lg-4">
					<li class="nav-item"><a class="nav-link" href="#tra-cuu">Tra
							cứu</a></li>
					<li class="nav-item"><a class="nav-link" href="#tinh-cuoc">Tính
							cước</a></li>
					<li class="nav-item"><a class="nav-link" href="#dich-vu">Dịch
							vụ</a></li>
					<li class="nav-item"><a class="nav-link" href="#quy-trinh">Quy
							trình</a></li>
				</ul>

				<div id="navGuest" class="d-flex gap-2 mt-2 mt-lg-0">
					<a class="btn btn-sm btn-outline-light"
						href="${pageContext.request.contextPath}/login">Đăng nhập</a> <a
						class="btn btn-sm btn-accent"
						href="${pageContext.request.contextPath}/register">Đăng ký</a>
				</div>

				<div id="navUser"
					class="d-none align-items-center gap-2 mt-2 mt-lg-0">
					<span id="navName" class="text-white small"></span> <a
						id="navDashboard" class="btn btn-sm btn-light" href="#">Bảng
						điều khiển</a>
					<button id="navLogout" type="button"
						class="btn btn-sm btn-outline-light">Đăng xuất</button>
				</div>
			</div>
		</div>
	</nav>

	<!-- ===================== HERO ===================== -->
	<header class="hero">
		<div class="container position-relative" style="z-index: 1">
			<div class="row align-items-center g-5">
				<div class="col-lg-7">
					<span
						class="badge rounded-pill text-bg-light mb-3 px-3 py-2 fw-semibold"><i
						class="bi bi-lightning-charge-fill text-warning"></i> Giao nhận
						tại TP. Hồ Chí Minh</span>
					<h1 class="display-4 mb-3">
						Gửi hàng <span>nhanh chóng</span>,<br>theo dõi từng bước
					</h1>
					<p class="lead mb-4">UTEExpress kết nối người gửi, bưu cục và
						shipper tại TP. Hồ Chí Minh trên một hệ thống: đặt đơn, thanh toán,
						lấy hàng và theo dõi hành trình ngay trên website.</p>

					<div id="heroGuest" class="d-flex flex-wrap gap-2">
						<a class="btn btn-accent btn-lg px-4"
							href="${pageContext.request.contextPath}/register">Bắt đầu
							miễn phí</a> <a class="btn btn-outline-light btn-lg px-4"
							href="#tra-cuu">Tra cứu vận đơn</a>
					</div>
					<div id="heroUser" class="d-none flex-wrap gap-2">
						<a id="heroDashboard" class="btn btn-accent btn-lg px-4" href="#">Vào
							bảng điều khiển</a> <a class="btn btn-outline-light btn-lg px-4"
							href="#tra-cuu">Tra cứu vận đơn</a>
					</div>
				</div>

				<div class="col-lg-5 d-none d-lg-block">
					<div class="glass-card">
						<div class="d-flex justify-content-between align-items-center mb-2">
							<div class="fw-bold">Hành trình mẫu</div>
							<span class="badge text-bg-light">Ví dụ</span>
						</div>
						<div class="mini-step on">
							<span class="mini-dot"></span>
							<div>
								<div class="fw-semibold">Đang giao hàng</div>
								<div class="small opacity-75">Shipper đang trên đường tới
									người nhận</div>
							</div>
						</div>
						<div class="mini-step">
							<span class="mini-dot"></span>
							<div>
								<div class="fw-semibold">Đã lấy hàng</div>
								<div class="small opacity-75">Shipper đã nhận kiện từ
									người gửi</div>
							</div>
						</div>
						<div class="mini-step">
							<span class="mini-dot"></span>
							<div>
								<div class="fw-semibold">Đã phân công shipper</div>
								<div class="small opacity-75">Đơn hàng được thanh toán và
									điều phối</div>
							</div>
						</div>
						<div class="mini-step">
							<span class="mini-dot"></span>
							<div>
								<div class="fw-semibold">Tạo vận đơn</div>
								<div class="small opacity-75">Hệ thống sinh mã vận đơn</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</header>

	<!-- ===================== TRACKING ===================== -->
	<section id="tra-cuu" class="track-wrap">
		<div class="container">
			<div class="panel-card p-4 p-md-5">
				<div class="row align-items-center g-3">
					<div class="col-lg-4">
						<div class="section-eyebrow">Tra cứu</div>
						<h2 class="h3 section-title mb-1">Vận đơn của bạn đang ở đâu?</h2>
						<p class="text-muted mb-0">Nhập mã vận đơn, không cần đăng
							nhập.</p>
					</div>
					<div class="col-lg-8">
						<form id="trackForm" class="input-group input-group-lg">
							<span class="input-group-text bg-white border-end-0"
								style="border-radius: .8rem 0 0 .8rem"><i
								class="bi bi-search text-muted"></i></span> <input id="trackCode"
								type="text" class="form-control track-input border-start-0"
								placeholder="Nhập mã vận đơn, ví dụ UTE..." maxlength="40"
								autocomplete="off" aria-label="Mã vận đơn" required>
							<button id="trackBtn" class="btn btn-primary track-btn px-4"
								type="submit">Tra cứu</button>
						</form>
					</div>
				</div>

				<div id="trackResult" class="mt-4" aria-live="polite"></div>
			</div>
		</div>
	</section>

	<!-- ===================== CALCULATOR ===================== -->
	<section id="tinh-cuoc" class="py-5 mt-4">
		<div class="container">
			<div class="row g-4 align-items-stretch">
				<div class="col-lg-5">
					<div class="section-eyebrow">Tính cước</div>
					<h2 class="section-title mb-3">Biết trước phí gửi hàng</h2>
					<p class="text-muted">Dịch vụ hiện áp dụng cho các quận, huyện
						trong TP. Hồ Chí Minh. Phí tính theo khối lượng quy đổi, dịch vụ
						giao và tiền thu hộ COD.</p>
					<ul class="list-unstyled text-muted">
						<li class="mb-2"><i class="bi bi-check-circle-fill text-success"></i>
							Quy đổi khối lượng theo kích thước (tuỳ chọn)</li>
						<li class="mb-2"><i class="bi bi-check-circle-fill text-success"></i>
							Phụ phí giao hỏa tốc rõ ràng</li>
						<li class="mb-2"><i class="bi bi-check-circle-fill text-success"></i>
							Phí thu hộ COD minh bạch</li>
					</ul>
				</div>

				<div class="col-lg-7">
					<div class="panel-card p-4">
						<form id="quoteForm" novalidate>
							<div class="row g-3">
								<div class="col-md-6">
									<label class="form-label small fw-semibold">Gửi từ (quận/huyện TP.HCM)</label>
									<input name="from" class="form-control" list="districtList"
										placeholder="VD: Quận 1" required>
								</div>
								<div class="col-md-6">
									<label class="form-label small fw-semibold">Giao đến (quận/huyện TP.HCM)</label>
									<input name="to" class="form-control" list="districtList"
										placeholder="VD: Thủ Đức" required>
								</div>
								<div class="col-md-4">
									<label class="form-label small fw-semibold">Khối lượng (kg)</label>
									<input name="weight" type="number" class="form-control"
										min="0.01" max="50" step="0.01" placeholder="0.5" required>
								</div>
								<div class="col-md-4">
									<label class="form-label small fw-semibold">Dịch vụ</label> <select
										name="service" class="form-select">
										<option value="STANDARD">Tiêu chuẩn</option>
										<option value="EXPRESS">Hỏa tốc</option>
									</select>
								</div>
								<div class="col-md-4">
									<label class="form-label small fw-semibold">Thu hộ COD
										(VNĐ)</label> <input name="cod" type="number" class="form-control"
										min="0" step="1000" placeholder="0">
								</div>
								<div class="col-12">
									<label class="form-label small fw-semibold">Kích thước
										(cm, tuỳ chọn)</label>
									<div class="row g-2">
										<div class="col-4">
											<input name="length" type="number" min="1"
												class="form-control" placeholder="Dài">
										</div>
										<div class="col-4">
											<input name="width" type="number" min="1"
												class="form-control" placeholder="Rộng">
										</div>
										<div class="col-4">
											<input name="height" type="number" min="1"
												class="form-control" placeholder="Cao">
										</div>
									</div>
								</div>
								<div class="col-12">
									<button id="quoteBtn" type="submit"
										class="btn btn-primary w-100 btn-lg">Tính cước phí</button>
								</div>
							</div>
						</form>

						<div id="quoteResult" class="mt-3" aria-live="polite"></div>
					</div>

					<datalist id="districtList">
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
				</div>
			</div>
		</div>
	</section>

	<!-- ===================== SERVICES ===================== -->
	<section id="dich-vu" class="py-5 bg-white">
		<div class="container">
			<div class="text-center mb-5">
				<div class="section-eyebrow">Dịch vụ</div>
				<h2 class="section-title">Mọi thứ bạn cần cho một đơn hàng</h2>
			</div>

			<div class="row g-4">
				<div class="col-md-6 col-lg-4">
					<div class="card service-card p-4">
						<span class="icon-badge mb-3"><i class="bi bi-truck"></i></span>
						<h3 class="h5 fw-bold">Giao tiêu chuẩn</h3>
						<p class="text-muted mb-0">Giao nhận nội thành TP. Hồ Chí Minh,
							có thời gian giao dự kiến ngay khi báo giá.</p>
					</div>
				</div>
				<div class="col-md-6 col-lg-4">
					<div class="card service-card p-4">
						<span class="icon-badge orange mb-3"><i
							class="bi bi-lightning-charge"></i></span>
						<h3 class="h5 fw-bold">Giao hỏa tốc</h3>
						<p class="text-muted mb-0">Ưu tiên xử lý và giao nhanh cho đơn
							gấp, với phụ phí hiển thị rõ trước khi đặt.</p>
					</div>
				</div>
				<div class="col-md-6 col-lg-4">
					<div class="card service-card p-4">
						<span class="icon-badge mb-3"><i class="bi bi-cash-coin"></i></span>
						<h3 class="h5 fw-bold">Thu hộ COD</h3>
						<p class="text-muted mb-0">Shipper thu tiền người nhận giúp
							bạn, theo dõi khoản COD đang chờ thu trên hệ thống.</p>
					</div>
				</div>
				<div class="col-md-6 col-lg-4">
					<div class="card service-card p-4">
						<span class="icon-badge orange mb-3"><i
							class="bi bi-geo-alt"></i></span>
						<h3 class="h5 fw-bold">Theo dõi hành trình</h3>
						<p class="text-muted mb-0">Mỗi thay đổi trạng thái đều được ghi
							lại và hiển thị theo dòng thời gian cho người gửi.</p>
					</div>
				</div>
				<div class="col-md-6 col-lg-4">
					<div class="card service-card p-4">
						<span class="icon-badge mb-3"><i class="bi bi-bell"></i></span>
						<h3 class="h5 fw-bold">Thông báo tức thì</h3>
						<p class="text-muted mb-0">Nhận thông báo khi đơn được phân
							công hoặc đổi trạng thái, không cần tải lại trang.</p>
					</div>
				</div>
				<div class="col-md-6 col-lg-4">
					<div class="card service-card p-4">
						<span class="icon-badge orange mb-3"><i
							class="bi bi-star"></i></span>
						<h3 class="h5 fw-bold">Đánh giá shipper</h3>
						<p class="text-muted mb-0">Chấm điểm sau khi nhận hàng để giữ
							chất lượng giao nhận luôn ở mức tốt.</p>
					</div>
				</div>
			</div>
		</div>
	</section>

	<!-- ===================== HOW IT WORKS ===================== -->
	<section id="quy-trinh" class="py-5">
		<div class="container">
			<div class="text-center mb-5">
				<div class="section-eyebrow">Quy trình</div>
				<h2 class="section-title">Gửi hàng chỉ với 4 bước</h2>
			</div>

			<div class="row g-4 text-center steps-row">
				<div class="col-6 col-lg-3 step-col">
					<span class="step-num mb-3">1</span>
					<h3 class="h6 fw-bold">Tạo đơn</h3>
					<p class="small text-muted">Nhập thông tin người gửi, người
						nhận và kiện hàng.</p>
				</div>
				<div class="col-6 col-lg-3 step-col">
					<span class="step-num mb-3">2</span>
					<h3 class="h6 fw-bold">Thanh toán</h3>
					<p class="small text-muted">Thanh toán phí vận chuyển để đơn
						được điều phối.</p>
				</div>
				<div class="col-6 col-lg-3 step-col">
					<span class="step-num mb-3">3</span>
					<h3 class="h6 fw-bold">Shipper lấy hàng</h3>
					<p class="small text-muted">Quản lý phân công shipper tới lấy
						hàng tại địa chỉ của bạn.</p>
				</div>
				<div class="col-6 col-lg-3 step-col">
					<span class="step-num mb-3">4</span>
					<h3 class="h6 fw-bold">Giao &amp; theo dõi</h3>
					<p class="small text-muted">Theo dõi hành trình đến khi người
						nhận ký nhận.</p>
				</div>
			</div>
		</div>
	</section>

	<!-- ===================== CTA ===================== -->
	<section class="pb-5">
		<div class="container">
			<div
				class="cta-band p-4 p-md-5 d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-3">
				<div>
					<h2 class="h3 fw-bold mb-1">Sẵn sàng gửi đơn đầu tiên?</h2>
					<p class="mb-0 opacity-75">Tạo tài khoản trong vài giây và bắt
						đầu gửi hàng cùng UTEExpress.</p>
				</div>
				<div id="ctaGuest" class="d-flex gap-2">
					<a class="btn btn-accent btn-lg px-4"
						href="${pageContext.request.contextPath}/register">Đăng ký
						ngay</a> <a class="btn btn-outline-light btn-lg px-4"
						href="${pageContext.request.contextPath}/login">Đăng nhập</a>
				</div>
			</div>
		</div>
	</section>

	<!-- ===================== FOOTER ===================== -->
	<footer class="site-footer py-4">
		<div
			class="container d-flex flex-column flex-md-row justify-content-between align-items-center gap-2">
			<div>
				<strong class="text-white">UTEExpress</strong> - Hệ thống quản lý
				chuỗi giao nhận Logistic
			</div>
			<div class="small d-flex gap-3">
				<a href="#tra-cuu">Tra cứu</a> <a href="#tinh-cuoc">Tính cước</a>
				<a href="#dich-vu">Dịch vụ</a> <a href="#quy-trinh">Quy trình</a>
			</div>
		</div>
	</footer>

	<script src="${pageContext.request.contextPath}/js/api.js"></script>
	<script
		src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
	<script>
		(function () {
			"use strict";

			var ctx = "${pageContext.request.contextPath}";
			var form = document.getElementById("trackForm");
			var codeInput = document.getElementById("trackCode");
			var button = document.getElementById("trackBtn");
			var result = document.getElementById("trackResult");

			// The graph only covers Ho Chi Minh City, so skip other provinces.
			function isHcm(province) {
				var plain = String(province || "").normalize("NFD").replace(/[\u0300-\u036f]/g, "")
					.replace(/đ/gi, "d").toLowerCase();
				return /ho chi minh|hcm|sai gon/.test(plain);
			}

			async function loadRoute(tracking, box) {
				if (!tracking.pickupDistrict || !tracking.deliveryDistrict) { return; }
				if (!isHcm(tracking.pickupProvince) || !isHcm(tracking.deliveryProvince)) { return; }
				try {
					var route = await window.apiData(ctx + "/api/routes?from=" + encodeURIComponent(tracking.pickupDistrict)
						+ "&to=" + encodeURIComponent(tracking.deliveryDistrict), { auth: false });

					var block = document.createElement("div");
					block.className = "mt-3 p-3 rounded-4 border";
					var heading = document.createElement("div");
					heading.className = "small text-muted mb-2";
					heading.textContent = "Tuyến vận chuyển dự kiến (" + route.hops + " chặng)";
					block.appendChild(heading);

					route.path.forEach(function (district, index) {
						var badge = document.createElement("span");
						badge.className = "badge text-bg-light border";
						badge.textContent = district.name;
						block.appendChild(badge);
						if (index < route.path.length - 1) {
							var arrow = document.createElement("i");
							arrow.className = "bi bi-arrow-right text-muted mx-2";
							arrow.setAttribute("aria-hidden", "true");
							block.appendChild(arrow);
						}
					});
					box.appendChild(block);
				} catch (error) {
					// Districts outside the route graph have no route to display.
				}
			}

			form.addEventListener("submit", async function (event) {
				event.preventDefault();
				result.replaceChildren();
				button.disabled = true;
				button.textContent = "Đang tra cứu...";

				try {
					var tracking = await window.apiData(ctx + "/api/shipments/tracking/"
						+ encodeURIComponent(codeInput.value.trim()), { auth: false });
					var box = document.createElement("div");
					box.className = "p-3 rounded-4 border";

					var title = document.createElement("h3");
					title.className = "h5 mb-2";
					title.textContent = "Vận đơn " + tracking.trackingCode;
					box.appendChild(title);

					var status = document.createElement("p");
					status.className = "mb-0";
					status.textContent = "Trạng thái: " + (tracking.statusText || tracking.status || "Không rõ");
					box.appendChild(status);
					result.appendChild(box);
					await loadRoute(tracking, box);
				} catch (error) {
					var alert = document.createElement("div");
					alert.className = "alert alert-danger mb-0";
					alert.setAttribute("role", "alert");
					alert.textContent = error.message || "Không tìm thấy vận đơn. Vui lòng kiểm tra mã và thử lại.";
					result.appendChild(alert);
				} finally {
					button.disabled = false;
					button.textContent = "Tra cứu";
				}
			});
		})();
	</script>

</body>
</html>