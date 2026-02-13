from database import DBManager


class FoodRecommender:
    def __init__(self):
        self.db = DBManager()

    def recommend_meal(self, user_no):
        # 1. 유저의 권장 칼로리 정보 가져오기
        user_info = self.db.fetch_one(
            "SELECT target_calories FROM user_health_info WHERE user_no = %s",
            (user_no,)
        )

        if not user_info or not user_info['target_calories']:
            return {"error": "유저의 권장 칼로리 정보가 없습니다."}

        target_cal = user_info['target_calories'] / 3  # 한 끼 권장량 (단순히 1/3)

        # 2. 최근 3일간 먹은 음식 제외 리스트 뽑기
        recent_foods = self.db.fetch_all(
            "SELECT food_no FROM diet_log WHERE user_no = %s AND eat_date >= DATE_SUB(CURDATE(), INTERVAL 3 DAY)",
            (user_no,)
        )
        exclude_ids = [str(f['food_no']) for f in recent_foods]

        # 3. DB에서 조건에 맞는 음식 랜덤 추천
        # 조건: 한 끼 권장 칼로리의 +- 100kcal 범위 & 최근 먹은 음식 제외
        query = f"""
            SELECT * FROM food_info 
            WHERE calories BETWEEN %s AND %s
            {"AND food_no NOT IN (" + ",".join(exclude_ids) + ")" if exclude_ids else ""}
            ORDER BY RAND() 
            LIMIT 3
        """
        params = (target_cal - 100, target_cal + 100)

        return self.db.fetch_all(query, params)


# test
if __name__ == "__main__":
    print(f"전체 수집 완료.")