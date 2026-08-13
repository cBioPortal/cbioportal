package org.cbioportal.domain.wsi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Intrinsic image metadata required by the browser tile client. */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public record WsiTileMetadata(
    Dimensions dimensions,
    int levels,
    @JsonProperty("level_dimensions") List<Dimensions> levelDimensions,
    @JsonProperty("level_downsamples") List<Double> levelDownsamples,
    @JsonProperty("max_zoom") int maxZoom,
    @JsonProperty("tile_size") int tileSize,
    Mpp mpp,
    @JsonProperty("objective_power") Integer objectivePower,
    String vendor) {

  @JsonInclude(JsonInclude.Include.ALWAYS)
  public record Dimensions(int width, int height) {}

  @JsonInclude(JsonInclude.Include.ALWAYS)
  public record Mpp(double x, double y) {}
}
