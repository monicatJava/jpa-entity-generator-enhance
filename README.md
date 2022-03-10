2022/3/10 gradle to maven fail
2022/3/10 oracle 連不上

# 1. 前言

以下提供了根据数据库表生成JPA Entity的Java组件，项目地址为 https://github.com/Adrninistrator/jpa-entity-generator-enhance ，在原始项目 https://github.com/smartnews/jpa-entity-generator 基础上进行了优化。

# 2. 优化范围

- 为字段增加长度属性

jpa-entity-generator（0.99.8）原始项目生成的Entity中，字段的@Column注解不包含长度属性length。当通过JPA Entity自动创建数据库表时，字段长度会使用length默认值255。对于数据库中字符串类型字段，可能由于长度过小导致测试失败；或由于长度过大导致测试时不能发现问题。

在增强版中，生成的Entity字段的@Column注解中会包含长度属性length，使用字段的实际长度。

- 为数据库表增加索引信息

原始项目生成的Entity中，类的@Table注解不包含索引信息。无法验证唯一索引相关功能。

在增强版中，生成的Entity类的@Table注解会包含表的实际索引信息，包含唯一索引与非唯一索引。

- 增加自定义修改字段类型功能

原始项目生成的Entity中，字段的类型可能不准确。例如生成MySQL数据库表对应的Entity时，blob、tinyblob、text类型的字段，生成的Entity字段均为String类型；当通过JPA Entity自动创建数据库表时，String类型生成的字段类型为varchar，导致与原始类型不一致。

在增强版中，支持按照需要修改数据库字段类型生成的Entity字段类型，可以避免生成的字段类型不准确的问题。

- 为DECIMAL增加总位数和小数位数

原始项目生成的Entity中，数据库表decimal类型字段对应的字段不包含precision、scale属性。当通过JPA Entity自动创建数据库表时，类型为decimal的字段的总位数和小数位数会使用默认值，可能与实际需要不符。

在增强版中，生成的Entity中，对于decimal类型对应的字段，@Column注解中会包含precision、scale属性，使用字段的实际值。

- 增加通过测试类执行的方式

原始项目支持的执行方式为Gradle或Maven插件。

在增强版中，增加通过测试类执行的方式，可以不通过Gradle或Maven插件方式执行。

- 生成Entity目录支持相对路径格式

原始项目的生成Entity目录只支持指定绝对路径，不支持相对路径，在不同开发环境执行时不方便。

在增强版中，生成Entity目录支持指定绝对路径及相对路径。

- 复合主键导入Serializable

原始项目生成的Entity中，复合主键对应的类实现了java.io.Serializable接口，但未添加对该接口的导入，需要手工添加。

在增强版中，对于出现了复合主键的表对应的Entity，会导入java.io.Serializable。

- 是否允许为空默认值省略

原始项目生成的Entity中，字段的@Column注解的“nullable = true”默认值未省略。

在增强版中，会将字段的@Column注解的“nullable = true”默认值省略。

- 打印生成文件路径

原始项目生成Entity时，未打印生成文件路径。在增强版中，会打印生成文件的完整路径。

# 3. 使用说明

可参考项目 https://github.com/Adrninistrator/UnitTest 。

## 3.1. 执行方式

### 3.1.1. Gradle插件方式（原有）

- Gradle脚本修改

在Gradle脚本中，增加以下内容：

```java
apply plugin: 'entitygen'

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath 'com.github.adrninistrator:jpa-entity-generator-enhance:0.0.1'
        classpath 'mysql:mysql-connector-java:5.1.32'
    }
}

entityGen {
    configPath = 'src/test/resources/entityGenConfig/entityGenConfig.yml'
}
```

以上添加的数据库驱动需要根据实际情况选择。

entityGen任务中的configPath参数用于指定配置文件路径。

在JPA Entity生成后保存的模块中，还需要添加以下依赖：

```
"javax.persistence:javax.persistence-api:2.2"
"org.projectlombok:lombok:1.18.12"
```

- 执行Gradle插件任务

```
gradlew/gradle entityGen
```

### 3.1.2. Maven插件方式（原有）

参考原始项目说明 https://github.com/smartnews/jpa-entity-generator/blob/master/README.md ，并修改引用的jar包。

