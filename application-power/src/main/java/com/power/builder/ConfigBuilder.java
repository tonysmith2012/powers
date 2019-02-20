package com.power.builder;

import com.power.constant.ConstVal;
import com.power.constant.PackageConfig;
import com.power.constant.ProjectConfig;
import com.power.constant.SpringBootProjectConfig;
import com.power.database.DbProvider;
import com.power.database.TableInfo;
import com.power.model.ProjectPath;
import com.power.utils.GeneratorProperties;
import com.power.utils.PathUtil;

import java.util.*;

/**
 * 构建配置
 */
public class ConfigBuilder {
    /**
     * 路径配置信息
     */
    private Map<String, String> pathInfo;

    /**
     * 包配置详情
     */
    private Map<String, String> packageInfo;
    /**
     * 工程基础配置文件
     */
    private Map<String, String> baseConfigFilesPath;
    /**
     * 工程基础配置文件路径
     */
    private Map<String, String> baseConfigPathInfo;

    /**
     * 获取表信息
     */
    private List<TableInfo> tableInfo;

    /**
     * maven项目的路径结构
     */
    private ProjectPath projectPath;

    /**
     * 初始化构建一个maven项目配置表
     */
    private void initProjectPath() {
        projectPath = new ProjectPath();
        String fileSeparator = System.getProperty("file.separator");
        String applicationName = GeneratorProperties.applicationName();
        String basePath = GeneratorProperties.outDir() + fileSeparator + applicationName;
        String javaDir = basePath + fileSeparator + ConstVal.JAVA_PATH;
        String testDir = basePath + fileSeparator + ConstVal.TEST_JAVA_PATH;
        String resourceDir = basePath + fileSeparator + ConstVal.RESOURCE_DIR;

        projectPath.setBasePath(basePath);
        projectPath.setJavaSrcPath(javaDir);
        projectPath.setResourceDir(resourceDir);
        projectPath.setTestJavaSrcPath(testDir);

    }

    public ConfigBuilder(DbProvider dataBaseInfo, PackageConfig packageConfig, ProjectConfig projectConfig) {
        //全局设置项目的机构
        initProjectPath();
        //包配置
        if (null == packageConfig) {
            handlerPackage(new PackageConfig());
        } else {
            handlerPackage(packageConfig);
        }
        //创建工程所需配置
        if (null == projectConfig) {
            projectConfig = new ProjectConfig();
            handlerBaseConfigPath(projectConfig);
            handlerBaseConfigFiles(projectConfig);
        } else {
            projectConfig = new ProjectConfig();
            handlerBaseConfigPath(projectConfig);
            handlerBaseConfigFiles(projectConfig);
        }
        getTableInfoList(dataBaseInfo);

    }

    /**
     * 构建spring boot
     *
     * @param dataBaseInfo
     * @param packageConfig
     * @param projectConfig
     */
    public ConfigBuilder(DbProvider dataBaseInfo, PackageConfig packageConfig, SpringBootProjectConfig projectConfig) {
        //全局设置项目的结构
        initProjectPath();
        //包配置
        if (null == packageConfig) {
            handlerPackage(new PackageConfig());
        } else {
            handlerPackage(packageConfig);
        }
        //创建工程所需配置
        if (null == projectConfig) {
            projectConfig = new SpringBootProjectConfig();
            handlerSpringBootConfigPath(projectConfig);
            handlerSpringBootConfigFiles(projectConfig);
        } else {
            projectConfig = new SpringBootProjectConfig();
            handlerSpringBootConfigPath(projectConfig);
            handlerSpringBootConfigFiles(projectConfig);
        }
        getTableInfoList(dataBaseInfo);
    }


    /**
     * 获取列信息
     *
     * @param dbProvider
     */
    private void getTableInfoList(DbProvider dbProvider) {
        tableInfo = dbProvider.getTablesInfo(GeneratorProperties.getTableName());
    }

