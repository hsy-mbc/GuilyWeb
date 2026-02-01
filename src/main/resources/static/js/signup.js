document.addEventListener('DOMContentLoaded', function() {

    const btnCheckId = document.getElementById('btn-check-id');
    const userIdInput = document.querySelector('input[name="userId"]');

    btnCheckId?.addEventListener('click', async function() {
        const userId = userIdInput.value;
        if (!userId) { alert("아이디를 입력하세요."); return; }

        try {
            const isExists = await apiRequest(`/api/auth/check-id?userId=${userId}`);

            if (result.available) {
                idMsg.innerText = "사용 가능한 아이디입니다.";
                idMsg.style.color = "#009b63";
                userIdInput.readOnly = true;
            } else {
                idMsg.innerText = "이미 사용 중인 아이디입니다.";
                idMsg.style.color = "#ef4444";
            }
        } catch (err) {
            console.error(err);
        }
    });

    const signupForm = document.getElementById('signupForm');
    signupForm?.addEventListener('submit', function(e) {
        const password = document.querySelector('input[name="password"]').value;
        const passwordConfirm = document.querySelector('input[name="passwordConfirm"]').value;

        if (password !== passwordConfirm) {
            e.preventDefault();
            alert("비밀번호가 일치하지 않습니다.");
            return;
        }

    });
});