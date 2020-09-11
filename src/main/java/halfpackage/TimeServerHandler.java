package halfpackage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.Date;

public class TimeServerHandler extends ChannelHandlerAdapter {
    private int counter;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String buf = (String)msg;
//        byte[] req = new byte[((ByteBuf) msg).readableBytes()];
//        buf.readBytes(req);
//        //.substring(0,req.length-System.getProperty("line.separator").length());
//        String body = new String(req,"UTF-8");
        System.out.println("the time server receive order :" + buf + "; the counter is" + counter++);
        String currentTime = "QUERY TIME".equalsIgnoreCase(buf)?new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
        currentTime += "$_";
        ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
        ctx.writeAndFlush(resp);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
