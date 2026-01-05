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

// sources of inspiration for this code: 
// * views/components/views/MissingRequirements.tsx
import React, {useEffect, useState} from 'react';
import { IntlProvider, FormattedMessage } from 'react-intl';
import { LinkContainer } from 'react-router-bootstrap';
import messages_fr from 'translations/fr.json';
import { Input, Row, Col, Button } from 'components/bootstrap';
import { DocumentTitle, PageHeader} from 'components/common';
import UserNotification from 'util/UserNotification';
import { adjustFormat } from 'util/DateTime';
import Routes from 'routing/Routes';
import AlertListActions from 'wizard/actions/AlertListActions';
import FileSaver from 'wizard/logic/FileSaver';
import IconDownload from 'wizard/components/icons/Download';

const language = navigator.language.split(/[-_]/)[0];

const messages = {
    'fr': messages_fr
};

const ExportListPage = () => {

    const [alertLists, setAlertLists] = useState([]);
    const [selectedAlertLists, setSelectedAlertLists] = useState(new Set());

    useEffect(() => {
        AlertListActions.list().then(newLists => {
            setAlertLists(newLists);
        });
    }, []);

    const isEmpty = (obj) => {
        return ((obj === undefined) || (typeof obj.count === 'function' ? obj.count() === 0 : obj.length === 0));
    };

    const selectAllAlertLists = () => {
        const newSelection = new Set();
        alertLists.forEach((alertList) => newSelection.add(alertList.title));
        setSelectedAlertLists(newSelection);
    };

    const handleAlertListSelect = (event, title) => {
        const newSelection = new Set([...selectedAlertLists]);
        if (event.target.checked) {
            newSelection.add(title);
        } else {
            newSelection.delete(title);
        }
        setSelectedAlertLists(newSelection);
    };

    const formatAlertList = (alertList) => {
        return (
            <div className="checkbox">
                <Input id={`alertList_${alertList.title}`}
                       type="checkbox"
                       checked={selectedAlertLists.has(alertList.title)}
                       label={alertList.title}
                       onChange={event => handleAlertListSelect(event, alertList.title)}
                />
                <div className="help-inline"><FormattedMessage id= "wizard.fieldDescription" defaultMessage= "Description" />: <tt>{alertList.description}</tt></div>
                <div className="help-inline"><FormattedMessage id= "wizard.fieldLists" defaultMessage= "Lists" />: <tt>{alertList.lists}</tt></div>
            </div>
        );
    };

    const onSubmit = (evt) => {
        evt.preventDefault();
        const request = {
            titles: [...selectedAlertLists],
        };

        AlertListActions.exportAlertLists(request).then((response) => {
            UserNotification.success('Successfully export alert lists. Starting download...', 'Success!');
            let date = adjustFormat(new Date()).replace(/:/g, '').replace(/ /g, '_')
            FileSaver.save(response, date+'_alert_lists.json', 'application/json', 'utf-8');
        });
    };

    return (
        <IntlProvider locale={language} messages={messages[language]}>
            <DocumentTitle title="Export list">
                <div>
                    <PageHeader title={<FormattedMessage id= "wizard.exportWizardList" defaultMessage= "Wizard: Export lists" />}
                                actions={(
                                    <LinkContainer to={Routes.pluginRoute('WIZARD_LISTS')}>
                                        <Button bsStyle="info"><FormattedMessage id="wizard.backlist" defaultMessage= "Back to lists" /></Button>
                                    </LinkContainer>
                                )}>
                        <span>
                            <FormattedMessage id= "wizard.exportAlertList" defaultMessage= "You can export a list." />
                        </span>
                        <span>
                            <FormattedMessage id="wizard.documentationlist"
                                              defaultMessage= "Read more about Wizard lists in the documentation." />
                        </span>
                    </PageHeader>
                    <Row className="content">
                        <Col md={6}>
                            <form className="form-horizontal build-content-pack" onSubmit={onSubmit}>
                                <div className="form-group">
                                    <Col sm={2}>
                                        <label className="control-label" htmlFor="name">
                                            <FormattedMessage id ="wizard.alertsLists" defaultMessage="Lists" />
                                        </label>
                                    </Col>
                                    <Col sm={10}>
                                        {isEmpty(alertLists) ?
                                            <span className="help-block help-standalone">
                                                <FormattedMessage id ="wizard.noListsToExport" defaultMessage="There is no lists to export." />
                                            </span>
                                            :
                                            <span>
                                              <Button className="btn btn-sm btn-link select-all" onClick={selectAllAlertLists}>
                                                  <FormattedMessage id ="wizard.selectAll" defaultMessage="Select all" />
                                              </Button>
                                                {alertLists.sort((i1, i2) => { return i1.title.localeCompare(i2.title); }).map(formatAlertList)}
                                            </span>
                                        }
                                    </Col>
                                </div>
                                <div className="form-group">
                                    <Col smOffset={2} sm={10}>
                                        <Button bsStyle="success" type="submit">
                                            <IconDownload/>
                                            <FormattedMessage id ="wizard.downloadContentPack" defaultMessage="Download my content pack" />
                                        </Button>
                                    </Col>
                                </div>
                            </form>
                        </Col>
                    </Row>
                </div>
            </DocumentTitle>
        </IntlProvider>
    );
}

export default ExportListPage;
