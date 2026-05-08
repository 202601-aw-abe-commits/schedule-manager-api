const app = document.querySelector(".app-shell");
const friendUserId = Number(app?.dataset?.friendUserId || 0);
const startDmFromProfileButton = document.getElementById("startDmFromProfileButton");
const friendProfileMessage = document.getElementById("friendProfileMessage");

if (startDmFromProfileButton) {
    startDmFromProfileButton.addEventListener("click", async () => {
        friendProfileMessage.textContent = "";
        if (!friendUserId) {
            friendProfileMessage.style.color = "#be2f2f";
            friendProfileMessage.textContent = "DM開始に必要なユーザーIDが取得できません。";
            return;
        }
        try {
            const conversation = await fetchJson("/dm/start", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ partnerUserId: friendUserId })
            });
            window.location.href = `/dm/conversations/${conversation.id}`;
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
