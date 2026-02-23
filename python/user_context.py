from database import DBManager
from datetime import date, timedelta

db = DBManager()


class UserContext:
    """
    사용자 건강정보, 선호도, 식사 로그를 가져와서
    추천 로직에 필요한 컨텍스트를 구성하는 클래스
    """

    def __init__(self, user_no: int):
        self.user_no = user_no
        self.health_info = self._fetch_health_info()
        self.required_nutrients = self._calculate_required_nutrients()
        self.interaction_scores = self._fetch_interaction_scores()
        self.recent_food_nos = self._fetch_recent_food_nos()
        self.recent_recommended = self._fetch_recent_recommended()

    def _fetch_health_info(self) -> dict:
        """유저 건강 정보 조회"""
        return db.fetch_one(
            "SELECT * FROM user_health_info WHERE user_no = %s",
            (self.user_no,)
        )

    def _calculate_required_nutrients(self) -> dict:
        """
        건강 정보 기반으로 하루 권장 영양소 계산
        target_calories 기준으로 탄단지 비율 계산
        """
        if not self.health_info:
            # 기본값
            return {
                'calories': 2000,
                'protein': 55.0,
                'carbs': 275.0,
                'fat': 55.0
            }

        target_cal = self.health_info.get('target_calories', 2000)
        goal = self.health_info.get('diet_goal', '')

        # 목표에 따라 탄단지 비율 조정
        if goal == 'LOSS':         # 다이어트 → 단백질 높이고 탄수화물 줄임
            protein_ratio = 0.30
            carb_ratio    = 0.40
            fat_ratio     = 0.30
        elif goal == 'GAIN':       # 근육 증가 → 단백질 최대
            protein_ratio = 0.35
            carb_ratio    = 0.45
            fat_ratio     = 0.20
        else:                      # 유지 or 기본
            protein_ratio = 0.25
            carb_ratio    = 0.50
            fat_ratio     = 0.25

        print(f"target_calories: {self.health_info.get('target_calories')}")
        print(f"diet_goal: {self.health_info.get('diet_goal')}")

        return {
            'calories': target_cal,
            'protein':  round((target_cal * protein_ratio) / 4, 1),
            'carbs':    round((target_cal * carb_ratio)    / 4, 1),
            'fat':      round((target_cal * fat_ratio)     / 9, 1),
        }

    def _fetch_interaction_scores(self) -> dict:
        """
        반환: {food_no: {'score': ..., 'view_count': ...}}
        """
        rows = db.fetch_all(
            """
            SELECT food_no,
                   (eat_count * 2 + view_count) AS score,
                   view_count
            FROM user_food_interaction
            WHERE user_no = %s
            """,
            (self.user_no,)
        )
        return {
            row['food_no']: {
                'score': row['score'],
                'view_count': row['view_count']
            }
            for row in rows
        } if rows else {}

    def _fetch_recent_food_nos(self) -> dict:
        """
        최근 3일 식사 기록 조회
        반환: {food_no: 며칠전} 형태
        오늘: 패널티 최대 / 어제: 큰 감점 / 2~3일전: 감점
        """
        today = date.today()
        three_days_ago = today - timedelta(days=3)

        rows = db.fetch_all(
            """
            SELECT food_no, eat_date
            FROM diet_log
            WHERE user_no = %s
              AND eat_date >= %s
            ORDER BY eat_date DESC
            """,
            (self.user_no, three_days_ago)
        )

        recent = {}
        if rows:
            for row in rows:
                food_no = row['food_no']
                days_ago = (today - row['eat_date']).days
                if food_no not in recent:
                    recent[food_no] = days_ago

        return recent

    def _fetch_recent_recommended(self) -> set:
        """최근 3일 내 추천된 음식 food_no 집합"""
        rows = db.fetch_all(
            """
            SELECT food_no FROM user_food_interaction
            WHERE user_no = %s
              AND last_recommended_at >= DATE_SUB(NOW(), INTERVAL 3 DAY)
            """,
            (self.user_no,)
        )
        return {row['food_no'] for row in rows} if rows else set()

    def get_penalty_score(self, food_no: int) -> float:
        # 오늘 추천된 음식은 완전 제외
        if food_no in self.recent_recommended:
            return -9999

        penalty = 0.0
        days_ago = self.recent_food_nos.get(food_no)
        if days_ago is not None:
            penalty_map = {0: -100.0, 1: -50.0, 2: -20.0, 3: -10.0}
            penalty += penalty_map.get(days_ago, 0.0)

        data = self.interaction_scores.get(food_no)
        if data:
            view_count = data.get('view_count', 0)
            if view_count >= 7:
                penalty -= 30.0
            elif view_count >= 4:
                penalty -= 15.0
            elif view_count >= 1:
                penalty -= 5.0

        return penalty

    def get_preference_score(self, food_no: int) -> float:
        data = self.interaction_scores.get(food_no)
        if not data or not self.interaction_scores:
            return 0.0
        max_score = max(v['score'] for v in self.interaction_scores.values())
        return round((data['score'] / max_score) * 100, 2) if max_score > 0 else 0.0

    def update_interaction(self, food_no: int):
        """
        새로고침 시 호출 - view_count 증가 + last_recommended_at 갱신
        """
        existing = db.fetch_one(
            "SELECT interaction_no FROM user_food_interaction WHERE user_no=%s AND food_no=%s",
            (self.user_no, food_no)
        )
        if existing:
            db.execute(
                """UPDATE user_food_interaction 
                   SET view_count = view_count + 1,
                       last_recommended_at = NOW()
                   WHERE user_no=%s AND food_no=%s""",
                (self.user_no, food_no)
            )
        else:
            db.execute(
                """INSERT INTO user_food_interaction 
                   (user_no, food_no, view_count, eat_count, last_recommended_at)
                   VALUES (%s, %s, 1, 0, NOW())""",
                (self.user_no, food_no)
            )

    def summary(self):
        """디버깅용 요약 출력"""
        print(f"[UserContext] user_no={self.user_no}")
        print(f"  목표 칼로리: {self.required_nutrients['calories']} kcal")
        print(f"  단백질: {self.required_nutrients['protein']}g / "
              f"탄수화물: {self.required_nutrients['carbs']}g / "
              f"지방: {self.required_nutrients['fat']}g")
        print(f"  선호 음식 수: {len(self.interaction_scores)}개")
        print(f"  최근 3일 먹은 음식 수: {len(self.recent_food_nos)}개")