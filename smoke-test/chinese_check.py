import urllib.request
import urllib.parse
import json

base = "http://localhost:8080"

def post(path, body, token=None):
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = "Bearer " + token
    req = urllib.request.Request(base + path, data=json.dumps(body, ensure_ascii=False).encode("utf-8"),
                                 headers=headers, method="POST")
    return json.loads(urllib.request.urlopen(req).read().decode("utf-8"))

def get(path, token):
    req = urllib.request.Request(base + path, headers={"Authorization": "Bearer " + token})
    return json.loads(urllib.request.urlopen(req).read().decode("utf-8"))

token = post("/api/auth/login", {"username": "demo", "password": "demo123456"})["data"]["token"]

# 中文关键词搜索（验证 URL 编码）
kw = urllib.parse.quote("斗破")
search = get(f"/api/novel/search?keyword={kw}", token)
print("search code=", search["code"], "results=", len(search["data"]))
if search["data"]:
    r = search["data"][0]
    print("first name ok=", "斗破苍穹" in r["name"])
    print("cover=", r["cover"])
