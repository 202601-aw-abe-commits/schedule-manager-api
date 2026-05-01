const friendRequestForm = document.getElementById("friendRequestForm");
const friendUsernameInput = document.getElementById("friendUsername");
const friendMessage = document.getElementById("friendMessage");
const friendList = document.getElementById("friendList");
const incomingRequestList = document.getElementById("incomingRequestList");
const outgoingRequestList = document.getElementById("outgoingRequestList");
const levelRankingList = document.getElementById("levelRankingList");

friendRequestForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    friendMessage.textContent = "";

    try {
        await fetchJson("/api/friends/requests", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username: friendUsernameInput.value })
        });

        friendMessage.style.color = "#087057";
        friendMessage.textContent = "フレンド申請を送信しました。";
        friendUsernameInput.value = "";
        await loadFriendDashboard();
    } catch (error) {
        friendMessage.style.color = "#be2f2f";
        friendMessage.textContent = error.message;
    }
});

async function loadFriendDashboard() {
    try {
        const data = await fetchJson("/api/friends");
        renderFriendList(friendList, data.friends, "フレンドはまだいません。", (friend) => {
            return `${friend.displayName} (@${friend.username})`;
        });

        renderIncomingRequests(data.incomingRequests || []);
        renderFriendList(outgoingRequestList, data.outgoingRequests, "送信中の申請はありません。", (request) => {
            return `${request.requesterDisplayName} (@${request.requesterUsername})`;
        });
        renderLevelRanking(data.levelRanking || []);
    } catch (error) {
        friendMessage.style.color = "#be2f2f";
        friendMessage.textContent = error.message;
    }
}

function renderIncomingRequests(incomingRequests) {
    incomingRequestList.innerHTML = "";
    if (!Array.isArray(incomingRequests) || incomingRequests.length === 0) {
        incomingRequestList.innerHTML = "<li>受信申請はありません。</li>";
        return;
    }

    incomingRequests.forEach((request) => {
        const li = document.createElement("li");
        li.textContent = `${request.requesterDisplayName} (@${request.requesterUsername})`;

        const acceptButton = document.createElement("button");
        acceptButton.type = "button";
        acceptButton.textContent = "承認";
        acceptButton.addEventListener("click", async () => {
            try {
                await fetchJson(`/api/friends/requests/${request.id}/accept`, { method: "POST" });
                friendMessage.style.color = "#087057";
                friendMessage.textContent = "フレンド申請を承認しました。";
                await loadFriendDashboard();
            } catch (error) {
                friendMessage.style.color = "#be2f2f";
                friendMessage.textContent = error.message;
            }
        });

        li.appendChild(acceptButton);
        incomingRequestList.appendChild(li);
    });
}

function renderFriendList(targetElement, list, emptyText, itemTextBuilder) {
    targetElement.innerHTML = "";
    if (!Array.isArray(list) || list.length === 0) {
        targetElement.innerHTML = `<li>${emptyText}</li>`;
        return;
    }

    list.forEach((item) => {
        const li = document.createElement("li");
        li.textContent = itemTextBuilder(item);
        targetElement.appendChild(li);
    });
}

function renderLevelRanking(rankingRows) {
    levelRankingList.innerHTML = "";
    if (!Array.isArray(rankingRows) || rankingRows.length === 0) {
        levelRankingList.innerHTML = "<li>ランキング対象がいません。</li>";
        return;
    }

    rankingRows.forEach((row) => {
        const li = document.createElement("li");
        const name = row.displayName || row.username || "不明";
        const current = row.currentUser ? "（あなた）" : "";
        li.textContent = `${row.rank}位 ${name}${current} | Lv.${row.level} | ${row.totalPoints}pt`;
        levelRankingList.appendChild(li);
    });
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

loadFriendDashboard();
