package com.airbus_cyber_security.graylog.alert;

import java.util.List;

public interface AlertRuleStream {

	public String getID();
	
    public String getMatchingType();
    
    public List<FieldRuleImpl> getFieldRules();

}
