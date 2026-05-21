package com.airbus_cyber_security.graylog.wizard.alert.rest.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class AlertFields {

    private AlertFields() {
    }

    public static final String FIELD = "field";
    public static final String TYPE = "type";
    public static final String GROUPING_FIELDS = "grouping_fields";
    public static final String DISTINCT_BY = "distinct_by";
    public static final String TIME = "time";
    public static final String GRACE = "grace";
    public static final String ADDITIONAL_THRESHOLD = "additional_threshold";
    public static final String ADDITIONAL_THRESHOLD_TYPE = "additional_threshold_type";
    public static final String THRESHOLD_TYPE = "threshold_type";
    public static final String THRESHOLD = "threshold";
    public static final String THRESHOLD_TYPE_MORE = ">";
    public static final String THRESHOLD_TYPE_LESS = "<";
    public static final String SEARCH_QUERY = "search_query";
    public static final String ADDITIONAL_SEARCH_QUERY = "additional_search_query";

    public static final List<String> STATISTICAL_CONDITION_PARAMETER_TYPES = Collections.unmodifiableList(Arrays.asList("AVG", "MIN", "MAX", "SUM", "STDDEV", "CARD", "COUNT", "SUMOFSQUARES", "VARIANCE"));
}
