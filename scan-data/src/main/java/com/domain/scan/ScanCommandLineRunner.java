package com.domain.scan;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.domain.base.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ScanCommandLineRunner implements CommandLineRunner {
    private String path = "/Users/watson/IdeaProjects/data-backend/data/";
    //    private String path = "/www/16e/";
    private String url = "http://127.0.0.1:8080/tencent/build/batch";
    private String checkNameUrl = "http://127.0.0.1:8080/tencent/check";
    private String submitNameUrl = "http://127.0.0.1:8080/tencent/cache";

    @Override
    public void run(String... args) throws Exception {
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

            List<String> txtDataList = FileUtil.readLines(fileName, "utf-8");

            List<Tencent> list = new ArrayList<>();

            if (!CollectionUtils.isEmpty(txtDataList)) {
                for (String s : txtDataList) {
                    if (!s.contains("----")) {
                        continue;
                    }

                    String[] data = s.split("----");

                    if (data.length == 2) {
                        if (list.size() <= 5000) {
                            list.add(Tencent.builder()
                                    .qq(data[0])
                                    .email(data[0] + "@qq.com")
                                    .phone(data[1])
                                    .build());
                        } else {
                            try {
                                this.http(list, listFileName);
                                list.clear();
                            } catch (Exception e) {
                                log.error("{}", e.getLocalizedMessage(), e);
                            }
                        }
                    }
                }

                if (!CollectionUtils.isEmpty(list)) {
                    try {
                        this.http(list, listFileName);
                        list.clear();
                    } catch (Exception e) {
                        log.error("{}", e.getLocalizedMessage(), e);
                    }
                }

            } else {
                log.warn("文件为空警告-{}", fileName);
            }
        }

    }

    private void http(List<Tencent> list, String fileName) {
        try {
            log.info("提交数据到-{}-5000条数据-{}", url, list);
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
