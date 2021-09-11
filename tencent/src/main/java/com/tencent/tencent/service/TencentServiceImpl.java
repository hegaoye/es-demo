/*
 * 腾讯数据
 */
package com.tencent.tencent.service;

import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tencent.tencent.dao.TencentDAO;
import com.tencent.tencent.dao.mapper.TencentMapper;
import com.tencent.tencent.entity.Tencent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Date;


/**
 * 腾讯数据
 *
 * @author watson
 */
@Slf4j
@Service
public class TencentServiceImpl extends ServiceImpl<TencentMapper, Tencent> implements TencentService {

    @Autowired
    private TencentDAO tencentDAO;

    @Autowired
    private UidGenerator uidGenerator;

    @Transactional
    @Override
    public boolean save(Tencent entity) {
//        entity.setId(String.valueOf(uidGenerator.getUID()));
        return super.save(entity);
    }

}


