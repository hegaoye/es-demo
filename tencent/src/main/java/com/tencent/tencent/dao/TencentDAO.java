package com.tencent.tencent.dao;

import com.tencent.tencent.dao.mapper.TencentMapper;
import com.tencent.tencent.entity.Tencent;
import com.tencent.core.base.BaseDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Tencent DAO
 * 数据服务层
 *
 * @author watson
 */
@Repository
public class TencentDAO extends BaseDAO<TencentMapper, Tencent> {


    /**
     * Tencent mapper
     */
    @Autowired
    private TencentMapper tencentMapper;


}