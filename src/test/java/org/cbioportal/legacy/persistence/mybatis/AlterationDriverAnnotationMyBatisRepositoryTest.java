package org.cbioportal.legacy.persistence.mybatis;

import java.util.Arrays;
import java.util.List;
import org.cbioportal.legacy.AbstractLegacyTestcontainers;
import org.cbioportal.legacy.model.AlterationDriverAnnotation;
import org.cbioportal.legacy.persistence.config.MyBatisLegacyConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@Import({MyBatisLegacyConfig.class, AlterationDriverAnnotationMyBatisRepository.class})
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractLegacyTestcontainers.Initializer.class)
public class AlterationDriverAnnotationMyBatisRepositoryTest {

  @Autowired
  private AlterationDriverAnnotationMyBatisRepository alterationDriverAnnotationMyBatisRepository;

  @Test
  public void getAlterationDriverAnnotationsCna() {
    List<String> molecularProfileIds = Arrays.asList("study_tcga_pub_gistic");
    List<AlterationDriverAnnotation> annotations =
        alterationDriverAnnotationMyBatisRepository.getAlterationDriverAnnotations(
            molecularProfileIds);
    Assert.assertEquals(2, annotations.size());
  }

  @Test
  public void getAlterationDriverAnnotationsMutations() {
    List<String> molecularProfileIds = Arrays.asList("study_tcga_pub_mutations");
    List<AlterationDriverAnnotation> annotations =
        alterationDriverAnnotationMyBatisRepository.getAlterationDriverAnnotations(
            molecularProfileIds);
    Assert.assertEquals(8, annotations.size());
  }

  @Test
  public void getAlterationDriverAnnotationsAll() {
    List<String> molecularProfileIds =
        Arrays.asList("study_tcga_pub_gistic", "study_tcga_pub_mutations");
    List<AlterationDriverAnnotation> annotations =
        alterationDriverAnnotationMyBatisRepository.getAlterationDriverAnnotations(
            molecularProfileIds);
    Assert.assertEquals(10, annotations.size());
  }

  @Test
  public void getAlterationDriverAnnotationsNull() {
    List<AlterationDriverAnnotation> annotations =
        alterationDriverAnnotationMyBatisRepository.getAlterationDriverAnnotations(null);
    Assert.assertEquals(0, annotations.size());
  }
}
