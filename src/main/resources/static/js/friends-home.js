const friendMessage = document.getElementById("friendMessage");
const levelRankingList = document.getElementById("levelRankingList");
const rankingTabs = Array.from(document.querySelectorAll(".ranking-tab"));

let currentRankingPeriod = "week";

rankingTabs.forEach((tab) => {
    tab.addEventListener("click", async () => {
        currentRankingPeriod = tab.dataset.period || "week";
        rankingTabs.forEach((node) => node.classList.remove("active"));
        tab.classList.add("active");
        await loadTaskRanking(currentRankingPeriod);
    });
});

async function loadTaskRanking(period) {
    try {
        const data = await fetchJson(`/api/friends/ranking?period=${encodeURIComponent(period)}`);
        renderLevelRanking(data.rows || []);
    } catch (error) {
        if (friendMessage) {
            friendMessage.style.color = "#be2f2f";
            friendMessage.textContent = error.message;
        }
    }
}

function renderLevelRanking(rankingRows) {
    if (!levelRankingList) {
        return;
    }
    levelRankingList.innerHTML = "";
    if (!Array.isArray(rankingRows) || rankingRows.length === 0) {
        levelRankingList.innerHTML = "<li>ランキング対象がいません。</li>";
        return;
    }

    rankingRows.forEach((row) => {
        const li = document.createElement("li");
        li.className = "ranking-item";
        if (row.currentUser) {
            li.classList.add("current-user");
        }

        const medal = document.createElement("div");
        medal.className = "ranking-medal";
        medal.textContent = medalText(row.rank);

        const avatar = document.createElement("img");
        avatar.className = "ranking-avatar";
        avatar.alt = `${row.displayName || row.username || "ユーザー"} のプロフィール画像`;
        avatar.loading = "lazy";
        avatar.src = resolveRankingAvatarUrl(row);
        avatar.addEventListener("error", () => {
            avatar.src = buildDefaultProfileDataUrl(row.profileIconColor);
        });

        const body = document.createElement("div");
        body.className = "ranking-body";

        const title = document.createElement("div");
        title.className = "ranking-title";
        const name = row.displayName || row.username || "Unknown";
        title.textContent = `${row.rank}位 ${name}${row.currentUser ? " (あなた)" : ""}`;

        const meta = document.createElement("div");
        meta.className = "ranking-meta";
        meta.textContent = `達成数: ${row.completedCount}`;

        const progressTrack = document.createElement("div");
        progressTrack.className = "ranking-progress-track";
        const progressBar = document.createElement("div");
        progressBar.className = "ranking-progress-bar";
        progressBar.style.width = `${row.progressPercent || 0}%`;
        progressTrack.appendChild(progressBar);

        body.appendChild(title);
        body.appendChild(meta);
        body.appendChild(progressTrack);

        li.appendChild(medal);
        li.appendChild(avatar);
        li.appendChild(body);
        levelRankingList.appendChild(li);
    });
}

function medalText(rank) {
    if (rank === 1) return "1";
    if (rank === 2) return "2";
    if (rank === 3) return "3";
    return `${rank}`;
}

function resolveRankingAvatarUrl(row) {
    const userId = Number(row.id);
    if (row.hasProfileImage && Number.isFinite(userId) && userId > 0) {
        return `/api/users/${userId}/profile-image`;
    }
    return buildDefaultProfileDataUrl(row.profileIconColor);
}

function buildDefaultProfileDataUrl(color) {
    const fill = normalizeColorValue(color);
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100" role="img" aria-label="default profile"><rect width="100" height="100" fill="${fill}"/><circle cx="50" cy="36" r="18" fill="#ffffff"/><path d="M18 86c0-17.7 14.3-32 32-32s32 14.3 32 32v14H18z" fill="#ffffff"/></svg>`;
    return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`;
}

function normalizeColorValue(color) {
    const value = String(color || "").trim();
    if (/^#[0-9a-fA-F]{6}$/.test(value)) {
        return value;
    }
    return "#BFD6FF";
}

async function fetchJson(url, options = {}) {
    const response = await fetch(url, options);
    const contentType = response.headers.get("content-type") || "";
    const isJson = contentType.includes("application/json");
    const data = isJson ? await response.json() : null;
    if (!response.ok) {
        const message = data && data.message ? data.message : "通信に失敗しました。";
        throw new Error(message);
    }
    return data;
}

loadTaskRanking(currentRankingPeriod);
