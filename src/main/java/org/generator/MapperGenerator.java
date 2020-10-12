package org.generator;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import org.annotation.PrimaryKey;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.utils.GeneratorUtils.*;
import static javax.xml.transform.OutputKeys.*;

public final class MapperGenerator {

    private final static String MAPPER_PATH = "src/main/java/org/mapper/";
    private final static int XML_INDENT = 4;
    private final static HashMap<String, List<String>> mapperMethods = new HashMap<>();

    private MapperGenerator() {}

    private static class MethodDeclaration {

        private final String methodName;
        private final String parameterName;
        private final String parameterType;
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

        public String getResultType() {
            return resultType;
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
                            if (field.getName().endsWith("id") || field.getName().endsWith("Id") || field.getName().endsWith("ID")) {
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
            for (int i = 0; i < len; i++) {
                String mapperMethod = mapperMethodsList.get(i);
                MethodDeclaration declaration = parseSingleParamMethodDeclaration(mapperMethod);
                String methodName = declaration.getMethodName();
                String parameterName = declaration.getParameterName();
                String parameterType = declaration.getParameterType();
                String resultType = declaration.getResultType();
                if (methodName.startsWith("select")) {
                    sqlElement[i] = document.createElement("select");
                    sqlElement[i].setAttribute("id", methodName);
                    sqlElement[i].setAttribute("parameterType", parameterType);
                    if (resultType.contains("List")) {
                        int i1 = resultType.lastIndexOf('<');
                        int i2 = resultType.lastIndexOf('>');
                        resultType = resultType.substring(i1 + 1, i2);
                    }
                    sqlElement[i].setAttribute("resultType", resultType);
                    Text textNode = document.createTextNode("SELECT * FROM " + sqlTableName + " WHERE " + parameterName + " = #{" + parameterName + "}");
                    sqlElement[i].appendChild(textNode);
                } else if (methodName.startsWith("insert")) {
                    Field[] fields = entityClass.getDeclaredFields();
                    StringBuilder tempBuilder = new StringBuilder();
                    tempBuilder.append("INSERT INTO ").append(sqlTableName).append(" VALUES (");
                    for (Field f : fields) {
                        tempBuilder.append("#{").append(f.getName().toLowerCase()).append("}, ");
                    }
                    tempBuilder.delete(tempBuilder.lastIndexOf(", "), tempBuilder.length());
                    tempBuilder.append(')');
                    sqlElement[i] = document.createElement("insert");
                    sqlElement[i].setAttribute("id", methodName);
                    sqlElement[i].setAttribute("parameterType", parameterType);
                    Text textNode = document.createTextNode(tempBuilder.toString());
                    sqlElement[i].appendChild(textNode);
                } else if (methodName.startsWith("update")) {
                    Field[] fields = entityClass.getDeclaredFields();
                    StringBuilder tempBuilder = new StringBuilder();
                    tempBuilder.append("UPDATE ").append(entityClass.getSimpleName().toLowerCase()).append(" SET ");
                    String primaryKeyName = "";
                    int cnt = 0;
                    for (Field f : fields) {
                        PrimaryKey pkAnnotation = f.getAnnotation(PrimaryKey.class);
                        if (pkAnnotation == null) {
                            tempBuilder.append(f.getName().toLowerCase()).append(" = #{").append(f.getName().toLowerCase()).append("}, ");
                        } else {
                            cnt++;
                            primaryKeyName = f.getName();
                        }
                    }
                    // 约束每个实体类只能有1个主键
                    if (cnt > 1) return false;
                    tempBuilder.delete(tempBuilder.lastIndexOf(", "), tempBuilder.length());
                    tempBuilder.append(" WHERE ").append(primaryKeyName).append("= #{").append(primaryKeyName).append("}");
                    sqlElement[i] = document.createElement("update");
                    sqlElement[i].setAttribute("id", methodName);
                    sqlElement[i].setAttribute("parameterType", parameterType);
                    Text textNode = document.createTextNode(tempBuilder.toString());
                    sqlElement[i].appendChild(textNode);
                } else if (methodName.startsWith("delete")) {
                    sqlElement[i] = document.createElement("delete");
                    sqlElement[i].setAttribute("id", methodName);
                    sqlElement[i].setAttribute("parameterType", parameterType);
                    Text textNode = document.createTextNode("DELETE FROM " + sqlTableName + " WHERE " + parameterName + " = #{" + parameterName + "}");
                    sqlElement[i].appendChild(textNode);
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
