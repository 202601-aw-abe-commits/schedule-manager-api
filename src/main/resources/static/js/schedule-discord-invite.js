const app = document.querySelector(".app-shell");
const scheduleId = Number(app?.dataset?.scheduleId || 0);

const inviteDetailTitle = document.getElementById("inviteDetailTitle");
const inviteDetailMeta = document.getElementById("inviteDetailMeta");
const inviteForm = document.getElementById("inviteForm");
const discordInviteUrlInput = document.getElementById("discordInviteUrlInput");
const inviteMessage = document.getElementById("inviteMessage");

if (inviteForm) {
    inviteForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        inviteMessage.textContent = "";
        try {
            const payload = { discordInviteUrl: discordInviteUrlInput.value || null };
            await fetchJson(`/api/schedules/${scheduleId}/discord-invite`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            inviteMessage.style.color = "#087057";
            inviteMessage.textContent = "Discord招待URLを保存しました。";
            await loadSchedule();
        } catch (error) {
            inviteMessage.style.color = "#be2f2f";
            inviteMessage.textContent = error.message;
        }
    });
}

async function loadSchedule() {
    try {
        const item = await fetchJson(`/api/schedules/${scheduleId}/discord-invite`);
        renderSchedule(item);
    } catch (error) {
        inviteMessage.style.color = "#be2f2f";
        inviteMessage.textContent = error.message;
    }
}

function renderSchedule(item) {
    if (!item) {
        inviteDetailTitle.textContent = "予定が見つかりません。";
        return;
    }
    inviteDetailTitle.textContent = item.title || "予定";
    const owner = item.ownerDisplayName || item.ownerUsername || "不明";
    const date = item.scheduleDate || "-";
    const start = item.startTime || "--:--";
    const end = item.endTime || "--:--";
    inviteDetailMeta.textContent = `${date} ${start}-${end} | 作成者: ${owner}`;
    discordInviteUrlInput.value = item.discordInviteUrl || "";
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
