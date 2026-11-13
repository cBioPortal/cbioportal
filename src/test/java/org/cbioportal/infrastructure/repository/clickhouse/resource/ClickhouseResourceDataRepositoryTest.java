package org.cbioportal.infrastructure.repository.clickhouse.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cbioportal.domain.resource.ResourceIdentifierFixtures.samples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSessionFactory;
import org.cbioportal.domain.resource.ResourceColumnFilter;
import org.cbioportal.domain.resource.ResourceColumnInfo;
import org.cbioportal.domain.resource.ResourceFacetOption;
import org.cbioportal.domain.resource.ResourceGroupBy;
import org.cbioportal.domain.resource.ResourceNumericRange;
import org.cbioportal.domain.resource.ResourceQueryContext;
import org.cbioportal.domain.resource.ResourceTableMetadataView;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@Import({MyBatisConfig.class, ClickhouseResourceDataRepository.class})
@JdbcTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseResourceDataRepositoryTest {

  private static final String STUDY_TCGA_PUB = "study_tcga_pub";
  private static final String STUDY_ACC = "acc_tcga";

  @Autowired private ClickhouseResourceDataRepository repository;
  @Autowired private ClickhouseResourceDataMapper mapper;
  @Autowired private SqlSessionFactory sqlSessionFactory;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  public void
      getResourceTableFacets_metadataColumnsRemainDiscoverable_whenAnotherFilterZeroesRows() {
    // Regression test: key discovery must not run against the column-filtered query. Deselecting
    // every option in one column yields a "1 = 0" filter matching no rows, which would otherwise
    // drop every dynamic metadata column from the facets map, not just that one.
    ResourceColumnFilter zeroingFilter =
        new ResourceColumnFilter("metadata:stain", "in", List.of());
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "HE_SLIDE",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            List.of(zeroingFilter),
            null,
            null,
            null,
            null,
            null);

    Map<String, List<ResourceFacetOption>> facets =
        repository.getResourceTableMetadata(query, ResourceQueryContext.none()).facets();

    assertThat(facets).containsKey("metadata:magnification");
    assertThat(facets.get("metadata:magnification"))
        .containsExactlyInAnyOrder(
            new ResourceFacetOption("20x", 1L), new ResourceFacetOption("40x", 1L));
  }

  @Test
  public void getResourceTableFacetRanges_autoDetectedNumericColumn_returnsMinMax() {
    // FIGURES rows have {"pages": 10} / {"pages": 25} — a genuinely numeric key with no schema
    // override, so this must be auto-detected as numeric.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "FIGURES",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    Map<String, ResourceNumericRange> ranges =
        repository.getResourceTableMetadata(query, ResourceQueryContext.none()).facetRanges();

    assertThat(ranges).containsEntry("metadata:pages", new ResourceNumericRange(10.0, 25.0));
  }

  @Test
  public void getResourceTableFacets_autoDetectedNumericColumn_excludedFromCategoricalFacets() {
    // A numeric column shouldn't also show up as an enumerated categorical facet.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "FIGURES",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    Map<String, List<ResourceFacetOption>> facets =
        repository.getResourceTableMetadata(query, ResourceQueryContext.none()).facets();

    assertThat(facets).doesNotContainKey("metadata:pages");
  }

  @Test
  public void getResourceTableFacetRanges_unitSuffixedColumn_notTreatedAsNumeric() {
    // HE_SLIDE's "magnification" values ("20x"/"40x") don't parse as pure numbers, so it must
    // stay categorical (no facetRange) even though it looks numeric-ish.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "HE_SLIDE",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    Map<String, ResourceNumericRange> ranges =
        repository.getResourceTableMetadata(query, ResourceQueryContext.none()).facetRanges();

    assertThat(ranges).doesNotContainKey("metadata:magnification");
  }

  @Test
  public void getResourceTableFacetRanges_schemaOverridesStringForcesCategorical() {
    // RADIOLOGY's custom_metadata schema declares "dose_id" as "string" even though its values
    // ("1001", "1002") look numeric — the explicit override must win over auto-detection.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "RADIOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    Map<String, ResourceNumericRange> ranges =
        repository.getResourceTableMetadata(query, ResourceQueryContext.none()).facetRanges();
    Map<String, List<ResourceFacetOption>> facets =
        repository.getResourceTableMetadata(query, ResourceQueryContext.none()).facets();

    assertThat(ranges).doesNotContainKey("metadata:dose_id");
    assertThat(facets).containsKey("metadata:dose_id");
  }

  @Test
  public void getResourceTableFacetRanges_schemaDeclaresNumber_usesDeclaredType() {
    // RADIOLOGY's schema declares "score" as "number"; auto-detection would agree here too, but
    // this confirms the schema path resolves correctly end-to-end.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "RADIOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    Map<String, ResourceNumericRange> ranges =
        repository.getResourceTableMetadata(query, ResourceQueryContext.none()).facetRanges();

    assertThat(ranges).containsEntry("metadata:score", new ResourceNumericRange(42.0, 85.0));
  }

  // ---- custom_metadata contract drives metadata column presentation ----

  @Test
  public void getResourceTableMetadataColumns_usesContractLabelAndDescription() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "RADIOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    ResourceColumnInfo score = columnById(query, "metadata:score");

    assertThat(score.label()).isEqualTo("Dose Score");
    assertThat(score.description()).isEqualTo("Radiation dose score");
    assertThat(score.source()).isEqualTo(ResourceColumnInfo.SOURCE_METADATA);
    assertThat(score.dataType()).isEqualTo("number");
  }

  @Test
  public void getResourceTableMetadataColumns_dropsKeysTheContractDoesNotDeclare() {
    // "aperture" is present in the data but absent from RADIOLOGY's contract. A resource that
    // declares a contract gets exactly the columns it declares, so the key discovery sample
    // cannot decide whether a column appears.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "RADIOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    assertThat(repository.getResourceTableMetadata(query, ResourceQueryContext.none()).columns())
        .extracting(ResourceColumnInfo::id)
        .doesNotContain("metadata:aperture");
  }

  @Test
  public void getResourceTableMetadataColumns_followsContractOrder() {
    // The columns and their order both come from the contract: score, dose_id, operator, series.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "RADIOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    List<String> ids =
        repository.getResourceTableMetadata(query, ResourceQueryContext.none()).columns().stream()
            .map(ResourceColumnInfo::id)
            .toList();

    assertThat(ids)
        .containsExactly(
            "metadata:score", "metadata:dose_id", "metadata:operator", "metadata:series");
  }

  @Test
  public void getResourceTableMetadata_skipsKeyDiscoveryWhenTheContractTypesEveryField() {
    // RADIOLOGY's contract names every column and gives each a type, so nothing is left for the
    // discovery scan to answer and it must not run.
    ClickhouseResourceDataMapper spy = Mockito.spy(mapper);
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "RADIOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    new ClickhouseResourceDataRepository(spy)
        .getResourceTableMetadata(query, ResourceQueryContext.none());

    Mockito.verify(spy, Mockito.never())
        .getResourceTableMetadataKeyStats(
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.anyLong());
  }

  @Test
  public void getResourceTableMetadata_runsKeyDiscoveryWhenThereIsNoContract() {
    // HE_SLIDE has no contract, so the data is the only source of columns and the scan has to run.
    ClickhouseResourceDataMapper spy = Mockito.spy(mapper);
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "HE_SLIDE",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    new ClickhouseResourceDataRepository(spy)
        .getResourceTableMetadata(query, ResourceQueryContext.none());

    Mockito.verify(spy)
        .getResourceTableMetadataKeyStats(
            Mockito.any(), Mockito.any(), Mockito.isNull(), Mockito.anyInt(), Mockito.anyLong());
  }

  @Test
  public void divergingContracts_showTheUnionOfTheirFieldsInStudyOrder() {
    // PATHOLOGY is declared by both studies: acc_tcga declares grade/reviewer, study_tcga_pub
    // declares stain/grade. Taking either alone would hide the other's declared keys. Contracts
    // are ordered by study identifier, and acc_tcga sorts first.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB, STUDY_ACC),
            "PATHOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    assertThat(repository.getResourceTableMetadata(query, ResourceQueryContext.none()).columns())
        .extracting(ResourceColumnInfo::id)
        .containsExactly("metadata:grade", "metadata:reviewer", "metadata:stain");
  }

  @Test
  public void divergingContracts_takeEachKeyFromItsFirstDeclaration() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB, STUDY_ACC),
            "PATHOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    assertThat(columnById(query, "metadata:grade").label()).isEqualTo("Grade (acc)");
    assertThat(columnById(query, "metadata:stain").label()).isEqualTo("Stain (tcga)");
  }

  @Test
  public void divergingContracts_leaveAConflictingTypeToTheData() {
    // study_tcga_pub types grade as number, acc_tcga as string, and the data holds "3" and
    // "high". A declared number would put a range filter on text.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB, STUDY_ACC),
            "PATHOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    assertThat(columnById(query, "metadata:grade").dataType()).isEqualTo("string");
  }

  @Test
  public void aContractIsAuthoritativeOnlyWhereEveryStudyInScopeDeclaresOne() {
    // CYTOLOGY has rows in both studies but only acc_tcga declares a contract. study_tcga_pub's
    // rows were never checked against it, so restricting the columns to it would hide "fixative".
    ResourceTableQuery bothStudies =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB, STUDY_ACC),
            "CYTOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    assertThat(
            repository.getResourceTableMetadata(bothStudies, ResourceQueryContext.none()).columns())
        .extracting(ResourceColumnInfo::id)
        .containsExactly("metadata:preparation", "metadata:fixative");
  }

  @Test
  public void thatSameContractIsAuthoritativeForItsOwnStudyAlone() {
    ResourceTableQuery declaringStudyOnly =
        new ResourceTableQuery(
            List.of(STUDY_ACC),
            "CYTOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    assertThat(
            repository
                .getResourceTableMetadata(declaringStudyOnly, ResourceQueryContext.none())
                .columns())
        .extracting(ResourceColumnInfo::id)
        .containsExactly("metadata:preparation");
  }

  @Test
  public void keyDiscoveryStillRunsWhenOnlySomeStudiesDeclareAContract() {
    // The undeclared study's keys are only knowable from the data, so the scan cannot be skipped
    // however completely the one contract types itself.
    ClickhouseResourceDataMapper spy = Mockito.spy(mapper);
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB, STUDY_ACC),
            "CYTOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    new ClickhouseResourceDataRepository(spy)
        .getResourceTableMetadata(query, ResourceQueryContext.none());

    Mockito.verify(spy)
        .getResourceTableMetadataKeyStats(
            Mockito.any(), Mockito.any(), Mockito.isNull(), Mockito.anyInt(), Mockito.anyLong());
  }

  @Test
  public void getResourceTableMetadataColumns_keepsDeclaredKeysNoRowCarries() {
    // "series" is declared but absent from every row. The column still renders, so a resource's
    // column set stays stable no matter which rows happen to be populated.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "RADIOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    ResourceColumnInfo series = columnById(query, "metadata:series");

    assertThat(series.label()).isEqualTo("Series");
  }

  @Test
  public void getResourceTableMetadataColumns_sortsAlphabeticallyWhenNoContract() {
    // HE_SLIDE has no custom_metadata at all, so the pre-contract behavior must be preserved.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "HE_SLIDE",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    List<ResourceColumnInfo> columns =
        repository.getResourceTableMetadata(query, ResourceQueryContext.none()).columns();

    assertThat(columns.stream().map(ResourceColumnInfo::id).toList())
        .containsExactly("metadata:magnification", "metadata:stain");
    assertThat(columns).allMatch(ResourceColumnInfo::filterable);
    assertThat(columns.get(0).label()).isEqualTo("magnification");
  }

  @Test
  public void metadataColumns_hiddenUnlessTheContractOptsThemIn() {
    // RADIOLOGY's contract marks only "score" visibleByDefault; everything else, declared or
    // discovered, stays hidden so a resource with many keys cannot bury the builtin columns.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "RADIOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    List<ResourceColumnInfo> columns =
        repository.getResourceTableMetadata(query, ResourceQueryContext.none()).columns();

    assertThat(columns)
        .filteredOn(ResourceColumnInfo::visibleByDefault)
        .extracting(ResourceColumnInfo::id)
        .containsExactly("metadata:score");
  }

  @Test
  public void metadataColumns_hiddenByDefaultWhenNoContract() {
    // HE_SLIDE has no custom_metadata at all.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "HE_SLIDE",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    assertThat(repository.getResourceTableMetadata(query, ResourceQueryContext.none()).columns())
        .noneMatch(ResourceColumnInfo::visibleByDefault);
  }

  @Test
  public void filterableFalse_marksColumnUnfilterableAndSkipsItsFacets() {
    // RADIOLOGY's contract sets "filterable": false on "operator": the column still exists and
    // still renders, it just gets no filter control and costs no facet aggregation.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "RADIOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    ResourceColumnInfo operator = columnById(query, "metadata:operator");
    Map<String, List<ResourceFacetOption>> facets =
        repository.getResourceTableMetadata(query, ResourceQueryContext.none()).facets();

    assertThat(operator.filterable()).isFalse();
    assertThat(facets).doesNotContainKey("metadata:operator");
    assertThat(facets).containsKey("metadata:dose_id");
  }

  @Test
  public void filterableFalse_alsoSuppressesTheNumericRange() {
    // The gate has to cover facetRanges too, otherwise a non-filterable numeric column would still
    // render a range slider.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "RADIOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    Map<String, ResourceNumericRange> ranges =
        repository.getResourceTableMetadata(query, ResourceQueryContext.none()).facetRanges();

    assertThat(ranges).containsKey("metadata:score");
    assertThat(ranges).doesNotContainKey("metadata:operator");
  }

  // ---- which statements read the raw documents ----

  /** Records the SQL of every query the mapper runs while it is recording. */
  @Intercepts({
    @Signature(
        type = Executor.class,
        method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(
        type = Executor.class,
        method = "query",
        args = {
          MappedStatement.class,
          Object.class,
          RowBounds.class,
          ResultHandler.class,
          CacheKey.class,
          BoundSql.class
        })
  })
  static final class StatementRecorder implements Interceptor {
    static final StatementRecorder INSTANCE = new StatementRecorder();
    private final List<Map.Entry<String, String>> statements =
        Collections.synchronizedList(new ArrayList<>());
    private volatile boolean recording;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
      if (recording) {
        MappedStatement statement = (MappedStatement) invocation.getArgs()[0];
        BoundSql sql =
            invocation.getArgs().length > 5
                ? (BoundSql) invocation.getArgs()[5]
                : statement.getBoundSql(invocation.getArgs()[1]);
        String id = statement.getId();
        statements.add(Map.entry(id.substring(id.lastIndexOf('.') + 1), sql.getSql()));
      }
      return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
      return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {}
  }

  /** The ids of every statement the read runs. */
  private List<String> statementIds(Runnable read) {
    if (!sqlSessionFactory
        .getConfiguration()
        .getInterceptors()
        .contains(StatementRecorder.INSTANCE)) {
      sqlSessionFactory.getConfiguration().addInterceptor(StatementRecorder.INSTANCE);
    }
    StatementRecorder.INSTANCE.statements.clear();
    StatementRecorder.INSTANCE.recording = true;
    try {
      read.run();
    } finally {
      StatementRecorder.INSTANCE.recording = false;
    }
    return StatementRecorder.INSTANCE.statements.stream().map(Map.Entry::getKey).toList();
  }

  // ---- contracts taken from the resource scope ----

  private static final String CONTRACTS_STATEMENT = "getResourceDefinitionCustomMetadata";

  private static ResourceTableQuery resourceIdsQuery(
      List<String> studyIds,
      List<String> resourceIds,
      List<org.cbioportal.legacy.web.parameter.SampleIdentifier> sampleIdentifiers,
      String search) {
    return new ResourceTableQuery(
        studyIds,
        null,
        null,
        sampleIdentifiers,
        search,
        0,
        10,
        null,
        null,
        null,
        resourceIds,
        null,
        null,
        null,
        null);
  }

  /** The metadata read with the request's resource scope, as the service resolves it. */
  private ResourceTableMetadataView metadataWithScope(
      ResourceTableQuery query, List<String> statementIds) {
    ResourceQueryContext scoped =
        new ResourceQueryContext(null, repository.getResourceScope(query));
    ResourceTableMetadataView[] view = new ResourceTableMetadataView[1];
    statementIds.addAll(
        statementIds(() -> view[0] = repository.getResourceTableMetadata(query, scoped)));
    return view[0];
  }

  @Test
  public void resourceIdsWithOneContractAndNothingNarrowingTheRows_takeItFromTheScope() {
    // One declared contract (RADIOLOGY; GROUPED_SLIDE with GROUPED_SLIDE_PATIENT, which declare the
    // same one) or none at all (HE_SLIDE with CT_SCAN). Read over the rows the contract statement
    // would return the same, so it is not issued, and the result is the one it would give.
    for (ResourceTableQuery query :
        List.of(
            resourceIdsQuery(List.of(STUDY_TCGA_PUB), List.of("RADIOLOGY"), null, null),
            resourceIdsQuery(
                List.of("study_genie_pub"),
                List.of("GROUPED_SLIDE", "GROUPED_SLIDE_PATIENT"),
                null,
                null),
            resourceIdsQuery(
                List.of(STUDY_TCGA_PUB), List.of("HE_SLIDE", "CT_SCAN"), null, null))) {
      List<String> ids = new ArrayList<>();
      ResourceTableMetadataView view = metadataWithScope(query, ids);

      assertThat(ids).as("%s", query.resourceIds()).doesNotContain(CONTRACTS_STATEMENT);
      assertThat(view)
          .as("%s", query.resourceIds())
          .isEqualTo(repository.getResourceTableMetadata(query, ResourceQueryContext.none()));
    }
  }

  @Test
  public void resourceIdsWithSeveralContractsOrNarrowedRows_readTheContractsOverTheRows() {
    List<String> both = List.of(STUDY_TCGA_PUB, STUDY_ACC);
    List<String> pathology = List.of("PATHOLOGY");
    for (ResourceTableQuery query :
        List.of(
            // Two contracts: which apply depends on which studies' rows match.
            resourceIdsQuery(both, pathology, null, null),
            resourceIdsQuery(both, List.of("PATHOLOGY", "CYTOLOGY"), null, null),
            // One contract, but the rows are narrowed.
            resourceIdsQuery(
                List.of(STUDY_TCGA_PUB),
                List.of("RADIOLOGY"),
                samples(STUDY_TCGA_PUB, "tcga-a1-a0se-01"),
                null),
            resourceIdsQuery(List.of(STUDY_TCGA_PUB), List.of("RADIOLOGY"), null, "tech-a"))) {
      List<String> ids = new ArrayList<>();
      ResourceTableMetadataView view = metadataWithScope(query, ids);

      assertThat(ids).as("%s", query).contains(CONTRACTS_STATEMENT);
      assertThat(view)
          .as("%s", query)
          .isEqualTo(repository.getResourceTableMetadata(query, ResourceQueryContext.none()));
    }

    // Narrowed to study_tcga_pub's row, only its contract applies: acc_tcga's "reviewer", which the
    // scope would add, is not a column.
    ResourceTableMetadataView narrowed =
        metadataWithScope(
            resourceIdsQuery(both, pathology, samples(STUDY_TCGA_PUB, "tcga-a1-a0zz-01"), null),
            new ArrayList<>());
    assertThat(narrowed.columns())
        .extracting(ResourceColumnInfo::id)
        .containsExactly("metadata:stain", "metadata:grade");
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void aDeclaredResourceWithNoRows_listsItsDeclaredColumns() {
    // Over the rows there is no contract, so no columns; from the scope, the declared ones, as a
    // grouped table lists them.
    jdbcTemplate.update(
        "INSERT INTO resource_definition (resource_id, cancer_study_id, resource_type,"
            + " display_name, description, open_by_default, priority, custom_metadata) VALUES"
            + " ('EMPTY_DECLARED', 2, 'SAMPLE', 'Empty', 'Declared, no rows', 1, 9,"
            + " '{\"version\":1,\"fields\":[{\"key\":\"site\",\"type\":\"string\"}]}')");
    try {
      ResourceTableQuery query =
          resourceIdsQuery(List.of(STUDY_ACC), List.of("EMPTY_DECLARED"), null, null);

      assertThat(metadataWithScope(query, new ArrayList<>()).columns())
          .extracting(ResourceColumnInfo::id)
          .containsExactly("metadata:site");
      assertThat(repository.getResourceTableMetadata(query, ResourceQueryContext.none()).columns())
          .isEmpty();
    } finally {
      jdbcTemplate.execute(
          "ALTER TABLE resource_definition DELETE WHERE resource_id = 'EMPTY_DECLARED'"
              + " SETTINGS mutations_sync = 2");
    }
  }

  /** The metadata/fetch statements that read metadata values, by statement id. */
  private static final List<String> METADATA_VALUE_STATEMENTS =
      List.of(
          "getResourceTableMetadataKeyStats",
          "getResourceTableMetadataRanges",
          "getResourceTableMetadataFacets",
          "getResourceTableGroupedMetadataFacets");

  /** rdata.metadata, the raw document, as opposed to rdata.metadata_json. */
  private static final Pattern RAW_METADATA = Pattern.compile("rdata\\.metadata(?!_json)");

  /** The metadata statements a metadata/fetch for the query runs, by id, with their SQL. */
  private List<Map.Entry<String, String>> metadataStatements(ResourceTableQuery query) {
    if (!sqlSessionFactory
        .getConfiguration()
        .getInterceptors()
        .contains(StatementRecorder.INSTANCE)) {
      sqlSessionFactory.getConfiguration().addInterceptor(StatementRecorder.INSTANCE);
    }
    StatementRecorder.INSTANCE.statements.clear();
    StatementRecorder.INSTANCE.recording = true;
    try {
      repository.getResourceTableMetadata(query, ResourceQueryContext.none());
    } finally {
      StatementRecorder.INSTANCE.recording = false;
    }
    return StatementRecorder.INSTANCE.statements.stream()
        .filter(statement -> METADATA_VALUE_STATEMENTS.contains(statement.getKey()))
        .toList();
  }

  private static ResourceTableQuery metadataQuery(
      List<String> studyIds, String resourceId, String search, ResourceGroupBy groupBy) {
    return new ResourceTableQuery(
        studyIds,
        resourceId,
        null,
        null,
        search,
        0,
        10,
        null,
        null,
        null,
        null,
        null,
        groupBy,
        null,
        null);
  }

  @Test
  public void aContractCoveringEveryStudy_readsOnlySubcolumns_withoutASearch() {
    // PATHOLOGY over both studies: every study declares it, and the conflicting type leaves grade
    // to be classified from the data. RADIOLOGY types every field and has a numeric one, so it
    // reads a range. Grouped or not, none of these statements reads the raw documents.
    List<String> both = List.of(STUDY_TCGA_PUB, STUDY_ACC);
    List<String> tcga = List.of(STUDY_TCGA_PUB);
    for (ResourceGroupBy groupBy : new ResourceGroupBy[] {null, ResourceGroupBy.PATIENT}) {
      String facets =
          groupBy == null
              ? "getResourceTableMetadataFacets"
              : "getResourceTableGroupedMetadataFacets";

      List<Map.Entry<String, String>> pathology =
          metadataStatements(metadataQuery(both, "PATHOLOGY", null, groupBy));
      assertThat(pathology)
          .extracting(Map.Entry::getKey)
          .as("grouped by %s", groupBy)
          .containsExactlyInAnyOrder("getResourceTableMetadataKeyStats", facets);

      List<Map.Entry<String, String>> radiology =
          metadataStatements(metadataQuery(tcga, "RADIOLOGY", null, groupBy));
      assertThat(radiology)
          .extracting(Map.Entry::getKey)
          .as("grouped by %s", groupBy)
          .containsExactlyInAnyOrder("getResourceTableMetadataRanges", facets);

      for (Map.Entry<String, String> statement :
          java.util.stream.Stream.concat(pathology.stream(), radiology.stream()).toList()) {
        assertThat(statement.getValue())
            .as("%s, grouped by %s", statement.getKey(), groupBy)
            .contains("metadata_json")
            .doesNotContainPattern(RAW_METADATA);
      }
    }
  }

  @Test
  public void withoutACoveringContract_keyDiscoveryReadsTheRawDocuments() {
    // HE_SLIDE has no contract, so its keys can only be found by reading each document.
    assertThat(metadataStatements(metadataQuery(List.of(STUDY_TCGA_PUB), "HE_SLIDE", null, null)))
        .filteredOn(statement -> statement.getKey().equals("getResourceTableMetadataKeyStats"))
        .singleElement()
        .satisfies(statement -> assertThat(statement.getValue()).containsPattern(RAW_METADATA));
  }

  @Test
  public void underASearch_theValuesAreReadFromTheRawDocuments() {
    // The search reads every row's raw document anyway, so the values come from it rather than
    // from the subcolumns as well.
    List<Map.Entry<String, String>> statements =
        metadataStatements(
            metadataQuery(List.of(STUDY_TCGA_PUB, STUDY_ACC), "PATHOLOGY", "example", null));
    assertThat(statements).isNotEmpty();
    assertThat(statements)
        .allSatisfy(statement -> assertThat(statement.getValue()).doesNotContain("metadata_json"));
  }

  private ResourceColumnInfo columnById(ResourceTableQuery query, String id) {
    return repository
        .getResourceTableMetadata(query, ResourceQueryContext.none())
        .columns()
        .stream()
        .filter(c -> c.id().equals(id))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no metadata column " + id));
  }
}
