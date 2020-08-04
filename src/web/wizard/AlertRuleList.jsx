import PropTypes from 'prop-types';
import React from 'react';
import Reflux from 'reflux';
import createReactClass from 'create-react-class';
import AlertRuleStore from './AlertRuleStore';
import AlertRuleActions from './AlertRuleActions';
import StoreProvider from 'injection/StoreProvider';
import {Tooltip} from 'react-bootstrap';
import {DataTable, IfPermitted, OverlayElement, Spinner, Timestamp} from 'components/common';
import {Button} from 'components/graylog';
import PermissionsMixin from 'util/PermissionsMixin';
import Routes from 'routing/Routes';
import {LinkContainer} from 'react-router-bootstrap';
import AlertForm from './AlertForm';
import DateTime from 'logic/datetimes/DateTime';
import {FormattedMessage} from 'react-intl';
import AlertRuleText from 'wizard/AlertRuleText'

const StreamsStore = StoreProvider.getStore('Streams');

const AlertRuleList = createReactClass({
    displayName: 'AlertRuleList',

    mixins: [Reflux.connect(AlertRuleStore), PermissionsMixin],

    propTypes: {
        config: PropTypes.object.isRequired,
    },

    contextTypes: {
        intl: PropTypes.object.isRequired,
    },
    
    componentWillMount(){
        const fieldsTitle = {
                title: this.context.intl.formatMessage({id: "wizard.title", defaultMessage: "Title"}),
                severity: this.context.intl.formatMessage({id: "wizard.severity", defaultMessage: "Severity"}),
                description: this.context.intl.formatMessage({id: "wizard.fieldDescription", defaultMessage: "Description"}),
                created: this.context.intl.formatMessage({id: "wizard.created", defaultMessage: "Created"}),
                lastModified: this.context.intl.formatMessage({id: "wizard.lastModified", defaultMessage: "Last Modified"}),
                user: this.context.intl.formatMessage({id: "wizard.user", defaultMessage: "User"}),
                alerts: this.context.intl.formatMessage({id: "wizard.alerts", defaultMessage: "Alerts"}),
                status: this.context.intl.formatMessage({id: "wizard.status", defaultMessage: "Status"}),
                actions: this.context.intl.formatMessage({id: "wizard.actions", defaultMessage: "Actions"}),
                rule: this.context.intl.formatMessage({id: "wizard.rule", defaultMessage: "Rule"})
        };
        const messages = {
                infoDelete: this.context.intl.formatMessage({id: "wizard.buttonInfoDelete", defaultMessage: "Delete this alert rule"}),
                infoUpdate: this.context.intl.formatMessage({id: "wizard.buttonInfoUpdate", defaultMessage: "Edit this alert rule"}),
                infoEnable: this.context.intl.formatMessage({id: "wizard.buttonInfoEnable", defaultMessage: "Enable this alert rule"}),
                infoDisable: this.context.intl.formatMessage({id: "wizard.buttonInfoDisable", defaultMessage: "Disable this alert rule"}),
                infoAdvanced: this.context.intl.formatMessage({id: "wizard.buttonInfoAdvanced", defaultMessage: "Advanced settings for this alert rule"}),
                infoClone: this.context.intl.formatMessage({id: "wizard.buttonInfoClone", defaultMessage: "Clone this alert rule"}),
                createAlert: this.context.intl.formatMessage({id: "wizard.createAlert", defaultMessage: "Create alert rule"}),
                importAlert: this.context.intl.formatMessage({id: "wizard.importAlert", defaultMessage: "Import alert rules"}),
                exportAlert: this.context.intl.formatMessage({id: "wizard.exportAlert",  defaultMessage :"Export alert rules"}),
                confirmDeletion: this.context.intl.formatMessage({id: "wizard.confirmDeletion",  defaultMessage :"Do you really want to delete the alert rule"}),
                confirmDisable: this.context.intl.formatMessage({id: "wizard.confirmDisable",  defaultMessage :"Do you really want to disable the alert rule"}),
        };
        
        this.setState({fieldsTitle:fieldsTitle});
        this.setState({messages:messages});
        this.list();
    },
    list() {
        AlertRuleActions.listWithData().then(newAlerts => {
            this.setState({alerts: newAlerts});
        });
    },
    deleteAlert(name) {
        AlertRuleActions.deleteByName(name);
    },
    _deleteAlertFunction(name) {
        return () => {
            if (window.confirm(`${this.state.messages.confirmDeletion} "${name}" ?`)) {
                this.deleteAlert(name);
            }
        };
    },
    _onResume(stream, stream2) {
        return () => {
            StreamsStore.resume(stream, response => response).finally(() => this.list());
            if(stream2 !== null && stream2 !== ''){
                StreamsStore.resume(stream2, response => response).finally(() => this.list()); 
            }
        }
    },
    _onPause(name, stream, stream2) {
        return () => {
            if (window.confirm(`${this.state.messages.confirmDisable} "${name}" ?`)) {
                StreamsStore.pause(stream, response => response).finally(() => this.list());
                if(stream2 !== null && stream2 !== ''){
                    StreamsStore.pause(stream2, response => response);
                }
            }
        }
    },
    _headerCellFormatter(header) {
        let formattedHeaderCell;

        switch (header.toLocaleLowerCase()) {
            case '':
                formattedHeaderCell = <th className="user-type">{header}</th>;
                break;
            case 'actions':
                formattedHeaderCell = <th className="actions">{header}</th>;
                break;
            default:
                formattedHeaderCell = <th>{header}</th>;
        }

        return formattedHeaderCell;
    },

    _onClone(name) {
        return () => {
            this.refs.cloneForm.open(name);
        }
    },

    _onCloneSubmit(name, title, description) {
        AlertRuleActions.clone(name, title, description).finally(() => this.list());
    },
    
    _availableSeverityTypes() {
        return [
            {value: 'info', label: <FormattedMessage id= "wizard.info" defaultMessage= "Info" />},
            {value: 'low', label: <FormattedMessage id= "wizard.low" defaultMessage= "Low" />},
            {value: 'medium', label: <FormattedMessage id= "wizard.medium" defaultMessage= "Medium" />},
            {value: 'high', label: <FormattedMessage id= "wizard.high" defaultMessage= "High" />},
        ];
    },
    _getSeverityType(type) {
        return this._availableSeverityTypes().filter((t) => t.value === type)[0].label;
    },
    
    _availableFieldName() {
        return [
            {value: 'Severity', label: this.state.fieldsTitle.severity},
            {value: 'Description', label: this.state.fieldsTitle.description},
            {value: 'Created', label: this.state.fieldsTitle.created},
            {value: 'Last Modified', label: this.state.fieldsTitle.lastModified},
            {value: 'User', label: this.state.fieldsTitle.user},
            {value: 'Alerts', label: this.state.fieldsTitle.alerts},
            {value: 'Status', label: this.state.fieldsTitle.status},
            {value: 'Rule', label: this.state.fieldsTitle.rule},
        ];
    },
    _getFieldName(field) {
        return this._availableFieldName().filter((t) => t.value === field)[0].label;
    },

    _alertInfoFormatter(alert) {

        let alertValid;
        let colorValid;
        let streamID;
        if(alert.condition_parameters === null || alert.stream === null){
            alertValid = false;
            colorValid = "#F7230C";
            streamID = '';
        }else{
            alertValid = true;
            colorValid = "#000000";
            streamID = alert.stream.id;
            if(alert.disabled){
                colorValid = "#ABABAB";
            }
        }
        
        let streamId2 = null;
        if(alert.second_stream){
            streamId2 = alert.second_stream.id;
        }
        
        const deleteAction = (
            <IfPermitted permissions="wizard_alerts_rules:delete">
                <button id="delete-alert" type="button" className="btn btn-md btn-primary"
                        title={this.state.messages.infoDelete} onClick={this._deleteAlertFunction(alert.title)}>
                    <FormattedMessage id ="wizard.delete" defaultMessage="Delete" />
                </button>
            </IfPermitted>
        );

        const updateAlert = (
            <IfPermitted permissions="wizard_alerts_rules:read">
                <LinkContainer to={Routes.pluginRoute('WIZARD_UPDATEALERT_ALERTRULETITLE')(alert.title.replace(/\//g, '%2F'))} disabled={!alertValid}>
                    <Button bsStyle="info" type="submit" title={this.state.messages.infoUpdate} >
                        <FormattedMessage id ="wizard.edit" defaultMessage="Edit" />
                    </Button>
                </LinkContainer>
            </IfPermitted>
        );

        let toggleStreamLink;
        if (alert.disabled) {
            toggleStreamLink = (
                <Button bsStyle="success" onClick={this._onResume(streamID, streamId2)} disabled={!alertValid}
                        title={this.state.messages.infoEnable} style={{whiteSpace: 'pre'}} >
                    <FormattedMessage id ="wizard.enable" defaultMessage="Enable " />
                </Button>
            );
        } else {
            toggleStreamLink = (
                <Button bsStyle="primary" onClick={this._onPause(alert.title, streamID, streamId2)} disabled={!alertValid}
                        title={this.state.messages.infoDisable} >
                    <FormattedMessage id ="wizard.disable" defaultMessage="Disable" />
                </Button>
            );
        }

        const customizeLink = (
                <LinkContainer disabled={!alertValid} to={Routes.ALERTS.DEFINITIONS.edit(alert.condition)} >
                    <Button bsStyle="info" title={this.state.messages.infoAdvanced} >
                        <FormattedMessage id ="wizard.advancedSettings" defaultMessage="Advanced settings" />
                    </Button>
                </LinkContainer>
        );

        const cloneAlert = (
            <Button id="clone-alert" type="button" bsStyle="info" onClick={this._onClone(alert.title)} disabled={!alertValid}
                    title={this.state.messages.infoClone} >
                <FormattedMessage id ="wizard.clone" defaultMessage="Clone" />
            </Button>
        );

        const actions = (
            <div className="alert-actions pull-left">
                {updateAlert}{' '}
                {customizeLink}{' '}
                {cloneAlert}{' '}
                {deleteAction}{' '}
                {toggleStreamLink}{' '}
            </div>
        );

        const tooltipAlertCount = (
            <Tooltip id="default-alert-count-tooltip">
                <FormattedMessage id ="wizard.tooltipAlerts" defaultMessage="The daily throughput and the total number of triggered alerts since the last modification of the alert rule" />
               </Tooltip>);
        
        const tooltipUser = (
                <Tooltip id="default-user-tooltip">
                    <FormattedMessage id ="wizard.tooltipUser" defaultMessage="The last user who modified the alert rule" />
                </Tooltip>);

        let nbDays = (DateTime.now() - DateTime.parseFromString(alert.last_modified).toMoment()) / 1000 / 60 / 60 / 24;
        let nbAlertDay = Math.round(alert.alert_count / Math.ceil(nbDays));
        
        let tabFields = [<td className="limited">{alert.title_condition}</td>];
        this.props.config.field_order.map((field) => {
            if (field.enabled) {
                switch (field.name) {
                    case 'Severity':
                        tabFields.push(<td className="limited">{alert.severity ? this._getSeverityType(alert.severity) : ''}</td>);
                        break;
                    case 'Description':
                        tabFields.push(<td className="limited"><span style={{whiteSpace: 'pre-line'}}>{alert.description}</span></td>);
                        break;
                    case 'Created':
                        tabFields.push(<td className="limited"><Timestamp dateTime={DateTime.parseFromString(alert.created_at).toString(DateTime.Formats.DATETIME)} relative/></td>);
                        break;
                    case 'Last Modified':
                        tabFields.push(<td className="limited"><Timestamp dateTime={DateTime.parseFromString(alert.last_modified).toString(DateTime.Formats.DATETIME)} relative/>
                        </td>);
                        break;
                    case 'User':
                        tabFields.push(<td className="limited">
                                    <OverlayElement overlay={tooltipUser} placement="top" useOverlay={true}
                                                    trigger={['hover', 'focus']}>
                                        {alert.creator_user_id}
                                    </OverlayElement>
                                </td>);
                        break;
                    case 'Alerts':
                        tabFields.push(<td className="limited">
                            <OverlayElement overlay={tooltipAlertCount} placement="top" useOverlay={true}
                                            trigger={['hover', 'focus']}>
                                <div><FormattedMessage id ="wizard.manualCount" defaultMessage="{alertDay} alerts/day ({alertCount} total)" 
                                        values={{alertDay: nbAlertDay, alertCount: alert.alert_count}}/></div>
                            </OverlayElement>
                        </td>);
                        break;
                    case 'Status':
                        if(alertValid){
                            tabFields.push(<td className="limited">{alert.disabled ? 
                                    <span style={{backgroundColor: 'orange', color: 'white'}}><FormattedMessage id ="wizard.disabled" defaultMessage="Disabled" /></span> : 
                                    <FormattedMessage id ="wizard.enabled" defaultMessage="Enabled" />}</td>);
                        }else{
                            tabFields.push(<td className="limited"><FormattedMessage id ="wizard.corrupted" defaultMessage="Corrupted" /></td>);
                        }
                        break;
                    case 'Rule':
                        tabFields.push(<td className="limited"><AlertRuleText alert={alert} /></td>);
                        break;
                    default:
                        break;
                }
            }
        });

        return (
            <tr key={alert.title} style={{color:colorValid}}>
                {tabFields}
                <td style={{whiteSpace: 'nowrap'}}>{actions}</td>
            </tr>
        );
    },

    render() {
        const filterKeys = ['title', 'severity', 'created_at', 'last_modified', 'creator_user_id'];
        let headers = [this.state.fieldsTitle.title];
        this.props.config.field_order.map((field) => {
            if (field.enabled) {
                headers.push(this._getFieldName(field.name));
            }
        });
        headers.push(this.state.fieldsTitle.actions);

        if (this.state.alerts) {
            return (
                <div>
                    <div className="alert-actions pull-right">
                        <LinkContainer to={Routes.pluginRoute('WIZARD_NEWALERT')}>
                            <Button bsStyle="success" type="submit" title={this.state.messages.createAlert}>
                                <FormattedMessage id ="wizard.create" defaultMessage="Create" />
                            </Button>
                        </LinkContainer>
                        {' '}
                        <LinkContainer to={Routes.pluginRoute('WIZARD_IMPORTALERT')}>
                            <Button bsStyle="success" type="submit" title={this.state.messages.importAlert}>
                                <FormattedMessage id ="wizard.import" defaultMessage="Import" />
                            </Button>
                        </LinkContainer>
                        {' '}
                        <LinkContainer to={Routes.pluginRoute('WIZARD_EXPORTALERT')}>
                            <Button bsStyle="success" type="submit" title={this.state.messages.exportAlert}>
                                <FormattedMessage id ="wizard.export" defaultMessage="Export" />
                            </Button>
                        </LinkContainer>
                    </div>
                    <DataTable id="alert-list"
                               className="table-hover"
                               headers={headers}
                               headerCellFormatter={this._headerCellFormatter}
                               sortByKey={"title"}
                               rows={this.state.alerts}
                               filterBy="title"
                               dataRowFormatter={this._alertInfoFormatter}
                               filterLabel={<FormattedMessage id ="wizard.filter" defaultMessage="Filter alert rules" />}
                               filterKeys={filterKeys}/>
                    <AlertForm ref="cloneForm" onSubmit={this._onCloneSubmit}/>
                </div>
            );
        }

        return <Spinner/>
    },
});

export default AlertRuleList;
