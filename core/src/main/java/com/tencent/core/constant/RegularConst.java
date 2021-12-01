package com.tencent.core.constant;

/**
 * 正则表达则常量处理类
 * @author Amit
 */
public interface RegularConst {
    /**
     * 香港手机
     */
   String HK_PATTERN = "^(5|6|8|9)\\d{7}$";
    /**
     * 中国手机，正则匹配
     */
   String CHINA_PATTERN = "^((13[0-9])|(14[0,1,4-9])|(15[0-3,5-9])|(16[2,5,6,7])|(17[0-8])|(18[0-9])|(19[0-3,5-9]))\\d{8}$";
}
