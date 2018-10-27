package com.swim.manage.shiro;

import com.swim.manage.dto.admin.AdminUserDto;
import com.swim.manage.service.LoginService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserUtils {


    private static LoginService userService;


    @Autowired
    public UserUtils(LoginService initUserService) {
        this.userService = initUserService;
    }

    /**
     * shiro 获取principal
     * @return
     */
    public static Principal getPrincipal(){
        Subject subject = SecurityUtils.getSubject();
        Principal principal = (Principal) subject.getPrincipal();
        if (principal!=null){
            return principal;
        }
        return null;
    }


    /**
     * shiro 退出登陆
     */
    public static  void  loginOut(){
        Subject subject = SecurityUtils.getSubject();
        if(subject!=null)
        {
            subject.logout();
        }
    }

    /**
     * 获取 shiro 中的session
     * @return
     */
    public static Session getSession(){
        Subject subject = SecurityUtils.getSubject();
        // false 不创建新会话
        Session session = subject.getSession(false);
        if (session==null){
            session = subject.getSession();
        }
        return session;
    }


    /**
     * 获取用户
     * @return
     */
    public static AdminUserDto getUser(){
        Principal principal = getPrincipal();
        return userService.getByLoginName(principal.getLoginName());
    }



}
