# backend-python/database.py
import pymysql
import pymysql.cursors

class DBManager:
    def __init__(self):
        self.config = {
            'host': 'localhost',
            'port': 3306,
            'user': 'guily',
            'password': 'vhktn0402!!',
            'db': 'guilyweb',
            'charset': 'utf8mb4',
            'cursorclass': pymysql.cursors.DictCursor
        }

    def _get_connection(self):
        return pymysql.connect(**self.config)

    def fetch_all(self, query, params=None):
        """데이터 조회 (SELECT) - 여러 건"""
        conn = self._get_connection()
        try:
            with conn.cursor() as cursor:
                cursor.execute(query, params)
                return cursor.fetchall()
        finally:
            conn.close()

    def fetch_one(self, query, params=None):
        """데이터 조회 (SELECT) - 한 건"""
        conn = self._get_connection()
        try:
            with conn.cursor() as cursor:
                cursor.execute(query, params)
                return cursor.fetchone()
        finally:
            conn.close()

    def execute(self, query, params=None):
        """데이터 변경 (INSERT, UPDATE, DELETE)"""
        conn = self._get_connection()
        try:
            with conn.cursor() as cursor:
                cursor.execute(query, params)
                conn.commit()
                return cursor.lastrowid
        except Exception as e:
            conn.rollback()
            raise e
        finally:
            conn.close()