package com.geekbrains.gb.mycloud.handler;

import com.geekbraind.gb.mycloud.util.FileService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.logging.Logger;

public class FilesHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = Logger.getLogger(FilesHandler.class.getSimpleName());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FileService.getInstance().receiveFile((ByteBuf) msg);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.flush();
        ctx.close();
    }
}
