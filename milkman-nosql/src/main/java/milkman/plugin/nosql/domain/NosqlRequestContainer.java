package milkman.plugin.nosql.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import milkman.domain.RequestContainer;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NosqlRequestContainer extends RequestContainer {

  private String database;

  @Override
  public String getType() {
    return "NoSql";
  }

  public NosqlRequestContainer(String name, String database) {
    super(name);
    this.database = database;
  }

}
