package com.yupi.yurpc.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.yupi.yurpc.model.RpcRequest;
import com.yupi.yurpc.model.RpcResponse;
import com.yupi.yurpc.serializer.JdkSerializer;
import com.yupi.yurpc.serializer.Serializer;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ServiceProxy implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Serializer serializer = new JdkSerializer();

        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();

        byte[] bodyBytes = serializer.serialize(rpcRequest);
        try (HttpResponse httpResponse = HttpRequest.post("http://localhost:8080")
                .body(bodyBytes)
                .execute();) {
            byte[] result = httpResponse.bodyBytes();
            RpcResponse deserialize = serializer.deserialize(result, RpcResponse.class);
            return deserialize.getData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
