<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="vi">

<head>

<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">

<title>Đăng nhập - UTEExpress</title>

<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
	rel="stylesheet">

</head>

<body class="bg-light">

	<div class="container">

		<div class="row justify-content-center mt-5">

			<div class="col-md-5">

				<div class="card shadow">

					<div class="card-body p-4">

						<h2 class="text-center mb-3">UTEExpress</h2>

						<h4 class="text-center mb-4">Đăng nhập</h4>

						<%-- Thông báo đăng xuất --%>
						<%
						if ("true".equals(request.getParameter("logout"))) {
						%>
						<div class="alert alert-success">Bạn đã đăng xuất thành
							công.</div>
						<%
						}
						%>

						<%-- Thông báo lỗi (điền bằng JavaScript) --%>
						<div id="loginError" class="alert alert-danger d-none"
							role="alert"></div>

						<form id="loginForm" autocomplete="on">

							<div class="mb-3">
								<label class="form-label"> Tên đăng nhập </label> <input
									type="text" name="username" class="form-control"
									placeholder="Nhập username" autocomplete="username" required>
							</div>

							<div class="mb-3">
								<label class="form-label"> Mật khẩu </label> <input
									type="password" name="password" class="form-control"
									placeholder="Nhập mật khẩu" autocomplete="current-password"
									required>
							</div>

							<button id="loginBtn" type="submit" class="btn btn-primary w-100">
								Đăng nhập</button>

						</form>

						<p class="text-center mt-3 mb-0">
							Chưa có tài khoản? <a
								href="${pageContext.request.contextPath}/register">Đăng ký</a>
						</p>

					</div>

				</div>

			</div>

		</div>

	</div>

	<script>
		window.APP_CONTEXT_PATH = '<%= request.getContextPath() %>';
	</script>
	<script src="${pageContext.request.contextPath}/js/auth.js"></script>
	<script src="${pageContext.request.contextPath}/js/api.js"></script>
	<script>
		(function () {
			var form = document.getElementById("loginForm");
			var button = document.getElementById("loginBtn");
			var errorBox = document.getElementById("loginError");

			form.addEventListener("submit", async function (event) {
				event.preventDefault();
				errorBox.classList.add("d-none");
				button.disabled = true;
				button.textContent = "Đang đăng nhập...";

				try {
					var user = await window.Auth.login(
						form.elements.username.value.trim(),
						form.elements.password.value);
					window.location.href = window.Auth.homeFor(user.role);
				} catch (error) {
					errorBox.textContent = error.message || "Sai tài khoản hoặc mật khẩu!";
					errorBox.classList.remove("d-none");
					button.disabled = false;
					button.textContent = "Đăng nhập";
				}
			});
		})();
	</script>

</body>

</html>