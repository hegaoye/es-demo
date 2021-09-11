/*
 * 腾讯数据
 */
package com.tencent.tencent.service;

import cn.hutool.core.io.FileUtil;
import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tencent.tencent.dao.TencentDAO;
import com.tencent.tencent.dao.mapper.TencentMapper;
import com.tencent.tencent.entity.Tencent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;


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
        entity.setId(String.valueOf(uidGenerator.getUID()));
        return super.save(entity);
    }

    /**
     * 1.解析文件
     * 2.插入數據
     */
    @Override
    public void importTxt() {
        //1.解析文件
        String[] paths = {"/Users/watson/IdeaProjects/data-backend/data/1.txt"};
        for (String path : paths) {
            List<Tencent> list = new ArrayList<>();
            List<String> txtDataList = FileUtil.readLines(path, "utf-8");
            if (!CollectionUtils.isEmpty(txtDataList)) {
                for (String s : txtDataList) {
                    if (!s.contains("----")) {
                        continue;
                    }

                    String[] data = s.split("----");

                    if (data.length == 2) {
                        int count = tencentDAO.selectCount(new LambdaQueryWrapper<Tencent>()
                                .eq(Tencent::getQq, data[0]));
                        if (0 < count) {
                            continue;
                        }

                        if (list.size() <= 1000) {
                            list.add(Tencent.builder()
                                    .id(String.valueOf(uidGenerator.getUID()))
                                    .qq(data[0])
                                    .email(data[0] + "@qq.com")
                                    .phone(data[1])
                                    .build());
                        } else {
                            try {
                                //2.插入數據
                                this.saveBatch(list);
                                list.clear();
                            } catch (Exception e) {
                                log.error("{}", e.getLocalizedMessage(), e);
                            }
                        }
                    }
                }

                try {
                    //2.插入數據
                    this.saveBatch(list);
                    list.clear();
                } catch (Exception e) {
                    log.error("{}", e.getLocalizedMessage(), e);
                }

            }
        }

    }
}


