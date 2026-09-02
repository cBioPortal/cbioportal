package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.cbioportal.legacy.model.EntityType;

@Schema(name = "CoExpression", description = "Represents a co-expression result")
public record CoExpressionDTO(
    String geneticEntityId,
    EntityType geneticEntityType,
    BigDecimal spearmansCorrelation,
    BigDecimal pValue) {}
