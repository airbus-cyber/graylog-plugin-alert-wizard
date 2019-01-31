package com.airbus_cyber_security.graylog.alert;

public interface FieldRule {

	public String getID();
	
    public String getField();

    public int getType();

    public String getValue();

}