    /**
     * 处理包
     *
     * @param config
     */
    private void handlerPackage(PackageConfig config) {
        packageInfo = new HashMap<>(8);
        pathInfo = new LinkedHashMap<>(8);

        String basePackage = GeneratorProperties.basePackage();
        String javaDir = projectPath.getJavaSrcPath();
        String testDir = projectPath.getTestJavaSrcPath();
        String resourceDir = projectPath.getResourceDir();


        String layers = GeneratorProperties.layers();
        String[] layerArr = layers.split(",");
        Set<String> layerSet = new HashSet<>();
        layerSet.addAll(Arrays.asList(layerArr));
        for (String str : layerSet) {
            if (ConstVal.SERVICE.equals(str)) {
                packageInfo.put(ConstVal.SERVICE, joinPackage(basePackage, config.getService()));
                packageInfo.put(ConstVal.SERVICEIMPL, joinPackage(basePackage, config.getServiceImpl()));
                pathInfo.put(ConstVal.SERVICE_PATH, joinPath(javaDir, packageInfo.get(ConstVal.SERVICE)));
                pathInfo.put(ConstVal.SERVICEIMPL_PATH, joinPath(javaDir, packageInfo.get(ConstVal.SERVICEIMPL)));
            } else if (ConstVal.SERVICE_TEST.equals(str)) {
                packageInfo.put(ConstVal.SERVICE_TEST, joinPackage(basePackage, config.getService()));
                pathInfo.put(ConstVal.SERVICE_TEST_PATH, joinPath(testDir, packageInfo.get(ConstVal.SERVICE_TEST)));
            } else if (ConstVal.CONTROLLER_TEST.equals(str)) {
                packageInfo.put(ConstVal.CONTROLLER_TEST, joinPackage(basePackage, config.getController()));
                pathInfo.put(ConstVal.CONTROLLER_TEST_PATH, joinPath(testDir, packageInfo.get(ConstVal.CONTROLLER_TEST)));
            } else if (ConstVal.DAO.equals(str)) {
                packageInfo.put(ConstVal.DAO, joinPackage(basePackage, config.getDao()));
                pathInfo.put(ConstVal.DAO_PATH, joinPath(javaDir, packageInfo.get(ConstVal.DAO)));
            } else if (ConstVal.ENTITY.equals(str)) {
                packageInfo.put(ConstVal.ENTITY, joinPackage(basePackage, config.getEntity()));
                pathInfo.put(ConstVal.ENTITY_PATH, joinPath(javaDir, packageInfo.get(ConstVal.ENTITY)));
            } else if (ConstVal.MAPPER.equals(str)) {
                packageInfo.put(ConstVal.MAPPER, joinPackage(basePackage, config.getMapper()));
                pathInfo.put(ConstVal.MAPPER_PATH, joinPath(resourceDir, packageInfo.get(ConstVal.MAPPER)));
            } else if (ConstVal.CONTROLLER.equals(str)) {
                packageInfo.put(ConstVal.CONTROLLER, joinPackage(basePackage, config.getController()));
                pathInfo.put(ConstVal.CONTROLLER_PATH, joinPath(javaDir, packageInfo.get(ConstVal.CONTROLLER)));
            } else if (ConstVal.VO.equals(str)) {
                packageInfo.put(ConstVal.VO, joinPackage(basePackage, config.getVo()));
                pathInfo.put(ConstVal.VO_PATH, joinPath(javaDir, packageInfo.get(ConstVal.VO)));
            }
        }
        packageInfo.put(ConstVal.DATE_CONVERTER, joinPackage(basePackage, config.getConverter()));
        pathInfo.put(ConstVal.DATE_CONVERTER_PATH, joinPath(javaDir, packageInfo.get(ConstVal.DATE_CONVERTER)));

        packageInfo.put(ConstVal.COMMON_RESULT,joinPackage(basePackage,config.getVo()));
        pathInfo.put(ConstVal.COMMON_RESULT_PATH,joinPath(javaDir,packageInfo.get(ConstVal.COMMON_RESULT)));
    }

