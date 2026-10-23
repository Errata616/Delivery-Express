<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Quản lý - UTEExpress</title>

<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
	rel="stylesheet">
<link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>

<body class="bg-light" data-allowed-roles="MANAGER,ADMIN" hidden>

	<nav class="navbar navbar-dark bg-primary">
		<div class="container">
			<a class="navbar-brand fw-bold"
				href="${pageContext.request.contextPath}/">UTEExpress</a> <span
				class="navbar-text text-white me-auto ms-3">Bảng điều khiển
				quản lý</span>
			<div class="d-flex align-items-center gap-3">
				<span id="whoami" class="text-white small"></span>
				<%-- Notification bell --%>
				<div id="notif-bell-container"></div>
				<button id="logoutBtn" type="button"
					class="btn btn-sm btn-outline-light">Đăng xuất</button>
			</div>
		</div>
	</nav>

	<main class="container my-4">

		<div id="alertBox"></div>

		<div class="d-flex justify-content-between align-items-center mb-3">
			<h4 class="mb-0">Tổng quan vận hành</h4>
			<button id="refreshBtn" type="button"
				class="btn btn-sm btn-outline-primary">Làm mới</button>
		</div>

		<div class="row g-3 mb-4">
			<div class="col-6 col-lg-3">
				<div class="card shadow-sm h-100">
					<div class="card-body">
						<div class="text-muted small">Tổng vận đơn</div>
						<div class="fs-3 fw-bold" id="statTotalShipments">-</div>
					</div>
				</div>
			</div>
			<div class="col-6 col-lg-3">
				<div class="card shadow-sm h-100">
					<div class="card-body">
						<div class="text-muted small">Giao thành công hôm nay</div>
						<div class="fs-3 fw-bold" id="statDeliveredToday">-</div>
					</div>
				</div>
			</div>
			<div class="col-6 col-lg-3">
				<div class="card shadow-sm h-100">
					<div class="card-body">
						<div class="text-muted small">COD đang chờ thu</div>
						<div class="fs-4 fw-bold" id="statPendingCod">-</div>
					</div>
				</div>
			</div>
			<div class="col-6 col-lg-3">
				<div class="card shadow-sm h-100">
					<div class="card-body">
						<div class="text-muted small">Shipper</div>
						<div class="fs-3 fw-bold" id="statShippers">-</div>
					</div>
				</div>
			</div>
		</div>

		<div class="card shadow-sm mb-4">
			<div class="card-header fw-semibold">Vận đơn theo trạng thái</div>
			<ul class="list-group list-group-flush" id="shipmentStatusList"></ul>
		</div>

		<div class="card shadow-sm mb-4">
			<div
				class="card-header fw-semibold d-flex justify-content-between">
				<span>Vận đơn hiện tại</span> <span
					class="badge bg-warning text-dark" id="currentShipmentCount">0</span>
			</div>
			<div class="table-responsive">
				<table class="table table-hover align-middle mb-0">
					<thead class="table-light">
						<tr>
							<th>Mã vận đơn</th>
							<th>Người nhận</th>
							<th>Khu vực giao</th>
							<th>Dịch vụ</th>
							<th>Phí</th>
							<th>COD</th>
							<th>Ngày tạo</th>
							<th>Trạng thái</th>
							<th style="min-width: 260px">Phân công</th>
						</tr>
					</thead>
					<tbody id="currentShipmentsBody">
						<tr>
							<td colspan="9" class="text-center text-muted">Đang tải...</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<div class="card shadow-sm">
			<div class="card-header fw-semibold">Danh sách shipper</div>
			<div class="table-responsive">
				<table class="table table-sm align-middle mb-0">
					<thead class="table-light">
						<tr>
							<th>ID</th>
							<th>Họ tên</th>
							<th>Điện thoại</th>
							<th>Trạng thái</th>
						</tr>
					</thead>
					<tbody id="shipperBody">
						<tr>
							<td colspan="4" class="text-center text-muted">Đang tải...</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

	</main>

	<script>
		window.APP_CONTEXT_PATH = '<%= request.getContextPath() %>';
	</script>
	<script
		src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
	<script src="${pageContext.request.contextPath}/js/auth.js"></script>
	<script src="${pageContext.request.contextPath}/js/api.js"></script>
	<%-- WebSocket notification bell --%>
	<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
	<script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
	<script src="${pageContext.request.contextPath}/js/notification.js"></script>
	<script src="${pageContext.request.contextPath}/js/admin.js"></script>
	<script>
		document.addEventListener("DOMContentLoaded", function () {
			if (window.Notifications) {
				window.Notifications.init(window.APP_CONTEXT_PATH || "");
			}
		});
	</script>
</body>
</html>