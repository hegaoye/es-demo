package com.tencent.core.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * List 工具类
 * @author Amit
 * @date 2018年4月16日13:13:37
 */
public class ListUtils {

    /**
     * 将一个List均分成n个list,主要通过偏移量来实现的
     *
     * @param source 源集合
     * @param limit 最大值
     * @return
     */
    public static <T> List<List<T>> averageAssign(List<T> source, int limit) {
        if (null == source || source.isEmpty()) {
            return Collections.emptyList();
        }
        List<List<T>> result = new ArrayList<>();
        int listCount = (source.size() - 1) / limit + 1;
        // (先计算出余数)
        int remaider = source.size() % listCount;
        // 然后是商
        int number = source.size() / listCount;
        // 偏移量
        int offset = 0;
        for (int i = 0; i < listCount; i++) {
            List<T> value;
            if (remaider > 0) {
                value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                remaider--;
                offset++;
            } else {
                value = source.subList(i * number + offset, (i + 1) * number + offset);
            }
            result.add(value);
        }
        return result;
    }
}
