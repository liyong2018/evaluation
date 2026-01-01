package com.evaluate.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户实体类（从配置文件读取，不映射数据库表）
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @JsonProperty("username")
    private String username;

    /**
     * 密码
     */
    @JsonProperty("password")
    private String password;

    /**
     * 是否是管理员
     */
    @JsonProperty("isAdmin")
    private Boolean isAdmin;

    /**
     * 默认构造函数
     */
    public User() {
        this.isAdmin = false;
    }

    /**
     * 构造函数
     */
    public User(String username, String password, Boolean isAdmin) {
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin != null ? isAdmin : false;
    }
}
