package org.generator;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import org.annotation.PrimaryKey;
import org.utils.GeneratorUtils;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.utils.GeneratorUtils.*;
import static javax.xml.transform.OutputKeys.*;

public final class MapperGenerator {

    /** 指定Mapper路径 */
    private final static String MAPPER_PATH = "src/main/java/org/mapper/";
    /** 指定XML缩进字符个数 */
    private final static int XML_INDENT = 4;
    /** 存储实体类对应的Mapper方法声明列表 */
    private final static HashMap<String, List<String>> mapperMethods = new HashMap<>();

    private MapperGenerator() {}

    /** 创建单参数方法声明类 */
    private static class MethodDeclaration {

        /** 方法名 */
        private final String methodName;
        /** 参数名 */
        private final String parameterName;
        /** 参数类型 */
        private final String parameterType;
        /** 返回值类型 */
        private final String resultType;

        public MethodDeclaration(String methodName, String parameterName, String parameterType, String resultType) {
            this.methodName = methodName;
            this.parameterName = parameterName;
            this.parameterType = parameterType;
            this.resultType = resultType;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getParameterName() {
            return parameterName;
        }

        public String getParameterType() {
            return parameterType;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            MethodDeclaration that = (MethodDeclaration) obj;
            return Objects.equals(methodName, that.methodName) &&
                    Objects.equals(parameterName, that.parameterName) &&
                    Objects.equals(parameterType, that.parameterType) &&
                    Objects.equals(resultType, that.resultType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(methodName, parameterName, parameterType, resultType);
        }

        @Override
        public String toString() {
            return resultType + ' ' + methodName + '(' + parameterType + ' ' + parameterName + ");";
        }

    }

    /**
     * 生成Mapper接口java文件
     * @param entityClass 要生成Mapper接口的实体类
     * @return 生成成功返回true，否则返回false
     * @throws IOException 当发生I/O异常时抛出
     */
    public static boolean generateMapperJavaFile(Class<?> entityClass) throws IOException {
        String entityClassName = entityClass.getSimpleName();
        String mapperClassName = entityClassName + "Mapper";
        File directory = new File(MAPPER_PATH);
        boolean exceptionOccurred = false;
        if (!directory.exists()) {
            boolean mkdirRes = directory.mkdir();
            exceptionOccurred = !mkdirRes;
        }
        if (!exceptionOccurred) {
            File mapperJavaFile = new File(MAPPER_PATH + mapperClassName + ".java");
            if (!mapperJavaFile.exists()) {
                boolean createRes = mapperJavaFile.createNewFile();
                exceptionOccurred = !createRes;
            }
            if (!exceptionOccurred) {
                try (FileOutputStream fos = new FileOutputStream(mapperJavaFile)) {
                    int indents = 0;
                    fos.write("package org.mapper;\n\n".getBytes());
                    fos.write(("import org.entity." + entityClassName + ";\n").getBytes());
                    fos.write(("import java.util.List;\n\n").getBytes());
                    StringBuilder content = new StringBuilder("public interface " + mapperClassName + " {\n\n");
                    indents += XML_INDENT;
                    List<String> mapperMethodDeclarations = new ArrayList<>();
                    Field[] fields = entityClass.getDeclaredFields();
                    for (Field field : fields) {
                        if (field.getType() == Integer.class
                         || field.getType() == Short.class
                         || field.getType() == Long.class
                         || field.getType() == Float.class
                         || field.getType() == Double.class
                         || field.getType() == Byte.class
                         || field.getType() == Character.class
                         || field.getType() == Boolean.class
                         || field.getType() == String.class) {
                            if (field.getName().toLowerCase().endsWith("id")) {
                                mapperMethodDeclarations.add("int delete" + entityClassName + "By" + toCamelString(field.getName()) + "(" + field.getType().getSimpleName() + " " + field.getName() + ");");
                                mapperMethodDeclarations.add(entityClassName + " select" + entityClassName + "By" + toCamelString(field.getName()) + "(" + field.getType().getSimpleName() + " " + field.getName() + ");");
                            } else {
                                mapperMethodDeclarations.add("List<" + entityClassName + "> select" + entityClassName + "sBy" + toCamelString(field.getName()) + "(" + field.getType().getSimpleName() + " " + field.getName() + ");");
                            }
                        }
                    }
                    mapperMethodDeclarations.add("int insert" + entityClassName + "(" + entityClassName + " " + entityClassName.toLowerCase() + ");");
                    mapperMethodDeclarations.add("int update" + entityClassName + "(" + entityClassName + " " + entityClassName.toLowerCase() + ");");
                    mapperMethods.put(entityClass.getName(), mapperMethodDeclarations);
                    for (String methodDeclaration : mapperMethodDeclarations) {
                        content.append(generateNbsp(indents));
                        content.append(methodDeclaration);
                        content.append("\n\n");
                    }
                    content.append("}\n");
                    fos.write(content.toString().getBytes());
                }
            }
        }
        return !exceptionOccurred;
    }

    public static boolean generateMapperXmlFile(Class<?> entityClass) {

        DocumentBuilderFactory builderFactory = new DocumentBuilderFactoryImpl();
        try {
            Field[] fields = entityClass.getDeclaredFields();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element mapperElement = document.createElement("mapper");
            String mapperClassSimpleName = entityClass.getSimpleName() + "Mapper";
            String mapperClassName = "org.mapper." + mapperClassSimpleName;
            mapperElement.setAttribute("namespace", mapperClassName);
            // 由于此时还未生成Mapper的Java文件，因此不能通过Class.forName()加载Mapper接口
            // 只能通过mapperMethods获取Mapper接口中的方法名和参数名
            List<String> mapperMethodsList = mapperMethods.get(entityClass.getName());
            int len = mapperMethodsList.size();
            Element[] sqlElement = new Element[len];
            String sqlTableName = entityClass.getSimpleName().toLowerCase();
            // 在<mapper>标签中添加ResultMap
            Element resultMap = document.createElement("resultMap");
            String resultMapId = entityClass.getSimpleName().toLowerCase() + "ResultMap";
            resultMap.setAttribute("id", resultMapId);
            resultMap.setAttribute("type", entityClass.getName());
            int pkNum = 0;
            for (Field f : fields) {
                PrimaryKey pk = f.getAnnotation(PrimaryKey.class);
                Element elemId;
                if (pk != null) {
                    elemId = document.createElement("id");
                    pkNum++;
                } else {
                    elemId = document.createElement("result");
                }
                // 约束每个实体只能有一个主键
                if (pkNum > 1) {
                    throw new SQLException("Table corresponding to entity class must have only one primary key, but found " + pkNum);
                }
                elemId.setAttribute("property", f.getName());
                elemId.setAttribute("column", GeneratorUtils.fieldNameToColName(f.getName()));
                resultMap.appendChild(elemId);
            }
            mapperElement.appendChild(resultMap);
            // 在<mapper>标签中添加<select>, <insert>, <update>, <delete>标签
            for (int i = 0; i < len; i++) {
                String mapperMethod = mapperMethodsList.get(i);
                MethodDeclaration declaration = parseSingleParamMethodDeclaration(mapperMethod);
                String methodName = declaration.getMethodName();
                String parameterName = declaration.getParameterName();
                String parameterType = declaration.getParameterType();
                if (methodName.startsWith("select")) {
                    // <select>标签
                    sqlElement[i] = document.createElement("select");
                    sqlElement[i].setAttribute("id", methodName);
                    sqlElement[i].setAttribute("parameterType", parameterType);
                    sqlElement[i].setAttribute("resultMap", resultMapId);
                    Text textNode = document.createTextNode("SELECT * FROM " + sqlTableName + " WHERE " + GeneratorUtils.fieldNameToColName(parameterName) + " = #{" + parameterName + "}");
                    sqlElement[i].appendChild(document.createTextNode("\n" + GeneratorUtils.generateNbsp(2 * XML_INDENT)));
                    sqlElement[i].appendChild(textNode);
                    sqlElement[i].appendChild(document.createTextNode("\n" + GeneratorUtils.generateNbsp(XML_INDENT)));
                } else if (methodName.startsWith("insert")) {
                    // <insert>标签
                    StringBuilder tempBuilder = new StringBuilder();
                    tempBuilder.append("INSERT INTO ").append(sqlTableName).append(" VALUES (");
                    for (Field f : fields) {
                        tempBuilder.append("#{").append(f.getName()).append("}, ");
                    }
                    tempBuilder.delete(tempBuilder.lastIndexOf(", "), tempBuilder.length());
                    tempBuilder.append(')');
                    sqlElement[i] = document.createElement("insert");
                    sqlElement[i].setAttribute("id", methodName);
                    sqlElement[i].setAttribute("parameterType", parameterType);
                    Text textNode = document.createTextNode(tempBuilder.toString());
                    sqlElement[i].appendChild(document.createTextNode("\n" + GeneratorUtils.generateNbsp(2 * XML_INDENT)));
                    sqlElement[i].appendChild(textNode);
                    sqlElement[i].appendChild(document.createTextNode("\n" + GeneratorUtils.generateNbsp(XML_INDENT)));
                } else if (methodName.startsWith("update")) {
                    // <update>标签
                    StringBuilder tempBuilder = new StringBuilder();
                    tempBuilder.append("UPDATE ").append(entityClass.getSimpleName().toLowerCase()).append(" SET ");
                    String primaryKeyName = "";
                    pkNum = 0;
                    for (Field f : fields) {
                        PrimaryKey pkAnnotation = f.getAnnotation(PrimaryKey.class);
                        if (pkAnnotation == null) {
                            tempBuilder.append(GeneratorUtils.fieldNameToColName(f.getName())).append(" = #{").append(f.getName()).append("}, ");
                        } else {
                            pkNum++;
                            primaryKeyName = GeneratorUtils.fieldNameToColName(f.getName());
                        }
                    }
                    // 约束每个实体类只能有1个主键
                    if (pkNum > 1) {
                        throw new SQLException("Table corresponding to entity class must have only one primary key, but found " + pkNum);
                    }
                    tempBuilder.delete(tempBuilder.lastIndexOf(", "), tempBuilder.length());
                    tempBuilder.append(" WHERE ").append(primaryKeyName).append(" = #{").append(primaryKeyName).append("}");
                    sqlElement[i] = document.createElement("update");
                    sqlElement[i].setAttribute("id", methodName);
                    sqlElement[i].setAttribute("parameterType", parameterType);
                    Text textNode = document.createTextNode(tempBuilder.toString());
                    sqlElement[i].appendChild(document.createTextNode("\n" + GeneratorUtils.generateNbsp(2 * XML_INDENT)));
                    sqlElement[i].appendChild(textNode);
                    sqlElement[i].appendChild(document.createTextNode("\n" + GeneratorUtils.generateNbsp(XML_INDENT)));
                } else if (methodName.startsWith("delete")) {
                    // <delete>标签
                    sqlElement[i] = document.createElement("delete");
                    sqlElement[i].setAttribute("id", methodName);
                    sqlElement[i].setAttribute("parameterType", parameterType);
                    Text textNode = document.createTextNode("DELETE FROM " + sqlTableName + " WHERE " + parameterName + " = #{" + parameterName + "}");
                    sqlElement[i].appendChild(document.createTextNode("\n" + GeneratorUtils.generateNbsp(2 * XML_INDENT)));
                    sqlElement[i].appendChild(textNode);
                    sqlElement[i].appendChild(document.createTextNode("\n" + GeneratorUtils.generateNbsp(XML_INDENT)));
                }
            }

            for (int i = 0; i < len; i++) {
                mapperElement.appendChild(sqlElement[i]);
            }
            document.appendChild(mapperElement);
            DOMImplementation domImpl = document.getImplementation();
            String publicId = "-//mybatis.org//DTD Mapper 3.0//EN";
            String dtSysId = "http://mybatis.org/dtd/mybatis-3-mapper.dtd";
            DocumentType docType = domImpl.createDocumentType("doctype", publicId, dtSysId);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(DOCTYPE_PUBLIC, docType.getPublicId());
            transformer.setOutputProperty(DOCTYPE_SYSTEM, docType.getSystemId());
            transformer.setOutputProperty(METHOD, "xml");
            transformer.setOutputProperty(INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(XML_INDENT));
            File mapperXmlFile = new File(MAPPER_PATH + mapperClassSimpleName + ".xml");
            if (!mapperXmlFile.exists()) {
                boolean x = mapperXmlFile.createNewFile();
                if (!x) return false;
            }
            transformer.transform(new DOMSource(document), new StreamResult(mapperXmlFile));
            return true;
        } catch (Exception e) {
            e.printStackTrace(System.out);
            return false;
        }
    }

    private static MethodDeclaration parseSingleParamMethodDeclaration(String methodDeclaration) {
        // methodDeclaration: like "String myFunction(Integer param);"
        if (methodDeclaration == null || "".equals(methodDeclaration)) return null;
        String[] strArr = methodDeclaration.split(" ");
        if (strArr.length != 3) return null;
        String resultType = strArr[0];
        String[] newStrArr = strArr[1].split("\\(");
        if (newStrArr.length != 2) return null;
        String methodName = newStrArr[0];
        String parameterType = newStrArr[1];
        // index of ')' in strArr[2]
        int idx = strArr[2].lastIndexOf(')');
        String parameterName = strArr[2].substring(0, idx);
        return new MethodDeclaration(methodName, parameterName, parameterType, resultType);
    }

}
