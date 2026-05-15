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
        friendList.innerHTML = "<li class=\"friend-card-empty\">フレンドはまだいません。</li>";
        return;
    }

    list.forEach((friend) => {
        const li = document.createElement("li");
        li.className = "friend-list-item";

        const profileLink = document.createElement("a");
        profileLink.href = `/friends/profile/${encodeURIComponent(friend.username || "")}`;
        profileLink.className = "friend-list-link";

        const avatarWrap = document.createElement("div");
        avatarWrap.className = "friend-avatar-wrap";

        const avatar = document.createElement("img");
        avatar.className = "dm-avatar dm-avatar-image";
        avatar.alt = `${friend.displayName || friend.username || "ユーザー"} のプロフィール画像`;
        avatar.loading = "lazy";
        avatar.src = resolveFriendAvatarUrl(friend);
        avatar.addEventListener("error", () => {
            avatar.src = buildDefaultProfileDataUrl(friend.profileIconColor);
        });

        const info = document.createElement("div");
        info.className = "friend-list-info";

        const label = document.createElement("div");
        label.className = "friend-list-label";
        label.textContent = `${friend.displayName || friend.username}`;

        const username = document.createElement("div");
        username.className = "friend-list-username";
        username.textContent = `@${friend.username || ""}`;

        avatarWrap.appendChild(avatar);
        info.appendChild(label);
        info.appendChild(username);

        profileLink.appendChild(avatarWrap);
        profileLink.appendChild(info);
        li.appendChild(profileLink);

        const controls = document.createElement("div");
        controls.className = "friend-card-controls";

        const notifyButton = document.createElement("button");
        notifyButton.type = "button";
        const friendId = Number(friend.id);
        const isEnabled = enabledFriendNotificationUserIds.has(friendId);
        notifyButton.textContent = isEnabled ? "🔔 ON" : "🔕 OFF";
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

        controls.appendChild(notifyButton);

        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.className = "secondary";
        deleteButton.textContent = "削除";
        deleteButton.addEventListener("click", async () => {
            const displayName = friend.displayName || friend.username || "このユーザー";
            const firstConfirm = window.confirm(`${displayName} をフレンド一覧から削除しますか？`);
            if (!firstConfirm) {
                return;
            }
            const secondConfirm = window.confirm("本当に削除しますか？この操作は取り消せません。");
            if (!secondConfirm) {
                return;
            }
            try {
                await fetchJson(`/api/friends/${encodeURIComponent(friendId)}`, { method: "DELETE" });
                friendMessage.style.color = "#087057";
                friendMessage.textContent = `${displayName} を削除しました。`;
                await loadFriendList();
            } catch (error) {
                friendMessage.style.color = "#be2f2f";
                friendMessage.textContent = error.message;
            }
        });

        controls.appendChild(deleteButton);
        li.appendChild(controls);
        friendList.appendChild(li);
    });
}

function resolveFriendAvatarUrl(friend) {
    const friendId = Number(friend.id);
    if (friend.hasProfileImage && Number.isFinite(friendId) && friendId > 0) {
        return `/api/users/${friendId}/profile-image`;
    }
    return buildDefaultProfileDataUrl(friend.profileIconColor);
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

loadFriendList();
