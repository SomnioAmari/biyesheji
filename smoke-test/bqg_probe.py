import urllib.request
import ssl
import re

UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0 Safari/537.36"
ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

def fetch(url):
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    return urllib.request.urlopen(req, timeout=10, context=ctx).read()

base = "https://www.biquge365.net"
home = fetch(base).decode("utf-8", "ignore")
open(r"E:\系统\桌面\毕业设计\smoke-test\bqg_home.html", "w", encoding="utf-8").write(home)

# 找搜索表单
for m in re.finditer(r'<form[^>]*>.*?</form>', home, re.S):
    print("FORM:", m.group(0)[:300].replace("\n", " "))
for m in re.finditer(r'action="([^"]*search[^"]*)"', home, re.I):
    print("ACTION:", m.group(1))
for m in re.finditer(r'<input[^>]*name="([^"]*)"', home):
    print("INPUT name=", m.group(1))
