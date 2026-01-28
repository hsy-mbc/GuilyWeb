document.getElementById('btn-check-id').addEventListener('click', function() {
    const userId = document.querySelector('input[name="userId"]').value;
    if(!userId) { alert("아이디를 입력하세요."); return; }

    fetch(`/api/auth/check-id?userId=${userId}`)
        .then(res => res.json())
        .then(isExists => {
            if(isExists) {
                alert("이미 사용 중인 아이디입니다.");
            } else {
                alert("사용 가능한 아이디입니다.");
            }
        });
});

const form = document.querySelector('.auth-form');
form.addEventListener('submit', function(e) {
    const password = document.querySelector('input[name="password"]').value;
    const passwordConfirm = document.querySelector('input[name="passwordConfirm"]').value; // name 변경 권장

    if (password !== passwordConfirm) {
        e.preventDefault(); // 폼 전송 중단
        alert("비밀번호가 일치하지 않습니다.");
        return;
    }

    // 추가 유효성 검사 (필수 입력값 등)
});