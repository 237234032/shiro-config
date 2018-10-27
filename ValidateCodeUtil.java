package com.swim.manage.shiro;

import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

@Service
public class ValidateCodeUtil {

    @CachePut(value = "AdminUsers",key="'login'+#name")
    public int loginFailNum(String loginname,int newnum){
        return newnum+1;
    }

    @CachePut(value = "AdminUsers",key="'login'+#name")
    public int initLogin(String loginname){
        return 0;
    }
    @CachePut(value = "AdminUsers",key="'login'+#name")
    public void cleanLogin(String loginname){

    }

}
