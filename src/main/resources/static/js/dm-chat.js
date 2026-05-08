const app = document.querySelector(".app-shell");
const currentUsername = app?.dataset?.currentUsername || "";
const conversationId = Number(app?.dataset?.conversationId || 0);

const dmMessageThread = document.getElementById("dmMessageThread");
const dmSendForm = document.getElementById("dmSendForm");
const dmBodyInput = document.getElementById("dmBodyInput");
const dmAttachmentFile = document.getElementById("dmAttachmentFile");
const dmMessage = document.getElementById("dmMessage");
const dmChatTitle = document.getElementById("dmChatTitle");

if (dmSendForm) {
    dmSendForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        dmMessage.textContent = "";
        try {
            const formData = new FormData();
            formData.append("body", dmBodyInput.value || "");
            if (dmAttachmentFile && dmAttachmentFile.files && dmAttachmentFile.files[0]) {
                formData.append("file", dmAttachmentFile.files[0]);
            }
            await fetchJson(`/dm/conversations/${conversationId}/messages/upload`, {
                method: "POST",
                body: formData
            });
            dmBodyInput.value = "";
            if (dmAttachmentFile) {
                dmAttachmentFile.value = "";
            }
            await loadMessages();
        } catch (error) {
            dmMessage.style.color = "#be2f2f";
            dmMessage.textContent = error.message;
        }
    });
}

if (dmBodyInput && dmSendForm) {
    dmBodyInput.addEventListener("keydown", (event) => {
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
        dmSendForm.requestSubmit();
    });
}

async function initialize() {
    if (!conversationId) {
        dmMessage.style.color = "#be2f2f";
        dmMessage.textContent = "会話IDが不正です。";
        return;
    }
    await loadConversationHeader();
    await loadMessages();
    window.setInterval(loadMessages, 5000);
}

async function loadConversationHeader() {
    const rows = await fetchJson("/dm/conversations");
    const row = Array.isArray(rows) ? rows.find((item) => Number(item.id) === conversationId) : null;
    if (row && dmChatTitle) {
        dmChatTitle.textContent = `${row.partnerDisplayName || row.partnerUsername || "unknown"} (@${row.partnerUsername || "-"})`;
    }
}

async function loadMessages() {
    try {
        const rows = await fetchJson(`/dm/conversations/${conversationId}/messages`);
        renderMessages(rows || []);
    } catch (error) {
        dmMessage.style.color = "#be2f2f";
        dmMessage.textContent = error.message;
    }
}

function renderMessages(rows) {
    dmMessageThread.innerHTML = "";
    if (!Array.isArray(rows) || rows.length === 0) {
        dmMessageThread.innerHTML = "<li>メッセージはまだありません。</li>";
        return;
    }

    rows.forEach((row) => {
        const isMine = row.senderUsername === currentUsername;
        const li = document.createElement("li");
        li.className = `dm-message-item ${isMine ? "mine" : "theirs"}`;

        const bubble = document.createElement("div");
        bubble.className = "dm-message-bubble";
        if (row.body) {
            const text = document.createElement("div");
            text.textContent = row.body;
            bubble.appendChild(text);
        }
        if (row.hasAttachment) {
            const attachmentWrap = document.createElement("div");
            attachmentWrap.className = "dm-attachment-wrap";
            const contentType = String(row.attachmentContentType || "");
            const attachmentUrl = `/dm/messages/${row.id}/attachment`;
            if (contentType.startsWith("image/")) {
                const img = document.createElement("img");
                img.className = "dm-attachment-image";
                img.src = attachmentUrl;
                img.alt = row.attachmentFileName || "image";
                attachmentWrap.appendChild(img);
            } else if (contentType.startsWith("video/")) {
                const video = document.createElement("video");
                video.className = "dm-attachment-video";
                video.src = attachmentUrl;
                video.controls = true;
                video.preload = "metadata";
                attachmentWrap.appendChild(video);
            } else {
                const fileLink = document.createElement("a");
                fileLink.href = attachmentUrl;
                fileLink.target = "_blank";
                fileLink.rel = "noopener noreferrer";
                fileLink.textContent = row.attachmentFileName || "添付ファイルを開く";
                attachmentWrap.appendChild(fileLink);
            }
            bubble.appendChild(attachmentWrap);
        }

        const meta = document.createElement("div");
        meta.className = "dm-message-meta";
        const time = formatDateTime(row.createdAt);
        if (isMine) {
            meta.textContent = `${time} ${row.read ? "既読" : "未読"}`;
        } else {
            meta.textContent = time;
        }

        li.appendChild(bubble);
        li.appendChild(meta);
        dmMessageThread.appendChild(li);
    });

    dmMessageThread.scrollTop = dmMessageThread.scrollHeight;
}

function formatDateTime(value) {
    if (!value) return "-";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return String(value);
    const m = String(date.getMonth() + 1).padStart(2, "0");
    const d = String(date.getDate()).padStart(2, "0");
    const h = String(date.getHours()).padStart(2, "0");
    const min = String(date.getMinutes()).padStart(2, "0");
    return `${m}/${d} ${h}:${min}`;
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

initialize();
