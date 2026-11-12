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

legado_json = open(r"E:\系统\桌面\毕业设计\smoke-test\legado-source.json", encoding="utf-8").read()

# 1. 粘贴导入 legado JSON
r = post("/api/book-sources/import", {"ruleJson": legado_json}, token)
print("paste_import code=", r["code"], "count=", len(r["data"]), "name=", r["data"][0]["name"] if r["data"] else "-")

# 2. URL 导入（从本地服务器抓 JSON）
r2 = post("/api/book-sources/import-url", {"url": "http://localhost:8090/legado-source.json"}, token)
print("url_import code=", r2["code"], "count=", len(r2["data"]))

# 3. 中文关键词搜索（走导入的 legado 书源）
kw = urllib.parse.quote("斗破")
s = get(f"/api/novel/search?keyword={kw}", token)
print("search code=", s["code"], "results=", len(s["data"]))
if s["data"]:
    r0 = s["data"][0]
    print("  name_ok=", "斗破苍穹" in r0["name"], "cover_abs=", r0["cover"].startswith("http"))

    # 4. 详情
    d = get(f"/api/novel/detail?sourceId={r0['sourceId']}&bookUrl={r0['bookUrl']}", token)
    print("detail name_ok=", "斗破苍穹" in d["data"]["name"], "bookId=", d["data"]["bookId"])
    bid = d["data"]["bookId"]

    # 5. 目录（toc 在详情页上，走 {{bookUrl}} 逻辑）
    cat = get(f"/api/novel/{bid}/catalog", token)
    print("catalog count=", len(cat["data"]), "first=", cat["data"][0]["name"])

    # 6. 章节正文
    ch = cat["data"][0]
    ct = get(f"/api/novel/{bid}/{ch['id']}", token)
    print("chapter has_ad=", "广告" in ct["data"]["content"], "has_body=", "第一章" in ct["data"]["content"])
