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

    if (!response.ok) {
        if (response.status === 403) alert("권한이 없습니다. 로그인이 필요합니다.");
        else throw new Error("API 요청 실패");
    }

    return response.json();
}