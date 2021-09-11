/*
 * 腾讯数据
 */
package com.tencent.tencent.service;

import com.tencent.tencent.entity.Tencent;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 腾讯数据
 *
 * @author watson
 */
public interface TencentService extends IService<Tencent> {
    void importTxt();
}


