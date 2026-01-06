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
import React, {useState} from 'react';
import { Button, BootstrapModalForm, Col, FormGroup, Input, Table, Tooltip } from 'components/bootstrap';
import { Select, SortableList, Spinner, OverlayTrigger, IfPermitted } from 'components/common';
import ObjectUtils from 'util/ObjectUtils';
import { FormattedMessage } from 'react-intl';
import FormsUtils from 'util/FormsUtils';

const ManageSettings = ({config, onSave}) => {

    const _availableFieldName = [
        {value: 'Priority', label: <FormattedMessage id="wizard.priority" defaultMessage="Priority" />},
        {value: 'Description', label: <FormattedMessage id="wizard.fieldDescription" defaultMessage="Description" />},
        {value: 'Created', label: <FormattedMessage id="wizard.created" defaultMessage="Created" />},
        {value: 'Last Modified', label: <FormattedMessage id="wizard.lastModified" defaultMessage="Last Modified" />},
        {value: 'User', label: <FormattedMessage id="wizard.user" defaultMessage="User" />},
        {value: 'Status', label: <FormattedMessage id="wizard.status" defaultMessage="Status" />},
        {value: 'Rule', label: <FormattedMessage id="wizard.rule" defaultMessage="Rule" />},
    ];

    const [state, setState] = useState({
        config: config,
        showModal: false
    });

    const _openModal = () => {
        setState({ ...state, showModal: true });
    };

    const _closeModal = () => {
        setState({ ...state, showModal: false });
    };

    const _saveConfig = () => {
        onSave(state.config);
        _closeModal();
    };

    const _resetConfig = () => {
        setState({
            config: config,
            showModal: false
        });
    };
    
    const _getFieldName = (field) => {
        return _availableFieldName.filter((t) => t.value === field)[0].label;
    };
    
    const _updateSorting = (newSorting) => {
        const update = ObjectUtils.clone(state.config);

        update.field_order = newSorting.map((item) => {
            return {name: item.id, enabled: item.enabled};
        });

        setState({ ...state, config: update});
    };

    const _sortableItems = () => {
        return state.config.field_order.map((field) => {
            return {id: field.name, title: _getFieldName(field.name), enabled: field.enabled};
        });
    };

    const _toggleStatus = (event, idx) => {
        let update = ObjectUtils.clone(state.config);
        update.field_order[idx].enabled = event.target.checked;
        setState({...state, config: update});
    };

    const _statusForm = () => {
        return ObjectUtils.clone(state.config.field_order).map((field, idx) => {
            return (
                <tr key={idx}>
                    <td>{_getFieldName(field.name)}</td>
                    <td>
                        <Input type="checkbox"
                               checked={field.enabled}
                               onChange={event => _toggleStatus(event, idx)}/>
                    </td>
                </tr>
            );
        });
    };

    const _isLoading = () => {
        return !state.config;
    };

    const _availablePriorityTypes = () => {
        return [
            {value: '1', label: <FormattedMessage id="wizard.low" defaultMessage="Low" />},
            {value: '2', label: <FormattedMessage id="wizard.medium" defaultMessage="Normal" />},
            {value: '3', label: <FormattedMessage id="wizard.high" defaultMessage="High" />},
        ];
    };

    const _availableMatchingType = () => {
        return [
            {value: 'AND', label: <FormattedMessage id="wizard.all" defaultMessage="all" />},
            {value: 'OR', label: <FormattedMessage id="wizard.atLeastOne" defaultMessage="at least one" />},
        ];
    };

    const _availableThresholdTypes = () => {
        return [
            {value: '>', label: <FormattedMessage id="wizard.more" defaultMessage="more than" />},
            {value: '<', label: <FormattedMessage id="wizard.less" defaultMessage="less than" />},
        ];
    };

    const _availableTimeTypes = () => {
        return [
            {value: '1', label: <FormattedMessage id="wizard.minutes" defaultMessage="minutes" />},
            {value: '60', label: <FormattedMessage id="wizard.hours" defaultMessage="hours" />},
            {value: '1440', label: <FormattedMessage id="wizard.days" defaultMessage="days" />},
        ];
    };

    const _updateConfig = (field, value) => {
        const update = ObjectUtils.clone(state.config);
        update.default_values[field] = value;
        setState({...state, config: update});
    };

    const _onValueChanged = (field) => {
        return e => {
            _updateConfig(field, e.target.value);
        };
    };

    const _onPriorityTypeSelect = (value) => {
        _updateConfig('priority', parseInt(value));
    };

    const _onMatchingTypeSelect = (value) => {
        _updateConfig('matching_type', value);
    };

    const _onThresholdTypeSelect = (value) => {
        _updateConfig('threshold_type', value);
    };

    const _onTimeTypeSelect = (value) => {
        _updateConfig('time_type', parseInt(value));
    };

    const _onCheckboxClick = (event) => {
        _updateConfig(event.target.name, event.target.checked);
    };

    const _onRadioChange = (event) => {
        const update = ObjectUtils.clone(state.config);
        update.import_policy = FormsUtils.getValueFromInput(event.target);
        setState({...state, config: update});
    }

    const tooltipReplace = (<FormattedMessage id ="wizard.tooltipReplace" defaultMessage="Replace the alert rule with the one you are importing" />);
    const tooltipDoNothing = (<FormattedMessage id ="wizard.tooltipDoNothing" defaultMessage="No alert rules will be changed" />);
    const tooltipRename = (<FormattedMessage id ="wizard.tooltipRename" defaultMessage="The alert rule you are importing will be renamed <title(1)>" />);

    if (_isLoading()) {
        return <Spinner/>;
    }

    return (
        <span>
            <IfPermitted permissions="clusterconfigentry:edit">
                <Button bsStyle="info" bsSize="xs" onClick={_openModal}>Edit configuration</Button>
            </IfPermitted>

            <BootstrapModalForm show={state.showModal}
                                title={<FormattedMessage id="wizard.manageWizardSettings" defaultMessage="Manage Wizard settings" />}
                                onSubmitForm={_saveConfig}
                                onCancel={_resetConfig}
                                cancelButtonText={<FormattedMessage id="wizard.cancel" defaultMessage="Cancel" />}
                                submitButtonText={<FormattedMessage id="wizard.save" defaultMessage="Save" />}>
            <div>
              <Col md={6}>
                <h3><FormattedMessage id="wizard.order" defaultMessage="Order" /></h3>
                <p><FormattedMessage id="wizard.descriptionOrder" defaultMessage="Use drag and drop to change the order." /></p>
                <SortableList items={_sortableItems()} onMoveItem={_updateSorting}/>

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
                  {_statusForm()}
                </tbody>
                </Table>

                <h3><FormattedMessage id="wizard.importPolicy" defaultMessage="Import strategy" /></h3>
                <p><FormattedMessage id="wizard.descriptionImportPolicy" defaultMessage="Choose the action when the alert rule already exists." /></p>
                <FormGroup>
                  <OverlayTrigger overlay={tooltipDoNothing} placement="top" trigger={['hover', 'focus']}>
                      <div className="radio">
                        <label>
                          <input type="radio" value="DONOTHING" checked={state.config.import_policy === 'DONOTHING'} onChange={_onRadioChange}/>
                            <FormattedMessage id="wizard.doNothing" defaultMessage="Don't import" />
                        </label>
                      </div>
                  </OverlayTrigger>
                  <OverlayTrigger overlay={tooltipReplace} placement="top" trigger={['hover', 'focus']}>
                      <div className="radio">
                        <label>
                          <input type="radio" value="REPLACE" checked={state.config.import_policy === 'REPLACE'} onChange={_onRadioChange}/>
                            <FormattedMessage id="wizard.replace" defaultMessage="Import and Replace" />
                          </label>
                      </div>
                  </OverlayTrigger>
                  <OverlayTrigger overlay={tooltipRename} placement="top" trigger={['hover', 'focus']}>
                      <div className="radio">
                        <label>
                          <input type="radio" value="RENAME" checked={state.config.import_policy === 'RENAME'} onChange={_onRadioChange}/>
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
                <Input id="title" name="title" type="text" label={<FormattedMessage id ="wizard.title" defaultMessage="Title" />}
                       value={state.config.default_values.title} onChange={_onValueChanged("title")}/>
                <Input
                    id="priority"
                    label={<FormattedMessage id ="wizard.alertPriority" defaultMessage="Alert Priority" />}
                    help={<FormattedMessage id ="wizard.descriptionAlertPriority" defaultMessage="The default priority of logged alerts when adding a new notification" />}
                    name="priority">
                  <Select placeholder={<FormattedMessage id="wizard.select" defaultMessage="Select..." />}
                          options={_availablePriorityTypes()}
                          matchProp="value"
                          value={state.config.default_values.priority? state.config.default_values.priority.toString() : ''}
                          onChange={_onPriorityTypeSelect}
                          clearable={false}
                  />
                </Input>
                <Input id="matching_type" name="matching_type"
                       label={<FormattedMessage id="wizard.matchingType" defaultMessage="Matching type" />}>
                    <Select placeholder={<FormattedMessage id="wizard.select" defaultMessage="Select..." />}
                        autosize={false}
                        value={state.config.default_values.matching_type}
                        options={_availableMatchingType()}
                        matchProp="value"
                        onChange={_onMatchingTypeSelect}
                        clearable={false}
                    />
                </Input>
                <Input id="threshold_type" name="threshold_type"
                       label={<FormattedMessage id="wizard.thresholdType" defaultMessage="Threshold type" />}>
                    <Select placeholder={<FormattedMessage id="wizard.select" defaultMessage="Select..." />}
                        autosize={false}
                        value={state.config.default_values.threshold_type}
                        options={_availableThresholdTypes()}
                        matchProp="value"
                        onChange={_onThresholdTypeSelect}
                        clearable={false}
                    />
                </Input>
                <Input id="threshold" name="threshold" type="number" min="0"
                       label={<FormattedMessage id="wizard.threshold" defaultMessage="Threshold" />}
                       onChange={_onValueChanged("threshold")} value={state.config.default_values.threshold}/>
                <Input id="time" name="time" type="number" min="1"
                       label={<FormattedMessage id="wizard.time" defaultMessage="Time Range" />}
                       onChange={_onValueChanged("time")} value={state.config.default_values.time}/>
                <Input id="time_type" name="time_type"
                       label={<FormattedMessage id="wizard.timeType" defaultMessage="Time Range unit" />}>
                    <Select placeholder={<FormattedMessage id="wizard.select" defaultMessage="Select..." />}
                        autosize={false}
                        value={state.config.default_values.time_type? state.config.default_values.time_type.toString() : ''}
                        options={_availableTimeTypes()}
                        matchProp="value"
                        onChange={_onTimeTypeSelect}
                        clearable={false}
                    />
                </Input>
                <Input id="aggregation_time" name="aggregation_time" type="number" min="0"
                       label={<FormattedMessage id="wizard.aggregationTime" defaultMessage="Notification Aggregation Time Range (minutes)" />}
                       onChange={_onValueChanged("aggregation_time")} value={state.config.default_values.aggregation_time}/>
              </fieldset>
              <h3><FormattedMessage id="wizard.conditionDefaultValues" defaultMessage="Default values of the event" /></h3>
              <p><FormattedMessage id="wizard.changeDefaultValues" defaultMessage="Change the default values." /></p>
              <fieldset>
                <Input id="backlog" name="backlog" type="number" min="0"
                       label={<FormattedMessage id="wizard.msgBacklog" defaultMessage="Message Backlog" />}
                       onChange={_onValueChanged("backlog")} value={state.config.default_values.backlog}/>
                <Input id="grace" name="grace" type="number" min="1"
                       label={<FormattedMessage id="wizard.gracePeriod" defaultMessage="Execute search every (minutes)" />}
                       onChange={_onValueChanged("grace")} value={state.config.default_values.grace}/>
              </fieldset>
            </Col>
          </div>
        </BootstrapModalForm>
    </span>
    );
}

ManageSettings.propTypes = {
    onSave: PropTypes.func.isRequired,
    config: PropTypes.object.isRequired,
}

export default ManageSettings;
