package org.cbioportal.domain.fusion;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.cbioportal.legacy.web.parameter.PagingConstants;

/**
 * Criteria for fetching fusions.
 *
 * <p>{@code studyIds} is required: it is what the caller is authorized against, and the query
 * restricts its results to these studies. {@code molecularProfileIds} and {@code entrezGeneIds}
 * only narrow the result further.
 *
 * @param studyIds studies to read fusions from
 * @param molecularProfileIds optional; restrict to these molecular profiles
 * @param entrezGeneIds optional; restrict to fusions where either partner gene is one of these
 */
public record FusionFilter(
    @NotEmpty @Size(max = PagingConstants.MAX_PAGE_SIZE) List<String> studyIds,
    @Size(max = PagingConstants.MAX_PAGE_SIZE) List<String> molecularProfileIds,
    @Size(max = PagingConstants.MAX_PAGE_SIZE) List<Integer> entrezGeneIds) {}
