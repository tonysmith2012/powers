package com.power.builder;


import com.power.constant.ConstVal;
import com.power.constant.GeneratorConstant;
import com.power.database.Column;
import com.power.database.DbProvider;
import com.power.database.TableInfo;
import com.power.factory.DbProviderFactory;
import com.power.utils.BeetlTemplateUtil;
import com.power.utils.DateTimeUtil;
import com.power.utils.GeneratorProperties;
import com.power.utils.StringUtils;
import org.beetl.core.Template;

import java.util.*;

/**
 * @author sunyu on 2016/12/6.
 */
public class ModelBuilder {


    /**
     * 生成model
     *
     * @param tableInfo
     * @return
     */
    public String generateModel(TableInfo tableInfo) {
        String tableName = tableInfo.getName();
        String tableTemp = StringUtils.removePrefix(tableName, GeneratorProperties.tablePrefix());
        //类名
        String entitySimpleName = StringUtils.toCapitalizeCamelCase(tableTemp);
        DbProvider dbProvider = new DbProviderFactory().getInstance();
        Map<String, Column> columnMap = dbProvider.getColumnsInfo(tableName);
        boolean isSwagger = true;
        String fields = generateFields(columnMap,isSwagger);
        String gettersAndSetters = this.generateSetAndGetMethods(columnMap);
        String imports = this.generateImport(columnMap);
        String toString = this.generateToStringMethod(entitySimpleName, columnMap);
        Template template = BeetlTemplateUtil.getByName(ConstVal.TPL_ENTITY);
        // swagger 注解
        if(isSwagger) {
            String description = tableInfo.getRemarks();
            String apiModelProperty = generateApiModelProperty(entitySimpleName, description);
            // swagger 注解
            template.binding(GeneratorConstant.API_MODEL_PROPERTY, apiModelProperty);
        }
        template.binding(GeneratorConstant.AUTHOR, System.getProperty("user.name"));//作者
        template.binding(GeneratorConstant.ENTITY_SIMPLE_NAME, entitySimpleName);//类名
        template.binding(GeneratorConstant.BASE_PACKAGE, GeneratorProperties.basePackage());//基包名
        template.binding(GeneratorConstant.FIELDS, fields);//字段
        template.binding(GeneratorConstant.GETTERS_AND_SETTERS, gettersAndSetters);//get和set方法
        template.binding(GeneratorConstant.CREATE_TIME, DateTimeUtil.getTime());//创建时间
        template.binding(GeneratorConstant.TABLE_COMMENT, tableInfo.getRemarks());//表注释
        template.binding(GeneratorConstant.TO_STRING, toString);

        template.binding("SerialVersionUID", String.valueOf(UUID.randomUUID().getLeastSignificantBits()));
        template.binding("modelImports", imports);
        return template.render();
    }

