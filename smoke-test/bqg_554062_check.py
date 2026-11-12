import urllib.request
import ssl
import re

UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0 Safari/537.36"
ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

req = urllib.request.Request("https://www.biquge365.net/newbook/554062/",
                             headers={"User-Agent": UA, "Referer": "https://www.biquge365.net/"})
html = urllib.request.urlopen(req, timeout=15, context=ctx).read().decode("utf-8", "ignore")

print("has ul.info:", "ul.info" in html or 'class="info"' in html)
print("chapter links:", len(re.findall(r'/chapter/554062/', html)))
# 找章节列表容器
for m in re.finditer(r'<ul[^>]*class="([^"]*)"[^>]*>', html):
    print("UL class:", m.group(1))
