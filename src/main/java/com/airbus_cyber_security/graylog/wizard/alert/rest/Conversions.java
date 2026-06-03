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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.graylog.events.conditions.Expr;
import org.graylog.events.conditions.Expression;
import org.graylog.events.processor.EventProcessorConfig;
import org.graylog.events.processor.aggregation.AggregationConditions;
import org.graylog.events.processor.aggregation.AggregationEventProcessorConfig;
import org.graylog.plugins.views.search.searchtypes.pivot.HasField;
import org.graylog.plugins.views.search.searchtypes.pivot.SeriesSpec;
import org.graylog.plugins.views.search.searchtypes.pivot.series.Average;
import org.graylog.plugins.views.search.searchtypes.pivot.series.Cardinality;
import org.graylog.plugins.views.search.searchtypes.pivot.series.Count;
import org.graylog.plugins.views.search.searchtypes.pivot.series.HasOptionalField;
import org.graylog.plugins.views.search.searchtypes.pivot.series.Max;
import org.graylog.plugins.views.search.searchtypes.pivot.series.Min;
import org.graylog.plugins.views.search.searchtypes.pivot.series.SeriesSpecBuilder;
import org.graylog.plugins.views.search.searchtypes.pivot.series.StdDev;
import org.graylog.plugins.views.search.searchtypes.pivot.series.Sum;
import org.graylog.plugins.views.search.searchtypes.pivot.series.SumOfSquares;
import org.graylog.plugins.views.search.searchtypes.pivot.series.Variance;
import org.graylog2.plugin.streams.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.airbus_cyber_security.graylog.events.processor.correlation.CorrelationCountProcessorConfig;
import com.airbus_cyber_security.graylog.events.processor.correlation.checks.OrderType;
import com.airbus_cyber_security.graylog.wizard.alert.business.FieldRulesUtilities;
import com.airbus_cyber_security.graylog.wizard.alert.model.AlertConditionParameters;
import com.airbus_cyber_security.graylog.wizard.alert.model.AlertType;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.requests.AlertRuleRequest;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.requests.ImportAlertRuleRequest;
import com.airbus_cyber_security.graylog.wizard.alert.utilities.ConditionParametersAdapter;
import com.airbus_cyber_security.graylog.wizard.config.rest.AlertWizardConfiguration;
import com.airbus_cyber_security.graylog.wizard.config.rest.AlertWizardConfigurationService;
import com.airbus_cyber_security.graylog.wizard.config.rest.DefaultValues;
import com.airbus_cyber_security.graylog.wizard.database.Description;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;

/**
 * Converts from business model to rest model and vice versa
 */
// TODO should try to find a way to split this class. Too long
public class Conversions {

	private static final Logger LOG = LoggerFactory.getLogger(Conversions.class);

	private static final int MILLISECONDS_IN_A_MINUTE = 60 * 1000;

	// TODO should rather parse the conditionParameters as soon as it gets in the
	// system, in order to get a tidy class with getters
	// there is even a way to do it nicely with Jackson: see jackson-docs
	// polymorphic type handling, jsonsubtypes
	private final Validator validator;
	private final AlertWizardConfigurationService configurationService;

	@Inject
	public Conversions(FieldRulesUtilities fieldRulesUtilities, AlertWizardConfigurationService configurationService) {
		this.validator = new Validator(fieldRulesUtilities);
		this.configurationService = configurationService;
	}

	// TODO should avoid these conversions by always working with ms (from the IHM
	// down to the server)
	private long convertMillisecondsToMinutes(long value) {
		return value / MILLISECONDS_IN_A_MINUTE;
	}

	public long convertMinutesToMilliseconds(long value) {
		return value * MILLISECONDS_IN_A_MINUTE;
	}

