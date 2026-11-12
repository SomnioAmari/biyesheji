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

rule = {
    "name": "笔趣阁365",
    "url": "https://www.biquge365.net",
    "charset": "UTF-8",
    "headers": {"Referer": "https://www.biquge365.net/"},
    "searchUrl": "/s.php",
    "searchMethod": "POST",
    "searchBody": "s={{key}}&type=articlename",
    "searchList": "ul.search li:not(.fen)",
    "searchName": "span.name a",
    "searchAuthor": "span.zuo a",
    "searchDetail": "span.name a",
    "bookUrlPattern": "/book/(\\d+)/",
    "detailName": "h1",
    "detailAuthor": ".xinxi .x1 a",
    "detailCover": ".zhutu img",
    "detailIntro": ".x3",
    "tocUrl": "/newbook/{{bookId}}/",
    "catalogList": "ul.info li",
    "catalogName": "a",
    "catalogUrl": "a",
    "contentUrl": "{{chapterUrl}}",
    "bookContent": ".txt"
}

r = post("/api/book-sources/import", {"ruleJson": json.dumps(rule, ensure_ascii=False)}, token)
print("import count=", len(r["data"]), "name=", r["data"][0]["name"] if r["data"] else "-")

kw = urllib.parse.quote("斗破苍穹")
s = get(f"/api/novel/search?keyword={kw}", token)
print("search results=", len(s["data"]))
if s["data"]:
    r0 = s["data"][0]
    print("  first:", r0["name"], "/", r0["author"], "/", r0["bookUrl"])

    d = get(f"/api/novel/detail?sourceId={r0['sourceId']}&bookUrl={urllib.parse.quote(r0['bookUrl'], safe='/')}", token)
    print("  detail:", d["data"]["name"], "/", d["data"]["author"], "bookId=", d["data"]["bookId"])
    bid = d["data"]["bookId"]

    cat = get(f"/api/novel/{bid}/catalog", token)
    print("  catalog count=", len(cat["data"]), "first=", cat["data"][0]["name"])

    ch = cat["data"][0]
    ct = get(f"/api/novel/{bid}/{ch['id']}", token)
    print("  chapter len=", len(ct["data"]["content"]), "has_text=", len(ct["data"]["content"]) > 100)
