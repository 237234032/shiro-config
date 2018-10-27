package com.swim.manage.shiro;



public class UsernamePasswordToken extends org.apache.shiro.authc.UsernamePasswordToken {

    private String validateCode;

    public UsernamePasswordToken(String username, String password, String host,String validateCode) {
        super(username, password, host);
        this.validateCode = validateCode;
    }

    public String getValidateCode() {
        return validateCode;
    }

    public void setValidateCode(String volidateCode) {
        this.validateCode = validateCode;
    }
}
