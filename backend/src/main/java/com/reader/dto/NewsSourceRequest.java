package com.reader.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NewsSourceRequest {
    @NotNull(message = "源类型不能为空")
    private Integer type;

    @NotBlank(message = "源名称不能为空")
    private String name;

    @NotBlank(message = "源地址不能为空")
    private String url;

    private String category;
    private String ruleJson;
    private Integer weight;
    private Integer fetchInterval;
    private Integer enabled;
}
