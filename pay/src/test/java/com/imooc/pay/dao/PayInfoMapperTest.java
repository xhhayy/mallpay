package com.imooc.pay.dao;

import com.imooc.pay.PayApplicationTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/21 23:06
 */
public class PayInfoMapperTest extends PayApplicationTests {
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Test
    public void deleteByPrimaryKey() {
    }

    @Test
    public void insert() {
    }

    @Test
    public void insertSelective() {
    }

    @Test
    public void selectByPrimaryKey() {
        System.out.println(payInfoMapper.selectByPrimaryKey(1).toString());
    }

    @Test
    public void updateByPrimaryKeySelective() {
    }

    @Test
    public void updateByPrimaryKey() {
    }
}