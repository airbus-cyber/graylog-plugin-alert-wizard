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

import React, {useEffect, useState} from "react";
import ManageSettings from "./ManageSettings";
import WizardConfigurationResource from "../../resources/WizardConfigurationResource";
import { Spinner } from "components/common";
import { IntlProvider } from "react-intl";
import messages_fr from "../../../translations/fr.json";


export default function WizardAlertConfig() {
    const [configuration, setConfiguration] = useState(null);

    const language = navigator.language.split(/[-_]/)[0];

    const messages = {
        'fr': messages_fr
    };

    const _saveConfiguration = configuration => {
        WizardConfigurationResource.update(configuration).then(() => setConfiguration(configuration));
    };

    useEffect(() => {
        WizardConfigurationResource.get().then(configuration => {
            setConfiguration(configuration);
        });
    }, []);

    const _listToString = (value) => {
        if (!value) {
            return '[not set]';
        }

        return value.filter(x => x.enabled).map(x => x.name).join(', ');
    };

    const _importPolicyToString = (value) => {
        if (!value) {
            return '[not set]';
        }

        switch (value) {
            case 'DONOTHING':
                return "Don't import";
            case 'REPLACE':
                return "Import and Replace";
            case 'RENAME':
                return "Import, but keep both alert rules";
            default:
                return "Unknown";
        }
    };

    const _priorityToString = (value) => {
        if (!value) {
            return '[not set]';
        }

        switch (value) {
            case 1:
                return "Low";
            case 2:
                return "Normal";
            case 3:
                return "High";
            default:
                return "Unknown";
        }
    };

    const _matchingTypeToString = (value) => {
        if (!value) {
            return '[not set]';
        }

        switch (value) {
            case 'AND':
                return "All";
            case 'OR':
                return "At least one";
            default:
                return "Unknown";
        }
    };

    const _thresholdTypeToString = (value) => {
        if (!value) {
            return '[not set]';
        }

        switch (value) {
            case '>':
                return "More than";
            case '<':
                return "Less than";
            default:
                return "Unknown";
        }
    };

    const _timeTypeToString = (value) => {
        if (!value) {
            return '[not set]';
        }

        switch (value) {
            case 1:
                return "Minutes";
            case 60:
                return "Hours";
            case 1440:
                return "Days";
            default:
                return "Unknown";
        }
    };

    const _displayOptionalConfigurationValue = (value) => {
        if (!value) {
            return '[not set]';
        }
        return value;
    };

    if (!configuration) {
        return <Spinner text="Loading, please wait..." />;
    }

    return (
        <IntlProvider locale={language} messages={messages[language]}>
            <div>
                <h3>Wizard Alert Configuration</h3>

                <p>
                    Configuration for the Wizard Alert plugin.
                </p>
                <dl className="deflist">
                    <dt>Alert rules columns:</dt>
                    <dd>
                        {_listToString(configuration.field_order)}
                    </dd>
                </dl>
                <dl className="deflist">
                    <dt>Import strategy:</dt>
                    <dd>
                        {_importPolicyToString(configuration.import_policy)}
                    </dd>
                </dl>
                <span style={{fontStyle: 'italic'}}>Wizard default values:</span>
                <dl className="deflist">
                    <dt>Title:</dt>
                    <dd>
                        {_displayOptionalConfigurationValue(configuration.default_values.title)}
                    </dd>
                </dl>
                <dl className="deflist">
                    <dt>Priority:</dt>
                    <dd>
                        {_priorityToString(configuration.default_values.priority)}
                    </dd>
                </dl>
                <dl className="deflist">
                    <dt>Matching type:</dt>
                    <dd>
                        {_matchingTypeToString(configuration.default_values.matching_type)}
                    </dd>
                </dl>
                <dl className="deflist">
                    <dt>Threshold type:</dt>
                    <dd>
                        {_thresholdTypeToString(configuration.default_values.threshold_type)}
                    </dd>
                </dl>
                <dl className="deflist">
                    <dt>Threshold:</dt>
                    <dd>
                        {_displayOptionalConfigurationValue(configuration.default_values.threshold)}
                    </dd>
                </dl>
                <dl className="deflist">
                    <dt>Time range:</dt>
                    <dd>
                        {_displayOptionalConfigurationValue(configuration.default_values.time)}
                    </dd>
                </dl>
                <dl className="deflist">
                    <dt>Time range unit:</dt>
                    <dd>
                        {_timeTypeToString(configuration.default_values.time_type)}
                    </dd>
                </dl>
                <dl className="deflist">
                    <dt>Aggregation time range:</dt>
                    <dd>
                        {`${configuration.default_values.aggregation_time ?? 0} minutes`}
                    </dd>
                </dl>
                <span style={{fontStyle: 'italic'}}>Alert default values:</span>
                <dl className="deflist">
                    <dt>Message Backlog:</dt>
                    <dd>
                        {_displayOptionalConfigurationValue(configuration.default_values.backlog)}
                    </dd>
                </dl>
                <dl className="deflist">
                    <dt>Execute search every </dt>
                    <dd>
                        {_displayOptionalConfigurationValue(configuration.default_values.grace)} minutes
                    </dd>
                </dl>
                <br/>
                <ManageSettings config={configuration} onSave={_saveConfiguration}/>
            </div>
        </IntlProvider>
    );
}