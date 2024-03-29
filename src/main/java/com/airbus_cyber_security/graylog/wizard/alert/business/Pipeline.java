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


// TODO try to remove this class...
public class Pipeline {

    public String getPipelineID() {
        return pipelineID;
    }

    public String getPipelineRuleID() {
        return pipelineRuleID;
    }

    private final String pipelineID;
    private final String pipelineRuleID;

    public Pipeline(String pipelineID, String pipelineRuleID)
    {
        this.pipelineID = pipelineID;
        this.pipelineRuleID = pipelineRuleID;
    }
}
