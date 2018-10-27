package com.swim.manage.shiro;

import com.swim.manage.enums.UserState;
import com.swim.manage.model.xjd.AdminRoles;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 用户身份
 */
@Data
public class Principal {

    private Integer id;  // 主键Id
    private String loginName; // 登录名
    private String trueName; // 真实姓名
    private int adminLevel;
    private Integer parentId;
    private BigDecimal balance;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后登陆时间
     */
    private Date lastLogin;


    /**
     * 用户状态
     */
    private UserState state;

    private List<AdminRoles> roles;

    public Principal() {
    }

    /**
     *
     * @param id 主键Id
     * @param loginName 登陆名
     */
    public Principal(
            Integer id,
            String loginName,
            String trueName) {
        this.id = id;
        this.loginName = loginName;
        this.trueName = trueName;
    }

    public Principal(Integer id, String loginName, String trueName, Date createTime, Date lastLogin, UserState state, List<AdminRoles> roles) {
        this.id = id;
        this.loginName = loginName;
        this.trueName = trueName;
        this.createTime = createTime;
        this.lastLogin = lastLogin;
        this.state = state;
        this.roles = roles;
    }
}