### 3.1.3. 通过测试类执行（新增）

建议在项目的测试模块添加以下依赖：

|属性名|属性值|
| ------------ | ------------ |
|groupId|com.github.adrninistrator|
|artifactId|jpa-entity-generator-enhance|
|version|0.0.1|

- 使用Gradle

```
"com.github.adrninistrator:jpa-entity-generator-enhance:0.0.1"
```

- 使用Maven

```xml
<dependency>
    <groupId>com.github.adrninistrator</groupId>
    <artifactId>jpa-entity-generator-enhance</artifactId>
    <version>0.0.1</version>
    <scope>test</scope>
</dependency>
```

- 执行JpaEntityGenerator类

在测试类中执行JpaEntityGenerator.generate()方法。

以上默认使用“src/test/resources/entityGenConfig/entityGenConfig.yml”作为配置文件。

若需要使用其他配置文件，可通过JVM参数“entityGenConfig”指定配置文件路径。

例如在JVM参数中添加：

```
-DentityGenConfig=src/test/resources/entityGenConfig/entityGenConfig.yml
```

或在执行JpaEntityGenerator.generate()方法前增加：

```java
System.setProperty("entityGenConfig","src/test/resources/entityGenConfig/entityGenConfig.yml");
```

## 3.2. 配置文件参数（原有）

配置文件为yml格式，以下为配置文件中可使用的常用参数：

```yml
# 连接数据库参数
jdbcSettings:
  url: "jdbc:mysql://127.0.0.1:3306/testdb?useunicode=true&characterEncoding=utf8&allowMultiQueries=true&useAffectedRows=true"
  username: "test"
  password: "123456"
  driverClassName: "com.mysql.jdbc.Driver"

# 指定生成JPA Entity的目录，可使用绝对路径或相对路径
outputDirectory: "src/test/java"

# 指定生成JPA Entity的包名
packageName: "adrninistrator.test_jpa.entity"

tableScanMode: 'RuleBased'

# 指定需要生成JPA Entity的数据库表名，支持正则表达式
tableScanRules:
#  - tableNames: ["test_table", "test_table2", "test_table3"]
#  - tableNames: ["test_table[0-9]"]
#  - tableNames: ["test_table[2]", "test_table[3]"]
  - tableNames: ["test_.*"]

# 指定需要排除的JPA Entity的数据库表名，支持正则表达式
#tableExclusionRules:
#  - tableNames: ["test_table[0-9]"]

# 指定是否需要修改类型
modifyType: true

# 修改类型的配置文件路径，可使用绝对路径或相对路径
modifyTypePropertiesFile: "src/test/resources/entityGen/jpa_modify_type.properties"
```

modifyType与modifyTypePropertiesFile参数为增强版新增加参数，其他均为原始项目提供的参数。

## 3.3. 修改字段类型（新增）

- 修改字段类型功能说明

该功能为增强版新增加功能，可以按照需要修改数据库字段类型生成的Entity字段类型，用于解决Entity自动生成的数据库表字段类型不准确的问题。

当配置文件中的modifyType参数为true时，启用该功能。

默认使用jpa-entity-generator-enhance-xxx.jar中的entityGen/jpa_modify_type.properties文件作为修改字段类型的配置文件。

若配置文件中指定了modifyTypePropertiesFile参数，则使用该参数值对应文件作为修改字段类型的配置文件。

- 修改字段类型的默认配置文件

jpa-entity-generator-enhance-xxx.jar中的entityGen/jpa_modify_type.properties文件内容如下所示：

```properties
# LONGVARBINARY
-4=Blob
# BINARY
-2=byte[]
# LONGVARCHAR
-1=Clob
```

以上配置是为了解决原始项目根据MySQL数据库生成Entity时，blob、tinyblob、text类型生成的字段类型不正确，导致根据Entity生成的MySQL数据库表字段不正确的问题。

|原始MySQL字段类型|java.sql.Types类中对应的类型|修改字段类型的配置文件指定的生成Entity字段类型|根据Entity字段生成的MySQL字段类型|
| ------------ | ------------ | ------------ | ------------ |
|blob|LONGVARBINARY = -4|Blob|longblob|
|tinyblob|BINARY = -2|byte[]|tinyblob|
|text|LONGVARCHAR = -1|Clob|longtext|

