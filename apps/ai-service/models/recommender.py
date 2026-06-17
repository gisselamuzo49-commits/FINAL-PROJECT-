from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import numpy as np

class TFIDFRecommender:
    def recommend(self, perfil: str, ofertas: list[dict]) -> list[dict]:
        if not ofertas:
            return []
        corpus = [perfil] + [o["descripcion"] for o in ofertas]
        tfidf = TfidfVectorizer().fit_transform(corpus)
        scores = cosine_similarity(tfidf[0:1], tfidf[1:]).flatten()
        ranked = sorted(zip([o["id"] for o in ofertas], scores),
                       key=lambda x: x[1], reverse=True)
        return [{"id": id_, "score": round(float(score), 4)} for id_, score in ranked]
