package com.airbus_cyber_security.graylog;

import org.graylog2.alarmcallbacks.AlarmCallbackConfiguration;
import org.graylog2.alarmcallbacks.AlarmCallbackConfigurationService;
import org.graylog2.alarmcallbacks.AlarmCallbackFactory;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;

import org.graylog2.alerts.Alert;
import org.graylog2.alerts.AlertService;
import org.graylog2.database.NotFoundException;

import com.airbus_cyber_security.graylog.database.MongoDBServiceTest;
import com.google.common.collect.Maps;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;

import org.graylog2.events.ClusterEventBus;
import org.graylog2.indexer.IndexSet;
import org.graylog2.indexer.IndexSetRegistry;
import org.graylog2.indexer.indexset.IndexSetConfig;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.database.ValidationException;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugin.streams.Stream.MatchingType;
import org.graylog2.streams.StreamRuleService;
import org.graylog2.streams.StreamService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import com.airbus_cyber_security.graylog.alert.AlertRuleServiceImpl;
import com.airbus_cyber_security.graylog.alert.bundles.ExportAlertRuleRequest;
import com.airbus_cyber_security.graylog.alert.bundles.ExportAlertRule;
import com.airbus_cyber_security.graylog.alert.rest.AlertRuleResource;
import com.airbus_cyber_security.graylog.alert.rest.models.responses.GetAlertRule;
import com.airbus_cyber_security.graylog.alert.rest.models.responses.GetDataAlertRule;
import com.airbus_cyber_security.graylog.alert.rest.models.responses.GetListAlertRule;
import com.airbus_cyber_security.graylog.alert.rest.models.responses.GetListDataAlertRule;

import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.validation.Validator;

public class AlertRuleResourceTest extends MongoDBServiceTest{
	 
    @Mock
    private StreamRuleService streamRuleService;
    @Mock
    private AlarmCallbackFactory alarmCallbackFactory;
    @Mock
    private ClusterConfigService clusterConfigService;
    @Mock
    private Validator validator;
    
    private StreamService streamService;
	private AlertRuleResource alertRuleResource;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    @Before
    public void setUpService() throws Exception {
    	IndexSetRegistry indexSetRegistry = mock(IndexSetRegistry.class);
    	IndexSet indexSet = mock(IndexSet.class);
    	IndexSetConfig indexSetConfig = mock(IndexSetConfig.class);
    	ClusterEventBus clusterEventBus = mock(ClusterEventBus.class);
    	AlarmCallbackConfigurationService alarmCallbackConfigurationService  = mock(AlarmCallbackConfigurationService.class);
    	
        when(indexSetRegistry.getDefault()).thenReturn(indexSet);
        when(indexSet.getConfig()).thenReturn(indexSetConfig);
        when(indexSetConfig.id()).thenReturn("001");
        
        streamService = mock(StreamService.class);
    	final Stream stream = mock(Stream.class);
    	when(stream.getId()).thenReturn("5bc894ded9e3770323a780a7");
    	when(streamService.load(anyString())).thenReturn(stream);
    	when(stream.getMatchingType()).thenReturn(MatchingType.AND);

    	
    	final AlertCondition alertCondition = mock(AlertCondition.class);
    	when(streamService.getAlertCondition(stream, "657386c8-b1de-4187-82a6-28087cfbd05c")).thenReturn(alertCondition); 
    	when(alertCondition.getParameters()).thenReturn(Maps.newHashMap());
    	when(alertCondition.getTitle()).thenReturn("Test Count");
    	
    	AlarmCallbackConfiguration callbackConfiguration  = mock(AlarmCallbackConfiguration.class);
    	HashMap<String, Object> confCallback = new HashMap<>();
    	confCallback.put("severity", "Low");
    	when(callbackConfiguration.getConfiguration()).thenReturn(confCallback);
    	when(alarmCallbackConfigurationService.load("5bc894ded9e3770323a780a9")).thenReturn(callbackConfiguration);
    	
        AlertService alertService = mock(AlertService.class);
        when(alertService.loadRecentOfStream(eq("5bc894ded9e3770323a780a7"), any(DateTime.class), eq(999))).thenReturn(new ArrayList<Alert>());
    	
        AlertRuleServiceImpl alertRuleService = new AlertRuleServiceImpl(mongoRule.getMongoConnection(), mapperProvider, validator);
    	this.alertRuleResource = new AlertRuleResource(alertRuleService, streamService, streamRuleService, clusterEventBus, indexSetRegistry, 
    										alertService, alarmCallbackConfigurationService, alarmCallbackFactory, clusterConfigService);
    }
    
    @Test
    @UsingDataSet(locations = "alertWizardSingleRuleCount.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testGetAlertRuleValid() throws UnsupportedEncodingException, NotFoundException {
    	GetAlertRule alertRule = alertRuleResource.get("Test Count");
    	
    	assertNotNull("Returned alertRule should not be null", alertRule);
    }
    
