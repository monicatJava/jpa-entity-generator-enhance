package test_jpa.entity;

import java.io.Serializable;
import java.sql.*;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity(name = "test_jpa.entity.TestTable3")
@Table(name = "test_table3",
indexes = {
@Index(name = "idx_test_table3_1", columnList = "create_time")
})
@IdClass(TestTable3.PrimaryKeys.class)
public class TestTable3 {
  @Data
  public static class PrimaryKeys implements Serializable {
    private String flag;
    private String id;
  }

  @Id
  @Column(name = "\"flag\"", length = 32, nullable = false)
  private String flag;
  @Id
  @Column(name = "\"id\"", length = 32, nullable = false)
  private String id;
  @Column(name = "\"create_time\"", length = 19, nullable = false)
  private Timestamp createTime;
  @Column(name = "\"update_time\"", length = 19, nullable = false)
  private Timestamp updateTime;
}