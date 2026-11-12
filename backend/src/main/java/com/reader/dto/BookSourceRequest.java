package com.reader.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookSourceRequest {
    @NotBlank(message = "书源名称不能为空")
    private String name;

    @NotBlank(message = "书源地址不能为空")
    private String url;

    @NotBlank(message = "书源规则不能为空")
    private String ruleJson;

    private Integer enabled;
}
