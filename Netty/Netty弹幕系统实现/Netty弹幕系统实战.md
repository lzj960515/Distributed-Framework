## 弹幕系统

### 系统特点

- **实时性高**
- **并发量大**
- **弱一致性**

### 架构设计

![图片](https://images-cdn.shimo.im/PFqX2mNuPWwpqMrN/image.png!thumbnail)

### Netty代码实现

- Server

```java
public class WebsocketDanmuServer {

    private int port;

    public WebsocketDanmuServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new WebsocketDanmuServerInitializer())  //(4)
                    .option(ChannelOption.SO_BACKLOG, 10000)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
            System.out.println("弹幕系统 启动了" + port);

            // 绑定端口，开始接收进来的连接
            ChannelFuture f = b.bind(port).sync(); // (7)
            // 等待服务器  socket 关闭 。
            // 在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。
            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            System.out.println("弹幕系统 关闭了");
        }
    }

    public static void main(String[] args) throws Exception {
        new WebsocketDanmuServer(8080).run();
    }
}
```

- Initializer

```java
public class WebsocketDanmuServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("http-decodec", new HttpRequestDecoder());
        pipeline.addLast("http-encodec", new HttpResponseEncoder());
        pipeline.addLast("http-aggregator", new HttpObjectAggregator(65536));
        pipeline.addLast("http-chunked", new ChunkedWriteHandler());
        //自定义handler
        pipeline.addLast("http-request", new HttpRequestHandler("/ws"));
        pipeline.addLast("WebSocket-protocol", new WebSocketServerProtocolHandler("/ws"));
        //自定义handler
        pipeline.addLast("WebSocket-request", new TextWebSocketFrameHandler());
    }
}
```

- HttpRequestHandler

```java
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> { //1
    private final String wsUri;
    private static final File INDEX;

    static {
        URL location = HttpRequestHandler.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            String path = location.toURI() + "WebsocketDanMu.html";
            path = !path.contains("file:") ? path : path.substring(5);
            INDEX = new File(path);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to locate WebsocketChatClient.html", e);
        }
    }

    public HttpRequestHandler(String wsUri) {
        this.wsUri = wsUri;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (wsUri.equalsIgnoreCase(request.getUri())) {
            ctx.fireChannelRead(request.retain());
        } else {
            String uri = request.uri();
            DefaultFullHttpResponse response =
                    new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                            HttpResponseStatus.OK);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=utf-8");
            String src = "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <title>hello word</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "hello word\n" +
                    "</body>\n" +
                    "</html>";
            response.content().writeBytes(src.getBytes("UTF-8"));
            ChannelFuture f = ctx.writeAndFlush(response);
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

  
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
    	Channel incoming = ctx.channel();
		System.out.println("Client:"+incoming.remoteAddress()+"异常");
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
	}
}
```

- TextWebSocketFrameHandler

```java
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
	
	public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx,
                                TextWebSocketFrame msg) throws Exception { // (1)
		Channel incoming = ctx.channel();
		for (Channel channel : channels) {
            if (channel != incoming){
				channel.writeAndFlush(new TextWebSocketFrame(msg.text()));
			} else {
				channel.writeAndFlush(new TextWebSocketFrame("我发送的"+msg.text() ));
			}
        }
	}
	
	@Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {  // (2)
        Channel incoming = ctx.channel();
        
        // Broadcast a message to multiple Channels
        channels.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " 加入"));
        
        channels.add(incoming);
		System.out.println("Client:"+incoming.remoteAddress() +"加入");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {  // (3)
        Channel incoming = ctx.channel();
        
        // Broadcast a message to multiple Channels
        channels.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " 离开"));
        
		System.err.println("Client:"+incoming.remoteAddress() +"离开");

        // A closed Channel is automatically removed from ChannelGroup,
        // so there is no need to do "channels.remove(ctx.channel());"
    }
	    
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception { // (5)
        Channel incoming = ctx.channel();
		System.out.println("Client:"+incoming.remoteAddress()+"在线");
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception { // (6)
        Channel incoming = ctx.channel();
		System.err.println("Client:"+incoming.remoteAddress()+"掉线");
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)	// (7)
			throws Exception {
    	Channel incoming = ctx.channel();
		System.err.println("Client:"+incoming.remoteAddress()+"异常");
				// 当出现异常就关闭连接
				cause.printStackTrace();
        ctx.close();
	}

}
```

- index.html

```html
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta name="Keywords" content="danmu">
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>弹幕网站</title>
	<style type="text/css">
		body {
			background: url(http://ot0ak6eri.bkt.clouddn.com/01.jpg); no-repeat:top center;
			font-size: 12px;
			font-family: "微软雅黑";
		}


		* {
			margin: 0;
			padding: 0;
		}
		/* screen start*/
		.screen {
			width: 300px;
			height: 100px;
			background: #669900;
		}


		.dm {
			width: 100%;
			height: 100%;
			position: absolute;
			top: 0;
			left: 0;
			display: none;
		}


		.dm .d_screen .d_del {
			width: 38px;
			height: 38px;
			background: #600;
			display: block;
			text-align: center;
			line-height: 38px;
			text-decoration: none;
			font-size: 20px;
			color: #fff;
			border-radius: 19px;
			border: 1px solid #fff;
			z-index: 2;
			position: absolute;
			right: 20px;
			top: 20px;
			outline: none;
		}


		.dm .d_screen .d_del:hover {
			background: #F00;
		}


		.dm .d_screen .d_mask {
			width: 100%;
			height: 100%;
			background: #000;
			position: absolute;
			top: 0;
			left: 0;
			opacity: 0.6;
			filter: alpha(opacity = 60);
			z-index: 1;
		}


		.dm .d_screen .d_show {
			position: relative;
			z-index: 2;
		}


		.dm .d_screen .d_show div {
			font-size: 26px;
			line-height: 36px;
			font-weight: 500;
			position: absolute;
			top: 76px;
			left: 10px;
			color: #fff;
		}
		/*end screen*/


		/*send start*/
		.send {
			width: 100%;
			height: 76px;
			position: absolute;
			bottom: 0;
			left: 0;
			border: 1px solid red;
		}


		.send .s_filter {
			width: 100%;
			height: 76px;
			background: #000;
			position: absolute;
			bottom: 0;
			left: 0;
			opacity: 0.6;
			filter: alpha(opacity = 60);
		}


		.send  .s_con {
			width: 100%;
			height: 76px;
			position: absolute;
			top: 0;
			left: 0;
			z-index: 2;
			text-align: center;
			line-height: 76px;
		}


		.send .s_con .s_text {
			width: 800px;
			height: 36px;
			border: 0;
			border-radius: 6px 0 0 6px;
			outline: none;
		}


		.send .s_con .s_submit {
			width: 100px;
			height: 36px;
			border-radius: 0 6px 6px 0;
			outline: none;
			font-size: 14px;
			color: #fff;
			background: #65c33d;
			font-family: "微软雅黑";
			cursor: pointer;
			border: 1px solid #5bba32;
		}


		.send .s_con .s_submit:hover {
			background: #3eaf0e;
		}
		/*end send*/
	</style>


</head>
<body>
<a href="#" id="startDm">开启弹幕</a>
<!-- dm start -->
<div class="dm">
	<!-- d_screen start -->
	<div class="d_screen">
		<a href="#" class="d_del">X</a>
		<div class="d_mask"></div>
		<div class="d_show">
		</div>
	</div>
	<!-- end d_screen -->


	<!-- send start -->
	<div class="send">
		<div class="s_filter"></div>
		<div class="s_con">
			<input type="text" class="s_text" /> <input type="button"
														value="发表评论" class="s_submit" id="btn"/>
		</div>
	</div>
	<!-- end send -->
</div>
<!-- end dm-->
<script type="text/javascript"
		src="http://ajax.aspnetcdn.com/ajax/jQuery/jquery-1.8.0.js"></script>

<script type="text/javascript" >

    String.prototype.endWith=function(str){
        if(str==null||str==""||this.length==0||str.length>this.length)
            return false;
        if(this.substring(this.length-str.length)==str)
            return true;
        else
            return false;
        return true;
    }

    String.prototype.startWith=function(str){
        if(str==null||str==""||this.length==0||str.length>this.length)
            return false;
        if(this.substr(0,str.length)==str)
            return true;
        else
            return false;
        return true;
    }
</script>
<!--<script type="text/javascript" src="websocket.js"></script>-->
<script type="text/javascript">
    $(function() {

        $("#startDm,.d_del").click(function() {
            $("#startDm,.dm").toggle(1000);
//init_screen();
        });
        $("#btn").click(function(){
            send();
        });
        $(".s_text").keydown(function() {
            var code = window.event.keyCode;





            if (code == 13)//回车键按下时，输出到弹幕


            {
                send();
            }
        });


    });


    function launch()
    {

        var _height = $(window).height();
        var _left = $(window).width() - $("#"+index).width();
        var time=10000;
        if(index%2==0)
            time=20000;
        _top+=80;
        if(_top>_height-100)
            _top=80;
        $("#"+index).css({
            left:_left,
            top:_top,
            color:getRandomColor()

        });
        $("#"+index).animate({
                left:"-"+_left+"px"},
            time,
            function(){});
        index++;
    }



	/* //初始化弹幕
	 function init_screen() {
	 var _top = 0;
	 var _height = $(window).height();
	 $(".d_show").find("div").show().each(function() {
	 var _left = $(window).width() - $(this).width();
	 var time=10000;
	 if($(this).index()%2==0)
	 time=20000;
	 _top+=80;
	 if(_top>_height-100)
	 _top=80;
	 $(this).css({
	 left:_left,
	 top:_top,
	 color:getRandomColor()

	 });
	 $(this).animate({
	 left:"-"+_left+"px"},
	 time,
	 function(){});


	 });
	 } */

    //随机获取颜色值
    function getRandomColor() {
        return '#' + (function(h) {
                return new Array(7 - h.length).join("0") + h
            })((Math.random() * 0x1000000 << 0).toString(16))
    }
</script>

<script type="text/javascript">

    var websocket=null;
    var _top=80;
    var index=0;

    var host=window.location.host;
    //判断当前浏览器是否支持WebSocket
    if('WebSocket' in window){
        websocket=new WebSocket("ws://localhost:8080/ws");
    }
    else{
        alert("Not Support WebSocket!");
    }


    //连接发生错误的回调方法
    websocket.onerror = function(){
        setMessageInnerHTML("error");
    };

    //连接成功建立的回调方法
    websocket.onopen = function(event){
        setMessageInnerHTML("open");
    }

    //接收到消息的回调方法
	// 收到服务器发送的消息
    websocket.onmessage = function(){
        setMessageInnerHTML(event.data);
    }

    //连接关闭的回调方法
    websocket.onclose = function(){
        setMessageInnerHTML("close");
    }


    //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
    window.onbeforeunload = function(){
        websocket.close();
    }


    //将消息显示在网页上
    function setMessageInnerHTML(innerHTML){
        //修改背景图
        var imgurl;
        if (innerHTML.startWith("~background,")) {
            var cmd = innerHTML;
            imgurl = cmd.split(",")[1];
            document.body.style.background = "url("+imgurl+")";
        }else{
            $(".d_show").append("<div id='"+index+"'>"+ innerHTML + "</div>");
		}

        launch();
    }


    //发送消息
    function send(){
        //var message = document.getElementById('text').value;
        var message = $(".s_text").val();
        $(".s_text").val("");
        websocket.send(message);
    }
</script>

</body>
</html>
```

