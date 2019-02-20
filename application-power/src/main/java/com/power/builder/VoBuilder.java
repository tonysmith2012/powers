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

import java.util.Map;
import java.util.UUID;

/**
 * class description<br/>
 *
 * @author fujunsu
 * @version 1.0
 * @date 2018/3/29 13:57
 * @since JDK 1.8+
 */
public class VoBuilder extends ModelBuilder {

    /**
     * 生成model
     *
     * @param tableInfo tableInfo
     * @return String
     */
    @Override
    public String generateModel(TableInfo tableInfo) {
        String tableName = tableInfo.getName();
        String tableTemp = StringUtils.removePrefix(tableName, GeneratorProperties.tablePrefix());
        String entitySimpleName = StringUtils.toCapitalizeCamelCase(tableTemp);//类名
        DbProvider dbProvider = new DbProviderFactory().getInstance();
        Map<String, Column> columnMap = dbProvider.getColumnsInfo(tableName);

        String fields = generateFields(columnMap);
        String gettersAndSetters = generateSetAndGetMethods(columnMap);
        String imports = generateImport(columnMap);
        String toString = generateToStringMethod(entitySimpleName, columnMap);
        Template template = BeetlTemplateUtil.getByName(ConstVal.TPL_VO);
        // swagger 注解
        String description = tableInfo.getRemarks();
        String apiModelProperty = generateApiModelProperty(entitySimpleName, description);

        template.binding(GeneratorConstant.AUTHOR, System.getProperty("user.name"));//作者
        template.binding(GeneratorConstant.API_MODEL_PROPERTY, apiModelProperty);// swagger 注解
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

    @Override
    protected String generateFields(Map<String, Column> columnMap) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Column> entry : columnMap.entrySet()) {
            Column column = entry.getValue();

            if (StringUtils.isNotEmpty(column.getRemarks())) {
                builder.append("	/** \n	* ").append(column.getRemarks()).append("\n	*/\n");
                builder.append("	@ApiModelProperty(value = " + "\"" + column.getRemarks() + "\", dataType = \"" + column.getColumnType() + "\", required = " + column.isNullable() + ")\n");
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

}
