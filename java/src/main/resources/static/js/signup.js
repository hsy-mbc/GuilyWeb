document.addEventListener('DOMContentLoaded', function() {

    const btnCheckId = document.getElementById('btn-check-id');
    const userIdInput = document.querySelector('input[name="userId"]');
    const idMsg = document.getElementById('id-msg');

    btnCheckId?.addEventListener('click', async function() {
        const userId = userIdInput.value;
        if (!userId) {
            alert("아이디를 입력하세요");
            return;
        }

        try {
            const exists = await apiRequest(`/api/auth/check-id?userId=${userId}`);

            if (!exists) {
                idMsg.innerText = "사용 가능한 아이디입니다.";
                idMsg.style.color = "#009b63";
                userIdInput.readOnly = true;
                btnCheckId.disabled = true;
            } else {
                idMsg.innerText = "이미 사용 중인 아이디입니다.";
                idMsg.style.color = "#ef4444";
                userIdInput.readOnly = false;
            }
        } catch (err) {
            console.error(err);
            alert("아이디 중복 확인 중 오류가 발생했습니다.");
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

        if (!userIdInput.readOnly) {
            e.preventDefault();
            alert("아이디 중복 확인을 해주세요.");
            return;
        }
    });
});