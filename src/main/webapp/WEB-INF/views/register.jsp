<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="vi">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<title>Đăng ký - UTEExpress</title>
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
	<main class="container py-5">
		<div class="row justify-content-center">
			<div class="col-md-6 col-lg-5">
				<section class="card shadow-sm">
					<div class="card-body p-4">
						<h1 class="h3 text-center mb-4">Tạo tài khoản UTEExpress</h1>
						<div id="register-message" class="alert d-none" role="status" aria-live="polite"></div>
						<form id="register-form">
							<div class="mb-3">
								<label class="form-label" for="username">Tên đăng nhập</label>
								<input class="form-control" id="username" name="username" minlength="3" maxlength="50" required autocomplete="username">
							</div>
							<div class="mb-3">
								<label class="form-label" for="email">Email</label>
								<input class="form-control" id="email" name="email" type="email" maxlength="100" required autocomplete="email">
							</div>
							<div class="mb-3">
								<label class="form-label" for="password">Mật khẩu</label>
								<input class="form-control" id="password" name="password" type="password" minlength="8" maxlength="72" required autocomplete="new-password">
								<div class="form-text">Mật khẩu cần có ít nhất 8 ký tự.</div>
							</div>
							<div class="mb-3">
								<label class="form-label" for="fullName">Họ và tên</label>
								<input class="form-control" id="fullName" name="fullName" maxlength="100" autocomplete="name">
							</div>
							<div class="mb-4">
								<label class="form-label" for="phone">Số điện thoại</label>
								<input class="form-control" id="phone" name="phone" type="tel" maxlength="20" autocomplete="tel">
							</div>
							<button class="btn btn-primary w-100" type="submit">Đăng ký</button>
						</form>
						<p class="text-center mt-3 mb-0">Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a></p>
					</div>
				</section>
			</div>
		</div>
	</main>
	<script src="${pageContext.request.contextPath}/js/api.js"></script>
	<script>
		const registrationForm = document.getElementById("register-form");
		const registrationMessage = document.getElementById("register-message");
		const contextPath = '<%= request.getContextPath() %>';

		registrationForm.addEventListener("submit", async (event) => {
			event.preventDefault();
			registrationMessage.className = "alert d-none";
			const submitButton = registrationForm.querySelector("button[type=submit]");
			submitButton.disabled = true;

			try {
				await window.apiRequest(contextPath + "/api/auth/register", {
					method: "POST",
					body: Object.fromEntries(new FormData(registrationForm).entries())
				});
				registrationMessage.textContent = "Tạo tài khoản thành công. Bạn có thể đăng nhập.";
				registrationMessage.className = "alert alert-success";
				registrationForm.reset();
			} catch (error) {
				registrationMessage.textContent = error.message;
				registrationMessage.className = "alert alert-danger";
			} finally {
				submitButton.disabled = false;
			}
		});
	</script>
</body>
</html>
