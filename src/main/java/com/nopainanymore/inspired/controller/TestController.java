package com.nopainanymore.inspired.controller;

import com.nopainanymore.inspired.component.ApplicationUtil;
import com.nopainanymore.inspired.dao.TestDao;
import com.nopainanymore.inspired.dao.TestDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nopainanymore
 * @time 2019-03-27 22:56
 */
@RestController
public class TestController {


    private static final Logger log = LoggerFactory.getLogger(TestController.class);


    @RequestMapping("/")
    public void test() {
        TestDaoImpl testDao = ApplicationUtil.getBeanByName(TestDaoImpl.class);
        log.info("TestController- test- :{}",testDao.toString());
    }
}
