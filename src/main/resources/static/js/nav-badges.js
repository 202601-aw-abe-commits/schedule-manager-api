async function loadNavBadges() {
    try {
        const response = await fetch("/api/nav/badges");
        if (!response.ok) {
            return;
        }
        const data = await response.json();
        setNavBadge("friends", Number(data.friends || 0));
        setNavBadge("dm", Number(data.dm || 0));
        setNavBadge("calendar", Number(data.joins || 0));
        setHeroStats(Number(data.online || 0), Number(data.events || 0));
    } catch (error) {
        // ignore
    }
}

function setHeroStats(online, events) {
    const onlineNode = document.getElementById("heroOnlineCount");
    if (onlineNode) {
        onlineNode.textContent = String(Math.max(0, Number(online || 0)));
    }
    const eventNode = document.getElementById("heroEventCount");
    if (eventNode) {
        eventNode.textContent = String(Math.max(0, Number(events || 0)));
    }
}

function markCurrentNav() {
    const path = window.location.pathname;
    const navLinks = document.querySelectorAll(".hero-nav a[href]");
    navLinks.forEach((link) => {
        const href = link.getAttribute("href");
        if (!href || !href.startsWith("/")) {
            return;
        }
        const isCurrent =
            (href === "/calendar" && path.startsWith("/calendar")) ||
            (href === "/friends" && path.startsWith("/friends")) ||
            (href === "/board" && path.startsWith("/board")) ||
            (href === "/profile" && path.startsWith("/profile")) ||
            (href !== "/" && href !== "/calendar" && href !== "/friends" && href !== "/board" && href !== "/profile" && path === href);
        link.classList.toggle("is-current", isCurrent);
    });
}

function setNavBadge(key, count) {
    const node = document.querySelector(`[data-nav-badge="${key}"]`);
    if (!node) {
        return;
    }
    const label = node.dataset.navLabel || node.textContent.trim();
    node.textContent = label;

    const n = Math.max(0, Number(count || 0));
    const existing = node.querySelector(".nav-badge");
    if (existing) {
        existing.remove();
    }
    if (n <= 0) {
        return;
    }
    const badge = document.createElement("span");
    badge.className = "nav-badge";
    badge.textContent = n > 99 ? "99+" : String(n);
    badge.setAttribute("aria-label", `未読 ${n} 件`);
    node.appendChild(badge);
}

markCurrentNav();
loadNavBadges();
window.setInterval(loadNavBadges, 15000);
