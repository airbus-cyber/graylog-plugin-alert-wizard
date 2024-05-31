/*
 * Copyright (C) 2018 Airbus CyberSecurity (SAS)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package com.airbus_cyber_security.graylog.wizard.alert.rest;

import com.airbus_cyber_security.graylog.wizard.alert.model.TriggeringConditions;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.AlertRuleStream;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.FieldRule;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.requests.AlertRuleRequest;
import com.airbus_cyber_security.graylog.events.processor.correlation.CorrelationCountProcessorConfig;
import com.airbus_cyber_security.graylog.events.processor.correlation.checks.OrderType;
import com.airbus_cyber_security.graylog.wizard.alert.model.AlertType;
import com.airbus_cyber_security.graylog.wizard.database.Description;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.graylog.events.conditions.Expr;
import org.graylog.events.conditions.Expression;
import org.graylog.events.processor.EventProcessorConfig;
import org.graylog.events.processor.aggregation.AggregationConditions;
import org.graylog.events.processor.aggregation.AggregationEventProcessorConfig;
import org.graylog.events.processor.aggregation.AggregationFunction;
import org.graylog.events.processor.aggregation.AggregationSeries;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugin.streams.StreamRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import java.util.*;

/**
 * Converts from business model to rest model and vice versa
 */
// TODO should try to find a way to split this class. Too long
public class Conversions {

    private static final Logger LOG = LoggerFactory.getLogger(Conversions.class);

    private static final int MILLISECONDS_IN_A_MINUTE = 60 * 1000;

    // TODO should rather parse the conditionParameters as soon as it gets in the system, in order to get a tidy class with getters
    //      there is even a way to do it nicely with Jackson: see jackson-docs polymorphic type handling, jsonsubtypes
    private static final String FIELD = "field";
    private static final String TYPE = "type";
    private static final String GROUPING_FIELDS = "grouping_fields";
    private static final String DISTINCT_BY = "distinct_by";
    private static final String TIME = "time";
    private static final String GRACE = "grace";
    private static final String ADDITIONAL_THRESHOLD = "additional_threshold";
    private static final String ADDITIONAL_THRESHOLD_TYPE = "additional_threshold_type";
    private static final String THRESHOLD_TYPE = "threshold_type";
    private static final String THRESHOLD = "threshold";
    private static final String THRESHOLD_TYPE_MORE = ">";
    private static final String THRESHOLD_TYPE_LESS = "<";

    // TODO should avoid these conversions by always working with ms (from the IHM down to the server)
    private long convertMillisecondsToMinutes(long value) {
        return value / MILLISECONDS_IN_A_MINUTE;
    }

    private long convertMinutesToMilliseconds(long value) {
        return value * MILLISECONDS_IN_A_MINUTE;
    }

    // TODO should introduce constants for MORE and LESS
    private String convertCorrelationCountThresholdType(String thresholdType) {
        if (thresholdType.equals("MORE")) {
            return THRESHOLD_TYPE_MORE;
        } else {
            return THRESHOLD_TYPE_LESS;
        }
    }

    private int convertThreshold(Expression<Boolean> expression) {
        Expression<Double> expressionRight;
        if (expression instanceof Expr.Greater) {
            expressionRight = ((Expr.Greater) expression).right();
        } else if (expression instanceof Expr.GreaterEqual) {
            expressionRight = ((Expr.GreaterEqual) expression).right();
        } else if (expression instanceof Expr.Lesser) {
            expressionRight = ((Expr.Lesser) expression).right();
        } else if (expression instanceof Expr.LesserEqual) {
            expressionRight = ((Expr.LesserEqual) expression).right();
        } else if (expression instanceof Expr.Equal) {
            expressionRight = ((Expr.Equal) expression).right();
        } else {
            LOG.error("Can't get threshold, error cast Expression");
            return 0;
        }

        if (expressionRight instanceof Expr.NumberValue) {
            return (int) ((Expr.NumberValue) expressionRight).value();
        } else {
            LOG.error("Can't get threshold, error cast Right Expression");
            return 0;
        }
    }

