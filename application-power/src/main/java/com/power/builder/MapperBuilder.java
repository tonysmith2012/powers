package com.power.builder;

import com.power.constant.ConstVal;
import com.power.constant.GeneratorConstant;
import com.power.database.Column;
import com.power.database.DbProvider;
import com.power.factory.DbProviderFactory;
import com.power.utils.BeetlTemplateUtil;
import com.power.utils.GeneratorProperties;
import com.power.utils.StringUtils;
import org.beetl.core.Template;

import java.util.Map;

/**
 * 创建mybatis mapper文件
 *
 * @author sunyu on 2016/12/7.
 */
public class MapperBuilder {

    public String generateMapper(String tableName) {
        String tableTemp = StringUtils.removePrefix(tableName, GeneratorProperties.tablePrefix());
        String entitySimpleName = StringUtils.toCapitalizeCamelCase(tableTemp);//类名
        String firstLowName = StringUtils.firstToLowerCase(entitySimpleName);
        DbProvider dbProvider = new DbProviderFactory().getInstance();
        Map<String, Column> columnMap = dbProvider.getColumnsInfo(tableName);
        String insertSql = generateInsertSql(columnMap, tableName);
        String batchInsertSql = generateBatchInsertSql(columnMap, tableName);
        String updateSql = generateConditionUpdateSql(columnMap, tableName);
        String selectSql = generateSelectSql(columnMap, tableName);
        String results = generateResultMap(columnMap);
        Template mapper = BeetlTemplateUtil.getByName(ConstVal.TPL_MAPPER);
        mapper.binding(GeneratorConstant.FIRST_LOWER_NAME, firstLowName);
        mapper.binding(GeneratorConstant.ENTITY_SIMPLE_NAME, entitySimpleName);//类名
        mapper.binding(GeneratorConstant.BASE_PACKAGE, GeneratorProperties.basePackage());//基包名
        mapper.binding(GeneratorConstant.INSERT_SQL, insertSql);
        mapper.binding(GeneratorConstant.BATCH_INSERT_SQL, batchInsertSql);
        mapper.binding(GeneratorConstant.UPDATE_SQL, updateSql);
        mapper.binding(GeneratorConstant.SELECT_SQL, selectSql);
        mapper.binding(GeneratorConstant.RESULT_MAP, results);
        mapper.binding(GeneratorConstant.IS_RESULT_MAP, GeneratorProperties.getResultMap());
        mapper.binding(GeneratorConstant.TABLE_NAME, tableName);
        mapper.binding(GeneratorProperties.getGenerateMethods());//过滤方法
        return mapper.render();
    }

    /**
     * 生成insert语句，过滤掉则增列
     *
     * @param columnMap
     * @param tableName
     * @return
     */
    private String generateInsertSql(Map<String, Column> columnMap, String tableName) {
        StringBuilder insertSql = new StringBuilder();
        insertSql.append("insert into ").append(tableName).append("(\n");

        StringBuilder insertValues = new StringBuilder();
        int i = 0;
        int size = columnMap.size();
        Column column;
        for (Map.Entry<String, Column> entry : columnMap.entrySet()) {
            column = entry.getValue();
            if (!column.isAutoIncrement()) {
                if (i < size - 1) {
                    insertSql.append("			").append(entry.getKey()).append(",\n");
                    insertValues.append("			#{").append(StringUtils.underlineToCamel(entry.getKey())).append("},\n");
                } else {
                    insertSql.append("			").append(entry.getKey()).append("\n");
                    insertValues.append("			#{").append(StringUtils.underlineToCamel(entry.getKey())).append("}\n");
                }
            }
            i++;
        }
        insertSql.append("		) values (\n");
        insertSql.append(insertValues);
        insertSql.append("		)");
        return insertSql.toString();
    }

