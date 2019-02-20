package com.power.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sunyu 2016/12/5.
 */
public class GeneratorProperties {

    private static PropertiesUtils props = new PropertiesUtils("generator.properties");

    /**
     * 是否需要添加注释
     *
     * @return
     */
    public static Boolean comment() {
        return Boolean.valueOf(props.getProperty("generator.comment"));
    }

    /**
     * 获取基包名
     *
     * @return
     */
    public static String basePackage() {
        return props.getProperty("generator.basePackage");
    }

    /**
     * 获取表前缀
     *
     * @return
     */
    public static String tablePrefix() {
        return props.getProperty("generator.table.prefix");
    }

    /**
     * 获取应用名称
     *
     * @return
     */
    public static String applicationName() {
        return props.getProperty("generator.applicationName");
    }

    /**
     * 获取需要生成的代码层
     *
     * @return
     */
    public static String layers() {
        return props.getProperty("generator.layers");
    }

    /**
     * 代码数据目录
     *
     * @return
     */
    public static String outDir() {
        return props.getProperty("generator.outDir");
    }

    /**
     * 是否开启mybatis二级缓存
     *
     * @return
     */
    public static String enableCache() {
        return props.getProperty("generator.enableCache");
    }

    /**
     * 获取需要生成代码的表名
     *
     * @return
     */
    public static String getTableName() {
        return props.getProperty("generator.table.name");
    }

    /**
     * 是否需要resultMap
     *
     * @return
     */
    public static Boolean getResultMap() {
        return Boolean.valueOf(props.getProperty("generator.resultMap"));
    }

    /**
     * 获取需要生成的方法
     * @return
     */
    public static Map<String,Boolean> getGenerateMethods(){
        String methodsStr = props.getProperty("generator.methods");
        if(StringUtils.isEmpty(methodsStr)){
            throw new RuntimeException("generator.methods can not be null or ''");
        }
        String[] methods = methodsStr.split(",");
        //约定的方法，
        Map<String,Boolean> map = new HashMap<>();
        map.put("add",false);
        map.put("delete",false);
        map.put("update",false);
        map.put("query",false);
        map.put("page",false);
        map.put("queryToListMap",false);
        for(String str:methods){
            map.put(str,true);
        }
        return map;
    }

    public static void main(String[] args) {
        Map<String,Boolean> map = getGenerateMethods();
        System.out.println(map);
    }
}
