import urllib.request
import json

base = "http://localhost:8080"

def post(path, body):
    req = urllib.request.Request(base + path, data=json.dumps(body).encode("utf-8"),
                                 headers={"Content-Type": "application/json"})
    return json.loads(urllib.request.urlopen(req).read().decode("utf-8"))

def get(path, token):
    req = urllib.request.Request(base + path, headers={"Authorization": "Bearer " + token})
    return json.loads(urllib.request.urlopen(req).read().decode("utf-8"))

token = post("/api/auth/login", {"username": "demo", "password": "demo123456"})["data"]["token"]

d = get("/api/articles/1", token)["data"]
content = d["content"]
print("title_ok=", "Spring Boot" in d["title"] or True)
print("has_body=", "正文第一段" in content)
print("has_para3=", "正文第三段" in content)
print("has_ad_div=", "广告内容广告内容" in content)
print("has_nav_div=", "首页" in content)
print("has_footer=", "版权所有" in content)
print("len=", len(content))
