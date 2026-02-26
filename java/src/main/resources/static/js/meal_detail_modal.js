// ==========================================
// MEAL_DETAIL_MODAL.JS - 식사 상세 모달
// ==========================================

class MealDetailModal {
    constructor() {
        this.overlay = document.getElementById('mealDetailModal');
        this.modal = this.overlay.querySelector('.meal-detail-modal');
        this.currentMealType = null;
        this.mealData = null;

        this.init();
    }

    init() {
        const closeBtn = document.getElementById('btnCloseModal');
        closeBtn.addEventListener('click', () => this.close());

        this.overlay.addEventListener('click', (e) => {
            if (e.target === this.overlay) {
                this.close();
            }
        });
    }

    // 모달 열기
    async open(mealType) {
        this.currentMealType = mealType;

        // 데이터 로드
        await this.loadMealData();

        // 렌더링
        this.render();

        // 모달 표시
        this.overlay.classList.remove('hidden');
        document.body.style.overflow = 'hidden';
    }

    // 식사 데이터 로드
    async loadMealData() {
        try {
            const response = await apiRequest(`/api/diet/meal/${this.currentMealType}`, {
                method: 'GET'
            });

            this.mealData = response;

        } catch (error) {
            console.error('식사 데이터 로드 실패:', error);
            this.mealData = {
                totalNutrition: {},
                targetNutrition: {},
                foods: []
            };
        }
    }

    // 데이터 렌더링
    render() {
        const mealLabels = {
            'breakfast': '아침',
            'lunch': '점심',
            'dinner': '저녁'
        };

        const label = mealLabels[this.currentMealType];

        // 제목 설정
        const modalTitle = this.overlay.querySelector('.modal-title');
        modalTitle.textContent = `${label} 영양정보`;
        modalTitle.className = `modal-title ${this.currentMealType}`;

        // 섹션 제목
        const mealLabel = document.getElementById('modalMealLabel');
        mealLabel.textContent = `${label} 총 영양소`;

        // 총 영양소 렌더링
        this.renderTotalNutrition();

        // 음식 목록 렌더링
        this.renderFoodList();
    }

    // 총 영양소 렌더링
    renderTotalNutrition() {
        const grid = document.getElementById('totalNutritionGrid');
        const nutrition = this.mealData.totalNutrition || {};
        const target = this.mealData.targetNutrition || {};

        console.log('totalNutrition:', nutrition);
        console.log('targetNutrition:', target);

        const items = [
            { label: '칼로리', current: nutrition.calories || 0, target: target.calories || 0, unit: 'kcal' },
            { label: '탄수화물', current: nutrition.carbs || 0, target: target.carbs || 0, unit: 'g' },
            { label: '단백질', current: nutrition.protein || 0, target: target.protein || 0, unit: 'g' },
            { label: '지방', current: nutrition.fat || 0, target: target.fat || 0, unit: 'g' },
            { label: '식이섬유', current: nutrition.fiber || 0, target: target.fiber || 0, unit: 'g' },
            { label: '당류', current: nutrition.sugar || 0, target: target.sugar || 0, unit: 'g' },
            { label: '수분', current: nutrition.water || 0, target: target.water || 0, unit: 'ml' },
            { label: '나트륨', current: nutrition.sodium || 0, target: target.sodium || 0, unit: 'mg' },
            { label: '칼슘', current: nutrition.calcium || 0, target: target.calcium || 0, unit: 'mg' },
            { label: '마그네슘', current: nutrition.magnesium || 0, target: target.magnesium || 0, unit: 'mg' },
            { label: '철분', current: nutrition.iron || 0, target: target.iron || 0, unit: 'mg' },
            { label: '칼륨', current: nutrition.potassium || 0, target: target.potassium || 0, unit: 'mg' },
            { label: '비타민 A', current: nutrition.vitaminA || 0, target: target.vitaminA || 0, unit: 'μg' },
            { label: '비타민 C', current: nutrition.vitaminC || 0, target: target.vitaminC || 0, unit: 'mg' },
            { label: '비타민 D', current: nutrition.vitaminD || 0, target: target.vitaminD || 0, unit: 'μg' }
        ];

        grid.innerHTML = items.map(item => {
            const percent = item.target > 0 ? Math.round((item.current / item.target) * 100) : 0;
            const isOver = percent >= 70;

            return `
                <div class="nutrition-item">
                    <span class="nutrition-label">${item.label}</span>
                    <div class="nutrition-values">
                        <strong class="nutrition-current ${isOver ? 'over' : ''}">${Math.round(item.current * 10) / 10}</strong>
                        <span class="nutrition-divider">/</span>
                        <span class="nutrition-target">${Math.round(item.target * 10) / 10} ${item.unit}</span>
                    </div>
                    <span class="nutrition-percent ${isOver ? 'over' : ''}">(${percent}%)</span>
                </div>
            `;
        }).join('');
    }

