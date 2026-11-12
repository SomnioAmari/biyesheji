import urllib.request
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

t0 = time.time()
cat = get("/api/novel/4/catalog", token)
print(f"catalog count={len(cat['data'])} elapsed={time.time()-t0:.1f}s first={cat['data'][0]['name']}")

if cat["data"]:
    ch = cat["data"][0]
    t0 = time.time()
    ct = get(f"/api/novel/4/{ch['id']}", token)
    print(f"chapter len={len(ct['data']['content'])} elapsed={time.time()-t0:.1f}s")
    print("content_head:", ct["data"]["content"][:80])
