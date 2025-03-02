package com.yupi.yurpc;

import com.yupi.yurpc.Config.RegistryConfig;
import com.yupi.yurpc.Config.RpcConfig;
import com.yupi.yurpc.constant.RpcConstant;
import com.yupi.yurpc.registry.Registry;
import com.yupi.yurpc.registry.RegistryFactory;
import com.yupi.yurpc.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcApplication {
    public static volatile RpcConfig rpcConfig;

    public static void init(RpcConfig newRpcConfig) {
        rpcConfig = newRpcConfig;
        log.info("已加载RpcConfig: {}", newRpcConfig);

        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        registry.init(registryConfig);
        log.info("registry init, config = {}", registryConfig);

        Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));
    }

    public static void init() {
        RpcConfig newRpcConfig;
        try {
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        } catch (Exception e) {
            newRpcConfig = new RpcConfig();
        }
        init(newRpcConfig);
    }

    public static RpcConfig getRpcConfig() {
        if (rpcConfig == null) {
            synchronized (RpcApplication.class) {
                if (rpcConfig == null)
                    init();
            }
        }
        return rpcConfig;
    }
}