    /**
     * 处理ssm架构体系的工程配置文件
     *
     * @param config
     */
    private void handlerBaseConfigFiles(ProjectConfig config) {

        String basePath = projectPath.getBasePath();
        baseConfigFilesPath = new HashMap<>(10);

        baseConfigFilesPath.put(ConstVal.TPL_POM, connectPath(basePath, config.getPom()));
        baseConfigFilesPath.put(ConstVal.TPL_LOF4J2, connectPath(basePath, config.getLog4j2()));
        baseConfigFilesPath.put(ConstVal.TPL_400, connectPath(basePath, config.getHtml400()));
        baseConfigFilesPath.put(ConstVal.TPL_404, connectPath(basePath, config.getHtml404()));
        baseConfigFilesPath.put(ConstVal.TPL_500, connectPath(basePath, config.getHtml500()));
        baseConfigFilesPath.put(ConstVal.TPL_SPRING_MVC, connectPath(basePath, config.getSpringMvc()));
        baseConfigFilesPath.put(ConstVal.TPL_SPRING_MYBATIS, connectPath(basePath, config.getSpringMybatis()));
        baseConfigFilesPath.put(ConstVal.TPL_MYBATIS_CONFIG, connectPath(basePath, config.getMybatisConfig()));
        baseConfigFilesPath.put(ConstVal.TPL_WEB_XML, connectPath(basePath, config.getWebXml()));
        baseConfigFilesPath.put(ConstVal.TPL_JDBC, connectPath(basePath, config.getJdbc()));
    }

    /**
     * 处理SpringBoot体系的项目配置文件
     *
     * @param config
     */
    private void handlerSpringBootConfigFiles(SpringBootProjectConfig config) {

        String basePath = projectPath.getBasePath();
        baseConfigFilesPath = new HashMap<>(10);
        baseConfigFilesPath.put(ConstVal.TPL_SPRING_BOOT_POM, connectPath(basePath, config.getPom()));
        baseConfigFilesPath.put(ConstVal.TPL_SPRING_BOOT_CFG_YML, connectPath(basePath, config.getApplicationYml()));
        baseConfigFilesPath.put(ConstVal.TPL_LOF4J2, connectPath(basePath, config.getLog4j2()));
        baseConfigFilesPath.put(ConstVal.TPL_MYBATIS_CONFIG, connectPath(basePath, config.getMybatisConfig()));
        baseConfigFilesPath.put(ConstVal.TPL_400, connectPath(basePath, config.getHtml400()));
        baseConfigFilesPath.put(ConstVal.TPL_404, connectPath(basePath, config.getHtml404()));
        baseConfigFilesPath.put(ConstVal.TPL_500, connectPath(basePath, config.getHtml500()));
    }

    /**
     * 处理SpringBoot项目的配置路径
     *
     * @param config
     */
    private void handlerSpringBootConfigPath(SpringBootProjectConfig config) {
        String basePath = projectPath.getBasePath();
        baseConfigPathInfo = new HashMap<>(3);

        baseConfigPathInfo.put(ConstVal.RESOURCE_PATH, connectPath(basePath, config.getResource()));
        baseConfigPathInfo.put(ConstVal.STRING_BOOT_EORRO_DIR, connectPath(basePath, config.getErrorPath()));
    }

    private void handlerBaseConfigPath(ProjectConfig config) {
        String basePath = projectPath.getBasePath();
        baseConfigPathInfo = new HashMap<>(3);

        baseConfigPathInfo.put(ConstVal.RESOURCE_PATH, connectPath(basePath, config.getResource()));
        baseConfigPathInfo.put(ConstVal.WEB_INFO_PATH, connectPath(basePath, config.getWebInfoPath()));
        baseConfigPathInfo.put(ConstVal.ERROR_PATH, connectPath(basePath, config.getErrorPath()));
    }

    /**
     * 连接路径字符串
     *
     * @param parentDir   路径常量字符串
     * @param packageName 包名
     * @return 连接后的路径
     */
    private String joinPath(String parentDir, String packageName) {
        return PathUtil.joinPath(parentDir, packageName);
    }

    /**
     * 两个路径的连接
     *
     * @param parentDir
     * @param path
     * @return
     */
    private String connectPath(String parentDir, String path) {
        return PathUtil.connectPath(parentDir, path);
    }

    /**
     * 连接父子包名
     *
     * @param parent     父包名
     * @param subPackage 子包名
     * @return 连接后的包名
     */
    private String joinPackage(String parent, String subPackage) {
        return PathUtil.joinPackage(parent, subPackage);
    }

    public Map<String, String> getPathInfo() {
        return pathInfo;
    }

    public Map<String, String> getPackageInfo() {
        return packageInfo;
    }

    public Map<String, String> getBaseConfigFilesPath() {
        return baseConfigFilesPath;
    }

    public Map<String, String> getBaseConfigPathInfo() {
        return baseConfigPathInfo;
    }

    public List<TableInfo> getTableInfo() {
        return tableInfo;
    }

    public ProjectPath getProjectPath() {
        return projectPath;
    }
}
