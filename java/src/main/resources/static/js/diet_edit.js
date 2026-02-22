document.addEventListener('DOMContentLoaded', function() {

    const urlParams = new URLSearchParams(window.location.search);
    const mealType = urlParams.get('meal') || 'breakfast';

    const mealTypeInput = document.getElementById('currentMealType');
    const currentMealType = mealTypeInput ? mealTypeInput.value : mealType;

    const mealLabels = {
        'breakfast': '아침',
        'lunch': '점심',
        'dinner': '저녁'
    };

    const editTitle = document.getElementById('editTitle');
    if (editTitle) {
        editTitle.textContent = `${mealLabels[currentMealType]} 식단 수정`;
    }

    console.log('현재 끼니:', currentMealType);

    // ========================================
    // 1. 로그인 상태 확인
    // ========================================
    let isUserLoggedIn = false;

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
    // 2. 현재 식단 로드
    // ========================================
    async function loadCurrentMeal() {
        try {
            const response = await apiRequest(`/api/diet/meals/today`, {
                method: 'GET'
            });

            console.log('📥 오늘의 식사:', response);
            console.log('📥 현재 끼니 타입:', currentMealType.toUpperCase());
            console.log('📥 해당 끼니 데이터:', response[currentMealType.toUpperCase()]);

            if (response && response[currentMealType.toUpperCase()]) {
                const meals = response[currentMealType.toUpperCase()];
                console.log('📥 식사 개수:', meals.length);
                displayCurrentMeal(meals);
            } else {
                console.log('⚠️ 식사 데이터 없음');
                displayEmptyMeal();
            }

        } catch (error) {
            console.error('❌ 식단 로드 실패:', error);
            displayEmptyMeal();
        }
    }

    // ========================================
    // 3. 현재 식단 표시
    // ========================================
    function displayCurrentMeal(meals) {
        const mealList = document.getElementById('currentMealList');
        const emptyMeal = document.getElementById('emptyMeal');

        if (!meals || meals.length === 0) {
            displayEmptyMeal();
            return;
        }

        if (mealList) {
            mealList.innerHTML = meals.map(meal => `
                <li class="meal-item" data-log-no="${meal.logNo}">
                    <div class="meal-info">
                        <span class="food-name">${escapeHtml(meal.food_name)}</span>
                        <span class="food-kcal">${meal.calories} kcal</span>
                    </div>
                    <button class="btn-remove" data-log-no="${meal.logNo}">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M18 6 6 18M6 6l12 12"/>
                        </svg>
                    </button>
                </li>
            `).join('');
        }

        if (emptyMeal) {
            emptyMeal.style.display = 'none';
        }

        attachRemoveButtons();
    }

    function displayEmptyMeal() {
        const mealList = document.getElementById('currentMealList');
        const emptyMeal = document.getElementById('emptyMeal');

        if (mealList) {
            mealList.innerHTML = '';
        }

        if (emptyMeal) {
            emptyMeal.style.display = 'block';
        }
    }

    // ========================================
    // 4. 삭제 버튼 이벤트
    // ========================================
    function attachRemoveButtons() {
        document.querySelectorAll('.btn-remove').forEach(btn => {
            btn.addEventListener('click', async function() {
                const logNo = this.getAttribute('data-log-no');
                await deleteMeal(logNo);
            });
        });
    }

    async function deleteMeal(logNo) {
        if (!confirm('이 음식을 삭제하시겠습니까?')) {
            return;
        }

        try {
            await apiRequest(`/api/diet/${logNo}`, {
                method: 'DELETE'
            });

            alert('삭제되었습니다!');

            await loadCurrentMeal();

            await updateNutritionAnalysis();

        } catch (error) {
            console.error('❌ 삭제 실패:', error);
            alert('삭제에 실패했습니다.');
        }
    }

    // ========================================
    // 5. 추천 식단 로드
    // ========================================
    async function loadRecommendations() {
        try {
            let url = `/api/diet/recommend/single?mealType=${currentMealType.toUpperCase()}`;

            if (!isUserLoggedIn) {
                const savedCalc = sessionStorage.getItem('dietCalculation');
                const targetCal = savedCalc ? JSON.parse(savedCalc) : 2100;
                url += `&targetCal=${targetCal}`;
            }

            console.log('📤 추천 요청 URL:', url);

            const response = await apiRequest(url, { method: 'GET' });

            console.log('📥 추천 응답:', response);

            if (response && response.status === 'success' && response.data) {
                displayRecommendations(response.data);
            }

        } catch (error) {
            console.error('❌ 추천 로드 실패:', error);
        }
    }

    // ========================================
    // 6. 추천 식단 표시
    // ========================================
    function displayRecommendations(foods) {
        const recommendList = document.getElementById('recommendList');

        if (!recommendList) {
            console.warn('⚠️ #recommendList 요소를 찾을 수 없습니다');
            return;
        }

        if (!foods || foods.length === 0) {
            recommendList.innerHTML = '<li class="empty-recommend">추천 식단이 없습니다</li>';
            return;
        }

        recommendList.innerHTML = foods.map(food => `
            <li class="recommend-item">
                <div class="recommend-info">
                    <span class="food-name">${escapeHtml(food.food_name)}</span>
                    <span class="food-kcal">${Math.round(food.calories * 10) / 10} kcal</span>
                </div>
                <button class="btn-add-recommend" data-food-no="${food.food_no}">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M12 5v14M5 12h14"/>
                    </svg>
                </button>
            </li>
        `).join('');

        attachAddButtons();

        console.log('✅ 추천 표시 완료');
    }

    // ========================================
    // 7. 추가 버튼 이벤트
    // ========================================
    function attachAddButtons() {
        document.querySelectorAll('.btn-add-recommend').forEach(btn => {
            btn.addEventListener('click', async function() {
                const foodNo = this.getAttribute('data-food-no');
                await addFoodToMeal(foodNo);
            });
        });
    }

    async function addFoodToMeal(foodNo) {
        try {
            const response = await apiRequest('/api/diet/add', {
                method: 'POST',
                body: JSON.stringify({
                    foodNo: parseInt(foodNo),
                    mealType: currentMealType.toUpperCase(),
                    quantity: 1.0,
                    eatDate: new Date().toISOString().split('T')[0]
                })
            });

            console.log('✅ 식단 추가 성공:', response);

            await loadCurrentMeal();

            await updateNutritionAnalysis();

        } catch (error) {
            console.error('❌ 식단 추가 실패:', error);
            alert('식단 추가에 실패했습니다.');
        }
    }

    // ========================================
    // 8. 새로고침 버튼 이벤트
    // ========================================
    const btnRefreshRecommend = document.getElementById('btnRefreshRecommend');
    if (btnRefreshRecommend) {
        btnRefreshRecommend.addEventListener('click', function() {
            loadRecommendations();
        });
    }

    // ========================================
    // 9. 영양 분석 업데이트
    // ========================================
    async function updateNutritionAnalysis() {
        try {
            const response = await apiRequest(`/api/diet/meal/${currentMealType}`, {
                method: 'GET'
            });

            if (response && response.totalNutrition) {
                displayNutritionAnalysis(response.totalNutrition);
                displayNutritionChart(response.totalNutrition);
            }

        } catch (error) {
            console.error('❌ 영양 분석 로드 실패:', error);
        }
    }

    // ========================================
    // 10. 영양 분석 카드 표시
    // ========================================
    function displayNutritionAnalysis(nutrition) {
        // 총 칼로리
        const totalCaloriesEl = document.getElementById('totalCalories');
        if (totalCaloriesEl) {
            totalCaloriesEl.textContent = `${Math.round(nutrition.calories)} kcal`;
        }

        // 탄수화물
        const totalCarbsEl = document.getElementById('totalCarbs');
        if (totalCarbsEl) {
            totalCarbsEl.textContent = `${Math.round(nutrition.carbs)} g`;
        }

        // 단백질
        const totalProteinEl = document.getElementById('totalProtein');
        if (totalProteinEl) {
            totalProteinEl.textContent = `${Math.round(nutrition.protein)} g`;
        }

        // 지방
        const totalFatEl = document.getElementById('totalFat');
        if (totalFatEl) {
            totalFatEl.textContent = `${Math.round(nutrition.fat)} g`;
        }
    }

    // ========================================
    // 11. 영양 분석 차트 업데이트
    // ========================================
    let nutritionChart = null;

    function displayNutritionChart(nutrition) {
        const canvas = document.getElementById('nutritionChart');
        if (!canvas) return;

        const ctx = canvas.getContext('2d');

        if (nutritionChart) {
            nutritionChart.destroy();
        }

        const chartData = {
            labels: ['탄수화물', '단백질', '지방'],
            datasets: [{
                data: [
                    Math.round(nutrition.carbs || 0),
                    Math.round(nutrition.protein || 0),
                    Math.round(nutrition.fat || 0)
                ],
                backgroundColor: [
                    '#3b82f6',
                    '#ef4444',
                    '#f59e0b'
                ],
                borderWidth: 0
            }]
        };

        // 차트 생성
        nutritionChart = new Chart(ctx, {
            type: 'doughnut',
            data: chartData,
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return context.label + ': ' + context.parsed + 'g';
                            }
                        }
                    }
                }
            }
        });
    }

    // ========================================
    // 12. 음식 검색
    // ========================================
    const searchInput = document.getElementById('foodSearchInput');
    const btnSearch = document.getElementById('btnSearch');
    const searchResults = document.getElementById('searchResults');
    const btnClearSearch = document.getElementById('btnClearSearch');

    if (btnSearch) {
        btnSearch.addEventListener('click', function() {
            performSearch();
        });
    }

    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                performSearch();
            }
        });
    }

    if (btnClearSearch) {
        btnClearSearch.addEventListener('click', function() {
            clearSearch();
        });
    }

    async function performSearch() {
        const query = searchInput.value.trim();

        if (!query) {
            alert('음식 이름을 입력해주세요.');
            return;
        }

        try {
            const response = await apiRequest(`/api/diet/search?query=${encodeURIComponent(query)}&limit=20`, {
                method: 'GET'
            });

            if (response && response.foods) {
                displaySearchResults(response.foods);
            }

        } catch (error) {
            console.error('❌ 검색 실패:', error);
            alert('검색에 실패했습니다.');
        }
    }

    function displaySearchResults(foods) {
        const resultList = document.getElementById('searchResultList');
        const resultCount = document.getElementById('searchResultCount');

        if (!foods || foods.length === 0) {
            resultList.innerHTML = '<li class="empty-search-result">검색 결과가 없습니다</li>';
            resultCount.textContent = '0';
        } else {
            resultList.innerHTML = foods.map(food => `
                <li class="search-result-item">
                    <div class="search-result-info">
                        <span class="search-result-name">${escapeHtml(food.foodName)}</span>
                        <span class="search-result-details">
                            ${Math.round(food.calories)} kcal |
                            탄 ${Math.round(food.carbs)}g |
                            단 ${Math.round(food.protein)}g |
                            지 ${Math.round(food.fat)}g
                        </span>
                    </div>
                    <button class="btn-add-search" data-food-no="${food.foodNo}">
                        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M12 5v14M5 12h14"/>
                        </svg>
                    </button>
                </li>
            `).join('');

            resultCount.textContent = foods.length;

            attachSearchAddButtons();
        }

        searchResults.style.display = 'block';
    }

    function attachSearchAddButtons() {
        document.querySelectorAll('.btn-add-search').forEach(btn => {
            btn.addEventListener('click', async function() {
                const foodNo = this.getAttribute('data-food-no');
                await addFoodToMeal(foodNo);
            });
        });
    }

    function clearSearch() {
        searchInput.value = '';
        searchResults.style.display = 'none';
    }

    // ========================================
    // 13. 완료 버튼
    // ========================================
    const btnComplete = document.getElementById('btnComplete');
    if (btnComplete) {
        btnComplete.addEventListener('click', function() {
            location.href = '/diet';
        });
    }

    // ========================================
    // 초기화
    // ========================================
    async function init() {
        await checkLoginStatus();
        await loadCurrentMeal();
        await loadRecommendations();
        await updateNutritionAnalysis();
    }

    init();

});