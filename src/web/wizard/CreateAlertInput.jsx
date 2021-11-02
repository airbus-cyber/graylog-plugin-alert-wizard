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

import PropTypes from 'prop-types';
import React from 'react';
import createReactClass from 'create-react-class';
import {Nav, NavItem} from 'components/graylog';
import {Button, Col, Row} from 'components/graylog';
import {Spinner} from 'components/common';
import ObjectUtils from 'util/ObjectUtils';
import AlertRuleActions from './AlertRuleActions';
import Routes from 'routing/Routes';
import {LinkContainer} from 'react-router-bootstrap';
import {confirmAlert} from 'react-confirm-alert';
import 'react-confirm-alert/src/react-confirm-alert.css';
import {FormattedMessage} from 'react-intl';
import StoreProvider from 'injection/StoreProvider';
import LoaderTabs from 'components/messageloaders/LoaderTabs';
import WizardStyle from '!style!css!./WizardStyle.css';
import StatisticsCondition from 'wizard/ruletype/StatisticsCondition'
import GroupDistinctCondition from 'wizard/ruletype/GroupDistinctCondition'
import CorrelationCondition from 'wizard/ruletype/CorrelationCondition'
import OrCondition from 'wizard/ruletype/OrCondition'
import CountCondition from 'wizard/ruletype/CountCondition'
import history from 'util/History';
import ActionsProvider from 'injection/ActionsProvider';

const NodesActions = ActionsProvider.getActions('Nodes');
const StreamsStore = StoreProvider.getStore('Streams');
const PluginsStore = StoreProvider.getStore('Plugins');

const INIT_ALERT = {
        title: '',
        severity: '',
        condition_type: 'COUNT',
        condition_parameters: {
            time: 1,
            threshold_type: '',
            threshold: 0,
            additional_threshold_type: '',
            additional_threshold: 0,
            grouping_fields: [],
            distinction_fields: [],
            field: '',
            type: ''
        },
        stream: {
            matching_type: '',
            field_rule: [{field: '', type: '', value: ''}],
        },
        second_stream: {
            matching_type: '',
            field_rule: [{field: '', type: '', value: ''}],
        },
    };