- 按照需要指定配置文件参数

若以上默认配置不满足要求，可自定义修改modifyTypePropertiesFile参数值对应配置文件。

为了查看原始数据库字段类型，可将日志级别调整为debug，在测试类执行JpaEntityGenerator.generate()方法，在日志中查看“column info”相关记录，typeCode即为原始数据库字段在java.sql.Types类中对应的类型，如下所示：

```
column info: Column(name=blob2, length=255, precision=0, digits=0, typeCode=-2, typeName=TINYBLOB, nullable=true, primaryKey=false, autoIncrement=false, description=Optional[测试-blob2])
```

modifyTypePropertiesFile参数值对应的配置文件应为properties文件，key为原始数据库字段类型在java.sql.Types类中对应的数值，value指定生成的JPA Entity中对应字段的类型。若Entity中的字段类型为原始类型，或java.sql包中的类型，不需要指定包名；其他情况需要指定包名，如“java.math.BigDecimal”。

# 4. 相关资料

## 4.1. JPA相关

参考 https://www.oracle.com/technetwork/java/javaee/tech/persistence-jsp-140049.html 。

Java Persistence API（JPA）为对象关系映射（ORM）提供了POJO持久性模型。Java Persistence API是EJB 3.0软件专家组作为JSR 220的一部分开发的，但其使用并不限于EJB软件组件。Web应用程序和应用程序客户端也可以直接使用它。

JSR 220链接为 https://www.jcp.org/en/jsr/detail?id=220 。

可从 https://download.oracle.com/otndocs/jcp/ejb-3_0-fr-eval-oth-JSpec/ 下载JPA规范ejb-3_0-fr-spec-persistence.pdf。

### 4.1.1. Entity相关

- Entity

参考ejb-3_0-fr-spec-persistence.pdf，“Chapter 2 Entities”。

Entity是轻量级的持久域对象，是主要的编程目标。

Entity类必须使用@Entity注解，或在XML文件中指定。

Entity类必须具有无参数构造函数，也可以具有其他构造函数。无参数构造函数必须是public或protected。

Entity类必须是顶级类。枚举或接口不应指定为Entity。

Entity类不能是final。Entity类的任何方法或持久实例变量都不得为final。

如果Entity实例要通过值作为独立对象（例如，通过远程接口）传递，则该Entity类必须实现Serializable接口。

Entity的持久状态由实例变量表示，可以对应于Java-Beans属性。实例变量必须是private、protected，或包可见。

- @Table注解

参考 https://docs.jboss.org/hibernate/jpa/2.2/api/javax/persistence/Table.html 。

@Table注解用于指定Entity的主表。假如Entity类未指定@Table注解，则使用默认值。

。name属性用于指定表名，类型为Index[]的indexes属性用于指定表的索引信息。

- 主键

参考ejb-3_0-fr-spec-persistence.pdf，“2.1.4 Primary Keys and Entity Identity”。

每个Entity都必须有一个主键。

主键必须定义在作为Entity层次结构根目录的Entity上，或在Entity层次结构的映射超类上。在Entity层次结构中，主键必须定义一次。

一个简单的（即非复合的）主键必须对应于Entity类的单个持久字段或属性。@Id注解用于表示一个简单的主键。

组合主键必须对应于单个持久性字段或属性，或对应于如下所述的一组此类字段或属性。必须定义一个主键类来表示一个复合主键。当数据库key由几列组成，从传统数据库进行映射时，通常会出现复合主键。@EmbeddedId和@IdClass批注用于表示复合主键。

主键（或复合主键的字段或属性）应为以下类型之一：任意Java原始类型；任意原始包装器类型；java.lang.String；java.util.Date；java.sql.Date。一般来说，近似数字类型（例如浮点类型）不能在主键中使用。使用其他类型作为主键的Entity将不可移植。如果使用近似数字类型生成的主键，则仅整数类型是可移植的。如果将java.util.Date用作主键字段或属性，则应将时间类型指定为DATE。

主键类的访问类型（基于字段或基于属性的访问）由主键对应的Entity的访问类型确定。

以下规则适用于复合主键：

