package httpfile;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        if (!fullHttpRequest.decoderResult().isSuccess()) {
            sendError(channelHandlerContext, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        if (fullHttpRequest.method() != HttpMethod.GET) {
            sendError(channelHandlerContext, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }
        final String uri = fullHttpRequest.uri();
        final String path = sanitizeUri(uri);
        if (path == null){
            sendError(channelHandlerContext, HttpResponseStatus.FORBIDDEN);
            return;
        }
        File file = new File(path);
        if(file.isDirectory())
        {
            if(uri.endsWith("/"))
                sendListing(channelHandlerContext, file);
            else{
                sendRedirect(channelHandlerContext,uri+"/");
            }
            return;
        }
        if (!file.isFile()){
            sendError(channelHandlerContext, HttpResponseStatus.NOT_FOUND);
            return;
        }
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            HttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            HttpHeaderUtil.setContentLength(resp, raf.length());
            if (HttpHeaderUtil.isKeepAlive(fullHttpRequest)) {
                resp.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }
            channelHandlerContext.write(resp);
            ChannelFuture sendFileFuture = null;
            sendFileFuture = channelHandlerContext.write(new ChunkedFile(raf, 0, raf.length(), 1024), channelHandlerContext.newProgressivePromise());
            sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
                @Override
                public void operationComplete(ChannelProgressiveFuture channelProgressiveFuture) throws Exception {
                    System.out.println("Transfer complete.");
                }

                @Override
                public void operationProgressed(ChannelProgressiveFuture channelProgressiveFuture, long l, long l1) throws Exception {
                    if (l1 < 0)
                        System.err.println("Transfer progress: " + l);
                    else
                        System.err.println("Transfer progress: " + l + "/" + l1);
                }

            });
            ChannelFuture lastContentFuture = channelHandlerContext.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            if (!HttpHeaderUtil.isKeepAlive(fullHttpRequest))
                lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }
    private static final Pattern ALLOW_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

    private void sendError(ChannelHandlerContext ctx,HttpResponseStatus msg){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, msg,
                Unpooled.copiedBuffer("Failure: " + msg.toString() + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private String sanitizeUri(String uri){
        try {
            uri = URLDecoder.decode(uri,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                uri = URLDecoder.decode(uri,"ISO-8859-1");
            } catch (UnsupportedEncodingException unsupportedEncodingException) {
                unsupportedEncodingException.printStackTrace();
            }
        }
        uri = uri.replace('/', File.separatorChar);
        if(uri.contains(File.separator+'.')
            || uri.contains('.'+File.separator) || uri.startsWith(".")
            || uri.endsWith(".")){
            return null;
        }
        String filePath =  System.getProperty("user.dir");
        return filePath + uri;
    }

    private static void sendRedirect(ChannelHandlerContext ctx, String newUri){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
//        response.headers().set("LOCATIN", newUri);
        response.headers().set(HttpHeaderNames.LOCATION, newUri);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendListing(ChannelHandlerContext ctx,File dir){
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(CONTENT_TYPE,"text/html;charset=UTF-8");
        StringBuilder buf = new StringBuilder();
        buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head><title>");
        String dirPath = dir.getPath();
        buf.append(dirPath);
        buf.append("目录:");
        buf.append("</title></head><body>\r\n");
        buf.append("<h3>");
        buf.append(dirPath).append(" 目录：");
        buf.append("</h3>\r\n");
        buf.append("<ul>");
        buf.append("<li>上级目录：<a href=\"..\">..</a></li>\r\n");
        for(File file : dir.listFiles()){
            String name = file.getName();
            buf.append("<li><a href="+name+">"+name+"</a> </li>");
        }
        buf.append("</ul></body></html>\r\n");
        ByteBuf byteBuf = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
        response.content().writeBytes(byteBuf);
        byteBuf.release();
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
