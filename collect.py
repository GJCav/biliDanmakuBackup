"""
Description:
	Aollect danmaku from every danmaku xml file,
	and save them in another xml file.
Syntax: 
	collect.py <danmakuDir> <outputPath>
	 - danmakuDir should contain raw danmaku xml file.
	 - outputPath should ends with .xml
"""

import os
import re
import os.path as path
from sys import argv

patStr = '<d p="(.+?)">.*?</d>'
danmakuDir = argv[1]
xmlList = []
danmakuMap = {}
outputFile = argv[2]
xmlPre = \
"""<?xml version="1.0" encoding="UTF-8"?>
<i>
	<chatserver>chat.bilibili.com</chatserver>
	<chatid>73550718</chatid>
	<mission>0</mission>
	<maxlimit>{}</maxlimit>
	<state>0</state>
	<real_name>0</real_name>
"""
xmlSuf = '</i>'


for root, _, files in os.walk(danmakuDir):
	for file in files:
		xmlList.append(path.join(root, file))
		
def getDanmakuID(param):
	pList = param.split(',')
	return pList[7]

for xmlFile in xmlList:
	cnt = 0
	xml = ''
	with open(xmlFile, 'r', encoding='utf-8') as reader:
		xml = reader.read()
	
	for mat in re.finditer(patStr, xml):
		danmakuID = getDanmakuID(mat.group(1))
		if danmakuID in danmakuMap:
			continue
		danmakuMap[danmakuID] = mat.group(0)
		cnt += 1
	
	print('Add {: >4} danmaku(s) from file "{}"'.format(cnt, xmlFile))

print('Save result.')

outStr = xmlPre.format(min(1000, len(danmakuMap)))

for v in danmakuMap.values():
	outStr += '    ' + v + '\n'

outStr += '\n' + xmlSuf

with open(outputFile, 'w', encoding='utf-8') as writer:
	writer.write(outStr)

print('Done.')





















