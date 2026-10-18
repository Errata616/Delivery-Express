/*
 * auth.js - API login/logout and token storage.
 * Load BEFORE api.js. Pages should set window.APP_CONTEXT_PATH first
 * (see the <script> block in the JSPs) so URLs work under a context path.
 */
(function () {
	"use strict";

	var KEYS = {
		access: "ute.accessToken",
		refresh: "ute.refreshToken",
		user: "ute.user"
	};

	var refreshPromise = null;

	function url(path) {
		return (window.APP_CONTEXT_PATH || "") + path;
	}

	function read(key) {
		try { return window.localStorage.getItem(key); } catch (e) { return null; }
	}

	function write(key, value) {
		try { window.localStorage.setItem(key, value); } catch (e) { /* storage blocked */ }
	}

	function remove(key) {
		try { window.localStorage.removeItem(key); } catch (e) { /* storage blocked */ }
	}

	function getAccessToken() { return read(KEYS.access); }

	function getRefreshToken() { return read(KEYS.refresh); }

	function getUser() {
		var raw = read(KEYS.user);
		if (!raw) { return null; }
		try { return JSON.parse(raw); } catch (e) { return null; }
	}

	function saveSession(data) {
		write(KEYS.access, data.token);
		write(KEYS.refresh, data.refreshToken);
		write(KEYS.user, JSON.stringify({ username: data.username, role: data.role }));
	}

	function clearSession() {
		remove(KEYS.access);
		remove(KEYS.refresh);
		remove(KEYS.user);
	}

	// Plain fetch on purpose: apiRequest() attaches tokens and retries on 401,
	// which would loop for the auth endpoints themselves.
	async function postJson(path, body) {
		var response = await fetch(url(path), {
			method: "POST",
			headers: { "Content-Type": "application/json", "Accept": "application/json" },
			body: JSON.stringify(body)
		});
		var payload = null;
		try { payload = await response.json(); } catch (e) { /* empty or non-JSON body */ }
		return { ok: response.ok, status: response.status, payload: payload };
	}

	async function login(username, password) {
		var result = await postJson("/api/auth/login", { username: username, password: password });
		if (!result.ok || !result.payload || result.payload.success === false || !result.payload.data) {
			throw new Error((result.payload && result.payload.message) || "Đăng nhập thất bại");
		}
		saveSession(result.payload.data);
		return getUser();
	}

	// Refresh tokens are rotated, so two parallel refreshes would revoke each
	// other. Share one in-flight request between all callers.
	function refresh() {
		if (refreshPromise) { return refreshPromise; }

		var refreshToken = getRefreshToken();
		if (!refreshToken) { return Promise.reject(new Error("Chưa đăng nhập")); }

		refreshPromise = (async function () {
			try {
				var result = await postJson("/api/auth/refresh", { refreshToken: refreshToken });
				if (!result.ok || !result.payload || !result.payload.data) {
					clearSession();
					throw new Error("Phiên đăng nhập đã hết hạn");
				}
				saveSession(result.payload.data);
				return result.payload.data.token;
			} finally {
				refreshPromise = null;
			}
		})();

		return refreshPromise;
	}

	function navigate(target) {
		Auth.navigate(target);
	}

	async function logout() {
		var refreshToken = getRefreshToken();
		clearSession();
		if (refreshToken) {
			try { await postJson("/api/auth/logout", { refreshToken: refreshToken }); } catch (e) { /* best effort */ }
		}
		navigate(url("/login?logout=true"));
	}

	// Called when the server rejects our credentials and refreshing did not help.
	function expireSession() {
		clearSession();
		navigate(url("/login"));
	}

	function homeFor(role) {
		if (role === "ADMIN") { return url("/admin/dashboard"); }
		if (role === "MANAGER") { return url("/manager/dashboard"); }
		if (role === "SHIPPER") { return url("/shipper/dashboard"); }
		if (role === "USER") { return url("/booking"); }
		return url("/");
	}

	// Returns the user if they may view this page, otherwise redirects and returns null.
	function requireRole(roles) {
		var user = getUser();
		if (!getAccessToken() || !user) {
			navigate(url("/login"));
			return null;
		}
		if (roles && roles.length && roles.indexOf(user.role) === -1) {
			navigate(homeFor(user.role));
			return null;
		}
		return user;
	}

	var Auth = {
		url: url,
		getAccessToken: getAccessToken,
		getRefreshToken: getRefreshToken,
		getUser: getUser,
		login: login,
		logout: logout,
		refresh: refresh,
		expireSession: expireSession,
		requireRole: requireRole,
		homeFor: homeFor,
		// Overridable so tests (and odd hosting setups) can intercept redirects.
		navigate: function (target) { window.location.href = target; }
	};

	window.Auth = Auth;
})();