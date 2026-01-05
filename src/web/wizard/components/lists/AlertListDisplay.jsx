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

import React, {useEffect, useState} from 'react';
import { useIntl, FormattedMessage } from 'react-intl';
import { LinkContainer } from 'react-router-bootstrap';
import Reflux from 'reflux';
import { Button } from 'components/bootstrap';
import { toDateObject } from 'util/DateTime';
import { DataTable, IfPermitted, OverlayTrigger, Spinner, Timestamp } from 'components/common';
import Routes from 'routing/Routes';
import AlertListActions from 'wizard/actions/AlertListActions';
import AlertListStore from 'wizard/stores/AlertListStore';
import AlertListCloneForm from './AlertListCloneForm';

const AlertListDisplay = ({config}) => {
    const intl = useIntl();

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
        createAlertList: intl.formatMessage({id: "wizard.createAlertList", defaultMessage: "Create alert list"}),
        importAlertList: intl.formatMessage({id: "wizard.importAlertList", defaultMessage: "Import alert list"}),
        exportAlertList: intl.formatMessage({id: "wizard.exportAlertList",  defaultMessage :"Export alert list"}),
        confirmDeletion: intl.formatMessage({id: "wizard.confirmDeletionList",  defaultMessage :"Do you really want to delete the alert list"}),
    };

    const availableFieldName = [
        {value: 'Description', label: fieldsTitle.description},
        {value: 'Created', label: fieldsTitle.created},
        {value: 'Last Modified', label: fieldsTitle.lastModified},
        {value: 'User', label: fieldsTitle.user},
        {value: 'Usage', label: fieldsTitle.usage},
        {value: 'Lists', label: fieldsTitle.lists},
    ];

    const [lists, setLists] = useState([]);

    useEffect(() => {
        Reflux.connect(AlertListStore);
        _loadList();
    }, []);

    const _loadList = () => {
        AlertListActions.list().then(lists => {
            setLists(lists);
        });
    };

    const _deleteAlertListFunction = (name) => {
        return () => {
            if (window.confirm(`${messages.confirmDeletion} "${name}" ?`)) {
                AlertListActions.deleteByName(name).finally(() => {
                    _loadList();
                });
            }
        };
    };

    const _headerCellFormatter = (header) => {
        let formattedHeaderCell;

        switch (header.toLocaleLowerCase()) {
            case 'actions':
                formattedHeaderCell = (<th className="actions">{header}</th>);
                break;
            default:
                formattedHeaderCell = (<th>{header}</th>);
        }

        return formattedHeaderCell;
    };

    const _onCloneSubmit = (name, title, description) => {
        AlertListActions.get(name).then(list => {
            const newList = {
                title: title,
                description: description,
                lists: list.lists
            }
            AlertListActions.create(newList).finally(() => _loadList());
        });
    };

    const _getFieldName = (field) => {
        return availableFieldName.filter((t) => t.value === field)[0].label;
    };

    const _listInfoFormatter = (list) => {
        const deleteAction = (
            <IfPermitted permissions="wizard_alerts_rules:delete">
                <button id="delete-list" type="button" className="btn btn-primary" disabled={list.usage > 0}
                        title={list.usage ? messages.infoNoDelete : messages.infoDelete}
                        onClick={_deleteAlertListFunction(list.title)}>
                    <FormattedMessage id ="wizard.delete" defaultMessage="Delete" />
                </button>
            </IfPermitted>
        );

        const updateList = (
            <IfPermitted permissions="wizard_alerts_rules:read">
                <LinkContainer to={Routes.pluginRoute('WIZARD_UPDATELIST_ALERTLISTTITLE')(list.title.replace(/\//g, '%2F'))}>
                    <Button bsStyle="info" type="submit" title={messages.infoUpdate} >
                        <FormattedMessage id ="wizard.edit" defaultMessage="Edit" />
                    </Button>
                </LinkContainer>
            </IfPermitted>
        );

        const cloneList = <AlertListCloneForm listTitle={list.title} onSubmit={_onCloneSubmit}/>

        const actions = (
            <div className="pull-left">
                {updateList}{' '}
                {cloneList}{' '}
                {deleteAction}{' '}
            </div>
        );

        const tooltipUser = (<FormattedMessage id ="wizard.tooltipUserList" defaultMessage="The last user who modified the list" />);

        let tabFields = [<td className="limited">{list.title}</td>];
        config.field_order.map((field) => {
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
                            <OverlayTrigger overlay={tooltipUser}
                                            placement="top"
                                            trigger={['hover', 'focus']}>
                                <span>{list.creator_user_id}</span>
                            </OverlayTrigger>
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
    };

    if(lists) {
        const filterKeys = ['title', 'created_at', 'last_modified', 'creator_user_id', 'lists'];
        const headers = [fieldsTitle.title];
        config.field_order.map((field) => {
            if (field.enabled) {
                headers.push(_getFieldName(field.name));
            }
        });
        headers.push(fieldsTitle.actions);

        return (
            <div>
                <div className="pull-right has-bm">
                    <LinkContainer to={Routes.pluginRoute('WIZARD_NEWLIST')}>
                        <Button bsStyle="success" type="submit" title={messages.createAlertList}>
                            <FormattedMessage id="wizard.create" defaultMessage="Create"/>
                        </Button>
                    </LinkContainer>
                    {' '}
                    <LinkContainer to={Routes.pluginRoute('WIZARD_IMPORTLIST')}>
                        <Button bsStyle="success" type="submit" title={messages.importAlertList}>
                            <FormattedMessage id="wizard.import" defaultMessage="Import"/>
                        </Button>
                    </LinkContainer>
                    {' '}
                    <LinkContainer to={Routes.pluginRoute('WIZARD_EXPORTLIST')}>
                        <Button bsStyle="success" type="submit" title={messages.exportAlertList}>
                            <FormattedMessage id="wizard.export" defaultMessage="Export"/>
                        </Button>
                    </LinkContainer>
                </div>
                <DataTable id="alert-list"
                           className="table-hover"
                           headers={headers}
                           headerCellFormatter={_headerCellFormatter}
                           sortByKey={"title"}
                           rows={lists}
                           filterBy="title"
                           dataRowFormatter={_listInfoFormatter}
                           filterLabel={<FormattedMessage id="wizard.filterlists" defaultMessage="Filter lists"/>}
                           filterKeys={filterKeys}/>
            </div>
        );
    } else {
        return <Spinner/>
    }
};

export default AlertListDisplay;
