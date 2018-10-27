package com.swim.manage.shiro;


import com.swim.manage.annotion.Log;
import com.swim.manage.commons.utils.CryptUtils;
import com.swim.manage.commons.utils.StringUtils;
import com.swim.manage.keys.GlobalKey;
import com.swim.manage.model.xjd.AdminUsers;
import com.swim.manage.service.AdminUsersService;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * 表达验证
 */
@Service
public class FormAuthenticationFilter extends org.apache.shiro.web.filter.authc.FormAuthenticationFilter {
    @Autowired
    AdminUsersService adminUsersService;

    public static final String DEFAULT_VALIDATECODE_PARAM = "validateCode";
    public static final String DEFAULT_MESSAGE_PARAM = "message";

    private String validatecodeParam = DEFAULT_VALIDATECODE_PARAM;
    private String messageParam = DEFAULT_MESSAGE_PARAM;

    /**
     * 表单数据接收
     * @param request
     * @param response
     * @return
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        String username = getUsername(request);
        String password = getPassword(request);
        String validateCode = getValidateCode(request);
        String remoteAddr = StringUtils.getRemoteAddress((HttpServletRequest) request);
        return new UsernamePasswordToken(
                username,
                CryptUtils.AESEncode(GlobalKey.userPawSessionKey,password),
                remoteAddr,
                validateCode);
    }

    public String getValidatecodeParam() {
        return validatecodeParam;
    }

    public String getMessageParam() {
        return messageParam;
    }

    private String getValidateCode(ServletRequest request){
        String validateCode = WebUtils.getCleanParam(request,getValidatecodeParam());
        return validateCode;
    }

    /**
     * 认证成功
     * @param request
     * @param response
     * @throws Exception
     */
    @Override
    protected void issueSuccessRedirect(ServletRequest request, ServletResponse response) throws Exception {
        Principal principal = UserUtils.getPrincipal();
        AdminUsers adminUsers =new AdminUsers();
        if (principal!=null){

            WebUtils.issueRedirect(request,response,"/index",null,true);

        }else{
            WebUtils.issueRedirect(request,response,"/login",null,true);
        }
    }

    /**
     * 认证失败
     * @param token
     * @param e
     * @param request
     * @param response
     * @return
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        String name = e.getClass().getName(),message = "";
        if (IncorrectCredentialsException.class.getName().equals(name)|| UnknownAccountException.class.getName().equals(name)){
            message = "用户名或者密码错误，请重试!";
        }
        else if (e.getMessage()!=null&&StringUtils.startsWith(e.getMessage(),"msg")){
            message = StringUtils.replace(e.getMessage(),"msg:","");
        }
        else {
            message = "系统出现异常，请稍后登陆";
            e.printStackTrace();
        }
//         登陆失败后的提示信息
        request.setAttribute(getMessageParam(),message);

        // 登陆失败后的异常信息
        request.setAttribute(getFailureKeyAttribute(),name);
        return true;
    }
}
