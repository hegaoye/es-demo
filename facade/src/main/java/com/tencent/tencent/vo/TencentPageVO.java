/*
 * 腾讯数据
 */
package com.tencent.tencent.vo;

import io.swagger.annotations.ApiModelProperty;
import com.tencent.core.base.BaseVO;
import lombok.Data;

/**
 * 腾讯数据 分页 对象 VO
 *
 * @author watson
 */
@Data
public class TencentPageVO extends BaseVO implements java.io.Serializable {
    /**
     * 数据库字段:id  属性显示:id
     */
    @ApiModelProperty(value = "id")
    private java.lang.String id;
    /**
     * 数据库字段:qq  属性显示:qq
     */
    @ApiModelProperty(value = "qq")
    private java.lang.String qq;
    /**
     * 数据库字段:email  属性显示:邮箱
     */
    @ApiModelProperty(value = "邮箱")
    private java.lang.String email;
    /**
     * 数据库字段:phone  属性显示:手机号
     */
    @ApiModelProperty(value = "手机号")
    private java.lang.String phone;

}
