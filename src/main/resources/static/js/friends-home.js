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

        const avatar = document.createElement("div");
        avatar.className = "ranking-avatar";
        avatar.textContent = (row.avatarInitial || "?").slice(0, 1);

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
