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

// sources of inspiration for this code: views/components/common/EditableTitle.tsx
import PropTypes from 'prop-types';
import React from 'react';
import createReactClass from 'create-react-class';
import { LinkContainer } from 'react-router-bootstrap';
import { injectIntl, FormattedMessage } from 'react-intl';

import { Button, Col, Row, Nav, NavItem } from 'components/bootstrap';
import { Spinner } from 'components/common';
import ObjectUtils from 'util/ObjectUtils';
import LoaderTabs from 'components/messageloaders/LoaderTabs';
import StreamsStore from 'stores/streams/StreamsStore';

import Navigation from 'wizard/routing/Navigation';
import StatisticsCondition from 'wizard/components/conditions/StatisticsCondition'
import GroupDistinctCondition from 'wizard/components/conditions/GroupDistinctCondition'
import CorrelationCondition from 'wizard/components/conditions/CorrelationCondition'
import OrCondition from 'wizard/components/conditions/OrCondition'
import CountCondition from 'wizard/components/conditions/CountCondition'
import TitleSeverity from 'wizard/components/inputs/TitleSeverity';
import AlertValidation from 'wizard/logic/AlertValidation';

import styles from './AlertRuleForm.css';


// TODO should convert component into functional form
const AlertRuleForm = createReactClass({
    displayName: 'AlertRuleForm',

    propTypes: {
        alert: PropTypes.object.isRequired,
        navigationToRuleComponents: PropTypes.element,
        onSave: PropTypes.func.isRequired
    },

    componentDidMount() {
        this._handleSelect(this.state.alert.condition_type);
    },

    getInitialState() {
        let alert = ObjectUtils.clone(this.props.alert);

        let { time, time_type } = this._destructureTime(this.props.alert.condition_parameters.time);
        return {
            alert: alert,
            isModified: false,
            isValid: false,
            time: time,
            time_type: time_type,
            contentComponent: <Spinner/>,
        };
    },
    _destructureTime(time) {
        if (time >= 1440) {
            return {
                time: time / 1440,
                time_type: 1440
            }
        }
        if (time >= 60) {
            return {
                time: time / 60,
                time_type: 60
            }
        }
        return {
            time: time,
            time_type: 1
        }
    },
    _updateAlertField(field, value) {
        const update = ObjectUtils.clone(this.state.alert);
        update[field] = value;
        this.setState({alert: update});
        if (field !== "condition_type") {
            this.setState({isModified: true});
        }
        // TODO why is this check necessary???
        if (value === '') {
            this.setState({isValid: false});
        } else {
            this._checkAlert(update);
        }
    },  
    _updateAlert(alert) {
        this.setState({alert: alert});
        this.setState({isModified: true});
    },

    _checkAlert(alert) {
        let isFormValid = AlertValidation.isAlertValid(alert);
        if (this.state.isValid !== isFormValid) {
            this.setState({isValid: isFormValid});
        }
    },
    
    _onMessageLoaded(message) {
        this.setState({message: message});
        if (message !== undefined && this.state.alert.stream.id) {
            StreamsStore.testMatch(this.state.alert.stream.id, {message: message.fields}, (resultData) => {
                this.setState({matchData: resultData});
                this._handleSelect(this.state.alert.condition_type);
            });
        } else {
            this.setState({matchData: undefined});
        }
    },
    _handleSelect(selectedKey) {
        let alert = ObjectUtils.clone(this.state.alert);
        if (alert.condition_type !== selectedKey) {
            this._updateAlertField("condition_type", selectedKey);
            alert.condition_type = selectedKey;
        }

        switch (selectedKey) {
            case 'COUNT':
                this.setState({
                    contentComponent: <CountCondition onUpdate={this._updateAlertField} alert={alert} message={this.state.message}
                        matchData={this.state.matchData} />
                });
                break;
            case 'GROUP_DISTINCT':
                this.setState({
                    contentComponent: <GroupDistinctCondition onUpdate={this._updateAlertField} alert={alert} message={this.state.message} 
                        matchData={this.state.matchData} />
                });
                break;
            case 'STATISTICAL':
                this.setState({
                    contentComponent: <StatisticsCondition onUpdate={this._updateAlertField} alert={alert} message={this.state.message}
                        matchData={this.state.matchData} />
                });
                break;
            case 'THEN':
            case 'AND':
                this.setState({
                    contentComponent: <CorrelationCondition onUpdate={this._updateAlertField} onUpdateAlert={this._updateAlert} alert={alert}
                        message={this.state.message} matchData={this.state.matchData} />
                });
                break;
            case 'OR':
                this.setState({
                    contentComponent: <OrCondition onUpdate={this._updateAlertField} alert={alert} message={this.state.message}
                        matchData={this.state.matchData} />
                });
                break;
            default:
                this.setState({contentComponent: <div/>});
                break;
        }
    },
    
    render() {
        const { intl } = this.props;
        const messages = {
            titlePopup: intl.formatMessage({id: "wizard.titlePopup", defaultMessage: "Alert rule is saved"}),
            messagePopup: intl.formatMessage({id: "wizard.messagePopup", defaultMessage: "Go to Advanced settings?"}),
            advancedSettings: intl.formatMessage({id: "wizard.advancedSettings", defaultMessage: "Advanced settings"}),
            done: intl.formatMessage({id: "wizard.done", defaultMessage: "I'm done!"}),
            placeholderTitle: intl.formatMessage({id: "wizard.placeholderTitle", defaultMessage: "Title of the alert rule                 "}),
            add: intl.formatMessage({id: "wizard.add", defaultMessage: "Add"}),
            ruleType: intl.formatMessage({id: "wizard.ruleType", defaultMessage: "Rule Type"}),
            tooltipCountCondition: intl.formatMessage({id: "wizard.tooltipCountCondition", defaultMessage: "Count Condition"}),
            tooltipGroupDistinctCondition: intl.formatMessage({id: "wizard.tooltipGroupDistinctCondition", defaultMessage: "Group / Distinct Condition"}),
            tooltipStatisticalCondition: intl.formatMessage({id: "wizard.tooltipStatisticalCondition", defaultMessage: "Statistical Condition"}),
            tooltipThenCondition: intl.formatMessage({id: "wizard.tooltipThenCondition", defaultMessage: "THEN Condition"}),
            tooltipAndCondition: intl.formatMessage({id: "wizard.tooltipAndCondition", defaultMessage: "AND Condition"}),
            tooltipOrCondition: intl.formatMessage({id: "wizard.tooltipOrCondition", defaultMessage: "OR Condition"}),
        };
        const buttonCancel = (
            <LinkContainer to={Navigation.getWizardRoute()}>
                <Button><FormattedMessage id="wizard.cancel" defaultMessage="Cancel" /></Button>
            </LinkContainer>
        );

        const buttonSave = (
            <Button onClick={() => this.props.onSave(this.state.alert)}
                    disabled={!(this.state.isModified && this.state.isValid)}
                    bsStyle="primary" className="btn btn-primary">
                <FormattedMessage id="wizard.save" defaultMessage="Save" />
            </Button>
        );

        const actions = (
                <div className="pull-left">
                    {buttonCancel}{' '}
                    {buttonSave}{' '}
                </div>);
                
        const subnavigation = (
                <Nav stacked bsStyle="pills" activeKey={this.state.alert.condition_type} onSelect={key => this._handleSelect(key)}>
                    <NavItem key="divider" disabled title="Rule Type" className={styles.divider}>{messages.ruleType}</NavItem>
                    <NavItem eventKey={'COUNT'} title={messages.tooltipCountCondition}>
                        <FormattedMessage id= "wizard.countCondition" defaultMessage= "Count" />
                    </NavItem>
                    <NavItem eventKey={'GROUP_DISTINCT'} title={messages.tooltipGroupDistinctCondition}>
                        <FormattedMessage id= "wizard.groupDistinctCondition" defaultMessage= "Group / Distinct" />
                    </NavItem>
                    <NavItem eventKey={'STATISTICAL'} title={messages.tooltipStatisticalCondition}>
                        <FormattedMessage id= "wizard.StatisticsCondition" defaultMessage= "Statistics" />
                    </NavItem>
                    <NavItem eventKey={'THEN'} title={messages.tooltipThenCondition}>
                        <FormattedMessage id= "wizard.thenCondition" defaultMessage= "THEN" />
                    </NavItem>
                    <NavItem eventKey={'AND'} title={messages.tooltipAndCondition}>
                        <FormattedMessage id= "wizard.andCondition" defaultMessage= "AND" />
                    </NavItem>
                    <NavItem eventKey={'OR'} title={messages.tooltipOrCondition}>
                        <FormattedMessage id= "wizard.orCondition" defaultMessage= "OR" />
                    </NavItem>
                </Nav>
            );

        return (
            <div>
                <Row>
                    <Col md={2} className={styles.subnavigation}>{subnavigation}</Col>
                    <Col md={10} className={styles.contentpane}>
                        <h2>
                            <FormattedMessage id= "wizard.loadMessage" defaultMessage= "Load a message to test fields conditions" />
                        </h2>
                        <div className="stream-loader">
                            <LoaderTabs messageId={this.props.messageId} index={this.props.index}
                                        onMessageLoaded={this._onMessageLoaded}/>
                        </div>
                        <hr/>
                        <h2><FormattedMessage id= "wizard.titleParameters" defaultMessage= "Alert rule parameters" /></h2>
                        {this.props.navigationToRuleComponents}
                        <p className="description"><FormattedMessage id= "wizard.descripionParameters" defaultMessage= "Define the parameters of the alert rule." /></p>
                        <form className="form-inline">
                            <TitleSeverity onUpdate={this._updateAlertField} title={this.state.alert.title} severity={this.state.alert.severity} />
                            <br/>
                            {this.state.contentComponent}
                        </form>
                        {actions}
                    </Col>
                </Row>
            </div>
        );
    },
});

export default injectIntl(AlertRuleForm);
