"""
Description:
	Make an danmaku xml file to a http response from 
	api.bilibili.com. And the response file is suitable
	for Fiddler to intercept the real response from 
	bilibili and replace it using the danmaku xml file 
	specified.
Syntax:
	responselize.py <danmakuXMLPath> <responsePath>
"""

import zlib
from sys import argv
from io import BytesIO

pattern = \
"""\
HTTP/1.1 200 OK
Content-Type: text/xml
Content-Encoding: deflate
Content-Length: {}
Access-Control-Allow-Origin: https://www.bilibili.com

"""

infile = argv[1]
outfile = argv[2]

buf = BytesIO()

with open(infile, 'rb') as rdr:
	cps = zlib.compressobj(6)
	inData = rdr.read(1024)
	while inData:
		buf.write(cps.compress(inData))
		inData = rdr.read(1024)
	buf.write(cps.flush())

data = buf.getvalue()
outStr = pattern.format(len(data))

with open(outfile, 'wb') as w:
	w.write(outStr.encode('utf-8'))
	w.write(data)

print("Done")













