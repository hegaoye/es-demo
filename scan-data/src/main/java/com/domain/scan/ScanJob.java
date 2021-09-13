package com.domain.scan;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.domain.base.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
    @Value("${array:1}")
    private int array;

    private String[] pathArray1 = {"zhenniu (1).txt", "zhenniu (2).txt", "zhenniu (3).txt"};
    private String[] pathArray2 = {"zhenniu (4).txt", "zhenniu (5).txt", "zhenniu (6).txt"};
    private String[] pathArray3 = {"zhenniu (7).txt", "zhenniu (8).txt", "zhenniu (9).txt"};
    private String[] pathArray4 = {"zhenniu (10).txt", "zhenniu (11).txt", "zhenniu (12).txt"};
    private String[] pathArray5 = {"zhenniu (13).txt", "zhenniu (14).txt", "zhenniu (15).txt"};
    private String[] pathArray6 = {"zhenniu (16).txt", "zhenniu (17).txt", "zhenniu (18).txt"};
    private String[] pathArray7 = {"zhenniu (19).txt", "zhenniu (20).txt", "zhenniu (21).txt"};
    private String[] pathArray8 = {"zhenniu (22).txt", "zhenniu (23).txt", "zhenniu (24).txt"};
    private String[] pathArray9 = {"zhenniu (25).txt", "zhenniu (26).txt", "zhenniu (27).txt"};
    private String[] pathArray10 = {"zhenniu (28).txt", "zhenniu (29).txt", "zhenniu (30).txt"};
    private String[] pathArray11 = {"zhenniu (31).txt", "zhenniu (32).txt", "zhenniu (33).txt"};
    private String[] pathArray12 = {"zhenniu (34).txt", "zhenniu (35).txt", "zhenniu (36).txt"};
    private String[] pathArray13 = {"zhenniu (37).txt", "zhenniu (38).txt", "zhenniu (39).txt"};
    private String[] pathArray14 = {"zhenniu (40).txt"};

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
//        List<String> listFileNames = FileUtil.listFileNames(path);
//        if (CollectionUtils.isEmpty(listFileNames)) {
//            return;
//        }

        List<String> listFileNames = null;

        if (array == 1) {
            listFileNames = Arrays.asList(pathArray1);
        } else if (array == 2) {
            listFileNames = Arrays.asList(pathArray2);

        } else if (array == 3) {
            listFileNames = Arrays.asList(pathArray3);

        } else if (array == 4) {
            listFileNames = Arrays.asList(pathArray4);

        } else if (array == 5) {
            listFileNames = Arrays.asList(pathArray5);

        } else if (array == 6) {
            listFileNames = Arrays.asList(pathArray6);

        } else if (array == 7) {
            listFileNames = Arrays.asList(pathArray7);

        } else if (array == 8) {
            listFileNames = Arrays.asList(pathArray8);

        } else if (array == 9) {
            listFileNames = Arrays.asList(pathArray9);

        } else if (array == 10) {
            listFileNames = Arrays.asList(pathArray10);

        } else if (array == 11) {
            listFileNames = Arrays.asList(pathArray11);

        } else if (array == 12) {
            listFileNames = Arrays.asList(pathArray12);

        } else if (array == 13) {
            listFileNames = Arrays.asList(pathArray13);

        } else if (array == 14) {
            listFileNames = Arrays.asList(pathArray14);

        }
//        List<String> oldNameList = listName();
//        if (!CollectionUtils.isEmpty(oldNameList)) {
//            listFileNames = listFileNames.stream().filter(s -> !oldNameList.contains(s)).collect(Collectors.toList());
//        }

        if (CollectionUtils.isEmpty(listFileNames)) {
            return;
        }

        for (String listFileName : listFileNames) {
            String fileName = path + listFileName;
            log.info("读取文件-{}", fileName);

            try {
                List<Tencent> list = new ArrayList<>();

                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)), "UTF-8"));
                String lineTxt = null;

                while ((lineTxt = br.readLine()) != null) {
                    lineTxt = lineTxt.replace(" ", "");
                    if (!lineTxt.contains("----")) {
                        continue;
                    }

                    String[] data = lineTxt.split("----");

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
                br.close();
                log.info("读取完毕-{}", fileName);
            } catch (Exception e) {
                log.error("{}", e.getLocalizedMessage(), e);
            }


        }

    }

    private void http(List<Tencent> list, String fileName) {
        try {
            log.info("提交数据到-{}-1000条数据", url);
            HttpRequest httpRequest = HttpRequest
                    .post(url)
                    .header("nonce", RandomUtil.randomString(7))
                    .body(JSON.toJSONString(list));
            HttpResponse httpResponse = httpRequest.execute();
            log.info("提交数据-{}", httpResponse);
        } catch (Exception e) {
            log.error("{}", e.getLocalizedMessage(), e);
        }

//        try {
//            String subUrl = submitNameUrl + "?name=" + fileName;
//            log.info("完成文件名-{}", subUrl);
//            HttpRequest httpRequest = HttpRequest.get(subUrl);
//            HttpResponse httpResponse = httpRequest.execute();
//            log.info("完成文件名-{}", httpResponse);
//        } catch (Exception e) {
//            log.error("{}", e.getLocalizedMessage(), e);
//
//        }
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
