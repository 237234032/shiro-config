package com.swim.manage.shiro;


import com.google.code.kaptcha.Constants;
import com.swim.manage.annotion.Log;
import com.swim.manage.commons.utils.CryptUtils;
import com.swim.manage.commons.utils.StringUtils;
import com.swim.manage.dto.admin.AdminUserDto;
import com.swim.manage.enums.UserState;
import com.swim.manage.keys.GlobalKey;
import com.swim.manage.mapper.xjd.*;
import com.swim.manage.model.xjd.AdminPower;
import com.swim.manage.model.xjd.AdminRolePowerMapping;
import com.swim.manage.model.xjd.AdminUserRoleMapping;
import com.swim.manage.service.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service
public class SystemAuthorizingRealm extends AuthorizingRealm {
    private Logger logger =  LoggerFactory.getLogger(this.getClass());
    /**
     * 用户登陆服务
     */
    @Autowired
    private LoginService userService;
    @Autowired
    private AdminRoleService roleService;
    @Autowired
    AdminUserRoleMappingService userRoleMappingService;
    @Autowired
    AdminRoleMenuService roleMenuService;

    @Autowired
    private AdminPowerService powerService;
    @Autowired
    MenuService menuService;
    @Autowired
    AdminRolePowerService rolePowerService;
    @Autowired
    AdminPowerService adminPowerService;


    /**
     * 授权
     * @param principals
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        System.out.println("权限配置-->MyShiroRealm.doGetAuthorizationInfo()");
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        Object primaryPrincipal = principals.getPrimaryPrincipal();
        AdminUserDto adminUserDto = new AdminUserDto();
        try{
            //将身份令牌属性映射进实体类
            BeanUtils.copyProperties(primaryPrincipal,adminUserDto);
        }catch (Exception e){

        }
        Example UserRoleexample =new Example(AdminUserRoleMapping.class);
//        Example Roleexample =new Example(AdminRoles.class);
        Example RolePowerxample =new Example(AdminRolePowerMapping.class);
        UserRoleexample.createCriteria().andEqualTo("userid",adminUserDto.getId());

        List<AdminUserRoleMapping> adminUserRoleMappings = userRoleMappingService.selectByExampl(UserRoleexample);
        Set<String> list = new HashSet<>();
                for (AdminUserRoleMapping rolse:adminUserRoleMappings) {
                    String s = roleService.selcetAdminrRolesNameById(rolse.getRoleid());
                    RolePowerxample.createCriteria().andEqualTo("roleid", rolse.getRoleid());
                    List<AdminRolePowerMapping> adminRolePowerMappings = rolePowerService.selectByExample(RolePowerxample);
                    for (AdminRolePowerMapping rolepower:adminRolePowerMappings) {
                        System.out.println(rolepower.getRoleid());
                        AdminPower adminPower = adminPowerService.selectByPrimaryKey(rolepower.getPowerid());
                        if (org.apache.commons.lang3.StringUtils.isNotEmpty(adminPower.getPowername())&& org.apache.commons.lang3.StringUtils.isNotBlank(adminPower.getPowername())) {
                            list.add(adminPower.getPowername());
                        }
                        for (String p: list) {
                            authorizationInfo.addStringPermission(p);
                        }
                    }
                }
//        for (AdminUserRoleMapping rolse:adminUserRoleMappings) {
//            String s = roleService.selcetAdminrRolesNameById(rolse.getRoleid());
//            RoleMenuexample.createCriteria().andEqualTo("roleid",rolse.getRoleid());
//            List<AdminRoleMenuMapping> adminRoleMenuMappings = roleMenuService.selectByExample(RoleMenuexample);
//            for (AdminRoleMenuMapping menuRoleMapping: adminRoleMenuMappings) {
//                AdminMenu adminmenu =menuService.selectByPrimaryKey(menuRoleMapping.getMenuid());
//                if (org.apache.commons.lang3.StringUtils.isNotEmpty(adminmenu.getUrl()) && org.apache.commons.lang3.StringUtils.isNotBlank(adminmenu.getUrl())) {
//                    list.add(adminmenu.getUrl());
//                }
//            }
//            for (String p: list) {
//                authorizationInfo.addStringPermission(p);
//            }
//            authorizationInfo.addRole(s);
//        }
//        }
        return authorizationInfo;

    }

    /**
     * 认证
     * @param token
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) token;
        // 客户端用户信息
        String username = usernamePasswordToken.getUsername();
        String pwd = String.valueOf(usernamePasswordToken.getPassword());
        String code = usernamePasswordToken.getValidateCode();
        String checkCode = (String) getSession().getAttribute(Constants.KAPTCHA_SESSION_KEY);

        if(StringUtils.isNullOrEmpty(username)
                ||StringUtils.isNullOrEmpty(pwd)
                || StringUtils.isNullOrEmpty(code)){
            throw new AuthenticationException("msg:无效参数");
        }

        // 验证码校验
        if (code==null
                || checkCode == null
                || !code.equalsIgnoreCase(checkCode)){
            throw new AuthenticationException("msg:验证码错误，请重试");
        }

        // 获取用户信息
        AdminUserDto adminUser = userService.getByLogin(
                        username,
                CryptUtils.AESDncode(GlobalKey.userPawSessionKey,pwd));
        if (adminUser!=null && adminUser.getState() == UserState.NORMAL){

            Principal userDate =  new Principal(
                    adminUser.getId(),
                    adminUser.getLoginName(),
                    adminUser.getTrueName(),
                    adminUser.getCreateTime(),
                    adminUser.getLastLogin(),
                    adminUser.getState(),
                    adminUser.getRoles());
            userDate.setLastLogin(adminUser.getLastLogin());
            userDate.setAdminLevel(adminUser.getAdminLevel());
            userDate.setParentId(adminUser.getParentId());
            userDate.setBalance(adminUser.getBalance());
            SimpleAuthenticationInfo authenticationInfo =  new SimpleAuthenticationInfo(
                    userDate,
                    pwd,
                    adminUser.getLoginName());
            return authenticationInfo;
        }
        return null;
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
}
