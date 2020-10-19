package com.geekbrains.gb.mycloud;

import com.geekbraind.gb.mycloud.CommandMessage;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;

public class CloudServer {

    private int port;
    private String rootDir;

    public CloudServer (int port, String rootDir) {
        this.port = port;
        this.rootDir = rootDir;
    }

    public void run() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    new OutServerHandler(),
                                    new AuthHandler(rootDir, new CommandMessage()),
                                    new ServerHandler(rootDir, new ServerCommandMessage(rootDir)));
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int port = 8189;
        String rootDir = "storage_server";
        if(args.length > 1) {
            port = Integer.parseInt(args[0]);
            rootDir = args[1];
        }
        new CloudServer(port, rootDir).run();
    }


}
