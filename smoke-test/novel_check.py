import urllib.request
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

# 书源规则
rule = {
    "name": "local-novel",
    "url": "http://localhost:8090",
    "charset": "UTF-8",
    "searchUrl": "/search.html?q={{key}}",
    "searchList": "div.book",
    "searchName": "h3",
    "searchAuthor": ".author",
    "searchCover": "img",
    "searchDetail": "a",
    "bookUrlPattern": "/book(\\d+)\\.html",
    "detailName": "h1",
    "detailAuthor": ".author",
    "detailCover": ".cover img",
    "detailIntro": ".intro",
    "tocUrl": "/catalog{{bookId}}.html",
    "catalogList": "ul.catalog li",
    "catalogName": "a",
    "catalogUrl": "a",
    "contentUrl": "{{chapterUrl}}",
    "bookContent": "#content"
}

# 1. 创建书源
bs = post("/api/book-sources", {"name": "local-novel", "url": "http://localhost:8090", "ruleJson": json.dumps(rule, ensure_ascii=False)}, token)
print("book_source id=", bs["data"]["id"])

# 2. 搜索（ASCII 关键词，绕过 URL 编码问题验证规则引擎）
search = get("/api/novel/search?keyword=test", token)
print("search code=", search["code"], "results=", len(search["data"]))
for r in search["data"]:
    print("  -", r["name"], "/", r["author"], "/", r["bookUrl"])

# 3. 详情（取第一个结果）
first = search["data"][0]
detail = get(f"/api/novel/detail?sourceId={first['sourceId']}&bookUrl={first['bookUrl']}", token)
print("detail bookId=", detail["data"]["bookId"], "name=", detail["data"]["name"])
book_id = detail["data"]["bookId"]

# 4. 目录
catalog = get(f"/api/novel/{book_id}/catalog", token)
print("catalog count=", len(catalog["data"]), "first=", catalog["data"][0]["name"])

# 5. 章节正文
ch = catalog["data"][0]
content = get(f"/api/novel/{book_id}/{ch['id']}", token)
cd = content["data"]
print("chapter has_ad=", "广告" in cd["content"], "has_body=", "第一章" in cd["content"],
      "prev=", cd.get("prevId"), "next=", cd.get("nextId"))

# 6. 加入书架 + 列表
post(f"/api/bookshelf/{book_id}", {}, token)
shelf = get("/api/bookshelf", token)
print("bookshelf count=", len(shelf["data"]), "first=", shelf["data"][0]["name"])
