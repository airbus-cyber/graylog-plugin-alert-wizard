import PropTypes from 'prop-types';
import React from 'react';
import {Alert, Button, Col, Row, Table, FormGroup, Tooltip} from 'react-bootstrap';
import {LinkContainer} from 'react-router-bootstrap';
import {Select, SortableList, Spinner, OverlayElement} from 'components/common';
import ObjectUtils from 'util/ObjectUtils';
import {BootstrapModalForm, Input} from 'components/bootstrap';
import {FormattedMessage} from 'react-intl';
import FormsUtils from 'util/FormsUtils';

const ManageSettings = React.createClass({

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
        };
    },
    
    componentWillReceiveProps(nextProps){
        this.setState({config:nextProps.config});
    },

    _openModal() {
        this.refs.configModal.open();
    },

    _closeModal() {
        this.refs.configModal.close();
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
            {value: 'Severity', label: <FormattedMessage id= "wizard.severity" defaultMessage= "Severity" />},
            {value: 'Description', label: <FormattedMessage id= "wizard.fieldDescription" defaultMessage= "Description" />},
            {value: 'Created', label: <FormattedMessage id= "wizard.created" defaultMessage= "Created" />},
            {value: 'Last Modified', label: <FormattedMessage id= "wizard.lastModified" defaultMessage= "Last Modified" />},
            {value: 'User', label: <FormattedMessage id= "wizard.user" defaultMessage= "User" />},
            {value: 'Alerts', label: <FormattedMessage id= "wizard.alerts" defaultMessage= "Alerts" />},
            {value: 'Status', label: <FormattedMessage id= "wizard.status" defaultMessage= "Status" />},
            {value: 'Rule', label: <FormattedMessage id= "wizard.rule" defaultMessage= "Rule" />},
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

    _availableSeverityTypes() {
        return [
            {value: 'info', label: <FormattedMessage id= "wizard.info" defaultMessage= "Info" />},
            {value: 'low', label: <FormattedMessage id= "wizard.low" defaultMessage= "Low" />},
            {value: 'medium', label: <FormattedMessage id= "wizard.medium" defaultMessage= "Medium" />},
            {value: 'high', label: <FormattedMessage id= "wizard.high" defaultMessage= "High" />},
        ];
    },
    _availableMatchingType() {
        return [
            {value: 'AND', label: <FormattedMessage id= "wizard.all" defaultMessage= "all" />},
            {value: 'OR', label: <FormattedMessage id= "wizard.atLeastOne" defaultMessage= "at least one" />},
        ];
    },
    _availableThresholdTypes() {
        return [
            {value: 'MORE', label: <FormattedMessage id= "wizard.more" defaultMessage= "more than" />},
            {value: 'LESS', label: <FormattedMessage id= "wizard.less" defaultMessage= "less than" />},
        ];
    },
    _availableTimeTypes() {
        return [
            {value: 1, label: <FormattedMessage id= "wizard.minutes" defaultMessage= "minutes" />},
            {value: 60, label: <FormattedMessage id= "wizard.hours" defaultMessage= "hours" />},
            {value: 1440, label: <FormattedMessage id= "wizard.days" defaultMessage= "days" />},
        ];
    },
    _availableRuleType() {
        return [
            {value: 1, label: <FormattedMessage id= "wizard.matchesExactly" defaultMessage= "matches exactly" />},
            {value: -1, label: <FormattedMessage id= "wizard.notMatchesExactly" defaultMessage= "does not match exactly" />},
            {value: 2, label: <FormattedMessage id= "wizard.matchesRegularExpression" defaultMessage= "matches regular expression" />},
            {value: -2, label: <FormattedMessage id= "wizard.notMatchRegularExpression" defaultMessage= "does not match regular expression" />},
            {value: 3, label: <FormattedMessage id= "wizard.greaterThan" defaultMessage= "is greater than" />},
            {value: -3, label: <FormattedMessage id= "wizard.notGreaterThan" defaultMessage= "is not greater than" />},
            {value: 4, label: <FormattedMessage id= "wizard.smallerThan" defaultMessage= "is smaller than" />},
            {value: -4, label: <FormattedMessage id= "wizard.notSmallerThan" defaultMessage= "is not smaller than" />},
            {value: 5, label: <FormattedMessage id= "wizard.present" defaultMessage= "is present" />},
            {value: -5, label: <FormattedMessage id= "wizard.notPresent" defaultMessage= "is not present" />},
            {value: 6, label: <FormattedMessage id= "wizard.contains" defaultMessage= "contains" />},
            {value: -6, label: <FormattedMessage id= "wizard.notContain" defaultMessage= "does not contain" />},
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
    _onSeverityTypeSelect(value) {
        this._updateConfig('severity', value)
    },
    _onMatchingTypeSelect(value) {
        this._updateConfig('matching_type', value)
    },
    _onThresholdTypeSelect(value) {
        this._updateConfig('threshold_type', value)
    },
    _onTimeTypeSelect(value) {
        this._updateConfig('time_type', value)
    },
    _onRuleTypeSelect(value) {
        this._updateConfig('field_type', value);
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
        
        const tooltipReplace = (
                <Tooltip id="doNothing-tooltip">
                    <FormattedMessage id ="wizard.tooltipReplace" defaultMessage="Replace the alert rule with the one you are importing" />
                </Tooltip>);
        
        const tooltipDoNothing = (
                <Tooltip id="doNothing-tooltip">
                    <FormattedMessage id ="wizard.tooltipDoNothing" defaultMessage="No alert rules will be changed" />
                </Tooltip>);
        
        const tooltipRename = (
                <Tooltip id="doNothing-tooltip">
                    <FormattedMessage id ="wizard.tooltipRename" defaultMessage="The alert rule you are importing will be renamed <title(1)>" />
                </Tooltip>);

        return (
            <span>
        <Button bsStyle="info" onClick={this._openModal}><FormattedMessage id= "wizard.manageSettings" defaultMessage= "Manage settings" /></Button>
        <BootstrapModalForm ref="configModal"
                            title={<FormattedMessage id= "wizard.manageWizardSettings" defaultMessage= "Manage Wizard settings" />}
                            onSubmitForm={this._saveConfig}
                            onModalClose={this._resetConfig}
                            cancelButtonText={<FormattedMessage id= "wizard.cancel" defaultMessage= "Cancel" />}
                            submitButtonText={<FormattedMessage id= "wizard.save" defaultMessage= "Save" />}>
            <div>
              <Col md={6}>                   
                <h3><FormattedMessage id= "wizard.order" defaultMessage= "Order" /></h3>
                <p><FormattedMessage id= "wizard.descriptionOrder" defaultMessage= "Use drag and drop to change the order." /></p>
                <SortableList items={this._sortableItems()} onMoveItem={this._updateSorting}/>
                
                <h3><FormattedMessage id= "wizard.status" defaultMessage= "Status" /></h3>
                <p><FormattedMessage id= "wizard.checkboxesStatus" defaultMessage= "Change the checkboxes to change the status." /></p>
                <Table striped bordered condensed className="top-margin">
                <thead>
                  <tr>
                    <th><FormattedMessage id= "wizard.field" defaultMessage= "Field" /></th>
                    <th><FormattedMessage id= "wizard.enabled" defaultMessage= "Enabled" /></th>
                  </tr>
                </thead>
                <tbody>
                  {this._statusForm()}
                </tbody>
                </Table>
                  
                <h3><FormattedMessage id= "wizard.importPolicy" defaultMessage= "Import strategy" /></h3>
                <p><FormattedMessage id= "wizard.descriptionImportPolicy" defaultMessage= "Choose the action when the alert rule already exists." /></p>
                <FormGroup>
                  <OverlayElement overlay={tooltipDoNothing} placement="top" useOverlay={true} trigger={['hover', 'focus']}>
                      <div className="radio">
                        <label>
                          <input type="radio" value="DONOTHING" checked={this.state.config.import_policy === 'DONOTHING'} onChange={this._onRadioChange}/>
                            <FormattedMessage id= "wizard.doNothing" defaultMessage= "Don't import" />
                        </label>
                      </div>
                  </OverlayElement>
                  <OverlayElement overlay={tooltipReplace} placement="top" useOverlay={true} trigger={['hover', 'focus']}>
                      <div className="radio">
                        <label>
                          <input type="radio" value="REPLACE" checked={this.state.config.import_policy === 'REPLACE'} onChange={this._onRadioChange}/>
                            <FormattedMessage id= "wizard.replace" defaultMessage= "Import and Replace" />
                          </label>
                      </div>
                  </OverlayElement>
                  <OverlayElement overlay={tooltipRename} placement="top" useOverlay={true} trigger={['hover', 'focus']}>
                      <div className="radio">
                        <label>
                          <input type="radio" value="RENAME" checked={this.state.config.import_policy === 'RENAME'} onChange={this._onRadioChange}/>
                            <FormattedMessage id= "wizard.rename" defaultMessage= "Import, but keep both alert rules" />
                        </label>
                      </div>
                  </OverlayElement>
                </FormGroup>
                  
              </Col>
              <Col md={6}> 
                <h3><FormattedMessage id= "wizard.defaultValues" defaultMessage= "Default values of the alert rule" /></h3>
                <p><FormattedMessage id= "wizard.changeDefaultValues" defaultMessage= "Change the default values." /></p>
                <fieldset style={{ marginBottom: '15px' }}>
                <Input ref="title" id="title" name="title" type="text" label={<FormattedMessage id ="wizard.title" defaultMessage="Title" />}
                       value={this.state.config.default_values.title} onChange={this._onValueChanged("title")}/>
                <Input
                    id="severity"
                    label={<FormattedMessage id ="wizard.alertSeverity" defaultMessage="Alert Severity" />}
                    help={<FormattedMessage id ="wizard.descriptionAlertSeverity" defaultMessage="The default severity of logged alerts when adding a new notification" />}
                    name="severity">
                  <Select placeholder={<FormattedMessage id= "wizard.select" defaultMessage= "Select..." />}
                          options={this._availableSeverityTypes()}
                          matchProp="value"
                          value={this.state.config.default_values.severity}
                          onChange={this._onSeverityTypeSelect}
                  />
                </Input>     
                <Input ref="matching_type" id="matching_type" name="matching_type" 
                       label={<FormattedMessage id= "wizard.matchingType" defaultMessage= "Matching type" />}>
                    <Select placeholder={<FormattedMessage id= "wizard.select" defaultMessage= "Select..." />}
                        autosize={false}
                        value={this.state.config.default_values.matching_type}
                        options={this._availableMatchingType()}
                        matchProp="value"
                        onChange={this._onMatchingTypeSelect}
                    />
                </Input>
                <Input ref="threshold_type" id="threshold_type" name="threshold_type" 
                       label={<FormattedMessage id= "wizard.thresholdType" defaultMessage= "Threshold type" />}>
                    <Select placeholder={<FormattedMessage id= "wizard.select" defaultMessage= "Select..." />}
                        autosize={false}
                        value={this.state.config.default_values.threshold_type}
                        options={this._availableThresholdTypes()}
                        matchProp="value"
                        onChange={this._onThresholdTypeSelect}
                    />
                </Input>
                <Input ref="threshold" id="threshold" name="threshold" type="number" 
                       label={<FormattedMessage id= "wizard.threshold" defaultMessage= "Threshold" />}
                       onChange={this._onValueChanged("threshold")} value={this.state.config.default_values.threshold}/> 
                <Input ref="time" id="time" name="time" type="number" 
                       label={<FormattedMessage id= "wizard.time" defaultMessage= "Time Range" />}
                       onChange={this._onValueChanged("time")} value={this.state.config.default_values.time}/>
                <Input ref="time_type" id="time_type" name="time_type" 
                       label={<FormattedMessage id= "wizard.timeType" defaultMessage= "Time Range unit" />}>
                    <Select placeholder={<FormattedMessage id= "wizard.select" defaultMessage= "Select..." />}
                        autosize={false}
                        value={this.state.config.default_values.time_type}
                        options={this._availableTimeTypes()}
                        matchProp="value"
                        onChange={this._onTimeTypeSelect}
                    />
                </Input>

                <Input ref="field" id="field" name="field" type="text" 
                       label={<FormattedMessage id= "wizard.field" defaultMessage= "Field" />}
                       onChange={this._onValueChanged("field")} value={this.state.config.default_values.field}/>
                <Input ref="typefield_type" id="field_type" name="field_type" 
                       label={<FormattedMessage id= "wizard.fieldType" defaultMessage= "Field type" />}>
                    <Select placeholder={<FormattedMessage id= "wizard.select" defaultMessage= "Select..." />}
                        autosize={true}
                        value={this.state.config.default_values.field_type}
                        options={this._availableRuleType()}
                        matchProp="value"
                        onChange={this._onRuleTypeSelect}
                    />
                </Input>
                <Input ref="field_value" id="field_value" name="field_value" type="text" 
                       label={<FormattedMessage id= "wizard.fieldValue" defaultMessage= "Field value" />}
                       onChange={this._onValueChanged("field_value")}
                       value={this.state.config.default_values.field_value}/>
              </fieldset>
              <h3><FormattedMessage id= "wizard.conditionDefaultValues" defaultMessage= "Default values of the alert condition" /></h3>
              <p><FormattedMessage id= "wizard.changeDefaultValues" defaultMessage= "Change the default values." /></p>
              <fieldset>   
                <Input ref="repeat_notifications" id="repeat_notifications" name="repeat_notifications" type="checkbox" 
                   label={<FormattedMessage id= "wizard.repeatNotifications" defaultMessage= "Repeat notifications" />}
                   checked={this.state.config.default_values.repeat_notifications}
                   onChange={this._onCheckboxClick} />    
                <Input ref="backlog" id="backlog" name="backlog" type="number" min="0"
                       label={<FormattedMessage id= "wizard.msgBacklog" defaultMessage= "Message Backlog" />}
                       onChange={this._onValueChanged("backlog")} value={this.state.config.default_values.backlog}/>
                <Input ref="grace" id="grace" name="grace" type="number" min="0"
                       label={<FormattedMessage id= "wizard.gracePeriod" defaultMessage= "Grace Period" />}
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
