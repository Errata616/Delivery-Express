<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Quản trị - UTEExpress</title>

<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
	rel="stylesheet">
</head>

<%-- hidden until admin.js confirms the user may see this page --%>
<body class="bg-light" data-allowed-roles="ADMIN" hidden>

	<nav class="navbar navbar-dark bg-primary">
		<div class="container">
			<a class="navbar-brand fw-bold"
				href="${pageContext.request.contextPath}/">UTEExpress</a> <span
				class="navbar-text text-white me-auto ms-3">Bảng điều khiển
				quản trị</span>
			<div class="d-flex align-items-center gap-3">
				<span id="whoami" class="text-white small"></span>
				<button id="logoutBtn" type="button"
					class="btn btn-sm btn-outline-light">Đăng xuất</button>
			</div>
		</div>
	</nav>

	<main class="container my-4">

		<div id="alertBox"></div>

		<div class="d-flex justify-content-between align-items-center mb-3">
			<h4 class="mb-0">Tổng quan</h4>
			<button id="refreshBtn" type="button"
				class="btn btn-sm btn-outline-primary">Làm mới</button>
		</div>

		<!-- Statistics -->
		<div class="row g-3 mb-4">
			<div class="col-6 col-lg-4">
				<div class="card shadow-sm h-100">
					<div class="card-body">
						<div class="text-muted small">Tổng vận đơn</div>
						<div class="fs-3 fw-bold" id="statTotalShipments">-</div>
					</div>
				</div>
			</div>
			<div class="col-6 col-lg-4">
				<div class="card shadow-sm h-100">
					<div class="card-body">
						<div class="text-muted small">Giao thành công hôm nay</div>
						<div class="fs-3 fw-bold" id="statDeliveredToday">-</div>
					</div>
				</div>
			</div>
			<div class="col-6 col-lg-4">
				<div class="card shadow-sm h-100">
					<div class="card-body">
						<div class="text-muted small">Doanh thu phí vận chuyển</div>
						<div class="fs-4 fw-bold" id="statRevenue">-</div>
					</div>
				</div>
			</div>
			<div class="col-6 col-lg-4">
				<div class="card shadow-sm h-100">
					<div class="card-body">
						<div class="text-muted small">COD đang chờ thu</div>
						<div class="fs-4 fw-bold" id="statPendingCod">-</div>
					</div>
				</div>
			</div>
			<div class="col-6 col-lg-4">
				<div class="card shadow-sm h-100">
					<div class="card-body">
						<div class="text-muted small">Khách hàng</div>
						<div class="fs-3 fw-bold" id="statCustomers">-</div>
					</div>
				</div>
			</div>
			<div class="col-6 col-lg-4">
				<div class="card shadow-sm h-100">
					<div class="card-body">
						<div class="text-muted small">Shipper</div>
						<div class="fs-3 fw-bold" id="statShippers">-</div>
					</div>
				</div>
			</div>
		</div>

		<div class="row g-3 mb-4">
			<div class="col-md-6">
				<div class="card shadow-sm h-100">
					<div class="card-header fw-semibold">Vận đơn theo trạng thái</div>
					<ul class="list-group list-group-flush" id="shipmentStatusList"></ul>
				</div>
			</div>
			<div class="col-md-6">
				<div class="card shadow-sm h-100">
					<div class="card-header fw-semibold">
						Đơn hàng theo trạng thái <span class="text-muted small">(tổng:
							<span id="statTotalOrders">-</span>)
						</span>
					</div>
					<ul class="list-group list-group-flush" id="orderStatusList"></ul>
				</div>
			</div>
		</div>

		<!-- Unassigned shipments -->
		<div class="card shadow-sm mb-4">
			<div
				class="card-header fw-semibold d-flex justify-content-between">
				<span>Vận đơn chờ phân công</span> <span
					class="badge bg-warning text-dark" id="unassignedCount">0</span>
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
							<th style="min-width: 260px">Phân công</th>
						</tr>
					</thead>
					<tbody id="unassignedBody">
						<tr>
							<td colspan="8" class="text-center text-muted">Đang tải...</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<div class="row g-3">
			<!-- Shippers -->
			<div class="col-lg-7">
				<div class="card shadow-sm h-100">
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
			</div>

			<!-- Create user -->
			<div class="col-lg-5">
				<div class="card shadow-sm h-100">
					<div class="card-header fw-semibold">Tạo tài khoản nhân sự</div>
					<div class="card-body">
						<form id="createUserForm" autocomplete="off">
							<div class="mb-2">
								<label class="form-label small">Tên đăng nhập</label> <input
									name="username" class="form-control form-control-sm"
									minlength="3" maxlength="50" required>
							</div>
							<div class="mb-2">
								<label class="form-label small">Email</label> <input
									name="email" type="email" class="form-control form-control-sm"
									maxlength="100" required>
							</div>
							<div class="mb-2">
								<label class="form-label small">Mật khẩu (8-72 ký tự)</label> <input
									name="password" type="password"
									class="form-control form-control-sm" minlength="8"
									maxlength="72" autocomplete="new-password" required>
							</div>
							<div class="mb-2">
								<label class="form-label small">Họ tên</label> <input
									name="fullName" class="form-control form-control-sm"
									maxlength="100" required>
							</div>
							<div class="mb-2">
								<label class="form-label small">Điện thoại</label> <input
									name="phone" class="form-control form-control-sm"
									maxlength="20">
							</div>
							<div class="mb-3">
								<label class="form-label small">Vai trò</label> <select
									name="role" class="form-select form-select-sm" required>
									<option value="SHIPPER">Shipper</option>
									<option value="MANAGER">Manager</option>
								</select>
							</div>
							<button type="submit" class="btn btn-primary btn-sm w-100">Tạo
								tài khoản</button>
						</form>
					</div>
				</div>
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
	<script src="${pageContext.request.contextPath}/js/admin.js"></script>
</body>
</html>