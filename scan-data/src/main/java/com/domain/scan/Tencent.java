/*
 * 腾讯数据
 */
package com.domain.scan;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * 腾讯数据 的实体类
 *
 * @author watson
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Tencent implements java.io.Serializable {
    /**
     * 数据库字段:qq  属性显示:qq
     */
    private String qq;
    /**
     * 数据库字段:email  属性显示:邮箱
     */
    private String email;
    /**
     * 数据库字段:phone  属性显示:手机号
     */
    private String phone;

}
