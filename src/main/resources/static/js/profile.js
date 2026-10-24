/*
 * profile.js - shared profile avatar uploader for every authenticated role.
 * Load after auth.js and api.js.
 */
(function () {
	"use strict";

	var user = window.Auth && window.Auth.getUser();
	if (!user || !window.Auth.getAccessToken()) { return; }

	var contextPath = window.APP_CONTEXT_PATH || "";
	var targets = document.querySelectorAll("[data-profile-actions]");
	targets.forEach(function (target) {
		var button = document.createElement("button");
		button.type = "button";
		button.className = "profile-avatar-button";
		button.setAttribute("aria-label", "Cập nhật ảnh đại diện");
		button.title = "Cập nhật ảnh đại diện";

		var dialog = document.createElement("dialog");
		dialog.className = "profile-avatar-dialog";
		dialog.setAttribute("aria-labelledby", "profile-avatar-dialog-title");

		var dialogContent = document.createElement("div");
		dialogContent.className = "profile-avatar-dialog-content";

		var dialogHeader = document.createElement("div");
		dialogHeader.className = "d-flex justify-content-between align-items-center mb-3";
		var dialogTitle = document.createElement("h2");
		dialogTitle.className = "h5 mb-0";
		dialogTitle.id = "profile-avatar-dialog-title";
		dialogTitle.textContent = "Ảnh đại diện";
		var closeButton = document.createElement("button");
		closeButton.type = "button";
		closeButton.className = "btn-close";
		closeButton.setAttribute("aria-label", "Đóng");
		dialogHeader.append(dialogTitle, closeButton);

		var preview = document.createElement("div");
		preview.className = "profile-avatar-preview";

		var image = document.createElement("img");
		image.className = "profile-avatar-preview-image";
		image.alt = "";
		image.hidden = true;

		var initials = document.createElement("span");
		initials.className = "profile-avatar-preview-initials";
		initials.textContent = String(user.username || "?").trim().charAt(0).toUpperCase() || "?";
		preview.append(image, initials);

		var removeButton = document.createElement("button");
		removeButton.type = "button";
		removeButton.className = "btn btn-outline-danger";
		removeButton.textContent = "Gỡ ảnh";
		removeButton.setAttribute("aria-label", "Gỡ ảnh đại diện");
		removeButton.title = "Gỡ ảnh đại diện";
		removeButton.hidden = true;

		var chooseButton = document.createElement("button");
		chooseButton.type = "button";
		chooseButton.className = "btn btn-primary";
		chooseButton.textContent = "Chọn ảnh";

		var dialogActions = document.createElement("div");
		dialogActions.className = "d-flex justify-content-center gap-2 mt-3";
		dialogActions.append(chooseButton, removeButton);

		var input = document.createElement("input");
		input.type = "file";
		input.accept = "image/png,image/jpeg";
		input.hidden = true;

		var status = document.createElement("span");
		status.className = "profile-avatar-status small";
		status.setAttribute("role", "status");
		status.setAttribute("aria-live", "polite");

		dialogContent.append(dialogHeader, preview, dialogActions, status);
		dialog.appendChild(dialogContent);
		target.append(button, input, dialog, status);

		function showAvatar(avatarUrl) {
			removeButton.hidden = !avatarUrl;
			if (!avatarUrl) {
				image.hidden = true;
				initials.hidden = false;
				image.removeAttribute("src");
				return;
			}
			image.onload = function () {
				image.hidden = false;
				initials.hidden = true;
			};
			image.onerror = function () {
				image.hidden = true;
				initials.hidden = false;
			};
			image.src = contextPath + avatarUrl;
		}

		button.addEventListener("click", function () {
			if (!dialog.open) { dialog.showModal(); }
		});
		chooseButton.addEventListener("click", function () { input.click(); });
		closeButton.addEventListener("click", function () { dialog.close(); });
		dialog.addEventListener("click", function (event) {
			if (event.target === dialog) { dialog.close(); }
		});
		input.addEventListener("change", async function () {
			if (!input.files || !input.files.length) { return; }
			var file = input.files[0];
			input.value = "";
			if (file.size > 5 * 1024 * 1024) {
				status.textContent = "Ảnh không được vượt quá 5 MB.";
				status.classList.add("text-warning");
				return;
			}
			if (file.type && ["image/png", "image/jpeg"].indexOf(file.type) === -1) {
				status.textContent = "Chỉ chấp nhận ảnh PNG hoặc JPEG.";
				status.classList.add("text-warning");
				return;
			}

			chooseButton.disabled = true;
			removeButton.disabled = true;
			status.textContent = "Đang tải ảnh...";
			status.classList.remove("text-warning");
			try {
				var form = new FormData();
				form.append("file", file);
				var profile = await window.apiData(window.Auth.url("/api/users/me/avatar"), {
					method: "POST",
					body: form
				});
				showAvatar(profile.avatarUrl);
				status.textContent = "Đã cập nhật ảnh đại diện.";
			} catch (error) {
				status.textContent = "Không cập nhật được ảnh: " + error.message;
				status.classList.add("text-warning");
			} finally {
				chooseButton.disabled = false;
				removeButton.disabled = false;
			}
		});

		removeButton.addEventListener("click", async function () {
			if (!window.confirm("Bạn có chắc muốn gỡ ảnh đại diện?")) { return; }
			chooseButton.disabled = true;
			removeButton.disabled = true;
			status.textContent = "Đang gỡ ảnh...";
			status.classList.remove("text-warning");
			try {
				var profile = await window.apiData(window.Auth.url("/api/users/me/avatar"), {
					method: "DELETE"
				});
				showAvatar(profile.avatarUrl);
				status.textContent = "Đã gỡ ảnh đại diện.";
			} catch (error) {
				status.textContent = "Không gỡ được ảnh: " + error.message;
				status.classList.add("text-warning");
			} finally {
				chooseButton.disabled = false;
				removeButton.disabled = false;
			}
		});

		window.apiData(window.Auth.url("/api/users/me"))
			.then(function (profile) {
				if (profile && typeof profile.avatarUrl !== "undefined") {
					showAvatar(profile.avatarUrl);
				} else {
					throw new Error("Thông tin ảnh đại diện không hợp lệ");
				}
			})
			.catch(function (error) {
				status.textContent = "Không tải được ảnh đại diện: " + error.message;
				status.classList.add("text-warning");
			});
	});

	var navGuest = document.getElementById("navGuest");
	var navUser = document.getElementById("navUser");
	if (navGuest && navUser) {
		navGuest.classList.add("d-none");
		navUser.classList.remove("d-none");
		navUser.classList.add("d-flex");
		var navName = document.getElementById("navName");
		if (navName) { navName.textContent = user.username; }
		var dashboard = document.getElementById("navDashboard");
		if (dashboard) { dashboard.href = window.Auth.homeFor(user.role); }
		var logout = document.getElementById("navLogout");
		if (logout) {
			logout.addEventListener("click", function () { window.Auth.logout(); });
		}
	}
})();
