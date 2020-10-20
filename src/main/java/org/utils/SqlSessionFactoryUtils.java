package org.utils;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.*;

import java.io.IOException;
import java.io.InputStream;

public final class SqlSessionFactoryUtils {

    private static final String CONFIG_LOCATION = "mybatis-conf.xml";

    private SqlSessionFactoryUtils() {}

    public static <T> T getMapper(Class<T> mapperClass) {
        try (InputStream inputStream = Resources.getResourceAsStream(CONFIG_LOCATION)) {
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            SqlSession sqlSession = sqlSessionFactory.openSession(true);
            return sqlSession.getMapper(mapperClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
