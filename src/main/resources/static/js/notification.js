/*
 * notification.js — Real-time notification bell via STOMP over SockJS.
 *
 * Depends on: auth.js, api.js, sockjs-client, stomp.js (loaded from CDN)
 *
 * Usage: call window.Notifications.init(contextPath) once after auth.js
 * is loaded and the user is confirmed to be logged in. The init() call
 * renders the bell icon into any element with id="notif-bell-container"
 * and connects to the WebSocket endpoint.
 *
 * The module is designed to be safe to call multiple times; subsequent
 * calls disconnect the previous STOMP session first.
 */
(function () {
    "use strict";

    var stompClient = null;

    /* ── helpers ────────────────────────────────────────────────── */

    function timeAgo(isoString) {
        if (!isoString) { return ""; }
        var diff = Math.floor((Date.now() - new Date(isoString)) / 1000);
        if (diff < 60)  { return "Vừa xong"; }
        if (diff < 3600){ return Math.floor(diff / 60) + " phút trước"; }
        if (diff < 86400){ return Math.floor(diff / 3600) + " giờ trước"; }
        return Math.floor(diff / 86400) + " ngày trước";
    }

    function typeIcon(type) {
        var map = {
            SHIPMENT_ASSIGNED : "bi-person-check-fill text-primary",
            SHIPMENT_PICKED   : "bi-box-seam-fill text-warning",
            SHIPMENT_DELIVERED: "bi-check-circle-fill text-success",
            SHIPMENT_FAILED   : "bi-x-circle-fill text-danger",
            PAYMENT_PAID      : "bi-credit-card-fill text-success",
            PAYMENT_REFUNDED  : "bi-arrow-counterclockwise text-warning",
            GENERAL           : "bi-bell-fill text-secondary"
        };
        return map[type] || "bi-bell-fill text-secondary";
    }

    /* ── DOM rendering ──────────────────────────────────────────── */

    function renderBell(container, unread) {
        container.innerHTML = [
            '<div class="dropdown" id="notif-dropdown">',
            '  <button class="btn btn-sm btn-outline-light position-relative" type="button"',
            '          id="notif-toggle" data-bs-toggle="dropdown" aria-expanded="false"',
            '          aria-label="Thông báo">',
            '    <i class="bi bi-bell-fill" aria-hidden="true"></i>',
            '    <span id="notif-badge"',
            '          class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger',
            (unread > 0 ? '"' : ' d-none"') + '>',
            '      <span id="notif-badge-count">' + (unread > 0 ? (unread > 99 ? "99+" : unread) : "") + '</span>',
            '      <span class="visually-hidden">thông báo chưa đọc</span>',
            '    </span>',
            '  </button>',
            '  <ul class="dropdown-menu dropdown-menu-end p-0 shadow" id="notif-list"',
            '      style="min-width:320px;max-width:380px;max-height:420px;overflow-y:auto">',
            '    <li class="d-flex justify-content-between align-items-center px-3 py-2 border-bottom">',
            '      <span class="fw-semibold small">Thông báo</span>',
            '      <button class="btn btn-link btn-sm p-0 text-decoration-none" id="notif-mark-all">',
            '        Đánh dấu tất cả đã đọc',
            '      </button>',
            '    </li>',
            '    <li id="notif-items-wrapper">',
            '      <div class="text-center text-muted py-4 small" id="notif-empty-msg">',
            '        <i class="bi bi-bell-slash mb-1 d-block fs-4"></i>Chưa có thông báo',
            '      </div>',
            '    </li>',
            '  </ul>',
            '</div>'
        ].join("\n");

        document.getElementById("notif-mark-all").addEventListener("click", markAllRead);
    }

    function renderItem(notif) {
        var li = document.createElement("li");
        li.className = "border-bottom";
        li.setAttribute("data-notif-id", notif.id);

        var isUnread = !notif.seen;
        var icon = typeIcon(notif.type);

        li.innerHTML = [
            '<a href="#" class="d-flex gap-2 px-3 py-2 text-decoration-none text-dark',
            (isUnread ? ' fw-semibold bg-light"' : '"') + '>',
            '  <i class="bi ' + icon + ' fs-5 mt-1 flex-shrink-0"></i>',
            '  <div class="flex-grow-1 overflow-hidden">',
            '    <div class="text-truncate small">' + escHtml(notif.title) + '</div>',
            '    <div class="text-muted" style="font-size:.75rem;white-space:normal">' + escHtml(notif.body) + '</div>',
            '    <div class="text-muted" style="font-size:.7rem">' + timeAgo(notif.createdAt) + '</div>',
            '  </div>',
            '</a>'
        ].join("\n");

        li.querySelector("a").addEventListener("click", function (e) {
            e.preventDefault();
            markRead(notif.id, li);
        });

        return li;
    }

    function escHtml(str) {
        if (!str) { return ""; }
        return String(str)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;");
    }

    function prependNotification(notif) {
        var wrapper = document.getElementById("notif-items-wrapper");
        if (!wrapper) { return; }
        var empty = document.getElementById("notif-empty-msg");
        if (empty) { empty.remove(); }

        var li = renderItem(notif);
        li.classList.add("notif-new"); // for CSS animation
        wrapper.insertBefore(li, wrapper.firstChild);
    }

    function updateBadge(delta) {
        var badge  = document.getElementById("notif-badge");
        var countEl = document.getElementById("notif-badge-count");
        if (!badge || !countEl) { return; }

        var current = parseInt(countEl.textContent, 10) || 0;
        var next = Math.max(0, current + delta);

        countEl.textContent = next > 99 ? "99+" : next;
        badge.classList.toggle("d-none", next === 0);
    }

    /* ── REST helpers ───────────────────────────────────────────── */

    function markRead(id, liEl) {
        if (!liEl.querySelector("a.fw-semibold")) { return; } // already read
        window.apiRequest(window.APP_CONTEXT_PATH + "/api/notifications/" + id + "/read", { method: "PUT" })
            .then(function () {
                liEl.querySelector("a").classList.remove("fw-semibold", "bg-light");
                updateBadge(-1);
            })
            .catch(function () { /* silent */ });
    }

    function markAllRead() {
        window.apiRequest(window.APP_CONTEXT_PATH + "/api/notifications/read-all", { method: "PUT" })
            .then(function () {
                var wrapper = document.getElementById("notif-items-wrapper");
                if (!wrapper) { return; }
                wrapper.querySelectorAll("a.fw-semibold").forEach(function (a) {
                    a.classList.remove("fw-semibold", "bg-light");
                });
                var badge = document.getElementById("notif-badge");
                var countEl = document.getElementById("notif-badge-count");
                if (badge) { badge.classList.add("d-none"); }
                if (countEl) { countEl.textContent = ""; }
            })
            .catch(function () { /* silent */ });
    }

    function loadHistory() {
        window.apiRequest(window.APP_CONTEXT_PATH + "/api/notifications?size=20")
            .then(function (resp) {
                var items = (resp && resp.data && resp.data.content) ? resp.data.content : [];
                var wrapper = document.getElementById("notif-items-wrapper");
                var empty   = document.getElementById("notif-empty-msg");
                if (!wrapper) { return; }
                if (items.length === 0) { return; }
                if (empty) { empty.remove(); }
                items.forEach(function (n) {
                    wrapper.appendChild(renderItem(n));
                });
            })
            .catch(function () { /* silent — bell still works for live pushes */ });
    }

    /* ── STOMP / WebSocket ──────────────────────────────────────── */

    function connect(contextPath, username) {
        if (typeof SockJS === "undefined" || typeof Stomp === "undefined") {
            console.warn("notification.js: SockJS or Stomp not loaded.");
            return;
        }

        var wsUrl = (contextPath || "") + "/ws";
        var token = window.Auth ? window.Auth.getAccessToken() : null;

        var headers = token ? { Authorization: "Bearer " + token } : {};

        var socket = new SockJS(wsUrl);
        stompClient = Stomp.over(socket);
        stompClient.debug = null; // silence console spam in production

        stompClient.connect(headers, function () {
            // Subscribe to private user queue
            stompClient.subscribe("/user/queue/notifications", function (frame) {
                try {
                    var notif = JSON.parse(frame.body);
                    prependNotification(notif);
                    updateBadge(+1);
                    showToast(notif);
                } catch (e) { /* malformed frame */ }
            });
        }, function (err) {
            // On error, retry after 10 s
            setTimeout(function () {
                connect(contextPath, username);
            }, 10000);
        });
    }

    function disconnect() {
        if (stompClient && stompClient.connected) {
            stompClient.disconnect();
        }
        stompClient = null;
    }

    /* ── Toast popup ────────────────────────────────────────────── */

    function showToast(notif) {
        var container = document.getElementById("notif-toast-container");
        if (!container) {
            container = document.createElement("div");
            container.id = "notif-toast-container";
            container.className = "toast-container position-fixed bottom-0 end-0 p-3";
            container.style.zIndex = "1090";
            document.body.appendChild(container);
        }

        var toastEl = document.createElement("div");
        toastEl.className = "toast align-items-center text-bg-primary border-0 notif-toast";
        toastEl.setAttribute("role", "alert");
        toastEl.setAttribute("aria-live", "polite");
        toastEl.innerHTML = [
            '<div class="d-flex">',
            '  <div class="toast-body">',
            '    <i class="bi ' + typeIcon(notif.type) + ' me-2"></i>',
            '    <strong>' + escHtml(notif.title) + '</strong><br>',
            '    <span class="small">' + escHtml(notif.body) + '</span>',
            '  </div>',
            '  <button type="button" class="btn-close btn-close-white me-2 m-auto"',
            '          data-bs-dismiss="toast" aria-label="Đóng"></button>',
            '</div>'
        ].join("\n");

        container.appendChild(toastEl);

        // Bootstrap 5 Toast API
        if (window.bootstrap && window.bootstrap.Toast) {
            var bsToast = new window.bootstrap.Toast(toastEl, { delay: 5000 });
            bsToast.show();
            toastEl.addEventListener("hidden.bs.toast", function () {
                toastEl.remove();
            });
        } else {
            // Fallback: auto-remove after 5 s
            setTimeout(function () { toastEl.remove(); }, 5000);
        }
    }

    /* ── Public API ─────────────────────────────────────────────── */

    function init(contextPath) {
        // Find the bell mount point
        var container = document.getElementById("notif-bell-container");
        if (!container) { return; } // page didn't include the mount point

        var user = window.Auth ? window.Auth.getUser() : null;
        if (!user) { return; } // not logged in

        // Get unread count first, then render bell
        window.apiRequest((contextPath || "") + "/api/notifications/unread-count")
            .then(function (resp) {
                var unread = (resp && resp.data && resp.data.unread) ? resp.data.unread : 0;
                renderBell(container, unread);
                loadHistory();
                disconnect();
                connect(contextPath, user.username);
            })
            .catch(function () {
                // Render bell with 0 even if request fails
                renderBell(container, 0);
                disconnect();
                connect(contextPath, user.username);
            });
    }

    window.Notifications = { init: init, disconnect: disconnect };
})();
