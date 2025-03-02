package com.yupi.yurpc.registry;

import com.yupi.yurpc.Config.RegistryConfig;
import com.yupi.yurpc.Config.RpcConfig;
import com.yupi.yurpc.model.ServiceMetaInfo;

import java.util.List;

public interface Registry {
    void init(RegistryConfig registryConfig);
    void register(ServiceMetaInfo serviceMetaInfo) throws Exception;
    void unRegister(ServiceMetaInfo serviceMetaInfo);
    List<ServiceMetaInfo> serviceDiscovery(String serviceKey);
    void destry();
}
