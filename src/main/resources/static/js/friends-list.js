const friendList = document.getElementById("friendList");
const friendMessage = document.getElementById("friendMessage");
let enabledFriendNotificationUserIds = new Set();

async function loadFriendList() {
    try {
        const data = await fetchJson("/api/friends");
        const friends = Array.isArray(data.friends) ? data.friends : [];
        enabledFriendNotificationUserIds = new Set(
            Array.isArray(data.enabledFriendNotificationUserIds)
                ? data.enabledFriendNotificationUserIds.map((id) => Number(id))
                : []
        );
        renderFriendListWithNotification(friends);
    } catch (error) {
        friendMessage.style.color = "#be2f2f";
        friendMessage.textContent = error.message;
    }
}

function renderFriendListWithNotification(list) {
    friendList.innerHTML = "";
    if (!Array.isArray(list) || list.length === 0) {
        friendList.innerHTML = "<li>フレンドはまだいません。</li>";
        return;
    }

    list.forEach((friend) => {
        const li = document.createElement("li");
        const profileLink = document.createElement("a");
        profileLink.href = `/friends/profile/${encodeURIComponent(friend.username || "")}`;
        profileLink.textContent = `${friend.displayName} (@${friend.username})`;
        li.appendChild(profileLink);
        li.appendChild(document.createTextNode(" "));

        const notifyButton = document.createElement("button");
        notifyButton.type = "button";
        const friendId = Number(friend.id);
        const isEnabled = enabledFriendNotificationUserIds.has(friendId);
        notifyButton.textContent = isEnabled ? "通知ON" : "通知OFF";
        notifyButton.className = isEnabled ? "primary" : "secondary";
        notifyButton.addEventListener("click", async () => {
            try {
                await fetchJson("/api/friends/notifications/preferences", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        friendUserId: friendId,
                        enabled: !isEnabled
                    })
                });
                friendMessage.style.color = "#087057";
                friendMessage.textContent = `${friend.displayName} の通知を${!isEnabled ? "ON" : "OFF"}にしました。`;
                await loadFriendList();
            } catch (error) {
                friendMessage.style.color = "#be2f2f";
                friendMessage.textContent = error.message;
            }
        });

        li.appendChild(notifyButton);
        friendList.appendChild(li);
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

loadFriendList();
