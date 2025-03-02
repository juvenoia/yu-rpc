package com.yupi.yurpc.server.tcp;

import com.yupi.yurpc.model.RpcRequest;
import com.yupi.yurpc.model.RpcResponse;
import com.yupi.yurpc.protocol.ProtocolMessage;
import com.yupi.yurpc.protocol.ProtocolMessageDecoder;
import com.yupi.yurpc.protocol.ProtocolMessageEncoder;
import com.yupi.yurpc.protocol.ProtocolMessageTypeEnum;
import com.yupi.yurpc.registry.LocalRegistry;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.Method;

public class TcpServerHandler implements Handler<NetSocket> {
    @Override
    public void handle(NetSocket netSocket) {

        TcpBufferHandlerWrapper tcpBufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
            ProtocolMessage<RpcRequest> protocolMessage;
            try {
                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            } catch (IOException e) {
                throw new RuntimeException("协议消息解码错误");
            }
            RpcRequest rpcRequest = protocolMessage.getBody();

            RpcResponse rpcResponse = new RpcResponse();
            try {
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                rpcResponse.setMessage("fail");
                rpcResponse.setException(e);
                e.printStackTrace();
            }
            ProtocolMessage.Header header = protocolMessage.getHeader();
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
            try {
                Buffer encode = ProtocolMessageEncoder.encode(rpcResponseProtocolMessage);
                netSocket.write(encode);
            } catch (IOException e) {
                throw new RuntimeException("消息协议编码错误");
            }
        });
        netSocket.handler(tcpBufferHandlerWrapper);
    }
}
