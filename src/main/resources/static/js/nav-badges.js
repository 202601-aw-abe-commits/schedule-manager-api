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
    } catch (error) {
        // ignore
    }
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

loadNavBadges();
window.setInterval(loadNavBadges, 15000);
