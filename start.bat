
REM BETA VERSION 0.1
REM.
REM.
REM Syantax:
REM java -jar backup.jar -b <frequence> -url=<-url> -st=<-st> -ed=<-ed> [op1, op2, ...]
REM
REM <frequence>
REM		-month backup monthly
REM <url>
REM 	-url video url, just for www.bilibili.com/video/XXXX
REM <-st> depend on <frequence>
REM <-ed> depend on <frequence>
REM [options]
REM		-delay=<millisecond>   delay time between two downloads
REM 	-timeout=<millisecond> download timeout
REM 	-cookie=<FILE_PATH>    provide cookie file if it's needed. The cookie file should be in format of Chrome.
REM.
REM ---- monthly backup ------
REM <-st> start month, in yyyy-MM format.
REM <-ed> end month, in yyyy-MM format.



java -jar backup.jar -delay=1000 -b -url="https://www.bilibili.com/video/av107" -month -st=2018-01 -ed=2018-12 -cookie=cookies.json











