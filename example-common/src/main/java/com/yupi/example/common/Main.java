package com.yupi.example.common;

import com.yupi.yurpc.Config.RpcConfig;
import com.yupi.yurpc.RpcApplication;

public class Main {
    public static void main(String[] args) {
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        System.out.println(rpcConfig.toString());
    }
}
