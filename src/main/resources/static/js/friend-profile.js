const app = document.querySelector(".app-shell");
const friendUsername = String(app?.dataset?.friendUsername || "").trim();
const canSendRequest = String(app?.dataset?.canSendRequest || "false") === "true";
const sendFriendRequestButton = document.getElementById("sendFriendRequestButton");
const friendProfileMessage = document.getElementById("friendProfileMessage");

if (sendFriendRequestButton && canSendRequest) {
    sendFriendRequestButton.addEventListener("click", async () => {
        friendProfileMessage.textContent = "";
        if (!friendUsername) {
            friendProfileMessage.style.color = "#be2f2f";
            friendProfileMessage.textContent = "申請先ユーザー名が取得できません。";
            return;
        }
        try {
            await fetchJson("/api/friends/requests", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username: friendUsername })
            });
            friendProfileMessage.style.color = "#087057";
            friendProfileMessage.textContent = "フレンド申請を送信しました。";
            sendFriendRequestButton.disabled = true;
        } catch (error) {
            friendProfileMessage.style.color = "#be2f2f";
            friendProfileMessage.textContent = error.message;
        }
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
