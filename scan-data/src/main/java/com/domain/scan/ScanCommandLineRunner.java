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
    private String path = "/www/16e/";
    private String url = "http://tencent.cg45.xyz/tencent/build/batch";
    private String checkNameUrl = "http://tencent.cg45.xyz/tencent/cache";
    private String submitNameUrl = "http://tencent.cg45.xyz/tencent/check";

    @Override
    public void run(String... args) throws Exception {
        List<String> listFileNames = FileUtil.listFileNames(path);
        if (CollectionUtils.isEmpty(listFileNames)) {
            return;
        }
        List<String> oldNameList = listName();
        listFileNames = listFileNames.stream().filter(s -> !oldNameList.contains(s)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(listFileNames)) {
            return;
        }

        for (String listFileName : listFileNames) {
            List<String> txtDataList = FileUtil.readLines(path + listFileName, "utf-8");
            List<Tencent> list = new ArrayList<>();
            if (!CollectionUtils.isEmpty(txtDataList)) {
                for (String s : txtDataList) {
                    if (!s.contains("----")) {
                        continue;
                    }

                    String[] data = s.split("----");

                    if (data.length == 2) {
                        if (list.size() <= 1000) {
                            list.add(Tencent.builder()
                                    .qq(data[0])
                                    .email(data[0] + "@qq.com")
                                    .phone(data[1])
                                    .build());
                        } else {
                            try {
                                //2.插入數據
                                this.http(list);
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
                        this.http(list);
                        list.clear();
                    } catch (Exception e) {
                        log.error("{}", e.getLocalizedMessage(), e);
                    }
                }

            }
        }

    }

    private void http(List<Tencent> list) {
        try {
            log.info("提交数据到-{}", url);
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
