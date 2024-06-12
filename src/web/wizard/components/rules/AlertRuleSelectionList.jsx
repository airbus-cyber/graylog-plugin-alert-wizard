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

import React from 'react';
import { useState } from 'react';

import { FormattedMessage } from 'react-intl';
import { SearchForm } from 'components/common';
import { Input } from 'components/bootstrap';
import ControlledTableList from 'components/common/ControlledTableList';

const AlertRuleSelectionList = ({alertRules, emptyMessage, onRuleSelectionChanged}) => {
    const [alertTitlesFilter, setAlertTitlesFilter] = useState('');
    // TODO simplify
    const [state, setState] = useState({selectedAlertTitles: new Set()});

    const onSearch = (filter) => {
        setAlertTitlesFilter(filter);
    };

    const onReset = () => {
        setAlertTitlesFilter('');
    };

    const handleRuleSelect = (event, title) => {
        const { selectedAlertTitles } = state;
        if (event.target.checked) {
            selectedAlertTitles.add(title);
        } else {
            selectedAlertTitles.delete(title);
        }
        setState({ selectedAlertTitles: selectedAlertTitles });
        onRuleSelectionChanged(selectedAlertTitles);
    };

    const formatAlertRule = (alertRule) => {
        // TODO think about it, try to remove the inline "style". And add this to the coding rules
        return (
            <ControlledTableList.Item key={`alertRule_${alertRule.title}`}>
                <Input id={`alertRule_${alertRule.title}`}
                       type="checkbox"
                       checked={state.selectedAlertTitles.has(alertRule.title)}
                       onChange={event => handleRuleSelect(event, alertRule.title)}
                       label={alertRule.title} />
                <p className="description" style={{'margin-left': '20px'}}>{alertRule.description}</p>
            </ControlledTableList.Item>
        );
    };

    // TODO remove function: write directly formattedAlertRules = ...
    const formatAlertRules = () => {
        return alertRules
            .sort((rule1, rule2) => rule1.title.localeCompare(rule2.title))
            .filter(rule => rule.title.includes(alertTitlesFilter))
            .map(formatAlertRule);
    };

    const toggleSelectAll = (event) => {
        let newSelection = new Set()
        if (event.target.checked) {
            alertRules.forEach(rule => newSelection.add(rule.title))
        }

        setState({ selectedAlertTitles: newSelection });
        onRuleSelectionChanged(newSelection);
    };

    // TODO create a method instead
    let alerts
    if (alertRules && alertRules.length !== 0) {
        const selectedItemsCount = state.selectedAlertTitles.size
        const label = <FormattedMessage id="wizard.selected"
                                        defaultMessage="{ruleCount, plural, =0 {Select all} =1 {One rule selected} other {# rules selected}}"
                                        values={{ruleCount: selectedItemsCount}} />
        alerts = (
            <ControlledTableList>
                <ControlledTableList.Header>
                    <Input id="select-all-checkbox"
                           type="checkbox"
                           label={label}
                           checked={selectedItemsCount === alertRules.length}
                           onChange={toggleSelectAll}
                           wrapperClassName="form-group-inline"
                    />

                </ControlledTableList.Header>
                {formatAlertRules()}
            </ControlledTableList>
        )
    } else {
        alerts = (
            <span className="help-block help-standalone">
                {emptyMessage}
            </span>
        )
    }

    return (
        <>
            <SearchForm onSearch={onSearch}
                        onReset={onReset}
                        searchButtonLabel="Filter"
                        placeholder="Filter alert rules by title..."
                        queryWidth={400}
                        resetButtonLabel="Reset"
                        searchBsStyle="info"
                        topMargin={0} />
            {alerts}
        </>
    )
}

export default AlertRuleSelectionList;
