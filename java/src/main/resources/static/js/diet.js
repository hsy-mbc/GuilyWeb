document.addEventListener('DOMContentLoaded', function() {

    let isUserLoggedIn = false;

    // ========================================
    // 1. 로그인 상태 확인
    // ========================================
    async function checkLoginStatus() {
        try {
            const response = await apiRequest('/api/user/basic-info', {
                method: 'GET'
            });

            if (response && response.gender) {
                isUserLoggedIn = true;
                console.log('✅ 로그인 상태');
            } else {
                isUserLoggedIn = false;
                console.log('❌ 비로그인 상태');
            }

        } catch (error) {
            isUserLoggedIn = false;
            console.log('❌ 에러 발생:', error);
        }
    }

    // ========================================
    // 2. AI 추천 로드
    // ========================================
    async function loadRecommendations() {
        try {
            let url = '/api/diet/recommend/daily';

            if (!isUserLoggedIn) {
                const savedCalc = sessionStorage.getItem('dietCalculation');
                const targetCal = savedCalc ? JSON.parse(savedCalc) : 2100;
                url += `?targetCal=${targetCal}`;
            }

            console.log('📤 추천 요청 URL:', url);

            const response = await apiRequest(url, { method: 'GET' });

            console.log('📥 추천 응답:', response);

            if (response && response.status === 'success' && response.data) {
                displayRecommendations(response.data);
            }

        } catch (error) {
            console.error('❌ AI 추천 로드 실패:', error);
        }
    }

    // ========================================
    // 3. AI 추천 표시
    // ========================================
    const MEAL_CONFIG = {
        BREAKFAST: {
            label: '아침',
            iconClass: 'apple',
            iconSvg: `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 8h1a4 4 0 1 1 0 8h-1"></path>
                <path d="M3 8h14v9a4 4 0 0 1-4 4H7a4 4 0 0 1-4-4Z"></path>
            </svg>`
        },
        LUNCH: {
            label: '점심',
            iconClass: 'salad',
            iconSvg: `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M7 21h10"></path>
                <path d="M12 21a9 9 0 0 0 9-9H3a9 9 0 0 0 9 9Z"></path>
            </svg>`
        },
        DINNER: {
            label: '저녁',
            iconClass: 'tofu',
            iconSvg: `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
            </svg>`
        }
    };

    const refreshIconSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"></path>
        <path d="M21 3v5h-5"></path>
        <path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"></path>
        <path d="M8 16H3v5"></path>
    </svg>`;

    function displayRecommendations(data) {
        const grid = document.querySelector('.recommend-grid');
        if (!grid) {
            console.warn('⚠️ .recommend-grid 요소를 찾을 수 없습니다');
            return;
        }

        grid.innerHTML = ['BREAKFAST', 'LUNCH', 'DINNER'].map(mealType => {
            const config = MEAL_CONFIG[mealType];
            const foods = data[mealType] || [];
            const totalCalories = foods.reduce((sum, food) => sum + food.calories, 0);

            return `
                <div class="recommend-card">
                    <div class="recommend-card-header">
                        <div class="recommend-icon ${config.iconClass}">
                            ${config.iconSvg}
                        </div>
                        <h3 class="recommend-title">${config.label}</h3>
                    </div>

                    <ul class="recommend-list">
                        ${foods.map(food => `
                            <li>
                                <span class="food-name">${escapeHtml(food.food_name)}</span>
                                <span class="food-kcal">${food.calories} kcal</span>
                            </li>
                        `).join('')}
                    </ul>

                    <div class="recommend-total">
                        <span>총 칼로리</span>
                        <strong class="recommend-kcal">${totalCalories} kcal</strong>
                    </div>

                    <button class="btn-recommend-refresh" data-meal-type="${mealType}">
                        ${refreshIconSvg}
                        새로고침
                    </button>
                </div>
            `;
        }).join('');

        // 새로고침 버튼 이벤트
        document.querySelectorAll('.btn-recommend-refresh').forEach(btn => {
            btn.addEventListener('click', function() {
                loadRecommendations();
            });
        });

        console.log('✅ 추천 표시 완료');
    }

    // ========================================
    // 초기화
    // ========================================
    async function init() {
        await checkLoginStatus();
        await loadRecommendations();
    }

    init();

});