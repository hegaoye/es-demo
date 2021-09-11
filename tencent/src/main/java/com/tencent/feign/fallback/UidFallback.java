package com.tencent.feign.fallback;

import com.tencent.feign.UidClient;
import org.springframework.stereotype.Component;

@Component
public class UidFallback implements UidClient {


    @Override
    public Long uid() {
        return null;
    }


}
