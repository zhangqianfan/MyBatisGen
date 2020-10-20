package org.main;

import org.entity.Person;
import org.generator.MapperGenerator;
import org.generator.SqlTableGenerator;
import org.mapper.PersonMapper;
import org.utils.SqlSessionFactoryUtils;
import java.io.IOException;

public final class Demo {

    private Demo() {}

    public static void main(String[] args) {
//        customReverseEngineering();
        testReverseEngineering();
    }

    public static void customReverseEngineering() {
        try {
            Class<?> entityClass = Person.class;
            boolean res1 = MapperGenerator.generateMapperJavaFile(entityClass);
            System.out.println(res1 ? "实体类 " + entityClass.getSimpleName() + " 的 Mapper 接口生成成功！" : "实体类 " + entityClass.getSimpleName() + " 的 Mapper 接口生成失败！");
            boolean res2 = MapperGenerator.generateMapperXmlFile(entityClass);
            System.out.println(res2 ? "实体类 " + entityClass.getSimpleName() + " 的 Mapper XML 生成成功！" : "实体类 " + entityClass.getSimpleName() + " 的 Mapper XML 生成失败！");
            boolean res3 = SqlTableGenerator.createTable(Person.class);
            System.out.println(res3 ? "数据表 " + entityClass.getSimpleName().toLowerCase() + " 创建成功！" : "数据表 " + entityClass.getSimpleName().toLowerCase() + " 创建失败！");
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    public static void testReverseEngineering() {
//        testInsert(6);
        testSelect();
    }

    public static void testInsert(int dataNum) {
        PersonMapper mapper = SqlSessionFactoryUtils.getMapper(PersonMapper.class);
        if (mapper != null && dataNum > 0) {
            int row = 0;
            for (int i = 0; i < dataNum; i++) {
                row += mapper.insertPerson(new Person(i + 1, nBitsRandomLowercase(3), i + 31, nBitsRandomLowercase(2), nBitsRandomLowercase(2)));
            }
            System.out.println(row == dataNum ? "成功插入" + row + "条数据！" : "插入失败！");
        }
    }

    public static void testSelect() {
        PersonMapper mapper = SqlSessionFactoryUtils.getMapper(PersonMapper.class);
        if (mapper != null) {
            Person person = mapper.selectPersonById(3);
            System.out.println(person);
        }
    }

    private static String nBitsRandomLowercase(int length) {
        if (length < 1) return "";
        // ASCII Range: 97 ~ 122
        char[] value = new char[length];
        for (int i = 0; i < length; i++) {
            value[i] = (char) (Math.random() * 26 + 97);
        }
        return new String(value);
    }

}
