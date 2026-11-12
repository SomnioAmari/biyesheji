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

srcs = get("/api/book-sources", token)["data"]
print("TOTAL_SOURCES=", len(srcs))
for s in srcs:
    print("-----")
    print("id=", s["id"], "enabled=", s["enabled"], "name=", s["name"])
    print("url=", s["url"])
    try:
        rule = json.loads(s["ruleJson"])
        for k in ["searchUrl", "searchList", "searchName", "searchAuthor", "searchDetail",
                  "bookUrlPattern", "detailName", "tocUrl", "catalogList", "bookContent"]:
            print(f"  {k} = {rule.get(k)}")
    except Exception as e:
        print("  rule_parse_error=", e)
