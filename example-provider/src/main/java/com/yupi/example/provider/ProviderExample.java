package com.yupi.example.provider;

import com.yupi.example.common.service.UserService;
import com.yupi.yurpc.RpcApplication;
import com.yupi.yurpc.model.ServiceMetaInfo;
import com.yupi.yurpc.registry.LocalRegistry;
import com.yupi.yurpc.registry.Registry;
import com.yupi.yurpc.registry.RegistryFactory;
import com.yupi.yurpc.server.VertxHttpServer;

public class ProviderExample {
    public static void main(String[] args) {
        Registry registry = RegistryFactory
                .getInstance(RpcApplication.getRpcConfig().getRegistryConfig().getRegistry());
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(UserService.class.getName());
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(8080);
        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        VertxHttpServer vertxHttpServer = new VertxHttpServer();
        vertxHttpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}
