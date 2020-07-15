package com.nowcoder.community.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository("beandemingzi")
@Primary
public class alphaDaoHibenate implements alphaDao {
    @Override
    public String select() {
        return "hibernate";
    }
}
