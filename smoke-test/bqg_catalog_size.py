import urllib.request
import ssl
import time

UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0 Safari/537.36"
ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

for book in ["554062", "63611"]:
    url = f"https://www.biquge365.net/newbook/{book}/"
    t0 = time.time()
    try:
        req = urllib.request.Request(url, headers={"User-Agent": UA, "Referer": "https://www.biquge365.net/"})
        html = urllib.request.urlopen(req, timeout=20, context=ctx).read()
        print(f"{book}: len={len(html)} elapsed={time.time()-t0:.1f}s")
    except Exception as e:
        print(f"{book}: FAIL {type(e).__name__} {str(e)[:60]} elapsed={time.time()-t0:.1f}s")
