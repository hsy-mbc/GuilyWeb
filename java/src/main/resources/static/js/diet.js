document.addEventListener('DOMContentLoaded', function() {

    let isUserLoggedIn = false;
    let goalData = null;
    let calorieData = null;

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
    // 2. 목표 정보 로드
    // ========================================
    async function loadGoalInfo() {
        try {
            if (!isUserLoggedIn) {
                const savedCalc = sessionStorage.getItem('dietCalculation');
                if (savedCalc) {
                    goalData = {
                        targetCalories: JSON.parse(savedCalc),
                        dietGoal: 'MAINTAIN',
                        aiComment: '회원가입 후 더 정확한 분석을 받아보세요!'
                    };
                } else {
                    goalData = {
                        targetCalories: 2100,
                        dietGoal: 'MAINTAIN',
                        aiComment: '건강 정보를 입력하고 AI 분석을 받아보세요!'
                    };
                }
                updateGoalDisplay();
                return;
            }

            const response = await apiRequest('/api/diet/goal', {
                method: 'GET'
            });

            console.log('📥 목표 정보:', response);

            if (response && response.targetCalories) {
                goalData = response;
                updateGoalDisplay();
            } else {
                if (confirm('건강 정보가 없습니다. 설정 페이지로 이동하시겠습니까?')) {
                    location.href = '/health_setup';
                }
            }

        } catch (error) {
            console.error('❌ 목표 정보 로드 실패:', error);
        }
    }

    // ========================================
    // 3. 오늘의 칼로리 로드
    // ========================================
    async function loadTodayCalories() {
        const nutritionSection = document.querySelector('.nutrition-summary');
        const calorieSection = document.querySelector('.total-calorie-section');

        try {
            if (!isUserLoggedIn) {
                if (nutritionSection) nutritionSection.style.display = 'none';
                if (calorieSection) calorieSection.style.display = 'none';
                return;
            }

            if (nutritionSection) nutritionSection.style.display = 'block';
            if (calorieSection) calorieSection.style.display = 'block';

            const response = await apiRequest('/api/diet/calories/today', {
                method: 'GET'
            });

            console.log('📥 오늘의 칼로리:', response);

            if (response) {
                calorieData = response;
                updateCalorieDisplay();
                updateNutritionDisplay();
            }

        } catch (error) {
            console.error('❌ 칼로리 로드 실패:', error);
        }
    }

    // ========================================
    // 4. 목표 정보 화면 업데이트
    // ========================================
    function updateGoalDisplay() {
        if (!goalData) return;

        const caloriesEl = document.querySelector('.calories-big');
        if (caloriesEl) {
            caloriesEl.innerHTML = `${goalData.targetCalories.toLocaleString()} <span class="unit">kcal</span>`;
        }

        const goalTextMap = {
            'LOSS': '체중 감량',
            'MAINTAIN': '체중 유지',
            'GAIN': '체중 증가'
        };
        const goalBtn = document.querySelector('.btn-goal-link');
        if (goalBtn) {
            goalBtn.textContent = `목표: ${goalTextMap[goalData.dietGoal] || '체중 유지'}`;
        }

        const aiTextEl = document.querySelector('.ai-text');
        if (aiTextEl && goalData.aiComment) {
            aiTextEl.textContent = goalData.aiComment;
        }
    }

    // ========================================
    // 5. 칼로리 화면 업데이트
    // ========================================
    function updateCalorieDisplay() {
        if (!calorieData || !goalData) return;

        const currentEl = document.querySelector('.calorie-current');
        if (currentEl) {
            currentEl.textContent = calorieData.totalCalories.toLocaleString();
        }

        const goalEl = document.querySelector('.calorie-goal');
        if (goalEl) {
            goalEl.textContent = `${goalData.targetCalories.toLocaleString()} kcal`;
        }

        const fillEl = document.querySelector('.progress-fill.total');
        if (fillEl) {
            const percentage = Math.min((calorieData.totalCalories / goalData.targetCalories) * 100, 100);
            fillEl.style.width = `${percentage}%`;
        }

        const messageEl = document.querySelector('.progress-message');
        if (messageEl) {
            const remaining = goalData.targetCalories - calorieData.totalCalories;
            if (remaining > 0) {
                messageEl.innerHTML = `목표 칼로리까지 <strong>${remaining.toLocaleString()} kcal</strong> 남았습니다`;
            } else {
                messageEl.innerHTML = `목표 칼로리를 <strong>${Math.abs(remaining).toLocaleString()} kcal</strong> 초과했습니다`;
            }
        }
    }

    // ========================================
    // 6. 영양소 화면 업데이트
    // ========================================
    function updateNutritionDisplay() {
        if (!calorieData) return;

        updateNutritionBar('carbs', calorieData.carbs, calorieData.targetCarbs);
        updateNutritionBar('protein', calorieData.protein, calorieData.targetProtein);
        updateNutritionBar('fat', calorieData.fat, calorieData.targetFat);
    }

    function updateNutritionBar(type, current, target) {
        const row = document.querySelector(`.nutrition-row[data-type="${type}"]`);
        if (!row) return;

        const valueEl = row.querySelector('.nutrition-value');
        const fillEl = row.querySelector('.progress-fill');
        const barContainer = row.querySelector('.progress-bar');

        if (valueEl) {
            valueEl.textContent = `${Math.round(current)}g / ${target}g`;
        }

        if (fillEl && barContainer) {
            const percentage = (current / target) * 66.7;
            const displayPercentage = Math.min(percentage, 100);

            fillEl.style.width = `${displayPercentage}%`;

            if (percentage > 66.7) {
                fillEl.classList.add('over');
            } else {
                fillEl.classList.remove('over');
            }

            let recommendLine = barContainer.querySelector('.recommend-line');
            if (!recommendLine) {
                recommendLine = document.createElement('div');
                recommendLine.className = 'recommend-line';
                barContainer.appendChild(recommendLine);
            }
        }
    }

    // ========================================
    // 7. AI 추천 로드
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
    // 8. 단일 끼니 추천 로드
    // ========================================
    async function loadSingleRecommendation(mealType) {
        try {
            let url = `/api/diet/recommend/single?mealType=${mealType}`;

            if (!isUserLoggedIn) {
                const savedCalc = sessionStorage.getItem('dietCalculation');
                const targetCal = savedCalc ? JSON.parse(savedCalc) : 2100;
                url += `&targetCal=${targetCal}`;
            }

            console.log('📤 단일 끼니 추천 요청:', mealType);

            const response = await apiRequest(url, { method: 'GET' });

            console.log('📥 단일 끼니 추천 응답:', response);

            if (response && response.status === 'success' && response.data) {
                updateSingleMealCard(mealType, response.data);
            }

        } catch (error) {
            console.error('❌ 단일 끼니 추천 실패:', error);
        }
    }

    // ========================================
    // 9. 단일 끼니 카드 업데이트
    // ========================================
    function updateSingleMealCard(mealType, foods) {
        const card = document.querySelector(`.recommend-card[data-meal-type="${mealType}"]`);
        if (!card) return;

        const mealList = card.querySelector('.recommend-list');
        const totalKcal = card.querySelector('.recommend-kcal');

        if (!mealList || !totalKcal) return;

        const totalCalories = foods.reduce((sum, food) => sum + food.calories, 0);

        mealList.innerHTML = foods.map(food => `
            <li>
                <span class="food-name">${escapeHtml(food.food_name)}</span>
                <span class="food-kcal">${Math.round(food.calories * 10) / 10} kcal</span>
            </li>
        `).join('');

        totalKcal.textContent = `${Math.round(totalCalories * 10) / 10} kcal`;
    }

    // ========================================
    // 10. AI 추천 표시
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
                <circle cx="12" cy="12" r="4"></circle>
                <path d="M12 2v2"></path>
                <path d="M12 20v2"></path>
                <path d="m4.93 4.93 1.41 1.41"></path>
                <path d="m17.66 17.66 1.41 1.41"></path>
                <path d="M2 12h2"></path>
                <path d="M20 12h2"></path>
                <path d="m6.34 17.66-1.41 1.41"></path>
                <path d="m19.07 4.93-1.41 1.41"></path>
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
                <div class="recommend-card" data-meal-type="${mealType}">
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
                                <span class="food-kcal">${Math.round(food.calories * 10) / 10} kcal</span>
                            </li>
                        `).join('')}
                    </ul>

                    <div class="recommend-total">
                        <span>총 칼로리</span>
                        <strong class="recommend-kcal">${Math.round(totalCalories * 10) / 10} kcal</strong>
                    </div>

                    <button class="btn-recommend-refresh" data-meal-type="${mealType}">
                        ${refreshIconSvg}
                        새로고침
                    </button>
                </div>
            `;
        }).join('');

        document.querySelectorAll('.btn-recommend-refresh').forEach(btn => {
            btn.addEventListener('click', function() {
                const mealType = this.getAttribute('data-meal-type');
                loadSingleRecommendation(mealType);
            });
        });

        console.log('✅ 추천 표시 완료');
    }

    // ========================================
    // 11. 오늘의 식사 로드
    // ========================================
    async function loadTodayMeals() {
        const mealSection = document.querySelector('.meal-section');

        try {
            if (!isUserLoggedIn) {
                console.log('비회원: 식사 기록 숨김');
                if (mealSection) {
                    mealSection.style.display = 'none';
                }
                return;
            }

            if (mealSection) {
                mealSection.style.display = 'block';
            }

            const response = await apiRequest('/api/diet/meals/today', {
                method: 'GET'
            });

            console.log('📥 오늘의 식사:', response);

            if (response) {
                displayTodayMeals(response);
            }

        } catch (error) {
            console.error('❌ 식사 로드 실패:', error);
            if (mealSection) {
                mealSection.style.display = 'none';
            }
        }
    }

    // ========================================
    // 12. 오늘의 식사 화면 표시
    // ========================================
    const MEAL_ICON_CONFIG = {
        breakfast: `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 8h1a4 4 0 1 1 0 8h-1"></path>
            <path d="M3 8h14v9a4 4 0 0 1-4 4H7a4 4 0 0 1-4-4Z"></path>
        </svg>`,
        lunch: `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="4"></circle>
            <path d="M12 2v2"></path>
            <path d="M12 20v2"></path>
            <path d="m4.93 4.93 1.41 1.41"></path>
            <path d="m17.66 17.66 1.41 1.41"></path>
            <path d="M2 12h2"></path>
            <path d="M20 12h2"></path>
            <path d="m6.34 17.66-1.41 1.41"></path>
            <path d="m19.07 4.93-1.41 1.41"></path>
        </svg>`,
        dinner: `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
        </svg>`
    };

    function displayTodayMeals(mealsData) {
        ['BREAKFAST', 'LUNCH', 'DINNER'].forEach(mealType => {
            const meals = mealsData[mealType] || [];
            const mealCard = document.querySelector(`.meal-card[data-meal="${mealType.toLowerCase()}"]`);

            if (!mealCard) return;

            const iconContainer = mealCard.querySelector('.meal-icon');
            if (iconContainer) {
                iconContainer.innerHTML = MEAL_ICON_CONFIG[mealType.toLowerCase()];
            }

            const mealList = mealCard.querySelector('.meal-list');
            const totalKcal = mealCard.querySelector('.total-kcal');

            if (meals.length === 0) {
                mealList.innerHTML = '<li class="empty-meal">식사 기록이 없습니다</li>';
                if (totalKcal) {
                    totalKcal.textContent = '0 kcal';
                }
                return;
            }

            mealList.innerHTML = meals.map(meal => `
                <li data-log-no="${meal.logNo}">
                    <span class="food-name">${escapeHtml(meal.food_name)}</span>
                    <span class="food-kcal">${meal.calories} kcal</span>
                </li>
            `).join('');

            const total = meals.reduce((sum, meal) => sum + meal.calories, 0);
            if (totalKcal) {
                totalKcal.textContent = `${total} kcal`;
            }
        });
    }

    // ========================================
    // 13. 수정 버튼 이벤트
    // ========================================
    function attachMealEditButtons() {
        document.querySelectorAll('.btn-meal-edit').forEach(btn => {
            btn.addEventListener('click', function() {
                const mealType = this.getAttribute('data-meal');
                location.href = `/diet_edit?meal=${mealType}`;
            });
        });
    }

    // ========================================
    // 14. 상세 버튼 이벤트 (⭐ 이거 추가!)
    // ========================================
    function attachMealDetailButtons() {
        document.querySelectorAll('.btn-meal-detail').forEach(btn => {
            btn.addEventListener('click', function() {
                const mealType = this.getAttribute('data-meal');
                window.mealDetailModal.open(mealType);
            });
        });
    }

    // ========================================
    // 15. 전체 새로고침 버튼 이벤트
    // ========================================
    const btnRefreshAll = document.getElementById('btnRefreshAll');
    if (btnRefreshAll) {
        btnRefreshAll.addEventListener('click', function() {
            loadRecommendations();
        });
    }

    // ========================================
    // 초기화
    // ========================================
    async function init() {
        await checkLoginStatus();
        await loadGoalInfo();
        await loadRecommendations();
        await loadTodayMeals();
        await loadTodayCalories();
        attachMealEditButtons();
        attachMealDetailButtons();
    }

    init();

});