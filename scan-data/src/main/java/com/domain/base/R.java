package com.domain.base;

import lombok.Data;

import java.io.Serializable;

/**
 * 结果返回类
 */
@Data
public final class R implements Serializable {
    private String info = null;
    private Object data = null;
    private String code = null;
}