	// TODO should introduce constants for MORE and LESS
	private String convertCorrelationCountThresholdType(String thresholdType) {
		if (thresholdType.equals("MORE")) {
			return AlertConditionParameters.THRESHOLD_TYPE_MORE;
		} else {
			return AlertConditionParameters.THRESHOLD_TYPE_LESS;
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
			parametersCondition.put(AlertConditionParameters.THRESHOLD, correlationConfig.threshold());
			String thresholdType = convertCorrelationCountThresholdType(correlationConfig.thresholdType());
			String additionalThresholdType = convertCorrelationCountThresholdType(
					correlationConfig.additionalThresholdType());
			parametersCondition.put(AlertConditionParameters.THRESHOLD_TYPE, thresholdType);
			parametersCondition.put(AlertConditionParameters.ADDITIONAL_THRESHOLD,
					correlationConfig.additionalThreshold());
			parametersCondition.put(AlertConditionParameters.ADDITIONAL_THRESHOLD_TYPE, additionalThresholdType);
			parametersCondition.put(AlertConditionParameters.TIME,
					this.convertMillisecondsToMinutes(correlationConfig.searchWithinMs()));
			parametersCondition.put(AlertConditionParameters.GROUPING_FIELDS, correlationConfig.groupingFields());
			parametersCondition.put(AlertConditionParameters.GRACE,
					this.convertMillisecondsToMinutes(correlationConfig.executeEveryMs()));
			parametersCondition.put(AlertConditionParameters.SEARCH_QUERY, correlationConfig.searchQuery());
			parametersCondition.put(AlertConditionParameters.ADDITIONAL_SEARCH_QUERY,
					correlationConfig.additionalSearchQuery());
			break;
		case "aggregation-v1":
			AggregationEventProcessorConfig aggregationConfig = (AggregationEventProcessorConfig) eventConfig;
			parametersCondition.put(AlertConditionParameters.TIME,
					this.convertMillisecondsToMinutes(aggregationConfig.searchWithinMs()));
			parametersCondition.put(AlertConditionParameters.GRACE,
					this.convertMillisecondsToMinutes(aggregationConfig.executeEveryMs()));
			parametersCondition.put(AlertConditionParameters.SEARCH_QUERY, aggregationConfig.query());
			parametersCondition.put(AlertConditionParameters.THRESHOLD,
					convertThreshold(aggregationConfig.conditions().get().expression().get()));
			parametersCondition.put(AlertConditionParameters.THRESHOLD_TYPE,
					aggregationConfig.conditions().get().expression().get().expr());
			SeriesSpec series = aggregationConfig.series().get(0);
			parametersCondition.put(AlertConditionParameters.TYPE, series.type().toUpperCase(Locale.ENGLISH));
			String distinctBy = "";
			Optional<String> seriesField = Optional.empty();
			if (series instanceof HasField) {
				seriesField = Optional.of(((HasField) series).field());
			} else if (series instanceof HasOptionalField) {
				seriesField = ((HasOptionalField) series).field();
			}

			if (seriesField.isPresent()) {
				distinctBy = seriesField.get();
				parametersCondition.put(AlertConditionParameters.FIELD, distinctBy);
			}
			parametersCondition.put(AlertConditionParameters.GROUPING_FIELDS, aggregationConfig.groupBy());
			parametersCondition.put(AlertConditionParameters.DISTINCT_BY, distinctBy);
			break;
		default:
			throw new UnsupportedOperationException();
		}
		return parametersCondition;
	}

	public void checkIsValidRequest(AlertRuleRequest request) {
		this.validator.checkIsValidRequest(request);
	}

	public void checkIsValidImportRequest(ImportAlertRuleRequest request) {
		this.validator.checkIsValidImportRequest(request);
	}

	private String convertThresholdTypeToCorrelation(String thresholdType) {
		if (thresholdType.equals(AlertConditionParameters.THRESHOLD_TYPE_MORE)) {
			return "MORE";
		} else {
			return "LESS";
		}
	}

