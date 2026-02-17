from fastapi import FastAPI
from recommender import FoodRecommender

app = FastAPI()
recommender = FoodRecommender()

# 하루 전체 추천 (페이지 로드용)
@app.get("/ai/recommend/daily")
async def get_daily(user_no: int = None, guest_cal: int = None):
    plan = recommender.recommend_daily(user_no, guest_cal)
    return {"status": "success", "data": plan}

# 한 끼 단일 추천 (새로고침용)
@app.get("/ai/recommend/single")
async def get_single(meal_type: str, user_no: int = None, guest_cal: int = None):
    total_cal = recommender._get_target_cal(user_no, guest_cal)
    ratio = {"BREAKFAST": 0.3, "LUNCH": 0.4, "DINNER": 0.3}.get(meal_type, 0.4)

    meal_set = recommender.recommend_set(total_cal * ratio, user_no)
    return {"status": "success", "data": meal_set}

# uvicorn main:app --reload --port 8000