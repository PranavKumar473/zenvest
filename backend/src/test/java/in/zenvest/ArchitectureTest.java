package in.zenvest;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(packages = "in.zenvest", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

  @ArchTest
  static final ArchRule controllers_must_not_access_repositories =
      noClasses()
          .that()
          .areAnnotatedWith(RestController.class)
          .or()
          .areAnnotatedWith(Controller.class)
          .should()
          .dependOnClassesThat()
          .areAssignableTo(org.springframework.data.repository.Repository.class)
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule no_double_or_float_in_money_fields =
      fields()
          .that()
          .haveNameMatching(".*[aA]mount.*")
          .or()
          .haveRawType("in.zenvest.shared.api.Money")
          .should()
          .notHaveRawType(double.class)
          .andShould()
          .notHaveRawType(Double.class)
          .andShould()
          .notHaveRawType(float.class)
          .andShould()
          .notHaveRawType(Float.class)
          .allowEmptyShould(true);

  @org.junit.jupiter.api.Test
  void verifyModulithStructure() {
    ApplicationModules modules = ApplicationModules.of(ZenvestBackendApplication.class);
    modules.verify();
  }
}
