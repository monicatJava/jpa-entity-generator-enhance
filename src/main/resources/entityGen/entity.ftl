package ${packageName};

<#list importRules as rule>
import ${rule.importValue};
</#list>

<#if classComment?has_content>
${classComment}
</#if>
<#list classAnnotationRules as rule>
<#list rule.annotations as annotation>
${annotation.toString()}
</#list>
</#list>
<#-- NOTICE: the name attribute of @Table is intentionally unquoted  -->
<#if indexInfoList.size() \gt 0>
@Table(name = "${tableName}",
indexes = {
<#list indexInfoList as index>
@Index(name = "${index.name}", columnList = "${index.columnList}"<#if index.unique>, unique = true</#if>)<#if index_has_next>,</#if>
</#list>
})
<#else>
@Table(name = "${tableName}")
</#if>
<#if primaryKeyFields.size() \gt 1>
@IdClass(${className}.PrimaryKeys.class)
</#if>
public class ${className}<#if interfaceNames.size() \gt 0> implements ${interfaceNames?join(", ")}</#if> {
<#if primaryKeyFields.size() \gt 1>
  @Data
  public static class PrimaryKeys implements Serializable {
  <#list primaryKeyFields as field>
    private ${field.type} ${field.name}<#if field.defaultValue??> = ${field.defaultValue}</#if>;
  </#list>
  }
</#if>

<#list topAdditionalCodeList as code>
${code}

</#list>
<#list fields as field>
<#if field.comment?has_content>
${field.comment}
</#if>
<#if field.primaryKey>
  @Id
</#if>
<#if field.autoIncrement>
  <#if field.generatedValueStrategy?has_content>
  @GeneratedValue(strategy = GenerationType.${field.generatedValueStrategy})
  <#else>
  @GeneratedValue
  </#if>
</#if>
<#list field.annotations as annotation>
  ${annotation.toString()}
</#list>
<#if requireJSR305 && !field.primitive>
  <#if field.nullable>@Nullable<#else>@Nonnull</#if>
</#if>
  @Column(name = "<#if jpa1Compatible>`<#else>\"</#if>${field.columnName}<#if jpa1Compatible>`<#else>\"</#if>"<#if field.length != 0>, length = ${field.length?c}</#if><#if field.precision \gt 0>, precision = ${field.precision?c}</#if><#if field.digits \gt 0>, scale = ${field.digits?c}</#if><#if !field.nullable>, nullable = false</#if>)
  private ${field.type} ${field.name}<#if field.defaultValue??> = ${field.defaultValue}</#if>;
</#list>
<#list bottomAdditionalCodeList as code>

${code}
</#list>
}
