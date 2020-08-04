package com.airbus_cyber_security.graylog;

import com.airbus_cyber_security.graylog.alert.utilities.AlertRuleUtils;
import com.google.common.collect.Maps;
import org.graylog2.alerts.AbstractAlertCondition;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AlertRuleUtilsTest {
	
	private static final String STATISTICAL = "STATISTICAL";
	private static final String GROUP_DISTINCT = "GROUP_DISTINCT";
	private static final String THEN = "THEN";
	private static final String AND = "AND";
	private static final String OR = "OR";
	private static final String EMPTY = "";
	
	private static final String TYPE_LOGGING_ALERT = "com.airbus_cyber_security.graylog.LoggingAlert";
	private static final String TYPE_CORRELATION = "com.airbus_cyber_security.graylog.CorrelationCount";
	private static final String TYPE_AGGREGATION = "com.airbus_cyber_security.graylog.AggregationCount";
	
	public static final String GROUPING_FIELDS = "grouping_fields";
	public static final String DISTINCTION_FIELDS = "distinction_fields";
	public static final String TIME = "time";
	public static final String GRACE = "grace";
	public static final String BACKLOG = "backlog";
	public static final String ADDITIONAL_STREAM = "additional_stream";
	public static final String ADDITIONAL_THRESHOLD = "additional_threshold";
	public static final String ADDITIONAL_THRESHOLD_TYPE = "additional_threshold_type";
	public static final String MESSAGES_ORDER = "messages_order";
    public static final String THRESHOLD_TYPE = "threshold_type";
    public static final String THRESHOLD = "threshold";
    public static final String MAIN_THRESHOLD_TYPE = "main_threshold_type";
    public static final String MAIN_THRESHOLD = "main_threshold";
    public static final String SEVERITY = "severity";
    public static final String CONTENT = "content";
    public static final String SPLIT_FIELDS = "split_fields";
    public static final String AGGREGATION_TIME = "aggregation_time";
    public static final String LIMIT_OVERFLOW = "limit_overflow";
    public static final String COMMENT = "comment";
    public static final String TYPE = "type";
    public static final String FIELD = "field";
    public static final String REPEAT_NOTIFICATION = "repeat_notifications";
    
	@Test
	public void testGetconditionType() {
		AlertRuleUtils alertRuleUtils = new AlertRuleUtils();
		assertEquals(AbstractAlertCondition.Type.FIELD_VALUE.toString(), alertRuleUtils.getGraylogConditionType(STATISTICAL));
		assertEquals(TYPE_AGGREGATION, alertRuleUtils.getGraylogConditionType(GROUP_DISTINCT));
		assertEquals(TYPE_CORRELATION, alertRuleUtils.getGraylogConditionType(THEN));
		assertEquals(TYPE_CORRELATION, alertRuleUtils.getGraylogConditionType(AND));
		assertEquals(TYPE_AGGREGATION, alertRuleUtils.getGraylogConditionType(OR));
		assertEquals(TYPE_AGGREGATION, alertRuleUtils.getGraylogConditionType(EMPTY));
	}
	
	@Test
	public void testGetConditionParametersStatistical() {
		
		String streamID = "001";
		String alertRuleCondType = STATISTICAL;
		Map<String, Object> alertRuleCondParameters = Maps.newHashMap();
		alertRuleCondParameters.put(TIME, 6);
		alertRuleCondParameters.put(THRESHOLD, 3);
		alertRuleCondParameters.put(THRESHOLD_TYPE, "MORE");
		alertRuleCondParameters.put(TYPE, "MEAN");
    	alertRuleCondParameters.put(FIELD, "level");
    		
    	Map<String, Object> condParametersStat = Maps.newHashMap();
    	condParametersStat.put(GRACE, 0);
    	condParametersStat.put(BACKLOG, 1000);
    	condParametersStat.put(TIME, 6);
    	condParametersStat.put(THRESHOLD, 3);
    	condParametersStat.put(THRESHOLD_TYPE, "MORE");
    	condParametersStat.put(TYPE, "MEAN");
    	condParametersStat.put(FIELD, "level");
    	condParametersStat.put(REPEAT_NOTIFICATION, false);
    	
		AlertRuleUtils alertRuleUtils = new AlertRuleUtils();
		
		Map<String, Object> condParameters = alertRuleUtils.getConditionParameters(streamID, alertRuleCondType, alertRuleCondParameters);
	
		assertTrue(condParameters.equals(condParametersStat));
	}
	
	@Test
	public void testGetConditionParametersGroupDistinct() {
		
		String streamID = "001";
		String alertRuleCondType = GROUP_DISTINCT;
		Map<String, Object> alertRuleCondParameters = Maps.newHashMap();
		alertRuleCondParameters.put(TIME, 6);
		alertRuleCondParameters.put(THRESHOLD, 3);
		alertRuleCondParameters.put(THRESHOLD_TYPE, "MORE");
		alertRuleCondParameters.put(GROUPING_FIELDS,  "ip_src");
		alertRuleCondParameters.put(DISTINCTION_FIELDS,  "ip_dst");
    		
    	Map<String, Object> condParametersGD = Maps.newHashMap();
    	condParametersGD.put(GRACE, 0);
    	condParametersGD.put(BACKLOG, 1000);
    	condParametersGD.put(TIME, 6);
    	condParametersGD.put(THRESHOLD, 3);
    	condParametersGD.put(THRESHOLD_TYPE, "MORE");
    	condParametersGD.put(GROUPING_FIELDS,  "ip_src");
    	condParametersGD.put(DISTINCTION_FIELDS,  "ip_dst");
    	condParametersGD.put(COMMENT,  "Generated by the alert wizard");
    	condParametersGD.put(REPEAT_NOTIFICATION, false);
    	
		AlertRuleUtils alertRuleUtils = new AlertRuleUtils();
		
		Map<String, Object> condParameters = alertRuleUtils.getConditionParameters(streamID, alertRuleCondType, alertRuleCondParameters);
	
		assertTrue(condParameters.equals(condParametersGD));
	}
	
	@Test
	public void testGetConditionParametersThen() {
		
		String streamID = "001";
		String alertRuleCondType = THEN;
		Map<String, Object> alertRuleCondParameters = Maps.newHashMap();
		alertRuleCondParameters.put(TIME, 6);
		alertRuleCondParameters.put(THRESHOLD, 3);
		alertRuleCondParameters.put(THRESHOLD_TYPE, "MORE");
		alertRuleCondParameters.put(ADDITIONAL_STREAM, streamID);
		alertRuleCondParameters.put(MESSAGES_ORDER, "AFTER");
		alertRuleCondParameters.put(ADDITIONAL_THRESHOLD, 4);
		alertRuleCondParameters.put(ADDITIONAL_THRESHOLD_TYPE, "LESS");
		alertRuleCondParameters.put(GROUPING_FIELDS, "ip_src");
    		
    	Map<String, Object> condParametersThen = Maps.newHashMap();
    	condParametersThen.put(GRACE, 0);
    	condParametersThen.put(BACKLOG, 1000);
    	condParametersThen.put(TIME, 6);
    	condParametersThen.put(ADDITIONAL_STREAM, streamID);
    	condParametersThen.put(MESSAGES_ORDER, "AFTER");
    	condParametersThen.put(MAIN_THRESHOLD, 3);
    	condParametersThen.put(MAIN_THRESHOLD_TYPE, "MORE");
    	condParametersThen.put(ADDITIONAL_THRESHOLD, 4);
    	condParametersThen.put(ADDITIONAL_THRESHOLD_TYPE, "LESS");
    	condParametersThen.put(GROUPING_FIELDS, "ip_src");
    	condParametersThen.put(COMMENT,  "Generated by the alert wizard");
    	condParametersThen.put(REPEAT_NOTIFICATION, false);

		AlertRuleUtils alertRuleUtils = new AlertRuleUtils();
		
		Map<String, Object> condParameters = alertRuleUtils.getConditionParameters(streamID, alertRuleCondType, alertRuleCondParameters);
	
		assertTrue(condParameters.equals(condParametersThen));
	}
	
	@Test
	public void testGetConditionParametersAnd() {
		
		String streamID = "001";
		String alertRuleCondType = AND;
		Map<String, Object> alertRuleCondParameters = Maps.newHashMap();
		alertRuleCondParameters.put(TIME, 6);
		alertRuleCondParameters.put(THRESHOLD, 3);
		alertRuleCondParameters.put(THRESHOLD_TYPE, "MORE");
		alertRuleCondParameters.put(ADDITIONAL_STREAM, streamID);
		alertRuleCondParameters.put(MESSAGES_ORDER, "ANY");
		alertRuleCondParameters.put(ADDITIONAL_THRESHOLD, 4);
		alertRuleCondParameters.put(ADDITIONAL_THRESHOLD_TYPE, "LESS");
		alertRuleCondParameters.put(GROUPING_FIELDS, "ip_src");
    		
    		
    	Map<String, Object> condParametersAnd = Maps.newHashMap();
    	condParametersAnd.put(GRACE, 0);
    	condParametersAnd.put(BACKLOG, 1000);
    	condParametersAnd.put(TIME, 6);
    	condParametersAnd.put(ADDITIONAL_STREAM, streamID);
    	condParametersAnd.put(MESSAGES_ORDER, "ANY");
    	condParametersAnd.put(MAIN_THRESHOLD, 3);
    	condParametersAnd.put(MAIN_THRESHOLD_TYPE, "MORE");
    	condParametersAnd.put(ADDITIONAL_THRESHOLD, 4);
    	condParametersAnd.put(ADDITIONAL_THRESHOLD_TYPE, "LESS");
    	condParametersAnd.put(GROUPING_FIELDS, "ip_src");
    	condParametersAnd.put(COMMENT,  "Generated by the alert wizard");
    	condParametersAnd.put(REPEAT_NOTIFICATION, false);

		AlertRuleUtils alertRuleUtils = new AlertRuleUtils();
		
		Map<String, Object> condParameters = alertRuleUtils.getConditionParameters(streamID, alertRuleCondType, alertRuleCondParameters);
	
		assertTrue(condParameters.equals(condParametersAnd));
	}
	
	@Test
	public void testGetConditionParametersOr() {
		
		String streamID = "001";
		String alertRuleCondType = OR;
		Map<String, Object> alertRuleCondParameters = Maps.newHashMap();
		alertRuleCondParameters.put(TIME, 6);
		alertRuleCondParameters.put(THRESHOLD, 3);
		alertRuleCondParameters.put(THRESHOLD_TYPE, "MORE");
    		
    	Map<String, Object> condParametersOr = Maps.newHashMap();
    	condParametersOr.put(GRACE, 0);
    	condParametersOr.put(BACKLOG, 1000);
    	condParametersOr.put(TIME, 6);
    	condParametersOr.put(THRESHOLD, 3);
    	condParametersOr.put(THRESHOLD_TYPE, "MORE");
    	condParametersOr.put(REPEAT_NOTIFICATION, false);

		AlertRuleUtils alertRuleUtils = new AlertRuleUtils();
		
		Map<String, Object> condParameters = alertRuleUtils.getConditionParameters(streamID, alertRuleCondType, alertRuleCondParameters);
	
		assertTrue(condParameters.equals(condParametersOr));
	}
	
	@Test
	public void testGetConditionParametersEmpty() {
		
		String streamID = "001";
		String alertRuleCondType = EMPTY;
		Map<String, Object> alertRuleCondParameters = Maps.newHashMap();
		alertRuleCondParameters.put(TIME, 6);
		alertRuleCondParameters.put(THRESHOLD, 3);
		alertRuleCondParameters.put(THRESHOLD_TYPE, "MORE");
    		
    	Map<String, Object> condParametersMsgCount = Maps.newHashMap();
    	condParametersMsgCount.put(GRACE, 0);
    	condParametersMsgCount.put(BACKLOG, 1000);
    	condParametersMsgCount.put(TIME, 6);
    	condParametersMsgCount.put(THRESHOLD, 3);
    	condParametersMsgCount.put(THRESHOLD_TYPE, "MORE");
    	condParametersMsgCount.put(REPEAT_NOTIFICATION, false);

		AlertRuleUtils alertRuleUtils = new AlertRuleUtils();
		
		Map<String, Object> condParameters = alertRuleUtils.getConditionParameters(streamID, alertRuleCondType, alertRuleCondParameters);
	
		assertTrue(condParameters.equals(condParametersMsgCount));
	}
}
