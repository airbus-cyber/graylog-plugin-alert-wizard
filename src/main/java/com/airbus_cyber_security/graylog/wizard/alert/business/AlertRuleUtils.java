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

package com.airbus_cyber_security.graylog.wizard.alert.business;

import com.airbus_cyber_security.graylog.wizard.alert.model.AlertRuleStream;
import com.airbus_cyber_security.graylog.wizard.alert.model.FieldRule;
import com.airbus_cyber_security.graylog.events.notifications.types.LoggingNotificationConfig;
import com.airbus_cyber_security.graylog.events.processor.correlation.CorrelationCountProcessorConfig;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.requests.AlertRuleRequest;
import com.google.common.collect.Maps;
import org.graylog.events.conditions.Expr;
import org.graylog.events.conditions.Expression;
import org.graylog.events.processor.EventProcessorConfig;
import org.graylog.events.processor.aggregation.AggregationEventProcessorConfig;
import org.graylog.events.processor.aggregation.AggregationSeries;
import org.graylog2.plugin.streams.StreamRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

// TODO try to remove this class? What's its point? Part of it should be in rest? Only doing conversion...
public class AlertRuleUtils {

	private static final int MILLISECONDS_IN_A_MINUTE = 60 * 1000;
	private static final Logger LOG = LoggerFactory.getLogger(AlertRuleUtils.class);

	// TODO all these constants should be private => see where this leads us in term of code reorganisation (something like a conditionParametersParser?)
	//      indeed: should rather parse the conditionParameters as soon as it gets in the system, in order to get a tidy class with getters
	//      there is even a way to do it nicely with Jackson: see jackson-docs polymorphic type handling, jsonsubtypes
	public static final String FIELD = "field";
	public static final String TYPE = "type";
	public static final String GROUPING_FIELDS = "grouping_fields";
	public static final String DISTINCT_BY = "distinct_by";
	public static final String TIME = "time";
	public static final String GRACE = "grace";
	public static final String ADDITIONAL_THRESHOLD = "additional_threshold";
	public static final String ADDITIONAL_THRESHOLD_TYPE = "additional_threshold_type";
    public static final String THRESHOLD_TYPE = "threshold_type";
    private static final String THRESHOLD = "threshold";

	// TODO should try to make these constants private and group the code (more compact)
	public static final String THRESHOLD_TYPE_MORE = ">";
	public static final String THRESHOLD_TYPE_LESS = "<";

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

	// TODO should avoid these conversions by always working with ms (from the IHM down to the server)
	private long convertMillisecondsToMinutes(long value) {
		return value / MILLISECONDS_IN_A_MINUTE;
	}

	public long convertMinutesToMilliseconds(long value) {
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

	public Map<String, Object> getConditionParameters(EventProcessorConfig eventConfig) {
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
	
    public List<FieldRule> getListFieldRule(List<StreamRule> listStreamRule) {
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

	public int accessThreshold(Map<String, Object> conditionParameter) {
		return (int) conditionParameter.get(THRESHOLD);
	}

	private boolean isValidTitle(String title) {
		return !(title == null || title.isEmpty());
	}

	private boolean isValidStream(AlertRuleStream stream) {
		if (stream.getMatchingType().equals("AND") || stream.getMatchingType().equals("OR")) {
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
		return (conditionParameters.containsKey(AlertRuleUtils.ADDITIONAL_THRESHOLD) &&
				conditionParameters.containsKey(AlertRuleUtils.ADDITIONAL_THRESHOLD_TYPE) &&
				isValidThresholdType(conditionParameters.get(AlertRuleUtils.THRESHOLD_TYPE).toString()) &&
				isValidThresholdType(conditionParameters.get(AlertRuleUtils.ADDITIONAL_THRESHOLD_TYPE).toString()) &&
				isValidStream(secondStream));
	}

	private boolean isValidCondOr(Map<String, Object> conditionParameters, AlertRuleStream secondStream) {
		return (isValidThresholdType(conditionParameters.get(AlertRuleUtils.THRESHOLD_TYPE).toString()) &&
				isValidStream(secondStream));
	}

	private boolean isValidCondition(String conditionType, Map<String, Object> conditionParameters, AlertRuleStream secondStream) {
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
		if (conditionType.equals("STATISTICAL")) {
			return isValidCondStatistical(conditionParameters);
		} else if (conditionType.equals("THEN") || conditionType.equals("AND")) {
			return isValidCondCorrelation(conditionParameters, secondStream);
		} else if (conditionType.equals("OR")) {
			return isValidCondOr(conditionParameters, secondStream);
		}
		return true;
	}

	public boolean isValidRequest(AlertRuleRequest request){
		return (isValidTitle(request.getTitle()) &&
				isValidStream(request.getStream()) &&
				isValidCondition(request.getConditionType(), request.conditionParameters(), request.getSecondStream()));
	}
}