    /**
     * 生成model的字段
     *
     * @param columnMap
     * @return
     */
    protected String generateFields(Map<String, Column> columnMap) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Column> entry : columnMap.entrySet()) {
            Column column = entry.getValue();

            if (StringUtils.isNotEmpty(column.getRemarks())) {
                builder.append("	/** \n	* ").append(column.getRemarks()).append("\n	*/\n");

            }
            if ("Timestamp".equals(column.getColumnType())) {
                builder.append("	@JsonFormat(pattern = \"yyyy-MM-dd HH:mm:ss\",timezone = \"GMT+8\")\n");
            }
            builder.append("	private ").append(column.getColumnType()).append(" ");
            builder.append(StringUtils.underlineToCamel(column.getColumnName()));
            builder.append(";\n");
        }
        return builder.toString();
    }

    /**
     * generateFields isSwagger
     * @param columnMap columnMap
     * @param isSwagger true
     * @return
     */
    protected String generateFields(Map<String, Column> columnMap, boolean isSwagger) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Column> entry : columnMap.entrySet()) {
            Column column = entry.getValue();

            if (StringUtils.isNotEmpty(column.getRemarks())) {
                builder.append("	/** \n	* ").append(column.getRemarks()).append("\n	*/\n");
                if (isSwagger) {
                    builder.append("	@ApiModelProperty(value = " + "\"" + column.getRemarks() + "\", dataType = \"" + column.getColumnType() + "\", required = " + column.isNullable() + ")\n");
                }
            }
            if ("Timestamp".equals(column.getColumnType())) {
                builder.append("	@JsonFormat(pattern = \"yyyy-MM-dd HH:mm:ss\",timezone = \"GMT+8\")\n");
            }
            builder.append("	private ").append(column.getColumnType()).append(" ");
            builder.append(StringUtils.underlineToCamel(column.getColumnName()));
            builder.append(";\n");
        }
        return builder.toString();
    }

    /**
     * 生成model导包块
     *
     * @param columnMap
     * @return
     */
    protected String generateImport(Map<String, Column> columnMap) {
        StringBuilder builder = new StringBuilder();

        List<String> list = new ArrayList<>();
        boolean flag = true;
        for (Map.Entry<String, Column> entry : columnMap.entrySet()) {
            String type = entry.getValue().getColumnType();
            if ("BigDecimal".equals(type)) {
                list.add("import java.math.BigDecimal;\n");
            }
            if ("Date".equals(type)) {
                list.add("import java.sql.Date;\n");
            }
            if ("Timestamp".equals(type)) {
                list.add("import java.sql.Timestamp;\n");
                flag = true;
            }
            if ("Time".equals(type)) {
                list.add("import java.sql.Time;\n");
            }
        }
        if (flag) {
            list.add("import com.fasterxml.jackson.annotation.JsonFormat;\n\n");
            Collections.reverse(list);
        }
        Set<String> set = new HashSet<>();
        set.addAll(list);
        for (String str : set) {
            builder.append(str);
        }
        return builder.toString();
    }

    /**
     * 生成get和set方法
     *
     * @param columnMap
     * @return
     */
    protected String generateSetAndGetMethods(Map<String, Column> columnMap) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Column> entry : columnMap.entrySet()) {
            Column column = entry.getValue();
            String columnName = column.getColumnName();
            //get
            builder.append("	public ").append(column.getColumnType()).append(" get");
            builder.append(StringUtils.toCapitalizeCamelCase(column.getColumnName())).append("() {\n");
            builder.append("		return ").append(StringUtils.underlineToCamel(columnName)).append(";\n");
            builder.append("	}\n\n");
            //set
            builder.append("	public void set").append(StringUtils.toCapitalizeCamelCase(columnName));
            builder.append("(").append(column.getColumnType()).append(" ").append(StringUtils.underlineToCamel(columnName));
            builder.append(") {\n");
            builder.append("		this.").append(StringUtils.underlineToCamel(columnName));
            builder.append(" = ").append(StringUtils.underlineToCamel(columnName)).append(";\n");
            builder.append("	}\n\n");
        }
        return builder.toString();
    }

    /**
     * 生成model的toString方法
     *
     * @param className 类名
     * @param columnMap 类的字段map表
     * @return
     */
    protected String generateToStringMethod(String className, Map<String, Column> columnMap) {
        StringBuilder builder = new StringBuilder();
        builder.append("\"").append(className).append("{\" + \n");
        int index = 0;
        for (Map.Entry<String, Column> entry : columnMap.entrySet()) {
            Column column = entry.getValue();
            String columnName = column.getColumnName();
            String fieldName = StringUtils.underlineToCamel(columnName);
            String type = entry.getValue().getColumnType();
            if (index < 1) {
                if ("String".equals(type)) {
                    builder.append("                \"").append(fieldName).append(" ='\"");
                    builder.append(" + ").append(fieldName).append(" +'\\''").append("+");
                } else {
                    builder.append("                \"").append(fieldName).append(" =\"");
                    builder.append(" + ").append(fieldName).append(" +\n");
                }
            } else {
                if ("String".equals(type)) {
                    builder.append("                \",").append(fieldName).append(" ='\"");
                    builder.append(" + ").append(fieldName).append(" + '\\''").append(" +\n");
                } else {
                    builder.append("                \",").append(fieldName).append(" =\"");
                    builder.append(" + ").append(fieldName).append(" +\n");
                }
            }
            index++;
        }
        builder.append("                '}';");
        return builder.toString();
    }
    /**
     * 生成 apiModelProperty
     *
     * @param apiModelProperty apiModelProperty
     * @param description      description
     * @return String like @ApiModel(value = "apiModelProperty", description = "description")
     */
    protected String generateApiModelProperty(String apiModelProperty, String description) {
        if (!StringUtils.isNotEmpty(description)) {
            description = "no description, Please check!!!";
        }
        return "@ApiModel(value = \"" + apiModelProperty + "\", description = \"" + description + "\")";
    }
}
