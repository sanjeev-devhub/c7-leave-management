package com.example.leavemanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic paged result envelope. Camunda 7 uses offset paging
 * (firstResult / maxResults), so we expose the total count.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paged result envelope")
public class PagedResponse<T> {

    private List<T> items;

    @Schema(description = "Total number of matching records")
    private Long total;

    @Schema(description = "Zero-based offset of the first returned item")
    private Integer firstResult;

    @Schema(description = "Page size used for this query")
    private Integer pageSize;
}
