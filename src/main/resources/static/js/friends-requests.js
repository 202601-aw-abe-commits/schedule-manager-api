const incomingRequestList = document.getElementById("incomingRequestList");
const outgoingRequestList = document.getElementById("outgoingRequestList");
const friendMessage = document.getElementById("friendMessage");

async function loadRequests() {
    try {
        const data = await fetchJson("/api/friends");
        renderIncomingRequests(data.incomingRequests || []);
        renderOutgoingRequests(data.outgoingRequests || []);
    } catch (error) {
        friendMessage.style.color = "#be2f2f";
        friendMessage.textContent = error.message;
    }
}

function renderIncomingRequests(incomingRequests) {
    incomingRequestList.innerHTML = "";
    if (!Array.isArray(incomingRequests) || incomingRequests.length === 0) {
        incomingRequestList.innerHTML = "<li class=\"friend-card-empty\">受信した申請はありません。</li>";
        return;
    }

    incomingRequests.forEach((request) => {
        const li = createRequestCard(request);

        const acceptButton = document.createElement("button");
        acceptButton.type = "button";
        acceptButton.className = "primary";
        acceptButton.textContent = "承認";
        acceptButton.addEventListener("click", async () => {
            try {
                await fetchJson(`/api/friends/requests/${request.id}/accept`, { method: "POST" });
                friendMessage.style.color = "#087057";
                friendMessage.textContent = "フレンド申請を承認しました。";
                await loadRequests();
            } catch (error) {
                friendMessage.style.color = "#be2f2f";
                friendMessage.textContent = error.message;
            }
        });

        const controls = document.createElement("div");
        controls.className = "friend-card-controls";
        controls.appendChild(acceptButton);
        li.appendChild(controls);
        incomingRequestList.appendChild(li);
    });
}

function renderOutgoingRequests(outgoingRequests) {
    outgoingRequestList.innerHTML = "";
    if (!Array.isArray(outgoingRequests) || outgoingRequests.length === 0) {
        outgoingRequestList.innerHTML = "<li class=\"friend-card-empty\">送信中の申請はありません。</li>";
        return;
    }
    outgoingRequests.forEach((request) => {
        const li = createRequestCard(request);
        const controls = document.createElement("div");
        controls.className = "friend-card-controls";

        const statusButton = document.createElement("button");
        statusButton.type = "button";
        statusButton.className = "secondary";
        statusButton.textContent = "送信中";
        statusButton.disabled = true;

        controls.appendChild(statusButton);
        li.appendChild(controls);
        outgoingRequestList.appendChild(li);
    });
}

function createRequestCard(request) {
    const li = document.createElement("li");
    li.className = "friend-list-item";

    const profileLink = document.createElement("a");
    profileLink.href = `/friends/profile/${encodeURIComponent(request.requesterUsername || "")}`;
    profileLink.className = "friend-list-link";

    const avatarWrap = document.createElement("div");
    avatarWrap.className = "friend-avatar-wrap";

    const avatar = document.createElement("img");
    avatar.className = "dm-avatar dm-avatar-image";
    avatar.alt = `${request.requesterDisplayName || request.requesterUsername || "ユーザー"} のプロフィール画像`;
    avatar.loading = "lazy";
    avatar.src = resolveRequestAvatarUrl(request);
    avatar.addEventListener("error", () => {
        avatar.src = buildDefaultProfileDataUrl(request.profileIconColor);
    });

    const info = document.createElement("div");
    info.className = "friend-list-info";

    const label = document.createElement("div");
    label.className = "friend-list-label";
    label.textContent = `${request.requesterDisplayName || request.requesterUsername || ""}`;

    const username = document.createElement("div");
    username.className = "friend-list-username";
    username.textContent = `@${request.requesterUsername || ""}`;

    avatarWrap.appendChild(avatar);
    info.appendChild(label);
    info.appendChild(username);
    profileLink.appendChild(avatarWrap);
    profileLink.appendChild(info);
    li.appendChild(profileLink);
    return li;
}

function resolveRequestAvatarUrl(request) {
    const userId = Number(request.requesterUserId);
    if (request.hasProfileImage && Number.isFinite(userId) && userId > 0) {
        return `/api/users/${userId}/profile-image`;
    }
    return buildDefaultProfileDataUrl(request.profileIconColor);
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

loadRequests();
