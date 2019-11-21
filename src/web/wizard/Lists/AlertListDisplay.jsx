import PropTypes from 'prop-types';
import React from 'react';
import Reflux from 'reflux';
import createReactClass from 'create-react-class';
import AlertListStore from './AlertListStore';
import AlertListActions from './AlertListActions';
import StoreProvider from 'injection/StoreProvider';
import {Button, Tooltip} from 'react-bootstrap';
import {DataTable, IfPermitted, OverlayElement, Spinner, Timestamp} from 'components/common';
import PermissionsMixin from 'util/PermissionsMixin';
import Routes from 'routing/Routes';
import {LinkContainer} from 'react-router-bootstrap';
import DateTime from 'logic/datetimes/DateTime';
import {FormattedMessage} from 'react-intl';
import AlertListForm from "./AlertListForm";

const CurrentUserStore = StoreProvider.getStore('CurrentUser');

const AlertListDisplay = createReactClass({
        displayName: 'AlertListDisplay',

    mixins: [Reflux.connect(CurrentUserStore), Reflux.connect(AlertListStore), PermissionsMixin],

    propTypes: {
        config: PropTypes.object.isRequired,
    },

    contextTypes: {
        intl: PropTypes.object.isRequired,
    },

    componentWillMount() {
        const fieldsTitle = {
            title: this.context.intl.formatMessage({id: "wizard.title", defaultMessage: "Title"}),
            description: this.context.intl.formatMessage({id: "wizard.fieldDescription", defaultMessage: "Description"}),
            created: this.context.intl.formatMessage({id: "wizard.created", defaultMessage: "Created"}),
            lastModified: this.context.intl.formatMessage({id: "wizard.lastModified", defaultMessage: "Last Modified"}),
            user: this.context.intl.formatMessage({id: "wizard.user", defaultMessage: "User"}),
            usage: this.context.intl.formatMessage({id: "wizard.usage", defaultMessage: "Usage"}),
            lists: this.context.intl.formatMessage({id: "wizard.lists", defaultMessage: "Lists"}),
            actions: this.context.intl.formatMessage({id: "wizard.actions", defaultMessage: "Actions"}),
        };
        const messages = {
            infoDelete: this.context.intl.formatMessage({id: "wizard.buttonInfoDeleteList", defaultMessage: "Delete this alert list"}),
            infoUpdate: this.context.intl.formatMessage({id: "wizard.buttonInfoUpdateList", defaultMessage: "Edit this alert list"}),
            infoClone: this.context.intl.formatMessage({id: "wizard.buttonInfoCloneList", defaultMessage: "Clone this alert list"}),
            createAlertList: this.context.intl.formatMessage({id: "wizard.createAlertList", defaultMessage: "Create alert list"}),
            importAlertList: this.context.intl.formatMessage({id: "wizard.importAlertList", defaultMessage: "Import alert list"}),
            exportAlertList: this.context.intl.formatMessage({id: "wizard.exportAlertList",  defaultMessage :"Export alert list"}),
            confirmDeletion: this.context.intl.formatMessage({id: "wizard.confirmDeletionList",  defaultMessage :"Do you really want to delete the alert list"}),
        };

        this.setState({fieldsTitle:fieldsTitle});
        this.setState({messages:messages});
        this.list();
    },

    list() {
        AlertListActions.list().then(lists => {
            this.setState({lists: lists});
        });
    },

    deleteAlertList(name) {
        AlertListActions.deleteByName(name);
    },
    _deleteAlertListFunction(name) {
        return () => {
            if (window.confirm(`${this.state.messages.confirmDeletion} "${name}" ?`)) {
                this.deleteAlertList(name);
            }
        };
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
        AlertListActions.clone(name, title, description).finally(() => this.list());
    },

    _availableFieldName() {
        return [
            {value: 'Description', label: this.state.fieldsTitle.description},
            {value: 'Created', label: this.state.fieldsTitle.created},
            {value: 'Last Modified', label: this.state.fieldsTitle.lastModified},
            {value: 'User', label: this.state.fieldsTitle.user},
            {value: 'Usage', label: this.state.fieldsTitle.usage},
            {value: 'Lists', label: this.state.fieldsTitle.lists},
        ];
    },
    _getFieldName(field) {
        return this._availableFieldName().filter((t) => t.value === field)[0].label;
    },

    _listInfoFormatter(list) {

        let colorValid = "#000000";
        let listValid = true;

        const deleteAction = (
            <IfPermitted permissions="wizard_alerts_rules:delete">
                <button id="delete-list" type="button" className="btn btn-md btn-primary"
                        title={this.state.messages.infoDelete} onClick={this._deleteAlertListFunction(list.title)}>
                    <FormattedMessage id ="wizard.delete" defaultMessage="Delete" />
                </button>
            </IfPermitted>
        );

        const updateList = (
            <IfPermitted permissions="wizard_alerts_rules:read">
                <LinkContainer to={Routes.pluginRoute('WIZARD_UPDATELIST_ALERTLISTTITLE')(list.title.replace(/\//g, '%2F'))} disabled={!listValid}>
                    <Button bsStyle="info" type="submit" title={this.state.messages.infoUpdate} >
                        <FormattedMessage id ="wizard.edit" defaultMessage="Edit" />
                    </Button>
                </LinkContainer>
            </IfPermitted>
        );

        const cloneList = (
            <Button id="clone-list" type="button" bsStyle="info" onClick={this._onClone(list.title)}
                    title={this.state.messages.infoClone} >
                <FormattedMessage id ="wizard.clone" defaultMessage="Clone" />
            </Button>
        );

        const actions = (
            <div className="alert-actions pull-left">
                {updateList}{' '}
                {cloneList}{' '}
                {deleteAction}{' '}
            </div>
        );

        const tooltipUser = (
            <Tooltip id="default-user-tooltip">
                <FormattedMessage id ="wizard.tooltipUserList" defaultMessage="The last user who modified the list" />
            </Tooltip>);

        let tabFields = [<td className="limited">{list.title}</td>];
        this.props.config.field_order.map((field) => {
            if (field.enabled) {
                switch (field.name) {
                    case 'Description':
                        tabFields.push(<td className="limited"><span style={{whiteSpace: 'pre-line'}}>{list.description}</span></td>);
                        break;
                    case 'Created':
                        tabFields.push(<td className="limited"><Timestamp dateTime={DateTime.parseFromString(list.created_at).toString(DateTime.Formats.DATETIME)} relative/></td>);
                        break;
                    case 'Last Modified':
                        tabFields.push(<td className="limited"><Timestamp dateTime={DateTime.parseFromString(list.last_modified).toString(DateTime.Formats.DATETIME)} relative/>
                        </td>);
                        break;
                    case 'User':
                        tabFields.push(<td className="limited">
                            <OverlayElement overlay={tooltipUser} placement="top" useOverlay={true}
                                            trigger={['hover', 'focus']}>
                                {list.creator_user_id}
                            </OverlayElement>
                        </td>);
                        break;
                    case 'Usage':
                        tabFields.push(<td className="limited"><span style={{whiteSpace: 'pre-line'}}></span></td>);
                        break;
                    case 'Lists':
                        tabFields.push(<td className="limited"><span style={{whiteSpace: 'pre-line'}}>{list.lists}</span></td>);
                        break;
                }
            }
        });

        return (
            <tr key={list.title} style={{color:colorValid}}>
                {tabFields}
                <td style={{whiteSpace: 'nowrap'}}>{actions}</td>
            </tr>
        );
    },

    render () {

        const filterKeys = ['title', 'created_at', 'last_modified', 'creator_user_id'];
        let headers = [this.state.fieldsTitle.title];
        this.props.config.field_order.map((field) => {
            if (field.enabled) {
                headers.push(this._getFieldName(field.name));
            }
        });
        headers.push(this.state.fieldsTitle.actions);

        if(this.state.lists) {
              return (
                  <div>
                      <div className="alert-actions pull-right">
                          <LinkContainer to={Routes.pluginRoute('WIZARD_NEWLIST')}>
                              <Button bsStyle="success" type="submit" title={this.state.messages.createAlertList}>
                                  <FormattedMessage id="wizard.create" defaultMessage="Create"/>
                              </Button>
                          </LinkContainer>
                          {' '}
                          <LinkContainer to={Routes.pluginRoute('WIZARD_IMPORTLIST')}>
                              <Button bsStyle="success" type="submit" title={this.state.messages.importAlertList}>
                                  <FormattedMessage id="wizard.import" defaultMessage="Import"/>
                              </Button>
                          </LinkContainer>
                          {' '}
                          <LinkContainer to={Routes.pluginRoute('WIZARD_EXPORTLIST')}>
                              <Button bsStyle="success" type="submit" title={this.state.messages.exportAlertList}>
                                  <FormattedMessage id="wizard.export" defaultMessage="Export"/>
                              </Button>
                          </LinkContainer>
                      </div>
                      <DataTable id="alert-list"
                                 className="table-hover"
                                 headers={headers}
                                 headerCellFormatter={this._headerCellFormatter}
                                 sortByKey={"title"}
                                 rows={this.state.lists}
                                 filterBy="title"
                                 dataRowFormatter={this._listInfoFormatter}
                                 filterLabel={<FormattedMessage id="wizard.filterlists" defaultMessage="Filter lists"/>}
                                 filterKeys={filterKeys}/>
                      <AlertListForm ref="cloneForm" onSubmit={this._onCloneSubmit}/>
                  </div>
              );
          }
          return <Spinner/>
    },
});

export default AlertListDisplay;