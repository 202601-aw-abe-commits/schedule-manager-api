const app = document.querySelector(".app-shell");
const scheduleId = Number(app?.dataset?.scheduleId || 0);

const joinDetailTitle = document.getElementById("joinDetailTitle");
const joinDetailMeta = document.getElementById("joinDetailMeta");
const joinDetailDescription = document.getElementById("joinDetailDescription");
const joinDiscordInviteWrap = document.getElementById("joinDiscordInviteWrap");
const joinDiscordInviteLink = document.getElementById("joinDiscordInviteLink");
const joinRequestForm = document.getElementById("joinRequestForm");
const joinCommentInput = document.getElementById("joinCommentInput");
const joinGameIdInput = document.getElementById("joinGameIdInput");
const joinMessage = document.getElementById("joinMessage");

if (joinRequestForm) {
    joinRequestForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        joinMessage.textContent = "";
        try {
            await fetchJson(`/api/schedules/${scheduleId}/join`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    comment: joinCommentInput.value,
                    gameId: joinGameIdInput ? joinGameIdInput.value : null
                })
            });
            joinMessage.style.color = "#087057";
            joinMessage.textContent = "参加希望を送信しました。";
            await loadSchedule();
        } catch (error) {
            joinMessage.style.color = "#be2f2f";
            joinMessage.textContent = error.message;
        }
    });
}

if (joinCommentInput && joinRequestForm) {
    joinCommentInput.addEventListener("keydown", (event) => {
        if (event.key !== "Enter") {
            return;
        }
        if (event.shiftKey) {
            return;
        }
        if (event.isComposing) {
            return;
        }
        event.preventDefault();
        joinRequestForm.requestSubmit();
    });
}

async function loadSchedule() {
    try {
        const item = await fetchJson(`/api/schedules/${scheduleId}`);
        renderSchedule(item);
    } catch (error) {
        joinMessage.style.color = "#be2f2f";
        joinMessage.textContent = error.message;
    }
}

function renderSchedule(item) {
    if (!item) {
        joinDetailTitle.textContent = "予定が見つかりません。";
        return;
    }
    joinDetailTitle.textContent = item.title || "予定";
    const owner = item.ownerDisplayName || item.ownerUsername || "不明";
    const date = item.scheduleDate || "-";
    const start = item.startTime || "--:--";
    const end = item.endTime || "--:--";
    joinDetailMeta.textContent = `${date} ${start}-${end} | 作成者: ${owner}`;
    joinDetailDescription.textContent = item.description || "備考なし";
    if (joinDiscordInviteWrap) {
        joinDiscordInviteWrap.hidden = true;
    }

    if (item.joinRequestStatusForCurrentUser === "PENDING" && item.joinRequestCommentForCurrentUser) {
        joinCommentInput.value = item.joinRequestCommentForCurrentUser;
        if (joinGameIdInput) {
            joinGameIdInput.value = item.joinRequestGameIdForCurrentUser || "";
        }
        joinMessage.style.color = "#6c4a00";
        joinMessage.textContent = "参加希望は承認待ちです。了承されるとシェアできるようになります。";
    } else if (item.joinRequestStatusForCurrentUser === "APPROVED") {
        if (joinGameIdInput) {
            joinGameIdInput.value = item.joinRequestGameIdForCurrentUser || "";
        }
        if (item.discordInviteUrl && joinDiscordInviteWrap && joinDiscordInviteLink) {
            joinDiscordInviteLink.href = item.discordInviteUrl;
            joinDiscordInviteWrap.hidden = false;
        }
        joinMessage.style.color = "#087057";
        joinMessage.textContent = "参加希望が了承されました。カレンダー画面からこの募集をシェアできます。";
    } else {
        if (joinGameIdInput) {
            joinGameIdInput.value = "";
        }
        joinMessage.textContent = "";
    }
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

loadSchedule();
