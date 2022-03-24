## Redis的单线程和高性能

### 单线程的优缺点

在6.x版本之前，Redis是使用单线程进行处理请求。

优点：

- 保证安全性，所有请求串行执行，不会发生并发问题；
- 无线程切换的性能损耗。

缺点：

- 若有一个请求执行的指令较为耗时，则会造成后面的所有请求阻塞；
- 单个线程使得无法最大利用多CPU多核的特点，造成服务器资源的浪费。

> Redis 6.x版本已升级为多线程了，不过笔者这里还没体验过，不敢多说【尴尬.jpg】

### 单线程为何却高性能？

据官方所测，Redis的最大能达到10w的并发，那么Redis是如何做到快速处理这么多的连接呢？

- Redis为内存数据库，所有运算都是内存级别的；
- Redis使用epoll实现IO多路复用。

> 什么是epoll? 参考连接：[https://my.oschina.net/editorial-story/blog/3052308#comments ]( https://my.oschina.net/editorial-story/blog/3052308#comments )

