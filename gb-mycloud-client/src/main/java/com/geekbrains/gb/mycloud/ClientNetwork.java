package com.geekbrains.gb.mycloud;

import com.geekbraind.gb.mycloud.CommandMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

public class ClientNetwork {
    private String host;
    private int port;
    private Channel currentChannel;
    private String rootDir;
    private boolean isAuthorised;

    private ClientNetwork(String host, int port) {
        this.host = host;
        this.port = port;
        this.isAuthorised = false;
    }

    private static ClientNetwork ourInstance = new ClientNetwork("localhost", 8189);

    public static ClientNetwork getInstance() {
        return ourInstance;
    }

    public Channel getCurrentChannel() {
        return currentChannel;
    }

    public boolean isAuthorised () {
        return isAuthorised;
    }

    public void setNetworSettings(String host, int port){
        this.host = host;
        this.port = port;
    }

    public void setRootDir (String rootDir) {
        this.rootDir = rootDir;
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
                    socketChannel.pipeline().addLast(new OutClientHandler(), new InClientHandler(rootDir,new CommandMessage()));
                    currentChannel = socketChannel;
                }
            });
            ChannelFuture channelFuture = b.connect().sync();
            countDownLatch.countDown();
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
}