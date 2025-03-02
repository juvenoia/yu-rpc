package com.yupi.yurpc.loadbalancer;

import com.yupi.yurpc.spi.SpiLoader;

public class LoadBalancerFactory {
    static {
        SpiLoader.load(LoadBalancer.class);
    }

    private static final LoadBalancer DEFAULT_LOAD_BALANCER = new RoundRobinLoadBalancer();

    public static LoadBalancer getInstanc(String key) {
        return SpiLoader.getInstance(LoadBalancer.class, key);
    }
}
