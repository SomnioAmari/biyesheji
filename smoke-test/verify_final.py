import urllib.request
import urllib.parse
import json
import time

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

rc = post("/api/book-sources/reconvert", {}, token)
print("reconvert count=", rc["data"], flush=True)

srcs = get("/api/book-sources", token)["data"]
for s in srcs:
    if s["id"] in (26, 16, 15, 13, 12):
        rule = json.loads(s["ruleJson"])
        print(f"id={s['id']} name={s['name']} searchUrl={rule.get('searchUrl')} m={rule.get('searchMethod')} "
              f"list={rule.get('searchList')} name={rule.get('searchName')} type={rule.get('searchNameType')}", flush=True)

kw = urllib.parse.quote("斗破苍穹")
t0 = time.time()
r = get(f"/api/novel/search?keyword={kw}", token)
print(f"search results={len(r['data'])} elapsed={time.time()-t0:.1f}s", flush=True)
