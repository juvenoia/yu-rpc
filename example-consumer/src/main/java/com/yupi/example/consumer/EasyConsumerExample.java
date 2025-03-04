package com.yupi.example.consumer;

import com.yupi.example.common.model.User;
import com.yupi.example.common.service.UserService;
import com.yupi.yurpc.bootstrap.ConsumerBootstrap;
import com.yupi.yurpc.proxy.ServiceProxyFactory;

public class EasyConsumerExample {
    public static void main(String[] args) {

        ConsumerBootstrap.init();

        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("yupi");

        User newUser = userService.getUser(user);

        if (newUser == null) {
            System.out.println("no such user");
        } else {
            System.out.println("username:" + newUser.getName());
        }

        short number = userService.getNumber();
        System.out.println(number);
    }
}
