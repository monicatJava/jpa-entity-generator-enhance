package test_jpa.entity;

import java.sql.*;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity(name = "test_jpa.entity.TestTable")
@Table(name = "test_table",
indexes = {
@Index(name = "idx_test_table_1", columnList = "flag")
})
public class TestTable {

  @Id
  @Column(name = "\"id\"", length = 32, nullable = false)
  private String id;
  @Column(name = "\"create_time\"", length = 19, nullable = false)
  private Timestamp createTime;
  @Column(name = "\"flag\"", length = 32, nullable = false)
  private String flag;
  @Column(name = "\"update_time\"", length = 19, nullable = false)
  private Timestamp updateTime;
}