const CreateAlertInput = createReactClass({
    displayName: 'CreateAlertInput',

    propTypes: {
        alert: PropTypes.object,
        create: PropTypes.bool.isRequired,
    },
    contextTypes: {
        intl: PropTypes.object.isRequired,
    },
    componentWillMount(){
        const messages = {
                titlePopup: this.context.intl.formatMessage({id: "wizard.titlePopup", defaultMessage: "Alert rule is saved"}),
                messagePopup: this.context.intl.formatMessage({id: "wizard.messagePopup", defaultMessage: "Go to Advanced settings?"}),
                advancedSettings: this.context.intl.formatMessage({id: "wizard.advancedSettings", defaultMessage: "Advanced settings"}),
                done: this.context.intl.formatMessage({id: "wizard.done", defaultMessage: "I'm done!"}),
                placeholderTitle: this.context.intl.formatMessage({id: "wizard.placeholderTitle", defaultMessage: "Title of the alert rule                 "}),
                add: this.context.intl.formatMessage({id: "wizard.add", defaultMessage: "Add"}),
                ruleType: this.context.intl.formatMessage({id: "wizard.ruleType", defaultMessage: "Rule Type"}),
                tooltipCountCondition: this.context.intl.formatMessage({id: "wizard.tooltipCountCondition", defaultMessage: "Count Condition"}),
                tooltipGroupDistinctCondition: this.context.intl.formatMessage({id: "wizard.tooltipGroupDistinctCondition", defaultMessage: "Group / Distinct Condition"}),
                tooltipStatisticalCondition: this.context.intl.formatMessage({id: "wizard.tooltipStatisticalCondition", defaultMessage: "Statistical Condition"}),
                tooltipThenCondition: this.context.intl.formatMessage({id: "wizard.tooltipThenCondition", defaultMessage: "THEN Condition"}),
                tooltipAndCondition: this.context.intl.formatMessage({id: "wizard.tooltipAndCondition", defaultMessage: "AND Condition"}),
                tooltipOrCondition: this.context.intl.formatMessage({id: "wizard.tooltipOrCondition", defaultMessage: "OR Condition"}),
            };
        this.setState({messages:messages});
    },
    componentDidMount() {
        this._isPluginsPresent();
    },
    getDefaultProps() {
        return {
            alert: INIT_ALERT,
            default_values: {
                title: '',
                severity: '',
                matching_type: '',
                threshold_type: '',
                threshold: 0,
                time: 1,
                time_type: 1,
                field: '',
                field_type: '',
                field_value: '',
                repeat_notifications: false,
                grace: 1,
                backlog: 500
            },
        };
    },

    getInitialState() {
        let time;
        let time_type;
        let alert = ObjectUtils.clone(this.props.alert);

        if (this.props.create) {
            alert.title = this.props.default_values.title;
            alert.severity = this.props.default_values.severity;
            alert.condition_parameters.threshold_type = this.props.default_values.threshold_type;
            alert.condition_parameters.threshold = this.props.default_values.threshold;
            alert.stream.matching_type = this.props.default_values.matching_type;
            alert.stream.field_rule[0].field = this.props.default_values.field;
            alert.stream.field_rule[0].type = this.props.default_values.field_type.toString();
            alert.stream.field_rule[0].value = this.props.default_values.field_value;
            time = this.props.default_values.time;
            time_type = this.props.default_values.time_type;
            alert.condition_parameters.time = time * time_type;
            alert.condition_parameters.repeat_notifications = this.props.default_values.repeat_notifications;
            alert.condition_parameters.grace = this.props.default_values.grace;
            alert.condition_parameters.backlog = this.props.default_values.backlog;

        } else {
            /* Display title condition */
            alert.title = this.props.alert.title_condition;
            
            if (this.props.alert.condition_parameters.time >= 1440) {
                time = this.props.alert.condition_parameters.time / 1440;
                time_type = 1440;
            } else if (this.props.alert.condition_parameters.time >= 60) {
                time = this.props.alert.condition_parameters.time / 60;
                time_type = 60;
            } else {
                time = this.props.alert.condition_parameters.time;
                time_type = 1;
            }
        }
        
        return {
            alert: alert,
            isModified: false,
            isValid: false,
            time: time,
            time_type: time_type,
            contentComponent: <Spinner/>,
            isPluginAggregation: true,
            isPluginCorrelation: true,
            isPluginLoggingAlert: true,
        };
    },
    _isPluginsPresent(){
        NodesActions.list().then(nodes => {
            if(nodes.nodes[0]) {
                PluginsStore.list(nodes.nodes[0].node_id).then(plugins => {
                    let isPluginAggregationPresent = false;
                    let isPluginCorrelationPresent = false;
                    let isPluginLoggingAlertPresent = false;
                    for (let i = 0; i < plugins.length; i++) {
                        if (plugins[i].unique_id === "com.airbus-cyber-security.graylog.AggregationCountPlugin") {
                            isPluginAggregationPresent = true;
                        } else if (plugins[i].unique_id === "com.airbus-cyber-security.graylog.CorrelationCountPlugin") {
                            isPluginCorrelationPresent = true;
                        } else if (plugins[i].unique_id === "com.airbus-cyber-security.graylog.LoggingAlertPlugin") {
                            isPluginLoggingAlertPresent = true;
                        }
                    }
                    this.setState({
                        isPluginAggregation: isPluginAggregationPresent,
                        isPluginCorrelation: isPluginCorrelationPresent,
                        isPluginLoggingAlert: isPluginLoggingAlertPresent
                    });
                    if (isPluginLoggingAlertPresent === false && this.state.alert.severiry !== '') {
                        const update = ObjectUtils.clone(this.state.alert);
                        update['severity'] = '';
                        this.setState({alert: update});
                    }

                    this._handleSelect(this.state.alert.condition_type);
                });
            }
        });
    },
    _save() {
        AlertRuleActions.create.triggerPromise(this.state.alert).then((response) => {
            if (response === true) {
                AlertRuleActions.getData(this.state.alert.title).then(alert => {
                    this.setState({alert: alert});
                    this._advancedSettings();
                });
            }
        });
        this.setState({isModified: false});
    },
    _update() {
        AlertRuleActions.update.triggerPromise(this.props.alert.title, this.state.alert).then((response) => {
            if (response === true) {
                AlertRuleActions.getData(this.state.alert.title).then(alert => {
                    this.setState({alert: alert});
                    this._advancedSettings();
                });
            }
        });
        this.setState({isModified: false});
    },
    _advancedSettings() {
        const options = {
            title: this.state.messages.titlePopup,
            message: this.state.messages.messagePopup,
            buttons: [
                {
                    label: this.state.messages.advancedSettings,
                    onClick: () => history.push({pathname: Routes.ALERTS.DEFINITIONS.edit(this.state.alert.condition)})
                },
                {
                    label: this.state.messages.done,
                    onClick: () => history.push({pathname: Routes.pluginRoute('WIZARD_ALERTRULES')})
                },
            ]
        };
       confirmAlert(options);
    },
    _updateAlertField(field, value) {
        const update = ObjectUtils.clone(this.state.alert);
        update[field] = value;
        this.setState({alert: update});
        if(field !== "condition_type"){
            this.setState({isModified: true});
        }
        if (value === '') {
            this.setState({isValid: false});
        }else{
            this._checkAlert(update);
        }
    },  
    _updateAlert(alert) {
        this.setState({alert: alert});
        this.setState({isModified: true});
    },  
    _isRuleValid(rule){
        if (!(rule.field !== '' &&
                (rule.type === 5 || rule.type === -5) ||
                rule.field !== '' && rule.value !== '' &&
                (rule.type === 1 || rule.type === -1 ||
                    rule.type === 2 || rule.type === -2 ||
                    rule.type === 3 || rule.type === -3 ||
                    rule.type === 4 || rule.type === -4 ||
                    rule.type === 6 || rule.type === -6) &&
                    (rule.type === 7 || rule.type === -7) ||
                rule.field !== '' && rule.value !== '')) {
                return false;
            }
        return true;
    },
    _isFieldRulesValid(stream) {
        for (let i = 0; i < stream.field_rule.length; i++) {
            if (!this._isRuleValid(stream.field_rule[i])){
                return false;
            }
        }
        return true;
    },
    
    _isAlertValid(alert){
        if (alert.title !== '' &&
            alert.stream.matching_type !== '' &&
            alert.stream.field_rule.length > 0 &&
            alert.condition_parameters.time !== null &&
            alert.condition_parameters.threshold_type !== '' &&
            alert.condition_parameters.threshold !== null && !isNaN(alert.condition_parameters.threshold) &&
            this._isFieldRulesValid(alert.stream)) {
                return true;
            }
        return false
    },
    _checkAlert(alert) {
        let isFormValid = this._isAlertValid(alert);
        if(this.state.isValid !== isFormValid){
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
        if(alert.condition_type !== selectedKey){
            this._updateAlertField("condition_type", selectedKey);
            alert.condition_type = selectedKey;
        }

        switch (selectedKey) {
            case 'COUNT':
                this.setState({
                    contentComponent: <CountCondition onUpdate={this._updateAlertField} alert={alert} message={this.state.message}
                        matchData={this.state.matchData} isPluginLoggingAlertPresent={this.state.isPluginLoggingAlert} />
                });
                break;
            case 'GROUP_DISTINCT':
                this.setState({
                    contentComponent: <GroupDistinctCondition onUpdate={this._updateAlertField} alert={alert} message={this.state.message} 
                        matchData={this.state.matchData} isPluginLoggingAlertPresent={this.state.isPluginLoggingAlert}/>
                });
                break;
            case 'STATISTICAL':
                this.setState({
                    contentComponent: <StatisticsCondition  onUpdate={this._updateAlertField} alert={alert} message={this.state.message} 
                        matchData={this.state.matchData} isPluginLoggingAlertPresent={this.state.isPluginLoggingAlert}/>
                });
                break;
            case 'THEN':
            case 'AND':
                this.setState({
                    contentComponent: <CorrelationCondition  onUpdate={this._updateAlertField} onUpdateAlert={this._updateAlert} alert={alert} 
                        message={this.state.message} matchData={this.state.matchData} isPluginLoggingAlertPresent={this.state.isPluginLoggingAlert}/>
                });
                break;
            case 'OR':
                this.setState({
                    contentComponent: <OrCondition  onUpdate={this._updateAlertField} alert={alert} message={this.state.message} 
                        matchData={this.state.matchData} isPluginLoggingAlertPresent={this.state.isPluginLoggingAlert}/>
                });
                break;
            default:
                this.setState({contentComponent: <div/>});
                break;
        }
    },
    
    render: function () {
        let actions;
        const buttonCancel = (
            <LinkContainer to={Routes.pluginRoute('WIZARD_ALERTRULES')}>
                <Button><FormattedMessage id= "wizard.cancel" defaultMessage= "Cancel" /></Button>
            </LinkContainer>
        );

        let buttonSave;
        if (this.props.create) {
            buttonSave = (
                <Button onClick={this._save} bsStyle="primary" disabled={!this.state.isValid} className="btn btn-md btn-primary">
                    <FormattedMessage id= "wizard.save" defaultMessage= "Save" />
                </Button>
            );
        } else {
            buttonSave = (
                <Button onClick={this._update} bsStyle="primary" disabled={!(this.state.isModified && this.state.isValid)}
                        className="btn btn-md btn-primary">
                    <FormattedMessage id= "wizard.save" defaultMessage= "Save" />
                </Button>
            );  
        }
        
        actions = (
                <div className="alert-actions pull-left">
                    {buttonCancel}{' '}
                    {buttonSave}{' '}
                </div>);
        
        let customizeLink;
        if(!this.props.create){
            customizeLink = (
              <div className="alert-actions pull-right">
                <LinkContainer disabled={this.state.isModified} to={Routes.ALERTS.DEFINITIONS.edit(this.state.alert.condition)}>
                    <Button bsStyle="info" title="Advanced settings for this alert rule">
                        <FormattedMessage id= "wizard.advancedSettings" defaultMessage= "Advanced settings" />
                    </Button>
                </LinkContainer>
              </div>
            );
        }
                
        const subnavigation = (
                <Nav stacked bsStyle="pills" activeKey={this.state.alert.condition_type} onSelect={key => this._handleSelect(key)}>
                    <NavItem key="divider" disabled title="Rule Type" className={WizardStyle.divider}>{this.state.messages.ruleType}</NavItem>
                    <NavItem eventKey={'COUNT'} title={this.state.messages.tooltipCountCondition}>
                        <FormattedMessage id= "wizard.countCondition" defaultMessage= "Count" />
                    </NavItem>
                    <NavItem eventKey={'GROUP_DISTINCT'} title={this.state.messages.tooltipGroupDistinctCondition} disabled={!this.state.isPluginAggregation}>
                        <FormattedMessage id= "wizard.groupDistinctCondition" defaultMessage= "Group / Distinct" />
                    </NavItem>
                    <NavItem eventKey={'STATISTICAL'} title={this.state.messages.tooltipStatisticalCondition}>
                        <FormattedMessage id= "wizard.StatisticsCondition" defaultMessage= "Statistics" />
                    </NavItem>
                    <NavItem eventKey={'THEN'} title={this.state.messages.tooltipThenCondition} disabled={!this.state.isPluginCorrelation}>
                        <FormattedMessage id= "wizard.thenCondition" defaultMessage= "THEN" />
                    </NavItem>
                    <NavItem eventKey={'AND'} title={this.state.messages.tooltipAndCondition} disabled={!this.state.isPluginCorrelation}>
                        <FormattedMessage id= "wizard.andCondition" defaultMessage= "AND" />
                    </NavItem>
                    <NavItem eventKey={'OR'} title={this.state.messages.tooltipOrCondition}>
                        <FormattedMessage id= "wizard.orCondition" defaultMessage= "OR" />
                    </NavItem>
                </Nav>
            );

        return (
        <div>
            <Row>
                <Col md={2} className={WizardStyle.subnavigation}>{subnavigation}</Col>
                <Col md={10} className={WizardStyle.contentpane}>
                    <h2>
                        <FormattedMessage id= "wizard.loadMessage" defaultMessage= "Load a message to test fields conditions" />
                    </h2>
                    <div className="stream-loader">
                        <LoaderTabs messageId={this.props.messageId} index={this.props.index}
                                    onMessageLoaded={this._onMessageLoaded}/>
                    </div>
                    <hr/>
                    <h2><FormattedMessage id= "wizard.titleParameters" defaultMessage= "Alert rule parameters" /></h2>
                    {customizeLink}
                    <p className="description"><FormattedMessage id= "wizard.descripionParameters" defaultMessage= "Define the parameters of the alert rule." /></p>                    
                    <form className="form-inline">
                        {this.state.contentComponent}
                    </form>
                    {actions}
                </Col>
            </Row>
        </div>
        );
    },
});

export default CreateAlertInput;