    /**
     * 生成批量插入的sql
     *
     * @param columnMap
     * @param tableName
     * @return
     */
    private String generateBatchInsertSql(Map<String, Column> columnMap, String tableName) {
        StringBuilder batchInsertSql = new StringBuilder();
        batchInsertSql.append("insert into ").append(tableName).append("(\n");
        StringBuilder insertValues = new StringBuilder();
        int counter = 0;
        int size = columnMap.size();
        Column column;
        String key;
        for (Map.Entry<String, Column> entry : columnMap.entrySet()) {
            column = entry.getValue();
            key = entry.getKey();
            if (!column.isAutoIncrement()) {
                if (counter < size - 1) {
                    batchInsertSql.append("			").append(key).append(",\n");
                    insertValues.append("			#{item.").append(StringUtils.underlineToCamel(key)).append("},\n");
                } else {
                    batchInsertSql.append("			").append(key).append("\n");
                    insertValues.append("			#{item.").append(StringUtils.underlineToCamel(key)).append("}\n");
                }
            }
            counter++;
        }
        batchInsertSql.append("		) values\n");
        batchInsertSql.append("        <foreach collection=\"list\" item=\"item\" index=\"index\" separator=\",\">\n");
        batchInsertSql.append("            (\n").append(insertValues);
        batchInsertSql.append("            )\n");
        batchInsertSql.append("        </foreach>");

        return batchInsertSql.toString();
    }

    /**
     * 生成update语句,过滤掉自增列
     *
     * @param columnMap
     * @param tableName
     * @return
     */
    private String generateUpdateSql(Map<String, Column> columnMap, String tableName) {
        StringBuilder updateSql = new StringBuilder();
        updateSql.append("update ").append(tableName).append(" set\n");
        int i = 0;
        int size = columnMap.size();
        Column column;
        for (Map.Entry<String, Column> entry : columnMap.entrySet()) {
            column = entry.getValue();
            if (!column.isAutoIncrement()) {
                if (i < size - 1) {
                    updateSql.append("			").append(entry.getKey()).append(" = #{");
                    updateSql.append(StringUtils.underlineToCamel(entry.getKey())).append("},\n");
                } else {
                    updateSql.append("			").append(entry.getKey()).append(" = #{");
                    updateSql.append(StringUtils.underlineToCamel(entry.getKey())).append("}");
                }
            }
            i++;
        }
        return updateSql.toString();
    }

    /**
     * 生成update语句,过滤掉自增列,使用trim
     *
     * @param columnMap
     * @param tableName
     * @return
     */
    private String generateConditionUpdateSql(Map<String, Column> columnMap, String tableName) {
        StringBuilder updateSql = new StringBuilder();
        updateSql.append("update ").append(tableName).append("\n");
        updateSql.append("\t\t<trim prefix=\"set\" suffixOverrides=\",\">\n");
        Column column;
        for (Map.Entry<String, Column> entry : columnMap.entrySet()) {
            column = entry.getValue();
            String camelKey = StringUtils.underlineToCamel(entry.getKey());
            if (!column.isAutoIncrement()) {
                updateSql.append("			").append("<if test=\"").append(camelKey).append("!=null\">");
                updateSql.append(entry.getKey()).append(" = #{");
                updateSql.append(StringUtils.underlineToCamel(entry.getKey())).append("},</if>\n");
            }
        }
        updateSql.append("\t\t</trim>");
        return updateSql.toString();
    }

    /**
     * 生成查询语句
     *
     * @param columnMap
     * @param tableName
     * @return
     */
    private String generateSelectSql(Map<String, Column> columnMap, String tableName) {
        StringBuilder selectSql = new StringBuilder();
        selectSql.append("select \n");
        int i = 0;
        int size = columnMap.size();
        for (Map.Entry<String, Column> entry : columnMap.entrySet()) {
            if (i < size - 1) {
                selectSql.append("			").append(entry.getKey()).append(",\n");
            } else {
                selectSql.append("			").append(entry.getKey()).append("\n");
            }
            i++;
        }
        selectSql.append(" 		from ").append(tableName);
        return selectSql.toString();
    }

    /**
     * mapper映射文件中resultMap下的result
     *
     * @param columnMap
     * @return
     */
    private String generateResultMap(Map<String, Column> columnMap) {
        StringBuilder results = new StringBuilder();
        String property;
        for (Map.Entry<String, Column> entry : columnMap.entrySet()) {
            property = StringUtils.underlineToCamel(entry.getKey());
            results.append("\t\t<result property=\"").append(property).append("\" column=\"");
            results.append(entry.getKey()).append("\" />\n");
        }
        return results.toString();
    }
}
