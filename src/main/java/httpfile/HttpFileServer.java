package httpfile;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpFileServer {
    private static final String DEFAULT_URL = "/NIO/src/main/java/httpfile/";

    public void run(final int port,final String url) throws Exception{
        // 创建主线程池组，处理客户端的连接
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 创建从线程池组，处理客户端的读写
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            // 创建netty引导类，配置和串联系列组件（设置线程模型，设置通道类型，设置客户端处理器handler，设置绑定端口号）
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 解码成HttpRequest(只有url中的参数)
                            socketChannel.pipeline().addLast("http-decoder",new HttpRequestDecoder());
                            // 解码成FullHttpRequest（请求体的参数）
                            socketChannel.pipeline().addLast("http-aggregator",new HttpObjectAggregator(65536));
                            socketChannel.pipeline().addLast("http-encoder",new HttpResponseEncoder());
                            // 支持异步发送大的码流，但不占用过多内存
                            socketChannel.pipeline().addLast("http-chunked",new ChunkedWriteHandler());
                            socketChannel.pipeline().addLast("fileServerHandler",new HttpFileServerHandler());
                        }
                    });
//            192.168.1.102
            ChannelFuture sync = b.bind("127.0.0.1", port).sync();
            System.out.println("HTTP 文件服务器启动，网址是： 127.0.0.1:" + port + url);
            sync.channel().closeFuture().sync();

        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        new HttpFileServer().run(port,DEFAULT_URL);
    }
}
