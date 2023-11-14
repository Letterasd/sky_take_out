package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j

public class UserServiceImpl implements UserService {
    //微信服务接口地址
    public static final String WX_LOGIN="https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    public WeChatProperties weChatProperties;

    @Autowired
    public UserMapper userMapper;
    /**
     * 微信登录
     * @param dto
     * @return
     */
    @Override
    public User wxlogin(UserLoginDTO dto) {
        //调用微信接口服务，获得微信用户的openid（在postman已经测试过了）
        String openid=getOpenid(dto.getCode());
        //判断获取到的openid是否为空或者说有效，如果非法就表示登录失败，抛出异常
        if (openid==null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //判断当前用户是否为点餐系统的新用户
        User user=userMapper.getByOpenid(openid);
        //如果是新用户 那么在我们当前的用户表中是不存在的，需要插入，否则继续运行
        if (user==null){
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        //封装好了信息返回这个用户对象给controller
        return user;
    }
    public String getOpenid(String code){
        Map<String, String> map=new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN, map);
        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");
        return openid;
    }


}
