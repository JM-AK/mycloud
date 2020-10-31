package com.geekbrains.gb.mycloud.handler;

import com.geekbraind.gb.mycloud.message.AbstractMsg;
import com.geekbraind.gb.mycloud.message.FileMsg;
import com.geekbraind.gb.mycloud.util.CmdService;
import com.geekbraind.gb.mycloud.util.FileService;
import com.geekbrains.gb.mycloud.util.ClientNetwork;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.logging.Logger;

public class OutClientHandler extends ChannelOutboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(ClientNetwork.class.getSimpleName());

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        if (msg instanceof FileMsg) {
            FileService.getInstance().sendFile((FileMsg) msg, 8 , null, ctx, future -> {
                if (!future.isSuccess()) {
                    logger.info("FAILED FILE SENT - " + msg.toString());
                } else {
                    logger.info("SUCCESS FILE SENT - " + msg.toString());
                }
            });
        } else {
            CmdService.getInstance().sendCommand(((AbstractMsg)msg).toString(), null, ctx, future -> {
                if (!future.isSuccess()) {
                    logger.info("FAILED MSG SENT - " + msg.toString());
                } else {
                    logger.info("SUCCESS MSG SENT - " + msg.toString());
                }
            });
        }
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        super.flush(ctx);
    }
}


