package com.airbus_cyber_security.graylog.alert;

import com.airbus_cyber_security.graylog.alert.rest.models.requests.AlertRuleRequest;
import com.airbus_cyber_security.graylog.alert.rest.models.requests.CloneAlertRuleRequest;
import com.airbus_cyber_security.graylog.alert.utilities.AlertRuleUtils;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

import org.graylog2.bindings.providers.MongoJackObjectMapperProvider;
import org.graylog2.database.CollectionName;
import org.graylog2.database.MongoConnection;
import org.joda.time.DateTime;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.*;

public class AlertRuleServiceImpl implements AlertRuleService {

	private final JacksonDBCollection<AlertRuleImpl, String> coll;
	private final Validator validator;
	private static final Logger LOG = LoggerFactory.getLogger(AlertRuleServiceImpl.class);
	private static final String TITLE = "title";

	@Inject
	public AlertRuleServiceImpl(MongoConnection mongoConnection, MongoJackObjectMapperProvider mapperProvider,
								Validator validator) {
		this.validator = validator;
		final String collectionName = AlertRuleImpl.class.getAnnotation(CollectionName.class).value();
		final DBCollection dbCollection = mongoConnection.getDatabase().getCollection(collectionName);
		this.coll = JacksonDBCollection.wrap(dbCollection, AlertRuleImpl.class, String.class, mapperProvider.get());
		this.coll.createIndex(new BasicDBObject(TITLE, 1), new BasicDBObject("unique", true));
	}

	@Override
	public long count() {
		return coll.count();
	}

	@Override
	public AlertRule create(AlertRule alert) {
		if (alert instanceof AlertRuleImpl) {
			final AlertRuleImpl alertImpl = (AlertRuleImpl) alert;

			final Set<ConstraintViolation<AlertRuleImpl>> violations = validator.validate(alertImpl);
			if (violations.isEmpty()) {
				return coll.insert(alertImpl).getSavedObject();
			} else {
				throw new IllegalArgumentException("Specified object failed validation: " + violations);
			}
		} else
			throw new IllegalArgumentException(
					"Specified object is not of correct implementation type (" + alert.getClass() + ")!");
	}
	
	@Override
	public AlertRule update(String title, AlertRule alert) {
		
		if (alert instanceof AlertRuleImpl) {
			final AlertRuleImpl alertImpl = (AlertRuleImpl) alert;
			LOG.debug("Alert to be updated [{}]", alertImpl);

			final Set<ConstraintViolation<AlertRuleImpl>> violations = validator.validate(alertImpl);
			if (violations.isEmpty()) {

				return coll.findAndModify(DBQuery.is(TITLE, title), new BasicDBObject(), new BasicDBObject(),
						false, alertImpl, true, false);

			} else {
				throw new IllegalArgumentException("Specified object failed validation: " + violations);
			}

		} else
			throw new IllegalArgumentException(
					"Specified object is not of correct implementation type (" + alert.getClass() + ")!");
	}


	@Override
	public List<AlertRule> all() {
		return toAbstractListType(coll.find());
	}

	@Override
	public int destroy(String alertTitle) {    

		return coll.remove(DBQuery.is(TITLE, alertTitle)).getN();
	}
	
	@Override
	public AlertRule load(String alertTitle) {
		return coll.findOne(DBQuery.is(TITLE, alertTitle));
	}
	
	@Override
	public boolean isPresent(String title) {
		if(coll.getCount(DBQuery.is(TITLE, title)) > 0) {
			return true;
		}
		return false;
	}

	private List<AlertRule> toAbstractListType(DBCursor<AlertRuleImpl> alerts) {
		return toAbstractListType(alerts.toArray());
	}

	private List<AlertRule> toAbstractListType(List<AlertRuleImpl> alerts) {
		final List<AlertRule> result = Lists.newArrayListWithCapacity(alerts.size());
		result.addAll(alerts);

		return result;
	}

	private boolean isValidTitle(String title) {
		return !(title == null || title.isEmpty());
	}
	
	private boolean isValidStream(AlertRuleStreamImpl stream) {
		if(stream.getMatchingType().equals("AND") || stream.getMatchingType().equals("OR")){
			for (FieldRuleImpl fieldRule : stream.getFieldRules()) {
				if(fieldRule.getField() == null || fieldRule.getField().isEmpty() ||
						fieldRule.getType() < -6 || fieldRule.getType() > 6	) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	private boolean isValidCondStatistical(Map<String, Object> conditionParameters) {
		return (conditionParameters.containsKey("type") &&
				conditionParameters.containsKey("field") &&
				isValidStatThresholdType(conditionParameters.get(AlertRuleUtils.THRESHOLD_TYPE).toString()));
	}
	
	private boolean isValidCondCorrelation(Map<String, Object> conditionParameters, AlertRuleStreamImpl secondStream) {
		return (conditionParameters.containsKey(AlertRuleUtils.ADDITIONAL_THRESHOLD) &&
				conditionParameters.containsKey(AlertRuleUtils.ADDITIONAL_THRESHOLD_TYPE) &&
				isValidThresholdType(conditionParameters.get(AlertRuleUtils.THRESHOLD_TYPE).toString()) &&
				isValidThresholdType(conditionParameters.get(AlertRuleUtils.ADDITIONAL_THRESHOLD_TYPE).toString()) &&
				isValidStream(secondStream));
	}
	
	private boolean isValidCondOr(Map<String, Object> conditionParameters, AlertRuleStreamImpl secondStream) {
		return (isValidThresholdType(conditionParameters.get(AlertRuleUtils.THRESHOLD_TYPE).toString()) &&
				isValidStream(secondStream));
	}
	
	private boolean isValidThresholdType(String thresholdType) {
		return  (thresholdType.equals("MORE") || thresholdType.equals("LESS"));
	}
	
	private boolean isValidStatThresholdType(String thresholdType) {
		return  (thresholdType.equals("HIGHER") || thresholdType.equals("LOWER"));
	}
	
	private boolean isValidCondition(String conditionType, Map<String, Object> conditionParameters, AlertRuleStreamImpl secondStream) {
		if(		conditionParameters.containsKey(AlertRuleUtils.TIME) && 
				conditionParameters.containsKey(AlertRuleUtils.THRESHOLD) &&	
				conditionParameters.containsKey(AlertRuleUtils.THRESHOLD_TYPE)
				) {
			if(conditionType.equals("STATISTICAL")) {
				return isValidCondStatistical(conditionParameters);
			}else if(conditionType.equals("THEN")  || conditionType.equals("AND")) {
				return isValidCondCorrelation(conditionParameters, secondStream);
			}else if(conditionType.equals("OR")) {
				return isValidCondOr(conditionParameters, secondStream);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean isValidRequest(AlertRuleRequest request){
		return (isValidTitle(request.getTitle()) && 
				isValidStream(request.getStream()) &&
				isValidCondition(request.getConditionType(), request.conditionParameters(), request.getSecondStream()) );
    }
}
