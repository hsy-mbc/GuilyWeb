from fastapi import FastAPI
from database import DBManager
from recommender import FoodRecommender

app = FastAPI()
db = DBManager()
recommender = FoodRecommender(db)

# 하루 전체 추천 (페이지 로드용)
@app.get("/ai/recommend/daily")
async def get_daily_recommend(user_no: int = None, guest_cal: int = None):
    result = recommender.recommend_daily(user_no, guest_cal)
    return {"status": "success", "data": result}

# 한 끼 단일 추천 (새로고침용)
@app.get("/ai/recommend/single")
async def get_single_refresh(
    meal_type: str,
    user_no: int = None,
    guest_cal: int = None
):
    current_plan = {}

    result = recommender.recommend_single(meal_type, current_plan, user_no, guest_cal)
    return {"status": "success", "data": result}

# uvicorn main:app --reload --port 8000