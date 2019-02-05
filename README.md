# Bilibili Danmaku backup

用于备份bilibili的弹幕。

## Feature

支持备份网址：https://www.bilibili.com/video/avXXXX下的视频。

支持：

1. 历史弹幕备份
2. 当前弹幕池下载



## Syntax

```shell
java -jar backup.jar -b <mode> -url=<-url> <args> [op1, op2, ...]

<mode>
	-history backup history (-cookie needed)
	-single backup single day (-cookie needed)
	-current backup current danmaku
<url>
	-url video url, just for www.bilibili.com/video/XXXX
<args> depend on <mode>
[options]
	-delay=<millisecond>   delay time between two downloads
	-timeout=<millisecond> download timeout
	-cookie=<FILE_PATH>    provide cookie file if it's needed. The cookie file should be in format of Chrome.
    -datf=<millisecond>    delay after receive a "Request too frequently" message.
```

### -history mode

```shell
java -jar backup.jar -b -history -url=<-url> -st=<-st> -ed=<-ed> -cookie=<COOKIE_PATH> [op1, op2, ...]
<-st> start month, in yyyy-MM format.
<-ed> end month, in yyyy-MM format.
[options]
	-delay=<millisecond>   2000 is recomanded. The default value is 1000.
	-timeout=<millisecond> usually, this can be ignored.
	-cookie=<FILE_PATH>    required.
	-datf=<millisecond>    usually, this can be ignored.
```

此功能会把st到ed中的每一天的弹幕都备份下来。

会在output下创建对应的项目文件夹，名字格式为`avXXXXX_yyyy-MM-dd_yyyy-MM-dd`。

补充说明：

-st 和 -ed 的时间间隔不易超过1年，不然会超出访问限制，然后`Request too frequently`。

-timeout和-datf参数一般来说不需要，使用默认值。

-delay最好1000ms以上，不然会`Request too frequently`。

如果真的`Request too frequently`了，先看看备份到什么地方了（比如最后一个是2014-02-05），

就把-st改成2014-02，重新下载，完毕后复制到原项目文件夹的danmaku子文件夹里。

如果还是`Request too frequently`，洗洗睡吧，访问次数超限了，明天再来吧。。。

**注**：如果是访问次数超限，那么-single也会超限，本质上-single模式和-history模式是一样的。



### -single mode

```shell
java -jar backup.jar -b -single -url=<-url> -date=<yyyy-MM-dd> -out=<OUTPUT_PATH> -cookie=<COOKIE_PATH>
```

备份历史中某一天的弹幕，保存在文件夹\<OUTPUT_PATH>下。

文件名模式为：`avXXXXX_yyyy-MM-dd.xml`。



### -current

```shell
java -jar backup.jar -b -current -url=<-url> -out=<OUTPUT_PATH>
```

保存当前弹幕池。

次模式下载的弹幕就是现在播放时我们看到的弹幕，保存在文件夹\<OUTPUT_PATH>下。

文件名模式：`avXXXX_curyyyy-MM-dd.xml`。



## Cookie

获取cookie，使用 使用了Chrome的内核 的浏览器。

首先，Chrome可以，Sogo浏览器可以，360的可以（还有其他一些国内的主流浏览器）

然后安装`EditThisCookie`插件([URL](http://www.editthiscookie.com/))，导出cookie到剪切板。

复制到一个文件里面，然后-cookie=加上路径就行了。



## Tools

本次发行包含了一些小工具（python的）。用于辅助。

在未来版本也许会被包含于java的主程序中。

### collect.py

用来把多个弹幕xml文件合并为一个，使用弹幕ID去重。

**Syntax:**

```shell
python collect.py <danmakuDir> <outputPath>
	 - danmakuDir should contain raw danmaku xml file.
	 - outputPath should ends with .xml
```

danmakuDir 是包含xml弹幕文件的文件夹路径。注意，如果子文件夹中包含xml弹幕文件，也会被合并。



### responselize.py

这个工具会把一个xml弹幕文件包装为一个httpResponse。

如果没有fiddler，这个没用。

如果有fiddler，可以设置拦截，替换api.bilibili.com的真实弹幕文件。

**Syntax:**

```shell
python responselize.py <danmakuXMLPath> <responsePath>
```



## Require

jre/jdk1.8 or later version.

python 3.7 if you need to run two small tools. 



## Dependences

gson - 但说实话我记不得我哪用了gson...<-_<-



## License

就这破玩意还要啥license。



