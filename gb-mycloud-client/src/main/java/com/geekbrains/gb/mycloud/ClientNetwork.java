package com.geekbrains.gb.mycloud;



import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

public class ClientNetwork {
    private String host;
    private int port;
    private Channel currentChannel;

    public ClientNetwork(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Channel getCurrentChannel() {
        return currentChannel;
    }

    public void start(CountDownLatch countDownLatch) throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.remoteAddress(new InetSocketAddress(host, port));
//            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast();
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