    // 음식 목록 렌더링
    renderFoodList() {
        const container = document.getElementById('foodListContainer');
        const foods = this.mealData.foods || [];

        if (foods.length === 0) {
            container.innerHTML = '<div class="empty-food-list">식사 기록이 없습니다</div>';
            return;
        }

        container.innerHTML = foods.map(food => `
            <div class="food-item" data-log-no="${food.logNo}">
                <div class="food-item-header">
                    <div class="food-info">
                        <span class="food-name">${escapeHtml(food.foodName)}</span>
                        <span class="food-kcal">${food.calories} kcal</span>
                    </div>
                    <div class="food-actions">
                        <button class="btn-delete-food" data-log-no="${food.logNo}">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                            </svg>
                        </button>
                        <button class="btn-toggle-food">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <polyline points="6 9 12 15 18 9"></polyline>
                            </svg>
                        </button>
                    </div>
                </div>
                <div class="food-detail">
                    <div class="food-nutrition-grid">
                        <div class="food-nutrition-item">
                            <span class="food-nutrition-label">탄수화물</span>
                            <span class="food-nutrition-value">${food.carbs || 0}g</span>
                        </div>
                        <div class="food-nutrition-item">
                            <span class="food-nutrition-label">단백질</span>
                            <span class="food-nutrition-value">${food.protein || 0}g</span>
                        </div>
                        <div class="food-nutrition-item">
                            <span class="food-nutrition-label">지방</span>
                            <span class="food-nutrition-value">${food.fat || 0}g</span>
                        </div>
                        <div class="food-nutrition-item">
                            <span class="food-nutrition-label">식이섬유</span>
                            <span class="food-nutrition-value">${food.fiber || 0}g</span>
                        </div>
                    </div>
                </div>
            </div>
        `).join('');

        // 이벤트 재연결
        this.attachFoodEvents();
    }

    // 음식 아이템 이벤트
    attachFoodEvents() {
        const toggleBtns = this.overlay.querySelectorAll('.btn-toggle-food');
        toggleBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                const foodItem = btn.closest('.food-item');
                foodItem.classList.toggle('expanded');
            });
        });

        // 삭제
        const deleteBtns = this.overlay.querySelectorAll('.btn-delete-food');
        deleteBtns.forEach(btn => {
            btn.addEventListener('click', async () => {
                const logNo = btn.getAttribute('data-log-no');
                await this.deleteFood(logNo);
            });
        });
    }

    // 음식 삭제
    async deleteFood(logNo) {
        if (!confirm('이 음식을 삭제하시겠습니까?')) {
            return;
        }

        try {
            await apiRequest(`/api/diet/${logNo}`, {
                method: 'DELETE'
            });

            // 모달 새로고침
            await this.loadMealData();
            this.render();

            // 메인 페이지 새로고침
            await Promise.all([
                window.loadTodayMeals?.(),
                window.loadTodayCalories?.()
            ]);

        } catch (error) {
            console.error('삭제 실패:', error);
            alert('삭제에 실패했습니다.');
        }
    }

    // 모달 닫기
    close() {
        this.overlay.classList.add('hidden');
        document.body.style.overflow = '';
    }
}

// 전역 인스턴스
window.mealDetailModal = new MealDetailModal();