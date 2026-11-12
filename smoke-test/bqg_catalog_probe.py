import urllib.request
import ssl

UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0 Safari/537.36"
ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

def fetch(url):
    req = urllib.request.Request(url, headers={"User-Agent": UA, "Referer": "https://www.biquge365.net/"})
    return urllib.request.urlopen(req, timeout=10, context=ctx).read().decode("utf-8", "ignore")

html = fetch("https://www.biquge365.net/newbook/63611/")
open(r"E:\系统\桌面\毕业设计\smoke-test\bqg_catalog.html", "w", encoding="utf-8").write(html)
print("catalog len=", len(html))
import re
# 找章节列表容器
for m in re.finditer(r'<div[^>]*id="([^"]*)"[^>]*>', html):
    print("DIV id=", m.group(1))
for m in re.finditer(r'<dl[^>]*class="([^"]*)"', html):
    print("DL class=", m.group(1))
