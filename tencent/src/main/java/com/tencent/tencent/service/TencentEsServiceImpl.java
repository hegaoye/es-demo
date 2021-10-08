package com.tencent.tencent.service;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tencent.tencent.entity.Tencent;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component
public class TencentEsServiceImpl implements TencentEsService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private UidGenerator uidGenerator;

    /**
     * @param idxName 索引名称
     * @param idxSQL  索引描述
     * @return void
     * @throws
     * @since
     */
    public void createIndex(String idxName, String idxSQL) {
        try {
            if (!this.indexExist(idxName)) {
                log.error(" idxName={} 已经存在,idxSql={}", idxName, idxSQL);
                return;
            }
            CreateIndexRequest request = new CreateIndexRequest(idxName);
            buildSetting(request);
            request.mapping(idxSQL, XContentType.JSON);
//            request.settings() 手工指定Setting
            CreateIndexResponse res = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
            if (!res.isAcknowledged()) {
                throw new RuntimeException("初始化失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * 制定配置项的判断索引是否存在，注意与 isExistsIndex 区别
     *
     * @param idxName index名
     * @return boolean
     * @throws
     * @since
     */
    public boolean indexExist(String idxName){
        boolean flag = false;
        GetIndexRequest request = new GetIndexRequest(idxName);
        //TRUE-返回本地信息检索状态，FALSE-还是从主节点检索状态
        request.local(false);
        //是否适应被人可读的格式返回
        request.humanReadable(true);
        //是否为每个索引返回所有默认设置
        request.includeDefaults(false);
        //控制如何解决不可用的索引以及如何扩展通配符表达式,忽略不可用索引的索引选项，仅将通配符扩展为开放索引，并且不允许从通配符表达式解析任何索引
        request.indicesOptions(IndicesOptions.lenientExpandOpen());
        try {
            flag = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 断某个index是否存在
     *
     * @param idxName index名
     * @return boolean
     * @throws
     * @since
     */
    public boolean isExistsIndex(String idxName) {
        Boolean flag = false;
        try {
            flag = restHighLevelClient.indices().exists(new GetIndexRequest(idxName), RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 设置分片
     *
     * @param request
     * @return void
     * @throws
     * @since
     */
    public void buildSetting(CreateIndexRequest request) {
        request.settings(Settings.builder().put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 2));
    }

    /**
     * @param idxName index
     * @param tencent 对象
     * @return void
     * @throws
     * @since
     */
    public void insertOrUpdateOne(String idxName, Tencent tencent) {
        IndexRequest request = new IndexRequest(idxName);
        log.error("Data : id={},entity={}", tencent.getId(), JSON.toJSONString(tencent));
        request.id(tencent.getId());
        request.source(JSON.toJSONString(tencent), XContentType.JSON);
        try {
            restHighLevelClient.index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param idxName index
     * @param tencent 对象
     * @return void
     * @throws
     * @since
     */
    public void deleteOne(String idxName, Tencent tencent) {
        DeleteRequest request = new DeleteRequest(idxName);
        request.id(tencent.getId());
        try {
            restHighLevelClient.delete(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 批量插入数据
     *
     * @param idxName index
     * @param list    带插入列表
     * @return void
     * @throws
     * @since
     */
    public void insertBatch(String idxName, List<Tencent> list) {
        BulkRequest request = new BulkRequest();
        list.forEach(item -> request.add(new IndexRequest(idxName).id(item.getId())
                .source(JSON.toJSONString(item), XContentType.JSON)));
        try {
            restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 批量插入数据
     *
     * @param idxName index
     * @param list    带插入列表
     * @return void
     * @throws
     * @since
     */
    public void insertBatchTrueObj(String idxName, List<Tencent> list) {
        BulkRequest request = new BulkRequest();
        list.forEach(item -> request.add(new IndexRequest(idxName).id(item.getId())
                .source(item, XContentType.JSON)));
        try {
            restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 批量删除
     *
     * @param idxName index
     * @param idList  待删除列表
     * @return void
     * @throws
     * @since
     */
    public <T> void deleteBatch(String idxName, Collection<T> idList) {
        BulkRequest request = new BulkRequest();
        idList.forEach(item -> request.add(new DeleteRequest(idxName, item.toString())));
        try {
            restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param idxName index
     * @param builder 查询参数
     * @param c       结果类对象
     * @return java.util.List<T>
     * @throws
     * @since
     */
    public <T> List<T> search(String idxName, SearchSourceBuilder builder, Class<T> c) {
        SearchRequest request = new SearchRequest(idxName);
        request.source(builder);
        try {
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            SearchHit[] hits = response.getHits().getHits();
            List<T> res = new ArrayList<>(hits.length);
            for (SearchHit hit : hits) {
                res.add(JSON.parseObject(hit.getSourceAsString(), c));
            }
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除index
     *
     * @param idxName
     * @return void
     * @throws
     * @since
     */
    public void deleteIndex(String idxName) {
        try {
            if (!this.indexExist(idxName)) {
                log.error(" idxName={} 已经存在", idxName);
                return;
            }
            restHighLevelClient.indices().delete(new DeleteIndexRequest(idxName), RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * @param idxName
     * @param builder
     * @return void
     * @throws
     * @since
     */
    public void deleteByQuery(String idxName, QueryBuilder builder) {

        DeleteByQueryRequest request = new DeleteByQueryRequest(idxName);
        request.setQuery(builder);
        //设置批量操作数量,最大为10000
        request.setBatchSize(10000);
        request.setConflicts("proceed");
        try {
            restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 1.解析文件
     * 2.插入數據
     */
    @Override
    public void importTxt() throws Exception {
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
                        Boolean flag = this.isExistsIndex(data[0]);
                        if (flag) {
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
                                this.insertBatch(data[0], list);
                                list.clear();
                            } catch (Exception e) {
                                log.error("{}", e.getLocalizedMessage(), e);
                            }
                        }
                    }
                }

                if (!CollectionUtils.isEmpty(list)) {
                    try {
                        //2.插入數據
                        this.insertBatch(null, list);
                        list.clear();
                    } catch (Exception e) {
                        log.error("{}", e.getLocalizedMessage(), e);
                    }
                }

            }
        }

    }
}