1. 主键类必须是public的，并且必须具有public无参数构造函数。
2. 如果使用基于属性的访问，则主键类的属性必须是public或protected。
3. 主键类必须可序列化（serializable）。
4. 主键类必须定义equals和hashCode方法。这些方法的值相等的语义，必须与键映射到的数据库类型的数据库相等性一致。
5. 复合主键必须表示并映射为可嵌入类（@EmbeddedId注解），或表示并映射至Entity类的多个字段或属性（@IdClass注解）。
6. 如果复合主键类映射到Entity类的多个字段或属性，则主键类中的主键字段或属性名称与Entity类的名称必须对应，并且它们的类型必须相同。

应用程序不得更改主键的值。如果发生这种情况，则该行为是不确定的。

**注：jpa-entity-generator（0.99.8）原始项目生成的Entity中，对于主键类会添加Lombok注解@Data，包含@EqualsAndHashCode注解的效果，即会自动生成equals与hashCode方法。**

- @Id注解

参考ejb-3_0-fr-spec-persistence.pdf，“9.1.8 Id Annotation”。

@Id注解指定Entity的主键属性或字段，可应用于Entity或映射的超类。

默认情况下，Entity主键的映射列被假定为主表的主键。如果未指定任何@Column注释，则假定主键列名是主键属性或字段的名称。

- @IdClass注解

参考ejb-3_0-fr-spec-persistence.pdf，“9.1.15 IdClass Annotation”。

@IdClass注解应用于Entity类或映射的超类，以指定映射到Entity的多个字段或属性的复合主键类。

主键类中的字段或属性的名称与Entity的主键字段或属性必须对应，并且它们的类型必须相同。

@Id注解也必须应用于Entity的相应字段或属性。

- @Column注解

参考 https://docs.jboss.org/hibernate/jpa/2.2/api/javax/persistence/Column.html 。

@Column注解用于指定持久属性或字段的映射列。如果未指定任何@Column注解，则使用默认值。

name属性为列的名称。默认为属性或字段名称。

length属性为列的长度。仅适用于使用字符串值的列。默认值为255.

nullable属性代表数据库列是否可为空。默认为true。

precision属性指定decimal列的精度，仅适用于decimal列。

scale属性指定decimal列的小数位数，仅适用于decimal列。

- 索引

索引需要通过@Table注解的indexes属性指定，参考 https://docs.jboss.org/hibernate/jpa/2.2/api/javax/persistence/Index.html 。

name属性为索引的名称，默认使用提供程序生成的名称。

columnList属性为索引中包含的列名称，按顺序排列。

unique属性代表是否为唯一索引，默认为false。

索引示例可参考 https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#schema-generation-columns-index 。

## 4.2. hibernate JPA实现

参考 https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#architecture-overview 。

Hibernate作为ORM解决方案，有效地位于Java应用程序数据访问层和关系数据库之间。作为JPA提供程序，Hibernate实现Java Persistence API规范。

### 4.2.1. Lob字段

参考 https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#basic-provided 。

JDBC类型BLOB对应的Java类型可为byte[]或java.sql.Blob。
JDBC类型CLOB对应的Java类型可为java.lang.String或java.sql.Clob。

参考 https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#basic-lob 。

映射LOB（数据库大对象）有两种形式，其中一种使用JDBC定位器类型。

JDBC LOB定位器允许有效访问LOB数据。它们允许JDBC驱动程序根据需要流式传输LOB数据的一部分，从而潜在地释放内存空间。但是可能不自然地处理并且具有一定的局限性。

JDBC LOB定位器类型包括：

```
java.sql.Blob
java.sql.Clob
java.sql.NClob
```

### 4.2.2. 依赖组件

参考 https://docs.jboss.org/hibernate/entitymanager/3.6/reference/en/html_single/#d0e215 。

hibernate-jpa-2.0-api-x.y.z.jar 是包含JPA 2.0 API的JAR，它提供了规范定义为公共API的所有接口和具体类。可以使用此JAR引导任何JPA提供程序实现。

org.hibernate.javax.persistence:hibernate-jpa-2.2-api组件已更名为javax.persistence:javax.persistence-api，可使用“javax.persistence:javax.persistence-api:2.2”版本的组件。
