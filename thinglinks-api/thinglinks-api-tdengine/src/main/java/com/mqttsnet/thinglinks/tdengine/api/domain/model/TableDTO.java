package com.mqttsnet.thinglinks.tdengine.api.domain.model;

import com.mqttsnet.thinglinks.tdengine.api.domain.Fields;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TableDTO implements Serializable {

  private static final long serialVersionUID = -1L;
  /**
   * 超级表普通列字段的值 值需要与创建超级表时普通列字段的数据类型对应上
   */
  private List<Fields> schemaFieldValues;

  /**
   * 超级表标签字段的值 值需要与创建超级表时标签字段的数据类型对应上
   */
  private List<Fields> tagsFieldValues;

  /**
   * 子表名称
   */
  private String tableName;

  /**
   * 数据库名称
   */
  private String dataBaseName;

  /**
   * 超级表名称
   */
  private String superTableName;

  public TableDTO() {

  }

  public TableDTO(List<Fields> schemaFieldValues, List<Fields> tagsFieldValues, String tableName, String dataBaseName, String superTableName) {
    this.schemaFieldValues = schemaFieldValues;
    this.tagsFieldValues = tagsFieldValues;
    this.tableName = tableName;
    this.dataBaseName = dataBaseName;
    this.superTableName = superTableName;
  }
}
