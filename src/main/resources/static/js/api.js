/*
 * api.js - fetch wrapper for the JSON API.
 *  - attaches the JWT (from auth.js) to every request unless { auth: false }
 *  - on a 401, refreshes the token once and retries the request
 *  - throws Error(message) using the server's ApiResponse message
 * Load AFTER auth.js. Pages that only call public endpoints (register.jsp)
 * can load api.js alone; it simply sends no token.
 */
window.apiRequest = async function (path, options) {
	options = options || {};
	var method = options.method || "GET";
	var body = options.body;
	var extraHeaders = options.headers || {};
	var useAuth = options.auth !== false;

	function send() {
		var headers = new Headers(extraHeaders);
		headers.set("Accept", "application/json");
		var isFormData = body instanceof FormData;
		if (body !== undefined && !isFormData) {
			headers.set("Content-Type", "application/json");
		}
		var token = useAuth && window.Auth ? window.Auth.getAccessToken() : null;
		if (token) {
			headers.set("Authorization", "Bearer " + token);
		}
		return fetch(path, {
			method: method,
			headers: headers,
			body: body === undefined ? undefined : isFormData ? body : JSON.stringify(body),
			credentials: "same-origin"
		});
	}

	var hadSession = useAuth && window.Auth && window.Auth.getAccessToken();
	var response = await send();

	if (response.status === 401 && hadSession) {
		try {
			if (!window.Auth.getRefreshToken()) { throw new Error("no refresh token"); }
			await window.Auth.refresh();
			response = await send();
		} catch (refreshError) {
			window.Auth.expireSession();
			throw new Error("Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại");
		}
		if (response.status === 401) {
			window.Auth.expireSession();
			throw new Error("Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại");
		}
	}

	var contentType = response.headers.get("content-type") || "";
	var text = await response.text();
	var data = null;
	if (text) {
		data = contentType.indexOf("application/json") !== -1 ? JSON.parse(text) : text;
	}

	if (!response.ok) {
		var message = null;
		if (data && typeof data === "object") {
			message = data.message || data.error || data.detail;
			// Validation errors arrive as { message, data: { field: "problem" } }
			if (data.data && typeof data.data === "object" && !Array.isArray(data.data)) {
				var details = Object.keys(data.data).map(function (k) { return data.data[k]; });
				if (details.length) { message = (message ? message + ": " : "") + details.join(", "); }
			}
		} else if (typeof data === "string") {
			message = data;
		}
		throw new Error(message || "Request failed (" + response.status + ")");
	}

	return data;
};

// Same as apiRequest, but unwraps the ApiResponse envelope ({success, data}) when present.
window.apiData = async function (path, options) {
	var result = await window.apiRequest(path, options);
	if (result && typeof result === "object" && "success" in result && "data" in result) {
		return result.data;
	}
	return result;
};