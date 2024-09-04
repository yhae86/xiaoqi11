package com.m.sql;

import com.m.bin.Bin;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import java.io.*;

public class MybatisUtil {
    private static SqlSessionFactory factory ;
    static{

        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        String configFile= "mybatis-config.xml";
        InputStream reader = null;
        try {
            reader= Resources.getResourceAsStream(configFile);
        } catch (IOException e) {
            e.printStackTrace();
            try {
                reader = new FileInputStream(Bin.getProjectPath()+java.io.File.separator+configFile);
            } catch (UnsupportedEncodingException | FileNotFoundException unsupportedEncodingException) {
                unsupportedEncodingException.printStackTrace();
            }
        }
        factory = builder.build(reader);

    }
    public static SqlSession createSqlSession(){
        return factory.openSession(false);
    }
    public static void closeSqlSession(SqlSession sqlSession){
        if(null!=sqlSession)sqlSession.close();
    }
}
