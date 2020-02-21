package test_jpa.entity;

import java.sql.*;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity(name = "test_jpa.entity.TestTable2")
@Table(name = "test_table2",
indexes = {
@Index(name = "uni_test_table2", columnList = "char1,char2", unique = true),
@Index(name = "idx_test_table2_1", columnList = "datetime1")
})
public class TestTable2 {

  @Id
  @Column(name = "\"id\"", length = 20, nullable = false)
  private String id;
  @Column(name = "\"blob1\"", length = 2147483647)
  private Blob blob1;
  @Column(name = "\"blob2\"", length = 255)
  private byte[] blob2;
  @Column(name = "\"char1\"", length = 1, nullable = false)
  private String char1;
  @Column(name = "\"char2\"", length = 30, nullable = false)
  private String char2;
  @Column(name = "\"datetime1\"", length = 19)
  private Timestamp datetime1;
  @Column(name = "\"datetime2\"", length = 19)
  private Timestamp datetime2;
  @Column(name = "\"decimal1\"", length = 10, precision = 10)
  private java.math.BigDecimal decimal1;
  @Column(name = "\"decimal2\"", length = 18, precision = 18, scale = 2)
  private java.math.BigDecimal decimal2;
  @Column(name = "\"int_1\"", length = 10)
  private Integer int1;
  @Column(name = "\"int_2\"", length = 10)
  private Integer int2;
  @Column(name = "\"text1\"", length = 2147483647)
  private Clob text1;
  @Column(name = "\"text2\"", length = 255)
  private String text2;
  @Column(name = "\"timestamp1\"", length = 19, nullable = false)
  private Timestamp timestamp1;
  @Column(name = "\"timestamp2\"", length = 19, nullable = false)
  private Timestamp timestamp2;
}