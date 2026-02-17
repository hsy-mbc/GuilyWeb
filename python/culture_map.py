# backend-python/engines/culture_map.py

class CultureMap:
    def __init__(self):
        self.DATA = {
            'KOREAN': {
                'main_keywords': ['찌개', '탕', '국밥', '조림', '볶음', '구이', '비빔밥', '쌈밥'],
                'side_categories': ['나물/무침류', '조림류', '김치류', '장아찌/절임류'],
                'base_categories': ['밥류', '죽 및 스프류']
            },
            'WESTERN': {
                'main_keywords': ['파스타', '스테이크', '커틀릿', '피자', '그라탕', '스파게티', '샌드위치', '햄버거'],
                'side_categories': ['샐러드', '채소류'],
                'side_keywords': ['피클', '코울슬로', '매쉬드', '발사믹', '올리브'],
                'base_categories': ['빵류', '면 및 만두류']
            },
            'JAPANESE': {
                'main_keywords': ['초밥', '돈카츠', '라멘', '우동', '소바', '회덮밥', '가츠동', '텐동'],
                'side_keywords': ['미소', '단무지', '초생강', '락교', '쯔케모노', '와사비', '가쓰오부시'],
                'base_categories': ['밥류', '면 및 만두류']
            }
        }

        self.WET_KEYWORDS = ['찌개', '탕', '국', '스프', '죽', '면', '라멘', '우동', '짬뽕']
        self.SPICY_KEYWORDS = ['매운', '볶음', '조림', '김치', '고추장', '마라', '떡볶이', '불닭']

    def detect_culture(self, food):
        """음식 이름과 카테고리를 분석해 문화권(KOREAN, WESTERN, JAPANESE) 반환"""
        name = food.get('food_name', '')
        cat = food.get('main_category', '')

        for culture, rules in self.DATA.items():
            if any(k in name for k in rules.get('main_keywords', [])):
                return culture
            if any(k in name for k in rules.get('side_keywords', [])):
                return culture
            if cat in rules.get('side_categories', []) or cat in rules.get('base_categories', []):
                return culture

        return 'KOREAN'

    def is_wet(self, food):
        """국물 요리인지 판별"""
        name = food.get('food_name', '')
        return any(k in name for k in self.WET_KEYWORDS)

    def is_spicy(self, food):
        """맵거나 자극적인 요리인지 판별"""
        name = food.get('food_name', '')
        return any(k in name for k in self.SPICY_KEYWORDS)

    def get_harmony_bonus(self, anchor_culture, candidate_food):
        """앵커 음식의 문화권과 후보 음식이 일치할 때 부여할 가산점 계산"""
        candidate_culture = self.detect_culture(candidate_food)
        if anchor_culture == candidate_culture:
            return 100
        return 0