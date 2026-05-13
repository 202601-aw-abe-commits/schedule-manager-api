const friendRequestForm = document.getElementById("friendRequestForm");
const friendUsernameInput = document.getElementById("friendUsername");
const friendMessage = document.getElementById("friendMessage");

if (friendRequestForm) {
    friendRequestForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        friendMessage.textContent = "";
        const action = event.submitter && event.submitter.value ? event.submitter.value : "search";
        const username = String(friendUsernameInput.value || "").trim();
        if (!username) {
            friendMessage.style.color = "#be2f2f";
            friendMessage.textContent = "ユーザー名を入力してください。";
            return;
        }

        if (action === "search") {
            try {
                const found = await fetchJson(`/api/friends/users/${encodeURIComponent(username)}`);
                const resolvedUsername = found && found.username ? found.username : username;
                window.location.href = `/friends/profile/${encodeURIComponent(resolvedUsername)}`;
                return;
            } catch (error) {
                friendMessage.style.color = "#be2f2f";
                friendMessage.textContent = "ユーザーが見つかりませんでした。";
                return;
            }
        }

        try {
            await fetchJson("/api/friends/requests", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username })
            });
            friendMessage.style.color = "#087057";
            friendMessage.textContent = "フレンド申請を送信しました。";
            friendUsernameInput.value = "";
        } catch (error) {
            friendMessage.style.color = "#be2f2f";
            friendMessage.textContent = error.message;
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
