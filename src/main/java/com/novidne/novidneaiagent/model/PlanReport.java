package com.novidne.novidneaiagent.model;

import lombok.Data;

import java.util.List;

@Data
public class PlanReport {
    private String title;
    private List<String> plans;
}