    @Test
    @UsingDataSet(locations = "alertWizardSingleRuleCount.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testDeleteAlertRule() throws UnsupportedEncodingException, NotFoundException {
    	alertRuleResource.delete("Test Count");
    }
    
    @Test
    @UsingDataSet(locations = "alertWizardSingleRuleCount.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testGetListAlertRuleOneRule() throws UnsupportedEncodingException, NotFoundException {
    	GetListAlertRule listAlertRule = alertRuleResource.list();
    	
    	assertNotNull("Returned list should not be null", listAlertRule);
    	assertEquals("There should be one alert rule in the collection", 1, listAlertRule.getAlerts().size());
    }
    
    @Test
    @UsingDataSet(locations = "alertWizardTwoRules.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testGetListAlertRuleMultipleRule() throws UnsupportedEncodingException, NotFoundException {
    	GetListAlertRule listAlertRule = alertRuleResource.list();
    	
    	assertNotNull("Returned list should not be null", listAlertRule);
    	assertEquals("There should be two alert rule in the collection", 2, listAlertRule.getAlerts().size());
    }
    
    @Test
    @UsingDataSet(locations = "alertWizardSingleRuleCount.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testGetDataAlertRule() throws UnsupportedEncodingException, NotFoundException {
    	GetDataAlertRule dataAlertRule = alertRuleResource.getData("Test Count");
    	
    	assertNotNull("Returned data alert rule should not be null", dataAlertRule);
    	assertEquals("Test Count", dataAlertRule.getTitle());
    	assertEquals("Test Count", dataAlertRule.getTitleCondition());
    	assertEquals("657386c8-b1de-4187-82a6-28087cfbd05c", dataAlertRule.getConditionID());
    	assertEquals("5bc894ded9e3770323a780a9", dataAlertRule.getNotificationID());
    	assertEquals("5bc894ded9e3770323a780a7", dataAlertRule.getStream().getID());
    	assertEquals("COUNT", dataAlertRule.getConditionType());
    	assertEquals("admin", dataAlertRule.getCreatorUserId());
    }
    
    @Test
    @UsingDataSet(locations = "alertWizardSingleRuleCount.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testGetListDataAlertRule() throws UnsupportedEncodingException, NotFoundException {
    	GetListDataAlertRule listDataAlertRule = alertRuleResource.listWithData();

    	assertNotNull("Returned list data alert rule should not be null", listDataAlertRule);
    	assertEquals("There should be one alert rule in the collection", 1, listDataAlertRule.getAlertsData().size());
    	
    	GetDataAlertRule dataAlertRule = listDataAlertRule.getAlertsData().get(0);
    	assertEquals("Test Count", dataAlertRule.getTitle());
    	assertEquals("Test Count", dataAlertRule.getTitleCondition());
    	assertEquals("657386c8-b1de-4187-82a6-28087cfbd05c", dataAlertRule.getConditionID());
    	assertEquals("5bc894ded9e3770323a780a9", dataAlertRule.getNotificationID());
    	assertEquals("5bc894ded9e3770323a780a7", dataAlertRule.getStream().getID());
    	assertEquals("COUNT", dataAlertRule.getConditionType());
    	assertEquals("admin", dataAlertRule.getCreatorUserId());
    }
    
    @Test
    public void testGetAlertRuleNotExist() throws UnsupportedEncodingException, NotFoundException {
    	String alertTitle = "Test Rule does not exist";
        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage("Alert <" + alertTitle + "> not found!");
    	GetAlertRule alertRule = alertRuleResource.get(alertTitle);
    
        assertNull(alertRule);
    }
    
    @Test
    public void testGetDataAlertRuleNotExist() throws UnsupportedEncodingException, NotFoundException {
    	String alertTitle = "Test Rule does not exist";
    	expectedException.expect(NotFoundException.class);
        expectedException.expectMessage("Alert <" + alertTitle + "> not found!");
    	GetDataAlertRule dataAlertRule = alertRuleResource.getData(alertTitle);
    	
        assertNull(dataAlertRule);
    }
    
    @Test
    @UsingDataSet(locations = "alertWizardSingleRuleCount.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testExportAlertRule() throws UnsupportedEncodingException, NotFoundException, ValidationException {
    	List<String> titles = new ArrayList<>();
    	titles.add("Test Count");

    	ExportAlertRuleRequest request = ExportAlertRuleRequest.create(titles);
    	List<ExportAlertRule> listExportAlertRule = alertRuleResource.getExportAlertRule(request);
    	
    	assertNotNull("Returned list should not be null", listExportAlertRule);
    	assertEquals("There should be one alert rule in the collection", 1, listExportAlertRule.size());
    }
}
