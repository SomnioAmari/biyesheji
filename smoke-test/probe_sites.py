import urllib.request
import ssl
import re

UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0 Safari/537.36"

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

sites = [
    "https://www.booktxt.net",
    "https://www.piaotian.com",
    "https://www.biquge.co",
    "https://www.xbiquge.la",
    "https://www.52bqg.com",
    "https://www.bqg123.com",
    "https://www.23qb.com",
    "https://www.shoujix.com",
    "https://www.biqumo.com",
    "https://www.biquge365.net",
]

for s in sites:
    try:
        req = urllib.request.Request(s, headers={"User-Agent": UA})
        resp = urllib.request.urlopen(req, timeout=8, context=ctx)
        html = resp.read(20000).decode("utf-8", "ignore")
        m = re.search(r"<title>(.*?)</title>", html, re.S)
        title = m.group(1).strip() if m else "?"
        print(f"OK   {s}  status={resp.status} len={len(html)} title={title}")
    except Exception as e:
        print(f"FAIL {s}  {type(e).__name__}: {str(e)[:80]}")
