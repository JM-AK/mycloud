package com.geekbrains.gb.mycloud.util;

import com.geekbraind.gb.mycloud.message.AbstractMsg;
import com.geekbraind.gb.mycloud.message.CommandMsg;
import com.geekbraind.gb.mycloud.message.FileMsg;
import com.geekbraind.gb.mycloud.util.CmdService;
import com.geekbraind.gb.mycloud.util.FileService;
import com.geekbrains.gb.mycloud.controller.MainController;
import com.geekbrains.gb.mycloud.data.ClientSettings;
import com.geekbrains.gb.mycloud.handler.MainClientHandler;
import com.geekbrains.gb.mycloud.handler.OutClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class ClientNetwork {
    private String host;
    private int port;
    private Channel currentChannel;
    private String rootDir;
    private boolean isAuthorised;
    private static ClientNetwork instance = new ClientNetwork();

    private static final Logger logger = Logger.getLogger(ClientNetwork.class.getSimpleName());

    private ClientNetwork() {
        this.rootDir = ClientSettings.getInstance().getLocalPath().toString();
        this.host = ClientSettings.getInstance().getServerIp();
        this.port = ClientSettings.getInstance().getServerPort();
        this.isAuthorised = false;
    }

    public static ClientNetwork getInstance() {
        return instance;
    }

    public Channel getCurrentChannel() {
        return currentChannel;
    }

    public boolean isAuthorised () {
        return isAuthorised;
    }

    public void setIsAuthorised (boolean isAuthorised) {
        this.isAuthorised = isAuthorised;
    }

    public void start(CountDownLatch countDownLatch) throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.remoteAddress(new InetSocketAddress(host, port));
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new OutClientHandler(), new MainClientHandler());
                    currentChannel = socketChannel;
                }
            });
            countDownLatch.countDown();
            ChannelFuture channelFuture = b.connect().sync();

            logger.info("Connected");
            channelFuture.channel().closeFuture().sync();
        } finally {
            try {
                workerGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        currentChannel.close();
    }

    public void sendObject (Object msg) {
        currentChannel.writeAndFlush(msg);
    }
}