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

token = post("/api/auth/login", {"username": "demo", "password": "demo123456"})["data"]["token"]

complex_rule = {
    "bookSourceName": "complex-test",
    "bookSourceUrl": "http://example.com",
    "ruleSearch": {
        "bookList": ".txt-list.0@tag.li",
        "name": "a.0@title",
        "author": ".s3.0@tag.a.0",
        "coverUrl": "img.0@src",
        "bookUrl": "a.0@href"
    },
    "ruleBookInfo": {
        "name": "//meta[@property='og:novel:book_name']/",
        "author": "class.author@text",
        "intro": "class.intro@text"
    },
    "ruleToc": {
        "chapterList": ".section-list.-1@tag.a",
        "chapterName": "a@text",
        "chapterUrl": "a@href"
    },
    "ruleContent": {
        "content": ".word_read.0@tag.p.!-1@text##\u5e7f\u544a.*"
    },
    "searchUrl": "/search.php,{'method':'POST','body':'s={{key}}','charset':'gbk'}"
}

r = post("/api/book-sources/import", {"ruleJson": json.dumps(complex_rule, ensure_ascii=False)}, token)
print("import count=", len(r["data"]))
src = r["data"][0]
rule = json.loads(src["ruleJson"])
for k in ["searchUrl", "searchMethod", "searchBody", "charset", "searchList",
          "searchName", "searchNameType", "searchAuthor", "searchAuthorType", "searchDetail",
          "detailName", "detailNameType", "detailIntroType", "catalogList", "bookContent"]:
    print(f"  {k} = {rule.get(k)}")
