package com.domain.scan;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.domain.base.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 监测业务
 */
@Slf4j
@Component
public class ScanJob {

    private String path = "/home/data/";
    private String url = "http://tencent.cg45.xyz/tencent/build/batch";
    private String checkNameUrl = "http://tencent.cg45.xyz/tencent/check";
    private String submitNameUrl = "http://tencent.cg45.xyz/tencent/cache";

    private Boolean isRunable = false;

    /**
     * 监测调度
     * 每5分鐘一次監測
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void nodeDetectTask() {
        try {

            if (!isRunable) {
                log.info("開始掃描文件");
                isRunable = true;
                this.run();
            } else {
                log.info("监测调度 執行中....");
            }
        } catch (Exception e) {
            log.error("調度監測錯誤-{}", e.getLocalizedMessage(), e);
        }
    }


    public void run() throws FileNotFoundException {
        List<String> listFileNames = FileUtil.listFileNames(path);
        if (CollectionUtils.isEmpty(listFileNames)) {
            return;
        }
        List<String> oldNameList = listName();
        if (!CollectionUtils.isEmpty(oldNameList)) {
            listFileNames = listFileNames.stream().filter(s -> !oldNameList.contains(s)).collect(Collectors.toList());
        }

        if (CollectionUtils.isEmpty(listFileNames)) {
            return;
        }

        for (String listFileName : listFileNames) {
            String fileName = path + listFileName;
            log.info("读取文件-{}", fileName);

            String s = FileUtil.readLine(new RandomAccessFile(new File(fileName), "r"), Charset.defaultCharset());

            if (!s.contains("----")) {
                continue;
            }

            String[] data = s.split("----");
            List<Tencent> list = new ArrayList<>();

            if (data.length == 2) {
                if (list.size() <= 1000) {
                    list.add(Tencent.builder()
                            .qq(data[0])
                            .email(data[0] + "@qq.com")
                            .phone(data[1])
                            .build());
                    continue;
                } else {
                    try {
                        this.http(list, listFileName);
                        list.clear();
                        continue;
                    } catch (Exception e) {
                        log.error("{}", e.getLocalizedMessage(), e);
                    }
                }
            } else {
                if (!CollectionUtils.isEmpty(list)) {
                    try {
                        this.http(list, listFileName);
                        list.clear();
                        continue;
                    } catch (Exception e) {
                        log.error("{}", e.getLocalizedMessage(), e);
                    }
                }
            }
        }

    }

    private void http(List<Tencent> list, String fileName) {
        try {
            log.info("提交数据到-{}-1000条数据-{}", url, list);
            HttpRequest httpRequest = HttpRequest
                    .post(url)
                    .header("nonce", RandomUtil.randomString(7))
                    .body(JSON.toJSONString(list));
            HttpResponse httpResponse = httpRequest.execute();
            log.info("提交数据-{}", httpResponse);
        } catch (Exception e) {
            log.error("{}", e.getLocalizedMessage(), e);
        }

        try {
            submitNameUrl = submitNameUrl + "?name=" + fileName;
            log.info("完成文件名-{}", submitNameUrl);
            HttpRequest httpRequest = HttpRequest
                    .get(submitNameUrl);
            HttpResponse httpResponse = httpRequest.execute();
            log.info("完成文件名-{}", httpResponse);
        } catch (Exception e) {
            log.error("{}", e.getLocalizedMessage(), e);

        }
    }

    private List<String> listName() {
        try {
            HttpRequest httpRequest = HttpRequest.get(checkNameUrl);
            HttpResponse httpResponse = httpRequest.execute();
            log.info("提交数据-{}", httpResponse);
            if (httpResponse.isOk()) {
                String json = httpResponse.body();
                R r = JSON.parseObject(json, R.class);
                return (List<String>) r.getData();
            }
        } catch (Exception e) {
            log.error("{}", e.getLocalizedMessage(), e);
        }
        return new ArrayList<>();
    }
}
