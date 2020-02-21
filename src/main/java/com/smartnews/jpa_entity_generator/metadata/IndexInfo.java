package com.smartnews.jpa_entity_generator.metadata;

import lombok.Data;

//add by zhengyi，索引信息
@Data
public class IndexInfo {

    private String name;
    private String columnList;
    private boolean unique = false;
}
