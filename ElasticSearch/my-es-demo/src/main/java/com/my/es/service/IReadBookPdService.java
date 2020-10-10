package com.my.es.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.my.es.domain.ReadBookPd;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
public interface IReadBookPdService extends IService<ReadBookPd> {

    int importAll();
    void findAll();
}
