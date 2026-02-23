from fastapi import FastAPI
from dotenv import load_dotenv
from google import genai
import os

from RecommendAI import RecommendResponse, build_prompt
from recommender import Recommender

load_dotenv()
client = genai.Client(api_key=os.getenv("GEMINI_API_KEY"))
app = FastAPI()
recommender = Recommender()

# 하루 전체 추천 (페이지 로드용)
@app.get("/ai/recommend/daily")
async def get_daily_recommend(user_no: int = None, targetCal: int = None):
    result = recommender.recommend_daily(user_no=user_no, guest_cal=targetCal)
    return {"status": "success", "data": result}

# 한 끼 단일 추천 (새로고침용)
@app.get("/ai/recommend/single")
async def get_single_refresh(meal_type: str, user_no: int = None, targetCal: int = None):
    result = recommender.recommend_single(meal_type, {}, user_no, targetCal)
    return {"status": "success", "data": result}

# ai 코멘트
@app.get("/ai/recommend/comment", response_model=RecommendResponse)
async def recommend(currentCalories: int, targetCalories: int, dietGoal: str):
    print(f"받은 데이터 - current: {currentCalories}, target: {targetCalories}, goal: {dietGoal}")
    try:
        prompt = build_prompt(currentCalories, targetCalories, dietGoal)
        response = client.models.generate_content(
            model="gemini-2.5-flash",
            contents=prompt
        )
        comment = response.text.strip()
        print(f"AI 코멘트: {comment}")

    except Exception as e:
        print(f"Gemini 호출 실패: {e}")
        percentage = currentCalories / targetCalories * 100
        remaining = targetCalories - currentCalories

        if percentage < 50:
            comment = f"현재까지 {currentCalories:,}kcal를 섭취하셨네요! {abs(remaining):,}kcal를 더 섭취하시면 목표를 달성할 수 있어요! 💪"
        elif percentage < 90:
            comment = f"현재까지 {currentCalories:,}kcal를 섭취하셨네요! 목표의 {percentage:.0f}%를 달성하셨습니다. 조금만 더 화이팅! 👍"
        elif percentage <= 110:
            comment = f"훌륭해요! 목표 칼로리를 {percentage:.0f}% 달성하셨습니다. 꾸준히 실천하고 계시네요! 🎉"
        else:
            comment = f"오늘 목표보다 {abs(remaining):,}kcal 초과했어요. 내일 다시 시작하면 돼요! 💚"

    return RecommendResponse(comment=comment)

# uvicorn main:app --reload --port 8000