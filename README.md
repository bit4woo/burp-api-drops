# Burp_Extender

这个项目主要收集了个人写burp插件时用到的主要的方法和代码：

处理http请求和响应：获取request中的header，body，paramaters，对它们进行修改和更新。

主要的修改和更新包括：对参数进行加解密或者解密，当修改了请求包后自动重新计算sign的值并更新sign字段。

src目录下的class文件，以C开头的都是个人添加，其他都是burp提供的。