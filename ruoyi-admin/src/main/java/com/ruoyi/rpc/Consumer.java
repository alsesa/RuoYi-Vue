package com.ruoyi.rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.concurrent.CompletableFuture;

public class Consumer {

    public int add(int a,int b) throws Exception {
        CompletableFuture<Integer> addResultFuture = new CompletableFuture();
        Bootstrap bootStrap = new Bootstrap();
        bootStrap.group(new NioEventLoopGroup(4))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    public void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline()
                                .addLast(new LineBasedFrameDecoder(1024))
                                .addLast(new StringDecoder())
                                .addLast(new StringEncoder())
                                .addLast(new SimpleChannelInboundHandler<String >() {

                                    @Override
                                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
                                         int result = Integer.parseInt(s);
                                         addResultFuture.complete(result);
                                         channelHandlerContext.close();
                                    }
                                });
                    }
                });
        ChannelFuture handlerFuture = bootStrap.connect("localhost", 8889).sync();
        handlerFuture.channel().writeAndFlush("add,"+a+","+b+"\n"); //aka add,$a,$b
        return addResultFuture.get();
    }

}


