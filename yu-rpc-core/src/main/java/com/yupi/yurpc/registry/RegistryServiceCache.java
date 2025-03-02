package com.yupi.yurpc.registry;

import com.yupi.yurpc.model.ServiceMetaInfo;

import java.util.List;

public class RegistryServiceCache {
    List<ServiceMetaInfo> serviceCache;
    void writeCache(List<ServiceMetaInfo> newServiceCache) {
        this.serviceCache = newServiceCache;
    }
    List<ServiceMetaInfo> readCache() {
        return serviceCache;
    }
    void clearCache() {
        this.serviceCache = null;
    }
}