    Map<String, Object> getConditionParameters(EventProcessorConfig eventConfig) {
        Map<String, Object> parametersCondition = Maps.newHashMap();

        switch (eventConfig.type()) {
            case "correlation-count":
                CorrelationCountProcessorConfig correlationConfig = (CorrelationCountProcessorConfig) eventConfig;
                parametersCondition.put(THRESHOLD, correlationConfig.threshold());
                String thresholdType = convertCorrelationCountThresholdType(correlationConfig.thresholdType());
                String additionalThresholdType = convertCorrelationCountThresholdType(correlationConfig.additionalThresholdType());
                parametersCondition.put(THRESHOLD_TYPE, thresholdType);
                parametersCondition.put(ADDITIONAL_THRESHOLD, correlationConfig.additionalThreshold());
                parametersCondition.put(ADDITIONAL_THRESHOLD_TYPE, additionalThresholdType);
                parametersCondition.put(TIME, this.convertMillisecondsToMinutes(correlationConfig.searchWithinMs()));
                parametersCondition.put(GROUPING_FIELDS, correlationConfig.groupingFields());
                parametersCondition.put(GRACE, this.convertMillisecondsToMinutes(correlationConfig.executeEveryMs()));
                break;
            case "aggregation-v1":
                AggregationEventProcessorConfig aggregationConfig = (AggregationEventProcessorConfig) eventConfig;
                parametersCondition.put(TIME, this.convertMillisecondsToMinutes(aggregationConfig.searchWithinMs()));
                parametersCondition.put(GRACE, this.convertMillisecondsToMinutes(aggregationConfig.executeEveryMs()));
                parametersCondition.put(THRESHOLD, convertThreshold(aggregationConfig.conditions().get().expression().get()));
                parametersCondition.put(THRESHOLD_TYPE, aggregationConfig.conditions().get().expression().get().expr());
                AggregationSeries series = aggregationConfig.series().get(0);
                parametersCondition.put(TYPE, series.function().toString());
                String distinctBy = "";
                Optional<String> seriesField = series.field();
                if (seriesField.isPresent()) {
                    // TODO think about this, but there is some code smell here...
                    // It is because AggregationEventProcessorConfig is used both for Count and Statistical conditions
                    distinctBy = seriesField.get();
                    // TODO should introduce constants here for "field"...
                    parametersCondition.put(FIELD, distinctBy);
                }
                parametersCondition.put(GROUPING_FIELDS, aggregationConfig.groupBy());
                parametersCondition.put(DISTINCT_BY, distinctBy);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return parametersCondition;
    }

    private boolean isValidTitle(String title) {
        return !(title == null || title.isEmpty());
    }

    private boolean isValidStream(AlertRuleStream stream) {
        if (stream.getMatchingType().equals(Stream.MatchingType.AND) || stream.getMatchingType().equals(Stream.MatchingType.OR)) {
            for (FieldRule fieldRule: stream.getFieldRules()) {
                if (fieldRule.getField() == null || fieldRule.getField().isEmpty() ||
                        fieldRule.getType() < -7 || fieldRule.getType() > 7) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean isValidStatThresholdType(String thresholdType) {
        return (thresholdType.equals("<") || thresholdType.equals("<=") ||
                thresholdType.equals(">") || thresholdType.equals(">=") || thresholdType.equals("=="));
    }

    private boolean isValidCondStatistical(Map<String, Object> conditionParameters) {
        if (!conditionParameters.containsKey(TYPE)) {
            LOG.debug("Missing condition parameter {}", TYPE);
            return false;
        }
        if (!conditionParameters.containsKey(FIELD)) {
            LOG.debug("Missing condition parameter {}", FIELD);
            return false;
        }
        String thresholdType = conditionParameters.get(THRESHOLD_TYPE).toString();
        if (!isValidStatThresholdType(thresholdType)) {
            LOG.debug("Invalid condition parameter {}, {}", THRESHOLD_TYPE, thresholdType);
            return false;
        }
        return true;
    }

    private boolean isValidThresholdType(String thresholdType) {
        return (thresholdType.equals(THRESHOLD_TYPE_MORE) || thresholdType.equals(THRESHOLD_TYPE_LESS));
    }

    private boolean isValidCondCorrelation(Map<String, Object> conditionParameters, AlertRuleStream secondStream) {
        return (conditionParameters.containsKey(ADDITIONAL_THRESHOLD) &&
                conditionParameters.containsKey(ADDITIONAL_THRESHOLD_TYPE) &&
                isValidThresholdType(conditionParameters.get(THRESHOLD_TYPE).toString()) &&
                isValidThresholdType(conditionParameters.get(ADDITIONAL_THRESHOLD_TYPE).toString()) &&
                isValidStream(secondStream));
    }

    private boolean isValidCondOr(Map<String, Object> conditionParameters, AlertRuleStream secondStream) {
        return (isValidThresholdType(conditionParameters.get(THRESHOLD_TYPE).toString()) &&
                isValidStream(secondStream));
    }

    private boolean isValidCondition(AlertType alertType, Map<String, Object> conditionParameters, AlertRuleStream secondStream) {
        if (!conditionParameters.containsKey(TIME)) {
            LOG.debug("Missing condition parameter: {}", TIME);
            return false;
        }
        if (!conditionParameters.containsKey(THRESHOLD)) {
            LOG.debug("Missing condition parameter: {}", THRESHOLD);
            return false;
        }
        if (!conditionParameters.containsKey(THRESHOLD_TYPE)) {
            LOG.debug("Missing condition parameter: {}", THRESHOLD_TYPE);
            return false;
        }
        return switch (alertType) {
            case STATISTICAL -> isValidCondStatistical(conditionParameters);
            case THEN, AND -> isValidCondCorrelation(conditionParameters, secondStream);
            case OR -> isValidCondOr(conditionParameters, secondStream);
            default -> true;
        };
    }

    public boolean isValidRequest(AlertRuleRequest request){
        return (isValidTitle(request.getTitle()) &&
                isValidStream(request.getStream()) &&
                isValidCondition(request.getConditionType(), request.conditionParameters(), request.getSecondStream()));
    }

    public void checkIsValidRequest(AlertRuleRequest request) {
        if (!this.isValidRequest(request)) {
            LOG.error("Invalid alert rule request");
            throw new BadRequestException("Invalid alert rule request.");
        }
    }

    private List<FieldRule> getListFieldRule(List<StreamRule> listStreamRule) {
        List<FieldRule> listFieldRule = new ArrayList<>();
        for (StreamRule streamRule: listStreamRule) {
            if (streamRule.getInverted()) {
                listFieldRule.add(FieldRule.create(streamRule.getId(), streamRule.getField(), -streamRule.getType().toInteger(), streamRule.getValue()));
            } else {
                listFieldRule.add(FieldRule.create(streamRule.getId(), streamRule.getField(), streamRule.getType().toInteger(), streamRule.getValue()));
            }
        }
        return listFieldRule;
    }

    // TODO inline
    AlertRuleStream constructAlertRuleStream(Stream stream, TriggeringConditions conditions) {
        // TODO why is this check necessary?
        if (stream == null) {
            return null;
        }

        List<FieldRule> fieldRules = new ArrayList<>();
        if (conditions.pipeline() != null) {
            List<FieldRule> pipelineFieldRules = conditions.pipeline().fieldRules();
            Optional.ofNullable(pipelineFieldRules).ifPresent(fieldRules::addAll);
        }
        Optional.ofNullable(this.getListFieldRule(stream.getStreamRules())).ifPresent(fieldRules::addAll);
        return AlertRuleStream.create(stream.getId(), stream.getMatchingType(), fieldRules);
    }

    private String convertThresholdTypeToCorrelation(String thresholdType) {
        if (thresholdType.equals(THRESHOLD_TYPE_MORE)) {
            return "MORE";
        } else {
            return "LESS";
        }
    }

    private int accessThreshold(Map<String, Object> conditionParameter) {
        return (int) conditionParameter.get(THRESHOLD);
    }

    // TODO move method to AlertRuleUtils?
    // TODO instead of a String, the type could already be a com.airbus_cyber_security.graylog.events.processor.correlation.checks.OrderType
    EventProcessorConfig createCorrelationCondition(AlertType type, String streamID, String streamID2, Map<String, Object> conditionParameter) {
        OrderType messageOrder;
        if (type == AlertType.THEN) {
            messageOrder = OrderType.AFTER;
        } else {
            messageOrder = OrderType.ANY;
        }
        String thresholdType = convertThresholdTypeToCorrelation((String) conditionParameter.get(THRESHOLD_TYPE));
        String additionalThresholdType = convertThresholdTypeToCorrelation((String) conditionParameter.get(ADDITIONAL_THRESHOLD_TYPE));

        int threshold = this.accessThreshold(conditionParameter);

        long searchWithinMs = this.convertMinutesToMilliseconds(Long.parseLong(conditionParameter.get(TIME).toString()));
        long executeEveryMs = this.convertMinutesToMilliseconds(Long.parseLong(conditionParameter.get(GRACE).toString()));

        return CorrelationCountProcessorConfig.builder()
                .stream(streamID)
                .thresholdType(thresholdType)
                .threshold(threshold)
                .additionalStream(streamID2)
                .additionalThresholdType(additionalThresholdType)
                .additionalThreshold((int) conditionParameter.get(ADDITIONAL_THRESHOLD))
                .messagesOrder(messageOrder)
                .searchWithinMs(searchWithinMs)
                .executeEveryMs(executeEveryMs)
                // TODO CorrelationCountProcessorConfig.groupingFields should be of type List (or better just Collection/Iterable) rather than Set
                .groupingFields((List<String>) conditionParameter.get(GROUPING_FIELDS))
                .comment(Description.COMMENT_ALERT_WIZARD)
                .searchQuery("*")
                .build();
    }

    private Expression<Boolean> createExpressionFromNumberThreshold(String identifier, String thresholdType, int threshold) {
        Expr.NumberReference left = Expr.NumberReference.create(identifier);
        Expr.NumberValue right = Expr.NumberValue.create(threshold);
        switch (thresholdType) {
            case THRESHOLD_TYPE_MORE:
                return Expr.Greater.create(left, right);
            case THRESHOLD_TYPE_LESS:
                return Expr.Lesser.create(left, right);
            default:
                throw new BadRequestException("createExpressionFromNumberThreshold: unexpected threshold type " + thresholdType);
        }
    }

    public EventProcessorConfig createAggregationCondition(String streamIdentifier, Map<String, Object> conditionParameter) {
        List<String> groupByFields = (List<String>) conditionParameter.get(GROUPING_FIELDS);
        String distinctBy = (String) conditionParameter.get(DISTINCT_BY);

        Set<String> streams = ImmutableSet.of(streamIdentifier);
        // TODO extract method to parse searchWithinMs
        long searchWithinMs = this.convertMinutesToMilliseconds(Long.parseLong(conditionParameter.get(TIME).toString()));
        // TODO extract method to parse executeEveryMs
        long executeEveryMs = this.convertMinutesToMilliseconds(Long.parseLong(conditionParameter.get(GRACE).toString()));

        String thresholdType = (String) conditionParameter.get(THRESHOLD_TYPE);
        int threshold = this.accessThreshold(conditionParameter);

        String identifier = UUID.randomUUID().toString();
        AggregationSeries.Builder seriesBuilder = AggregationSeries.builder().id(identifier);

        if (distinctBy.isEmpty()) {
            seriesBuilder.function(AggregationFunction.COUNT);
        } else {
            seriesBuilder.function(AggregationFunction.CARD).field(distinctBy);
        }

        AggregationSeries series = seriesBuilder.build();

        Expression<Boolean> expression = createExpressionFromNumberThreshold(identifier, thresholdType, threshold);
        AggregationConditions conditions = AggregationConditions.builder()
                .expression(expression)
                .build();

        return AggregationEventProcessorConfig.builder()
                .query("")
                .streams(streams)
                .groupBy(groupByFields)
                .series(ImmutableList.of(series))
                .conditions(conditions)
                .executeEveryMs(executeEveryMs)
                .searchWithinMs(searchWithinMs)
                .build();
    }

    private AggregationFunction mapTypeToAggregationFunction(String type) {
        switch (type) {
            case "AVG":
                return AggregationFunction.AVG;
            case "MIN":
                return AggregationFunction.MIN;
            case "MAX":
                return AggregationFunction.MAX;
            case "SUM":
                return AggregationFunction.SUM;
            case "STDDEV":
                return AggregationFunction.STDDEV;
            case "CARD":
                return AggregationFunction.CARD;
            case "COUNT":
                return AggregationFunction.COUNT;
            case "SUMOFSQUARES":
                return AggregationFunction.SUMOFSQUARES;
            case "VARIANCE":
                return AggregationFunction.VARIANCE;
            default:
                throw new BadRequestException();
        }
    }

    private Expression<Boolean> createExpressionFromThreshold(String identifier, String thresholdType, int threshold) {
        Expr.NumberReference left = Expr.NumberReference.create(identifier);
        Expr.NumberValue right = Expr.NumberValue.create(threshold);
        switch (thresholdType) {
            case ">":
                return Expr.Greater.create(left, right);
            case ">=":
                return Expr.GreaterEqual.create(left, right);
            case "<":
                return Expr.Lesser.create(left, right);
            case "<=":
                return Expr.LesserEqual.create(left, right);
            case "==":
                return Expr.Equal.create(left, right);
            default:
                throw new BadRequestException();
        }
    }

    public EventProcessorConfig createStatisticalCondition(String streamID, Map<String, Object> conditionParameter) {
        String type = conditionParameter.get(TYPE).toString();
        LOG.debug("Begin Stat, type: {}", type);
        // TODO extract method to parse searchWithinMs
        long searchWithinMs = this.convertMinutesToMilliseconds(Long.parseLong(conditionParameter.get(TIME).toString()));
        // TODO extract method to parse executeEveryMs
        long executeEveryMs = this.convertMinutesToMilliseconds(Long.parseLong(conditionParameter.get(GRACE).toString()));

        int threshold = this.accessThreshold(conditionParameter);

        String identifier = UUID.randomUUID().toString();
        AggregationSeries serie = AggregationSeries.builder()
                .id(identifier)
                .function(mapTypeToAggregationFunction(type))
                .field(conditionParameter.get(FIELD).toString())
                .build();

        Expression<Boolean> expression = createExpressionFromThreshold(identifier,
                conditionParameter.get(THRESHOLD_TYPE).toString(),
                threshold);

        return AggregationEventProcessorConfig.builder()
                .query("")
                .streams(new HashSet<>(Collections.singleton(streamID)))
                .series(ImmutableList.of(serie))
                .groupBy(ImmutableList.of())
                .conditions(AggregationConditions.builder()
                        .expression(expression)
                        .build())
                .searchWithinMs(searchWithinMs)
                .executeEveryMs(executeEveryMs)
                .build();
    }

    public EventProcessorConfig createEventConfiguration(AlertType alertType, Map<String, Object> conditionParameter, String streamIdentifier) {
        if (alertType == AlertType.STATISTICAL) {
            return createStatisticalCondition(streamIdentifier, conditionParameter);
        } else {
            return createAggregationCondition(streamIdentifier, conditionParameter);
        }
    }
}
