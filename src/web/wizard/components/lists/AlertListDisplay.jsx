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
import { injectIntl, FormattedMessage } from 'react-intl';
import { LinkContainer } from 'react-router-bootstrap';
import createReactClass from 'create-react-class';
import Reflux from 'reflux';
import { Button, Tooltip } from 'components/bootstrap';
import { toDateObject } from 'util/DateTime';
import { DataTable, IfPermitted, OverlayElement, Spinner, Timestamp } from 'components/common';
import PermissionsMixin from 'util/PermissionsMixin';
import Routes from 'routing/Routes';
import AlertListStore from 'wizard/stores/AlertListStore';
import AlertListActions from 'wizard/actions/AlertListActions';
import AlertListForm from './AlertListForm';
import { CurrentUserStore } from 'stores/users/CurrentUserStore';


const AlertListDisplay = createReactClass({
        displayName: 'AlertListDisplay',

    mixins: [Reflux.connect(CurrentUserStore), Reflux.connect(AlertListStore), PermissionsMixin],

    propTypes: {
        config: PropTypes.object.isRequired,
    },

    // TODO replace deprecated componentWillMount into a combination of getInitialState and componentDidMount
    componentWillMount() {
        const { intl } = this.props;
        const fieldsTitle = {
            title: intl.formatMessage({id: "wizard.title", defaultMessage: "Title"}),
            description: intl.formatMessage({id: "wizard.fieldDescription", defaultMessage: "Description"}),
            created: intl.formatMessage({id: "wizard.created", defaultMessage: "Created"}),
            lastModified: intl.formatMessage({id: "wizard.lastModified", defaultMessage: "Last Modified"}),
            user: intl.formatMessage({id: "wizard.user", defaultMessage: "User"}),
            usage: intl.formatMessage({id: "wizard.usage", defaultMessage: "Usage"}),
            lists: intl.formatMessage({id: "wizard.lists", defaultMessage: "Lists"}),
            actions: intl.formatMessage({id: "wizard.actions", defaultMessage: "Actions"}),
        };
        const messages = {
            infoDelete: intl.formatMessage({id: "wizard.buttonInfoDeleteList", defaultMessage: "Delete this alert list"}),
            infoNoDelete: intl.formatMessage({id: "wizard.buttonInfoNoDeleteList", defaultMessage: "List used in alert rules"}),
            infoUpdate: intl.formatMessage({id: "wizard.buttonInfoUpdateList", defaultMessage: "Edit this alert list"}),
            infoClone: intl.formatMessage({id: "wizard.buttonInfoCloneList", defaultMessage: "Clone this alert list"}),
            createAlertList: intl.formatMessage({id: "wizard.createAlertList", defaultMessage: "Create alert list"}),
            importAlertList: intl.formatMessage({id: "wizard.importAlertList", defaultMessage: "Import alert list"}),
            exportAlertList: intl.formatMessage({id: "wizard.exportAlertList",  defaultMessage :"Export alert list"}),
            confirmDeletion: intl.formatMessage({id: "wizard.confirmDeletionList",  defaultMessage :"Do you really want to delete the alert list"}),
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
        AlertListActions.get(name).then(list => {
            const newList = {
                title: title,
                description: description,
                lists: list.lists
            }
            AlertListActions.create(newList).finally(() => this.list());
        });
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
        const deleteAction = (
            <IfPermitted permissions="wizard_alerts_rules:delete">
                <button id="delete-list" type="button" className="btn btn-primary" disabled={list.usage > 0}
                        title={list.usage ? this.state.messages.infoNoDelete : this.state.messages.infoDelete}
                        onClick={this._deleteAlertListFunction(list.title)}>
                    <FormattedMessage id ="wizard.delete" defaultMessage="Delete" />
                </button>
            </IfPermitted>
        );

        const updateList = (
            <IfPermitted permissions="wizard_alerts_rules:read">
                <LinkContainer to={Routes.pluginRoute('WIZARD_UPDATELIST_ALERTLISTTITLE')(list.title.replace(/\//g, '%2F'))}>
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
            <div className="pull-left">
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
                        tabFields.push(<td className="limited"><Timestamp dateTime={toDateObject(list.created_at)} relative/></td>);
                        break;
                    case 'Last Modified':
                        tabFields.push(<td className="limited"><Timestamp dateTime={toDateObject(list.last_modified)} relative/>
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
                        tabFields.push(<td className="limited"><span style={{whiteSpace: 'pre-line'}}>{list.usage}</span></td>);
                        break;
                    case 'Lists':
                        tabFields.push(<td className="limited"><span style={{whiteSpace: 'pre-line'}}>{list.lists}</span></td>);
                        break;
                }
            }
        });

        return (
            <tr key={list.title}>
                {tabFields}
                <td style={{whiteSpace: 'nowrap'}}>{actions}</td>
            </tr>
        );
    },

    render () {

        const filterKeys = ['title', 'created_at', 'last_modified', 'creator_user_id', 'lists'];
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
                      <div className="pull-right has-bm">
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

export default injectIntl(AlertListDisplay);
