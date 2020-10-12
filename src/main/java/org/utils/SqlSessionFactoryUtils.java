package org.utils;

import org.apache.ibatis.session.*;

import java.io.FileReader;
import java.io.IOException;

public final class SqlSessionFactoryUtils {

    private SqlSessionFactoryUtils() {}

    public static <T> T getMapper(Class<T> mapperClass) throws IOException {
        FileReader reader = new FileReader("classpath:mybatis-conf.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        return sqlSession.getMapper(mapperClass);
    }

}
