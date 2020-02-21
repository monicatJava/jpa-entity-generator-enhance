package com.smartnews.jpa_entity_generator.generator;

import com.smartnews.jpa_entity_generator.CodeGenerator;
import com.smartnews.jpa_entity_generator.config.CodeGeneratorConfig;

//add by zhengyi，提供生成Entity的Java类
public class JpaEntityGenerator {

    private static final String CONFIG_FILE_DEFAULT = "src/test/resources/entityGenConfig/entityGenConfig.yml";

    public static void generate() throws Exception {

        String entityGenConfig = System.getProperty("entityGenConfig");

        if (entityGenConfig == null || entityGenConfig.isEmpty()) {
            entityGenConfig = CONFIG_FILE_DEFAULT;
        }

        CodeGeneratorConfig config = CodeGeneratorConfig.load(entityGenConfig);
        if (config.isJpa1SupportRequired()) {
            if (config.getPackageName().equals(config.getPackageNameForJpa1())) {
                throw new IllegalStateException("packageName and packageNameForJpa1 must be different.");
            }
            CodeGenerator.generateAll(config, true);
            return;
        }

        CodeGenerator.generateAll(config, false);
    }

    private JpaEntityGenerator() {
        throw new IllegalStateException("illegal");
    }
}
