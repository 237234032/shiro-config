package com.swim.manage.config;


import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import com.swim.manage.shiro.FormAuthenticationFilter;
import com.swim.manage.shiro.SystemAuthorizingRealm;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ShiroConfig
 */
@Configuration
public class ShiroConfig {

    /**
     * 过滤器
     * @param securityManager
     * @return
     */
    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shirFilter(SecurityManager securityManager) {
        FormAuthenticationFilter formAuthenticationFilter = new FormAuthenticationFilter();
        // 定义拦截器
        Map<String,String> filterChainDefinitionMap = new LinkedHashMap<String,String>();
        Map<String, Filter> filter =new LinkedHashMap<>();
        filter.put("authc",formAuthenticationFilter);
        // 不被拦截路由规则
        filterChainDefinitionMap.put("/static/**", "anon");
        filterChainDefinitionMap.put("/logout", "logout");
        // 验证码
        filterChainDefinitionMap.put("/api/v1/kaptcha/defaultKaptcha","anon");
        // 其他都需要认证才允许访问
        filterChainDefinitionMap.put("/**", "authc");
//        filterChainDefinitionMap.put("adminuser/**", "roles[管理员]");
//        filterChainDefinitionMap.put("/adminuser/*", "roles[管理员]");//用户为ROLE_USER 角色可以访问 . 由用户角色控制用户行为 .
        // 登陆页面
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setLoginUrl("/login");
        // 登录成功跳转页面
        shiroFilterFactoryBean.setSuccessUrl("/index");


        // 未授权处理
        shiroFilterFactoryBean.setUnauthorizedUrl("/403");
		//设置使用扩展后的FormAutjenticaticationFilter否则会出现无法强制转为扩展后的UserToken令牌的的异常
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        shiroFilterFactoryBean.setFilters(filter);
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        System.out.println("Shiro初始化成功");

        return shiroFilterFactoryBean;
    }

    /**
     * 过滤器注册
     * @return
     */
    @Bean
    public FilterRegistrationBean delegatingFilterProxy(){
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        DelegatingFilterProxy proxy = new DelegatingFilterProxy();
        proxy.setTargetFilterLifecycle(true);
        proxy.setTargetBeanName("shiroFilter");
        filterRegistrationBean.setFilter(proxy);
        return filterRegistrationBean;
    }

//    /**
//     *
//     * @return
//     */
//   @Bean
//  public HashedCredentialsMatcher hashedCredentialsMatcher(){
//       HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
//       hashedCredentialsMatcher.setHashAlgorithmName("md5");//散列算法:这里使用MD5算法;
//       hashedCredentialsMatcher.setHashIterations(1);//散列的次数，比如散列两次，相当于 md5(md5(""));
//       return hashedCredentialsMatcher;
//  }


    /**
     * 用户身份
     * @return
     */
    @Bean
    public SystemAuthorizingRealm myShiroRealm(){
        SystemAuthorizingRealm myShiroRealm = new SystemAuthorizingRealm();
        return myShiroRealm;
    }


    /**
     * 用户安全验证
     * @return
     */
    @Bean
    public SecurityManager securityManager(){
        DefaultWebSecurityManager securityManager =  new DefaultWebSecurityManager();
        securityManager.setRealm(myShiroRealm());
        return securityManager;
    }

    /**
     * Shiro自定义方言,结合前端做权限
     * **/
    @Bean(name = "shiroDialect")
    public ShiroDialect shiroDialect() {
        return new ShiroDialect();
    }

    /**
     *  开启shiro aop注解支持.
     *  使用代理方式;所以需要开启代码支持;
     * @param securityManager
     * @return
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager){
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

//    @Bean(name="simpleMappingExceptionResolver")
//    public SimpleMappingExceptionResolver
//    createSimpleMappingExceptionResolver() {
//        SimpleMappingExceptionResolver r = new SimpleMappingExceptionResolver();
//        Properties mappings = new Properties();
//        mappings.setProperty("DatabaseException", "databaseError");//数据库异常处理
//        mappings.setProperty("UnauthorizedException","403");
//        r.setExceptionMappings(mappings);  // None by default
//        r.setDefaultErrorView("error");    // No default
//        r.setExceptionAttribute("ex");     // Default is "exception"
//        //r.setWarnLogCategory("example.MvcLogger");     // No default
//        return r;
//    }

//   /**
//    * 配置无权限页面跳转
//    * **/
//    @Bean
//    public HandlerExceptionResolver solver(){
//        HandlerExceptionResolver handlerExceptionResolver=new MyExceptionResolver();
//        return handlerExceptionResolver;
//    }

}
