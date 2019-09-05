package com.airbus_cyber_security.graylog.list;

import org.joda.time.DateTime;

public interface AlertList {

    public String getTitle();

    public DateTime getCreatedAt();

    public String getCreatorUserId();

    public DateTime getLastModified();

    public String getDescription();

    public int getUsage();

    public String getLists();

}
