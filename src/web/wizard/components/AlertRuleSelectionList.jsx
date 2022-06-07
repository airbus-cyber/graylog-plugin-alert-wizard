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
import { SearchForm } from 'components/common';
import { Input } from 'components/bootstrap';
import ControlledTableList from 'components/common/ControlledTableList';


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

    toggleSelectAll(event) {
        let newSelection = new Set()
        if (event.target.checked) {
            this.props.alertRules.forEach(rule => newSelection.add(rule.title))
        }

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
        if (this.props.alertRules && this.props.alertRules.length !== 0) {
            const { selectedAlertTitles } = this.state;
            const selectedItemsCount = selectedAlertTitles.size
            alerts = (
                <ControlledTableList>
                    <ControlledTableList.Header>
                        <Input id="select-all-checkbox"
                               type="checkbox"
                               // TODO is it possible to add internationalization for the label?
                               label={selectedItemsCount === 0 ? 'Select all' : `${selectedItemsCount} selected`}
                               checked={selectedItemsCount === this.props.alertRules.length}
                               onChange={this.toggleSelectAll}
                               wrapperClassName="form-group-inline"
                        />

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