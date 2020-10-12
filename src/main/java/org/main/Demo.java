package org.main;

import org.entity.Person;
import org.generator.MapperGenerator;

import java.io.IOException;



public final class Demo {

    private Demo() {}

    public static void main(String[] args) {
        try {
            boolean res1 = MapperGenerator.generateMapperJavaFile(Person.class);
            System.out.println(res1 ? "Mapper Class 生成成功！" : "Mapper Class 生成失败！");
            boolean res2 = MapperGenerator.generateMapperXmlFile(Person.class);
            System.out.println(res2 ? "Mapper XML 生成成功！" : "Mapper XML 生成失败！");
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

}
