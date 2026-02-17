async function apiRequest(url, options = {}) {
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;

    const defaultHeaders = {
        'Content-Type': 'application/json'
    };

    if (token && header) {
        defaultHeaders[header] = token;
    }

    options.headers = {
        ...defaultHeaders,
        ...options.headers
    };

    const response = await fetch(url, options);

    if (response.status === 401) {
        if (confirm("로그인이 필요한 기능입니다. 로그인 페이지로 이동하시겠습니까?")) {
            location.href = "/login";
        }
        throw new Error("Unauthorized");
    }

    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        const error = new Error(errorData.message || "API 요청 실패");
        error.status = response.status;
        throw error;
    }

    return response.json();
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}