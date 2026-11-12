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

# 1. 原地重转换
rc = post("/api/book-sources/reconvert", {}, token)
print("reconvert count=", rc["data"])

# 2. 看几个 CSS 类书源转换后的规则
srcs = get("/api/book-sources", token)["data"]
for s in srcs:
    if s["id"] in (26, 16, 15, 13, 12):
        rule = json.loads(s["ruleJson"])
        print(f"--- id={s['id']} name={s['name']}")
        print("   searchUrl=", rule.get("searchUrl"), "method=", rule.get("searchMethod"))
        print("   searchList=", rule.get("searchList"), "name=", rule.get("searchName"), "nameType=", rule.get("searchNameType"))

# 3. 搜索测试
kw = urllib.parse.quote("斗破苍穹")
r = get(f"/api/novel/search?keyword={kw}", token)
print("search results=", len(r["data"]))