	// TODO move method to AlertRuleUtils?
	// TODO instead of a String, the type could already be a
	// com.airbus_cyber_security.graylog.events.processor.correlation.checks.OrderType
	EventProcessorConfig createCorrelationCondition(AlertType type, String streamIdentifier, String streamIdentifier2,
			Map<String, Object> conditionParameter) {
		ConditionParametersAdapter conditionParametersAdapter = new ConditionParametersAdapter(conditionParameter);
		if (streamIdentifier == null) {
			streamIdentifier = Stream.DEFAULT_STREAM_ID;
		}
		if (streamIdentifier2 == null) {
			streamIdentifier2 = Stream.DEFAULT_STREAM_ID;
		}
		OrderType messageOrder;
		if (type == AlertType.THEN) {
			messageOrder = OrderType.AFTER;
		} else {
			messageOrder = OrderType.ANY;
		}
		String thresholdType = convertThresholdTypeToCorrelation(conditionParametersAdapter.getThresholdType());
		String additionalThresholdType = convertThresholdTypeToCorrelation(
				conditionParametersAdapter.getAdditionalThresholdType());
		String searchQuery = conditionParametersAdapter.getSearchQuery();
		String additionalSearchQuery = conditionParametersAdapter.getAdditionalSearchQuery();

		int threshold = conditionParametersAdapter.getThreshold();
		int additionalThreshold = conditionParametersAdapter.getAdditionalThreshold();

		long searchWithinMs = this.convertMinutesToMilliseconds(conditionParametersAdapter.getTime());
		

		AlertWizardConfiguration pluginConfiguration = this.configurationService.getConfiguration();
		DefaultValues defaultValues = pluginConfiguration.accessDefaultValues();
		long graceInMinutes = null == conditionParametersAdapter.getGrace() ? defaultValues.getGrace() : conditionParametersAdapter.getGrace();
		long executeEveryMs = this.convertMinutesToMilliseconds(graceInMinutes);

		return CorrelationCountProcessorConfig.builder()
				.stream(streamIdentifier)
				.searchQuery(searchQuery)
				.thresholdType(thresholdType)
				.threshold(threshold)
				.additionalStream(streamIdentifier2)
				.additionalSearchQuery(additionalSearchQuery)
				.additionalThresholdType(additionalThresholdType)
				.additionalThreshold(additionalThreshold)
				.messagesOrder(messageOrder)
				.searchWithinMs(searchWithinMs)
				.executeEveryMs(executeEveryMs)
				// TODO CorrelationCountProcessorConfig.groupingFields should be of type List
				// (or better just Collection/Iterable) rather than Set
				.groupingFields(conditionParametersAdapter.getGroupingFields())
				.comment(Description.COMMENT_ALERT_WIZARD)
				.build();
	}

	private Expression<Boolean> createExpressionFromNumberThreshold(String identifier, String thresholdType,
			int threshold) {
		Expr.NumberReference left = Expr.NumberReference.create(identifier);
		Expr.NumberValue right = Expr.NumberValue.create(threshold);
		switch (thresholdType) {
		case AlertConditionParameters.THRESHOLD_TYPE_MORE:
			return Expr.Greater.create(left, right);
		case AlertConditionParameters.THRESHOLD_TYPE_LESS:
			return Expr.Lesser.create(left, right);
		default:
			throw new BadRequestException(
					"createExpressionFromNumberThreshold: unexpected threshold type " + thresholdType);
		}
	}

	public EventProcessorConfig createAggregationCondition(String streamIdentifier, Map<String, Object> conditionParameter) {
		ConditionParametersAdapter conditionParametersAdapter = new ConditionParametersAdapter(conditionParameter);
		List<String> groupByFields = conditionParametersAdapter.getGroupingFields();
		String distinctBy = conditionParametersAdapter.getDistinctBy();

		// TODO extract method to parse searchWithinMs
		long searchWithinMs = this.convertMinutesToMilliseconds(conditionParametersAdapter.getTime());
		// TODO extract method to parse executeEveryMs
		long executeEveryMs = this.convertMinutesToMilliseconds(conditionParametersAdapter.getGrace());

		String thresholdType = conditionParametersAdapter.getThresholdType();
		int threshold = conditionParametersAdapter.getThreshold();

		String identifier = UUID.randomUUID().toString();
		SeriesSpecBuilder<?, ?> seriesBuilder = createSeriesBuilder(identifier, distinctBy);

		SeriesSpec series = (SeriesSpec) seriesBuilder.build();

		Expression<Boolean> expression = createExpressionFromNumberThreshold(identifier, thresholdType, threshold);
		AggregationConditions conditions = AggregationConditions.builder().expression(expression).build();

		String searchQuery = conditionParametersAdapter.getSearchQuery();
		Set<String> streams = getStreamsParameterFromOutputStream(streamIdentifier);

		return AggregationEventProcessorConfig.builder().query(searchQuery)
				.streams(streams)
				.groupBy(groupByFields)
				.series(ImmutableList.of(series))
				.conditions(conditions)
				.executeEveryMs(executeEveryMs)
				.searchWithinMs(searchWithinMs)
				.build();
	}

