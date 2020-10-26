/*
 * graylog-plugin-alert-wizard Source Code
 * Copyright (C) 2018-2020 - Airbus CyberSecurity (SAS) - All rights reserved
 *
 * This file is part of the graylog-plugin-alert-wizard GPL Source Code.
 *
 * graylog-plugin-alert-wizard Source Code is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this code.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.airbus_cyber_security.graylog.alert;

import com.airbus_cyber_security.graylog.alert.bundles.ExportAlertRule;
import com.airbus_cyber_security.graylog.alert.rest.models.requests.AlertRuleRequest;
import com.mongodb.MongoException;
import org.graylog2.database.NotFoundException;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface AlertRuleService {
    long count();

    AlertRule update(String title, AlertRule alert);

    AlertRule create(AlertRule alert);
    
    List<AlertRule> all();
    
    int destroy(String alertTitle) throws MongoException, UnsupportedEncodingException;

    AlertRule load(String title)  throws NotFoundException;
    
    boolean isPresent(String title);

    boolean isValidRequest(AlertRuleRequest request);

    boolean isValidImportRequest(ExportAlertRule request);

}
