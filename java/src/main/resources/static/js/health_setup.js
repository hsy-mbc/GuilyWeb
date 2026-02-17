let isUserLoggedIn = false;

document.addEventListener('DOMContentLoaded', function() {

    // ========================================
    // 0. 초기 데이터 로드
    // ========================================
    async function loadUserBasicInfo() {
        try {
            const response = await apiRequest('/api/user/basic-info', {
                method: 'GET'
            });

            if (response && response.gender) {
                isUserLoggedIn = true;
                console.log('✅ 로그인 상태');

                // 성별 설정
                if (response.gender) {
                    const genderBtn = document.querySelector(`.gender-btn[data-value="${response.gender}"]`);
                    if (genderBtn) {
                        genderButtons.forEach(b => b.classList.remove('active'));
                        genderBtn.classList.add('active');
                        document.getElementById('gender').value = response.gender;
                    }
                }

                // 나이, 키, 체중 설정
                if (response.age) document.querySelector('input[name="age"]').value = response.age;
                if (response.height) document.querySelector('input[name="height"]').value = response.height;
                if (response.weight) document.querySelector('input[name="weight"]').value = response.weight;

                console.log('사용자 기본 정보 로드 완료:', response);
            } else {
                isUserLoggedIn = false;
                console.log('❌ 비로그인 상태');
            }

        } catch (error) {
            isUserLoggedIn = false;
            console.log('❌ 에러 발생:', error);
        }
    }

    // 페이지 로드 시 사용자 기본 정보 가져오기
    loadUserBasicInfo();

    // ========================================
    // 1. 성별 선택
    // ========================================
    const genderButtons = document.querySelectorAll('.gender-btn');
    const genderInput = document.getElementById('gender');

    genderButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            genderButtons.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            genderInput.value = this.getAttribute('data-value');

            console.log('성별 선택:', genderInput.value);
        });
    });

    // ========================================
    // 2. 활동 수준 선택
    // ========================================
    const activityOptions = document.querySelectorAll('.activity-option');

    activityOptions.forEach(option => {
        option.addEventListener('click', function() {
            // 모든 옵션 비활성화
            activityOptions.forEach(opt => opt.classList.remove('active'));
            // 클릭한 옵션 활성화
            this.classList.add('active');
            // 라디오 버튼 체크
            const radioInput = this.querySelector('input[type="radio"]');
            radioInput.checked = true;

            console.log('활동 수준 선택:', radioInput.value);
        });
    });

    // ========================================
    // 3. 생활 패턴 선택
    // ========================================
    const patternButtons = document.querySelectorAll('.pattern-btn');
    const patternInput = document.getElementById('lifestylePattern');

    patternButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            // 모든 버튼 비활성화
            patternButtons.forEach(b => b.classList.remove('active'));
            // 클릭한 버튼 활성화
            this.classList.add('active');
            // hidden input에 값 저장
            patternInput.value = this.getAttribute('data-value');

            console.log('생활 패턴 선택:', patternInput.value);
        });
    });

    // ========================================
    // 4. 식단 목표 선택
    // ========================================
    const goalButtons = document.querySelectorAll('.goal-btn');
    const goalInput = document.getElementById('dietGoal');

    goalButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            // 모든 버튼 비활성화
            goalButtons.forEach(b => b.classList.remove('active'));
            // 클릭한 버튼 활성화
            this.classList.add('active');
            // hidden input에 값 저장
            goalInput.value = this.getAttribute('data-value');

            console.log('식단 목표 선택:', goalInput.value);
        });
    });

    // ========================================
    // 5. 폼 제출 (수정)
    // ========================================
    const setupForm = document.getElementById('setupForm');

    setupForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        // 활동 수준 선택 확인
        const selectedActivity = document.querySelector('input[name="activity"]:checked');
        if (!selectedActivity) {
            alert('활동 수준을 선택해주세요.');
            return;
        }

        // 폼 데이터 수집
        const formData = {
            gender: document.getElementById('gender').value,
            height: parseFloat(document.querySelector('input[name="height"]').value),
            weight: parseFloat(document.querySelector('input[name="weight"]').value),
            age: parseInt(document.querySelector('input[name="age"]').value),
            activityCoefficient: parseFloat(selectedActivity.value),
            lifestylePattern: document.getElementById('lifestylePattern').value,
            dietGoal: document.getElementById('dietGoal').value
        };

        // 유효성 검사
        if (!formData.gender) {
            alert('성별을 선택해주세요.');
            return;
        }
        if (!formData.height || formData.height <= 0) {
            alert('키를 입력해주세요.');
            return;
        }
        if (!formData.weight || formData.weight <= 0) {
            alert('체중을 입력해주세요.');
            return;
        }
        if (!formData.age || formData.age <= 0) {
            alert('나이를 입력해주세요.');
            return;
        }
        if (!formData.lifestylePattern) {
            alert('생활 패턴을 선택해주세요.');
            return;
        }
        if (!formData.dietGoal) {
            alert('식단 목표를 선택해주세요.');
            return;
        }

        console.log('로그인 상태:', isUserLoggedIn);
        console.log('제출할 데이터:', formData);

        const endpoint = isUserLoggedIn ? '/api/diet/save' : '/api/diet/calculate';
        console.log('호출할 API:', endpoint);

        try {
            const submitBtn = document.querySelector('button[type="submit"][form="setupForm"]');

            if (submitBtn) {
                const originalText = submitBtn.textContent;
                submitBtn.disabled = true;
                submitBtn.textContent = '저장 중...';
            }

            const response = await apiRequest(endpoint, {
                method: 'POST',
                body: JSON.stringify(formData)
            });

            console.log('서버 응답:', response);

            if (isUserLoggedIn) {
                alert('건강 정보가 저장되었습니다!');
                location.href = '/diet';
            } else {
                alert('칼로리가 계산되었습니다!\n회원가입 후 더 많은 기능을 이용하세요.');
                sessionStorage.setItem('dietCalculation', JSON.stringify(response));
                location.href = '/diet';
            }

        } catch (error) {
            console.error('요청 실패:', error);
            alert('처리에 실패했습니다. 다시 시도해주세요.');

            const submitBtn = document.querySelector('button[type="submit"][form="setupForm"]');
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = '저장하고 시작하기';
            }
        }
    });
});