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

// TODO remove all the refs!!!
// TODO move into sub-namespace configuration?

import PropTypes from 'prop-types';
import React from 'react';
import createReactClass from 'create-react-class';
import { Button, BootstrapModalForm, Col, FormGroup, Input, Table, Tooltip } from 'components/bootstrap';
import { Select, SortableList, Spinner, OverlayTrigger, IfPermitted } from 'components/common';
import ObjectUtils from 'util/ObjectUtils';
import { FormattedMessage } from 'react-intl';
import FormsUtils from 'util/FormsUtils';

// TODO convert into functional component
const ManageSettings = createReactClass({
    displayName: 'ManageSettings',

    propTypes: {
        onSave: PropTypes.func.isRequired,
        config: PropTypes.object.isRequired,
    },

    getInitialState() {
        return {
            config: {
                field_order: this.props.config.field_order,
                default_values: this.props.config.default_values,
                import_policy: this.props.config.import_policy,
            },
            showModal: false
        };
    },
    
    componentDidMount(){
        this.setState({config: this.props.config});
    },

    _openModal() {
        this.setState({ showModal: true });
    },

    _closeModal() {
        this.setState({ showModal: false });
    },

    _saveConfig() {
        this.props.onSave(this.state.config);
        this._closeModal();
    },

    _resetConfig() {
        // Reset to initial state when the modal is closed without saving.
        this.setState(this.getInitialState());
    },

    _availableFieldName() {
        return [
            {value: 'Priority', label: <FormattedMessage id="wizard.priority" defaultMessage="Priority" />},
            {value: 'Description', label: <FormattedMessage id="wizard.fieldDescription" defaultMessage="Description" />},
            {value: 'Created', label: <FormattedMessage id="wizard.created" defaultMessage="Created" />},
            {value: 'Last Modified', label: <FormattedMessage id="wizard.lastModified" defaultMessage="Last Modified" />},
            {value: 'User', label: <FormattedMessage id="wizard.user" defaultMessage="User" />},
            {value: 'Status', label: <FormattedMessage id="wizard.status" defaultMessage="Status" />},
            {value: 'Rule', label: <FormattedMessage id="wizard.rule" defaultMessage="Rule" />},
        ];
    },
    
    _getFieldName(field) {
        return this._availableFieldName().filter((t) => t.value === field)[0].label;
    },
    
    _updateSorting(newSorting) {
        const update = ObjectUtils.clone(this.state.config);

        update.field_order = newSorting.map((item) => {
            return {name: item.id, enabled: item.enabled};
        });

        this.setState({config: update});
    },

    _sortableItems() {
        return this.state.config.field_order.map((field) => {
            return {id: field.name, title: this._getFieldName(field.name), enabled: field.enabled};
        });
    },

    _toggleStatus(idx) {
        return () => {
            let update = ObjectUtils.clone(this.state.config);
            update.field_order[idx].enabled = this.refs[idx].checked;
            this.setState({config: update});
        };
    },

    _statusForm() {
        return ObjectUtils.clone(this.state.config.field_order).map((field, idx) => {
            return (
                <tr key={idx}>
                    <td>{this._getFieldName(field.name)}</td>
                    <td>
                        <input ref={idx}
                               type="checkbox"
                               checked={field.enabled}
                               onChange={this._toggleStatus(idx)}/>
                    </td>
                </tr>
            );
        });
    },

    _isLoading() {
        return !this.state.config;
    },

    _availablePriorityTypes() {
        return [
            {value: '1', label: <FormattedMessage id="wizard.low" defaultMessage="Low" />},
            {value: '2', label: <FormattedMessage id="wizard.medium" defaultMessage="Normal" />},
            {value: '3', label: <FormattedMessage id="wizard.high" defaultMessage="High" />},
        ];
    },
    _availableMatchingType() {
        return [
            {value: 'AND', label: <FormattedMessage id="wizard.all" defaultMessage="all" />},
            {value: 'OR', label: <FormattedMessage id="wizard.atLeastOne" defaultMessage="at least one" />},
        ];
    },
    // TODO should factor constant with AlertRuleText.jsx?
    _availableThresholdTypes() {
        return [
            {value: '>', label: <FormattedMessage id="wizard.more" defaultMessage="more than" />},
            {value: '<', label: <FormattedMessage id="wizard.less" defaultMessage="less than" />},
        ];
    },
    _availableTimeTypes() {
        return [
            {value: '1', label: <FormattedMessage id="wizard.minutes" defaultMessage="minutes" />},
            {value: '60', label: <FormattedMessage id="wizard.hours" defaultMessage="hours" />},
            {value: '1440', label: <FormattedMessage id="wizard.days" defaultMessage="days" />},
        ];
    },

    _updateConfig(field, value) {
        const update = ObjectUtils.clone(this.state.config);
        update.default_values[field] = value;
        this.setState({config: update});
    },
    _onValueChanged(field) {
        return e => {
            this._updateConfig(field, e.target.value);
        };
    },
    _onPriorityTypeSelect(value) {
        this._updateConfig('priority', parseInt(value))
    },
    _onMatchingTypeSelect(value) {
        this._updateConfig('matching_type', value)
    },
    _onThresholdTypeSelect(value) {
        this._updateConfig('threshold_type', value)
    },
    _onTimeTypeSelect(value) {
        this._updateConfig('time_type', parseInt(value))
    },
    _onCheckboxClick(event) {
        this._updateConfig(event.target.name, event.target.checked);
    },
    _onRadioChange(event){
        const update = ObjectUtils.clone(this.state.config);
        update.import_policy = FormsUtils.getValueFromInput(event.target);
        this.setState({config: update});
    },

    render() {
        if (this._isLoading()) {
            return <Spinner/>;
        }

        const tooltipReplace = (<FormattedMessage id ="wizard.tooltipReplace" defaultMessage="Replace the alert rule with the one you are importing" />);
        
        const tooltipDoNothing = (<FormattedMessage id ="wizard.tooltipDoNothing" defaultMessage="No alert rules will be changed" />);
        
        const tooltipRename = (<FormattedMessage id ="wizard.tooltipRename" defaultMessage="The alert rule you are importing will be renamed <title(1)>" />);

        return (
            <span>
                <IfPermitted permissions="clusterconfigentry:edit">
                    <Button bsStyle="info" bsSize="xs" onClick={this._openModal}>Edit configuration</Button>
                </IfPermitted>

        <BootstrapModalForm show={this.state.showModal}
                            title={<FormattedMessage id="wizard.manageWizardSettings" defaultMessage="Manage Wizard settings" />}
                            onSubmitForm={this._saveConfig}
                            onCancel={this._resetConfig}
                            cancelButtonText={<FormattedMessage id="wizard.cancel" defaultMessage="Cancel" />}
                            submitButtonText={<FormattedMessage id="wizard.save" defaultMessage="Save" />}>
            <div>
              <Col md={6}>
                <h3><FormattedMessage id="wizard.order" defaultMessage="Order" /></h3>
                <p><FormattedMessage id="wizard.descriptionOrder" defaultMessage="Use drag and drop to change the order." /></p>
                <SortableList items={this._sortableItems()} onMoveItem={this._updateSorting}/>
                
                <h3><FormattedMessage id="wizard.status" defaultMessage="Status" /></h3>
                <p><FormattedMessage id="wizard.checkboxesStatus" defaultMessage="Change the checkboxes to change the status." /></p>
                <Table striped bordered condensed>
                <thead>
                  <tr>
                    <th><FormattedMessage id="wizard.field" defaultMessage="Field" /></th>
                    <th><FormattedMessage id="wizard.enabled" defaultMessage="Enabled" /></th>
                  </tr>
                </thead>
                <tbody>
                  {this._statusForm()}
                </tbody>
                </Table>
                  
                <h3><FormattedMessage id="wizard.importPolicy" defaultMessage="Import strategy" /></h3>
                <p><FormattedMessage id="wizard.descriptionImportPolicy" defaultMessage="Choose the action when the alert rule already exists." /></p>
                <FormGroup>
                  <OverlayTrigger overlay={tooltipDoNothing} placement="top" trigger={['hover', 'focus']}>
                      <div className="radio">
                        <label>
                          <input type="radio" value="DONOTHING" checked={this.state.config.import_policy === 'DONOTHING'} onChange={this._onRadioChange}/>
                            <FormattedMessage id="wizard.doNothing" defaultMessage="Don't import" />
                        </label>
                      </div>
                  </OverlayTrigger>
                  <OverlayTrigger overlay={tooltipReplace} placement="top" trigger={['hover', 'focus']}>
                      <div className="radio">
                        <label>
                          <input type="radio" value="REPLACE" checked={this.state.config.import_policy === 'REPLACE'} onChange={this._onRadioChange}/>
                            <FormattedMessage id="wizard.replace" defaultMessage="Import and Replace" />
                          </label>
                      </div>
                  </OverlayTrigger>
                  <OverlayTrigger overlay={tooltipRename} placement="top" trigger={['hover', 'focus']}>
                      <div className="radio">
                        <label>
                          <input type="radio" value="RENAME" checked={this.state.config.import_policy === 'RENAME'} onChange={this._onRadioChange}/>
                            <FormattedMessage id="wizard.rename" defaultMessage="Import, but keep both alert rules" />
                        </label>
                      </div>
                  </OverlayTrigger>
                </FormGroup>
              </Col>
              <Col md={6}> 
                <h3><FormattedMessage id="wizard.defaultValues" defaultMessage="Default values of the alert rule" /></h3>
                <p><FormattedMessage id="wizard.changeDefaultValues" defaultMessage="Change the default values." /></p>
                <fieldset style={{ marginBottom: '15px' }}>
                <Input ref="title" id="title" name="title" type="text" label={<FormattedMessage id ="wizard.title" defaultMessage="Title" />}
                       value={this.state.config.default_values.title} onChange={this._onValueChanged("title")}/>
                <Input
                    ref="priority"
                    id="priority"
                    label={<FormattedMessage id ="wizard.alertPriority" defaultMessage="Alert Priority" />}
                    help={<FormattedMessage id ="wizard.descriptionAlertPriority" defaultMessage="The default priority of logged alerts when adding a new notification" />}
                    name="priority">
                  <Select placeholder={<FormattedMessage id="wizard.select" defaultMessage="Select..." />}
                          options={this._availablePriorityTypes()}
                          matchProp="value"
                          value={this.state.config.default_values.priority? this.state.config.default_values.priority.toString() : ''}
                          onChange={this._onPriorityTypeSelect}
                          clearable={false}
                  />
                </Input>     
                <Input ref="matching_type" id="matching_type" name="matching_type" 
                       label={<FormattedMessage id="wizard.matchingType" defaultMessage="Matching type" />}>
                    <Select placeholder={<FormattedMessage id="wizard.select" defaultMessage="Select..." />}
                        autosize={false}
                        value={this.state.config.default_values.matching_type}
                        options={this._availableMatchingType()}
                        matchProp="value"
                        onChange={this._onMatchingTypeSelect}
                        clearable={false}
                    />
                </Input>
                <Input ref="threshold_type" id="threshold_type" name="threshold_type" 
                       label={<FormattedMessage id="wizard.thresholdType" defaultMessage="Threshold type" />}>
                    <Select placeholder={<FormattedMessage id="wizard.select" defaultMessage="Select..." />}
                        autosize={false}
                        value={this.state.config.default_values.threshold_type}
                        options={this._availableThresholdTypes()}
                        matchProp="value"
                        onChange={this._onThresholdTypeSelect}
                        clearable={false}
                    />
                </Input>
                <Input ref="threshold" id="threshold" name="threshold" type="number" min="0"
                       label={<FormattedMessage id="wizard.threshold" defaultMessage="Threshold" />}
                       onChange={this._onValueChanged("threshold")} value={this.state.config.default_values.threshold}/> 
                <Input ref="time" id="time" name="time" type="number" min="1"
                       label={<FormattedMessage id="wizard.time" defaultMessage="Time Range" />}
                       onChange={this._onValueChanged("time")} value={this.state.config.default_values.time}/>
                <Input ref="time_type" id="time_type" name="time_type" 
                       label={<FormattedMessage id="wizard.timeType" defaultMessage="Time Range unit" />}>
                    <Select placeholder={<FormattedMessage id="wizard.select" defaultMessage="Select..." />}
                        autosize={false}
                        value={this.state.config.default_values.time_type? this.state.config.default_values.time_type.toString() : ''}
                        options={this._availableTimeTypes()}
                        matchProp="value"
                        onChange={this._onTimeTypeSelect}
                        clearable={false}
                    />
                </Input>
              </fieldset>
              <h3><FormattedMessage id="wizard.conditionDefaultValues" defaultMessage="Default values of the event" /></h3>
              <p><FormattedMessage id="wizard.changeDefaultValues" defaultMessage="Change the default values." /></p>
              <fieldset>
                <Input ref="backlog" id="backlog" name="backlog" type="number" min="0"
                       label={<FormattedMessage id="wizard.msgBacklog" defaultMessage="Message Backlog" />}
                       onChange={this._onValueChanged("backlog")} value={this.state.config.default_values.backlog}/>
                <Input ref="grace" id="grace" name="grace" type="number" min="1"
                       label={<FormattedMessage id="wizard.gracePeriod" defaultMessage="Execute search every (minutes)" />}
                       onChange={this._onValueChanged("grace")} value={this.state.config.default_values.grace}/> 
              </fieldset>
            </Col>
          </div>
        </BootstrapModalForm>
      </span>
        );
    },
});

export default ManageSettings;
