package com.redis.application.constant;

import lombok.Getter;

/**
 * @Author: boris
 * @Date: 2020/12/08
 * @Description:: 异常定义
 */
@Getter
public enum GlobalExceptionEnum {

    BAD_REQUEST("400", "Bad Request!"),
    NOT_AUTHORIZATION("401", "用户未登陆"),
    USER_DISABLE("402", "用户被禁用"),
    USER_NOT_EXISTS("403", "用户不存在"),
    NOT_FOUND_REQUEST("404", "Not Found Request Path"),
    METHOD_NOT_ALLOWED("405", "Method Not Allowed"),
    NOT_ACCEPTABLE("406", "Not Acceptable"),
    INTERNAL_SERVER_ERROR("500", "Internal Server Error"),

    LOGIN_FIRST("999", "[服务器]请登录"),
    RUNTIME_EXCEPTION("1000", "[服务器]运行时异常"),
    NULL_POINTER_EXCEPTION("1001", "[服务器]空值异常"),
    CLASS_CAST_EXCEPTION("1002", "[服务器]数据类型转换异常"),
    IO_EXCEPTION("1003", "[服务器]IO异常"),
    NO_SUCH_METHOD_EXCEPTION("1004", "[服务器]未知方法异常"),
    INDEX_OUT_OF_BOUNDS_EXCEPTION("1005", "[服务器]数组越界异常"),
    CONNECT_EXCEPTION("1006", "[服务器]网络异常"),
    ERROR_MEDIA_TYPE("1007", "[服务器]Content-type错误，请使用application/json"),
    EMPTY_REQUEST_BOYD("1008", "[服务器]request请求body不能为空"),
    ERROR_REQUEST_BOYD("1009", "[服务器]request请求body非json对象"),

    ERROR_VERSION("2000", "[服务器]版本号错误"),
    ERROR_FORMAT_PARAMETER("2001", "[服务器]参数格式错误"),

    WRONG_USERNAME_PASSWORD("4011", "用户名或密码输入错误"),
    UN_ALLOW_USER_TYPE("4012", "不被允许的账户类型"),
    FORBIDEN_USER("4015", "账户已被禁用"),
    UUC_FIRST_LOGIN("4016", "首次登录请使用手机号登录"),
    LOGIN_FAIL5("4017", "账户连续登录失败5次"),
    LOGIN_CAPTCHA("4018", "登录验证码输入错误"),
    LOGIN_ACCOUNT_LOCKED("4019", "账号登录失败10次，冻结24小时"),
    UUC_PASSWORD_UNUSED_1("4020", "密码由8-16位，小写字母、大写字母、数字、特殊符号的两种及以上组合"),
    UUC_PASSWORD_UNUSED_2("4021", "不能包含6位及以上的相同字符"),
    UUC_PASSWORD_UNUSED_3("4022", "不能包含6位及以上的连续字符"),
    UUC_LOGIN_FAIL("4023", "登录失败"),

    TONKER_EXPIRED("4031", "登录失效,请重新登录"),
    OPERATION_USER_EXISTS("4032", "当前账户已存在"),
    ADD_OPERATION_USER_FAIL("4033", "添加失败,请联系管理员"),
    ACCOUNT_NOT_VALID("4034", "用户名不合法"),
    LOGOUT_FAIL("4035", "退出登录失败"),
    SEND_VERIFICATION_CODE_ERROR("4036", "退出登录失败"),
    PERMISSION_DENIED("4037", "无访问权限"),
    PERMISSION_DETAIL_FORMAT_ERROR("4038", "权限代码必须以operate:开头"),
    PARENT_MENU_ERROR("4039", "父级菜单不可用"),

    ADD_OR_EDIT_MENU_ERROR("4040", "添加或修改菜单失败"),
    ROLE_NOT_EXIST("4041", "角色信息不存在"),
    MENU_NOT_EXIST("4042", "菜单信息不存在"),
    SYS_USER_NOT_EXIST("4043", "管理员信息不存在"),
    ACCOUNT_NOT_EXIST("4044", "账户不存在"),
    ADD_OR_UPDATE_SYS_USER_ERROR("4045", "添加或修改管理员失败"),

    SYS_USER_DISABLE("4046", "id为{0}的管理员不可用"),
    ROLE_DISABLE("4047", "id为{0}的角色不可用"),
    SYS_USER_PERMISSION_DENIED("4048", "当前无任何访问权限,请联系管理员分配"),
    MENU_ID_IS_NOT_EXIST("4049", "菜单id{0},不存在"),

    SUPPLY_REQUIRE_NOT_EXIST("4051", "供应商必须条件不存在"),
    DELIVER_ADDRESS_NOT_EXIST("4052", "收货地址不存在"),

    BANNER_NOT_EXIST("4101", "轮播图信息不存在"),

    ENTERPRISE_MAIL_UPLOADFILE_ERROR("4201", "上传企业邮箱失败,文件有误"),
    ENTERPRISE_MAIL_NOT_EXIST("4202", "企业邮箱信息不存在"),
    ENTERPRISE_MAIL_INFO_ERROR("4202", "企业邮箱信息不合法"),

    ARTICLE_NOT_EXIST("4301", "资讯信息不存在"),
    ARTICLE_TAGS_NUMBER_EXCEEDED_MAX("4302", "资讯标签最多5个"),
    ARTICLE_TAGS_CONTENT_EXCEEDED_MAX("4303", "资讯标签内容不可超过24个字符"),
    INDUSTRY_INIT_NOT_EXIST("4304", "行业信息不存在"),

    FEEDBACK_NOT_EXIST("4401", "意见反馈信息不存在"),

    MESSAGE_NOT_EXIST("4501", "消息信息不存在"),
    MESSAGE_SEND_TIME_OUT("4502", "消息已过发送时间"),
    MESSAGE_SEND_OVER("4503", "消息不是未发送状态"),

    APPROVE_TYPE_NOT_EXIST("4601", "审核类型不存在"),
    APPROVE_LOG_NOT_EXIST("4602", "审核信息不存在"),
    APPROVE_DIRECT_PURCHASE_FAIL("4603", "商机当前为{0}状态,不可进行{1}"),
    APPROVE_CONTENT_NOT_EXIST("4604", "当前操作,审核原因必须填写"),

    DIRECT_PURCHASE_NOT_EXIST("4701", "直采商机不存在"),


    TENDER_PURCHASE_NOT_EXIST("4801", "招标商机不存在"),

    CUSTOMER_NOT_EXIST("4901", "用户不存在"),
    CUSTOMER_QUALIFICATION_STATE_NOT_SUBMITTED("4902", "用户当前资质认证状态是{0},不能进行{1}"),
    CUSTOMER_QUALIFICATION_AUTH_FAIL("4903", "用户资质认证审核失败"),


    ACQUIRE_LOCK_FAIL("10010", "获取分布式锁失败"),

    REDIS_EXCEPTION("9996", "redis操作出现异常"),
    DATABASE_SYSTEM_BUILD_ERROR("9997", "组装数据库参数异常"),
    DATABASE_SYSTEM_ERROR("9998", "数据库操作异常"),
    SYSTEM_ERROR("9999", "系统异常");




    /**
     * code
     */
    private final String code;

    /**
     * message
     */
    private final String message;

    GlobalExceptionEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
