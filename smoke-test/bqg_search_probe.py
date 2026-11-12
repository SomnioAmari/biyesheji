import urllib.request
import urllib.parse
import ssl

UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0 Safari/537.36"
ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

base = "https://www.biquge365.net"
body = urllib.parse.urlencode({"s": "斗破苍穹", "type": "articlename"}).encode("utf-8")
req = urllib.request.Request(base + "/s.php", data=body, headers={"User-Agent": UA, "Referer": base + "/"})
html = urllib.request.urlopen(req, timeout=10, context=ctx).read().decode("utf-8", "ignore")
open(r"E:\系统\桌面\毕业设计\smoke-test\bqg_search.html", "w", encoding="utf-8").write(html)
print("len=", len(html))
# 打印搜索结果相关结构（找 book 列表项）
import re
# 找常见的结果列表容器
for pat in ['class="bookbox"', 'class="novelslist', 'class="result', 'class="item', 'class="s2', 'class="content']:
    if pat in html:
        print("FOUND container:", pat)
# 打印前 3000 字符
print(html[:2000])