	public EventProcessorConfig createAdditionalAggregationCondition(String streamIdentifier,
			Map<String, Object> conditionParameter) {
		// Create the additional condition the same way as the first condition
		Map<String, Object> additionalConditionParameter = new HashMap<>(conditionParameter);
		ConditionParametersAdapter conditionParametersAdapter = new ConditionParametersAdapter(conditionParameter);
		additionalConditionParameter.put(AlertConditionParameters.SEARCH_QUERY, conditionParametersAdapter.getAdditionalSearchQuery());
		additionalConditionParameter.put(AlertConditionParameters.THRESHOLD, conditionParametersAdapter.getAdditionalThreshold());
		additionalConditionParameter.put(AlertConditionParameters.THRESHOLD_TYPE, conditionParametersAdapter.getAdditionalThresholdType());
		return this.createAggregationCondition(streamIdentifier, additionalConditionParameter);
	}

	private SeriesSpecBuilder<?, ?> createSeriesBuilder(String identifier, String distinctBy) {
		if (distinctBy == null || distinctBy.isEmpty()) {
			return Count.builder().id(identifier);
		}
		return Cardinality.builder().id(identifier).field(distinctBy);
	}

	private SeriesSpec createSeriesSpec(String type, String identifier, String field) {
		switch (type) {
		case "AVG":
			return Average.builder().id(identifier).field(field).build();
		case "MIN":
			return Min.builder().id(identifier).field(field).build();
		case "MAX":
			return Max.builder().id(identifier).field(field).build();
		case "SUM":
			return Sum.builder().id(identifier).field(field).build();
		case "STDDEV":
			return StdDev.builder().id(identifier).field(field).build();
		case "CARD":
			return Cardinality.builder().id(identifier).field(field).build();
		case "COUNT":
			return Count.builder().id(identifier).field(field).build();
		case "SUMOFSQUARES":
			return SumOfSquares.builder().id(identifier).field(field).build();
		case "VARIANCE":
			return Variance.builder().id(identifier).field(field).build();
		default:
			LOG.error("Unexpected series type: {}", type);
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

	public EventProcessorConfig createStatisticalCondition(String streamIdentifier,
			Map<String, Object> conditionParameter) {
		ConditionParametersAdapter conditionParametersAdapter = new ConditionParametersAdapter(conditionParameter);
		String type = conditionParametersAdapter.getType();
		// TODO extract method to parse searchWithinMs
		long searchWithinMs = this.convertMinutesToMilliseconds(conditionParametersAdapter.getTime());
		// TODO extract method to parse executeEveryMs
		long executeEveryMs = this.convertMinutesToMilliseconds(conditionParametersAdapter.getGrace());

		int threshold = conditionParametersAdapter.getThreshold();

		String identifier = UUID.randomUUID().toString();
		SeriesSpec series = createSeriesSpec(type, identifier, conditionParametersAdapter.getField());

		Expression<Boolean> expression = createExpressionFromThreshold(identifier,
				conditionParametersAdapter.getThresholdType(), threshold);

		String searchQuery = conditionParametersAdapter.getSearchQuery();
		Set<String> streams = getStreamsParameterFromOutputStream(streamIdentifier);

		return AggregationEventProcessorConfig.builder().query(searchQuery).streams(streams)
				.series(ImmutableList.of(series)).groupBy(ImmutableList.of())
				.conditions(AggregationConditions.builder().expression(expression).build())
				.searchWithinMs(searchWithinMs).executeEveryMs(executeEveryMs).build();
	}

	public EventProcessorConfig createEventConfiguration(AlertType alertType, Map<String, Object> conditionParameter,
			String streamIdentifier) {
		if (alertType == AlertType.STATISTICAL) {
			return createStatisticalCondition(streamIdentifier, conditionParameter);
		} else {
			return createAggregationCondition(streamIdentifier, conditionParameter);
		}
	}

	private Set<String> getStreamsParameterFromOutputStream(String streamIdentifier) {
		if (streamIdentifier == null) {
			return Collections.emptySet();
		}
		return ImmutableSet.of(streamIdentifier);
	}
}
