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
import createReactClass from 'create-react-class';
import { FormattedMessage } from 'react-intl';
import { SearchForm } from 'components/common';
import { Input } from 'components/bootstrap';
import ControlledTableList from 'components/common/ControlledTableList';
import { Button } from 'components/graylog';


const AlertRuleSelectionList = createReactClass({

    getInitialState() {
        return {
            alertTitlesFilter: '',
            selectedAlertTitles: new Set()
        };
    },

    onSearch(filter) {
        this.setState({ alertTitlesFilter: filter });
    },

    onReset() {
        this.setState({ alertTitlesFilter: '' });
    },

    selectAllAlertRules() {
        const { alertRules } = this.state;

        const newSelection = new Set(alertRules.map(rule => rule.title));

        this.setState({ selectedAlertTitles: newSelection });
        this.props.onRuleSelectionChanged(newSelection);
    },

    handleRuleSelect(event, title) {
        const { selectedAlertTitles } = this.state;
        if (event.target.checked) {
            selectedAlertTitles.add(title);
        } else {
            selectedAlertTitles.delete(title);
        }
        this.setState({ selectedAlertTitles: selectedAlertTitles });
        this.props.onRuleSelectionChanged(selectedAlertTitles);
    },

    formatAlertRule(alertRule) {
        const { selectedAlertTitles } = this.state;
        // TODO think about it, try to remove the inline "style". And add this to the coding rules
        return (
            <ControlledTableList.Item key={`alertRule_${alertRule.title}`}>
                <Input id={`alertRule_${alertRule.title}`}
                       type="checkbox"
                       checked={selectedAlertTitles.has(alertRule.title)}
                       onChange={event => this.handleRuleSelect(event, alertRule.title)}
                       label={alertRule.title} />
                <p className="description" style={{'margin-left': '20px'}}>{alertRule.description}</p>
            </ControlledTableList.Item>
        );
    },

    formatAlertRules() {
        return this.props.alertRules
            .sort((rule1, rule2) => rule1.title.localeCompare(rule2.title))
            .filter(rule => rule.title.includes(this.state.alertTitlesFilter))
            .map(this.formatAlertRule, this);
    },

    render() {
        let alerts
        if (this.props.alertRules) {
            alerts = (
                <ControlledTableList>
                    <ControlledTableList.Header>
                        <Button className="btn btn-sm btn-link select-all" onClick={this.selectAllAlertRules}>
                            <FormattedMessage id="wizard.selectAll" defaultMessage="Select all" />
                        </Button>
                    </ControlledTableList.Header>
                    {this.formatAlertRules()}
                </ControlledTableList>
            )
        } else {
            alerts = (
                <span className="help-block help-standalone">
                    {this.props.emptyMessage}
                </span>
            )
        }

        return (
            <>
                <SearchForm onSearch={this.onSearch}
                            onReset={this.onReset}
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
})

export default AlertRuleSelectionList;