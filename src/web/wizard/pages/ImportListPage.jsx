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

import React, {useState} from 'react';
import { IntlProvider, FormattedMessage } from 'react-intl';
import Routes from 'routing/Routes';
import useHistory from 'routing/useHistory';
import { LinkContainer } from 'react-router-bootstrap';
import { Input, Row, Col, Button } from 'components/bootstrap';
import messages_fr from 'translations/fr.json';
import AlertListActions from 'wizard/actions/AlertListActions';
import { DocumentTitle, PageHeader } from 'components/common';
import Navigation from 'wizard/routing/Navigation';

const language = navigator.language.split(/[-_]/)[0];

const messages = {
    'fr': messages_fr
};

const ImportListPage = () => {
    const history = useHistory();
    const [selectedFile, setSelectedFile] = useState(null);
    const [alertLists, setAlertLists] = useState([]);
    const [selectedAlertLists, setSelectedAlertLists] = useState(new Set());

    const onSelectUploadFile = (event) => {
        setSelectedFile(event.target.files[0]);
    };

    const onSubmitUploadFile = (submitEvent) => {
        submitEvent.preventDefault();

        if (selectedFile !== null) {
            const reader = new FileReader();
            reader.onload = (evt) => {
                setAlertLists(JSON.parse(evt.target.result));
            };

            reader.readAsText(selectedFile);
        }
    };

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
                       onChange={event => handleAlertListSelect(event, alertList.title)}
                       label={alertList.title}
                />
                <div className="help-inline"><FormattedMessage id= "wizard.fieldDescription" defaultMessage= "Description" />: <tt>{alertList.description}</tt></div>
                <div className="help-inline"><FormattedMessage id= "wizard.fieldLists" defaultMessage= "Lists" />: <tt>{alertList.lists}</tt></div>
            </div>
        );
    };

    const onSubmitApplyAlertLists = (evt) => {
        evt.preventDefault();
        const request = [];

        alertLists.forEach((alertList) => {
           if (selectedAlertLists.has(alertList.title)) {
               request.push(alertList);
           }
        });

        AlertListActions.importAlertLists(request).then(response => {
            if (response !== true) {
                return;
            }
            history.push(Navigation.getWizardListRoute());
        });
    };

    return (
        <IntlProvider locale={language} messages={messages[language]}>
            <DocumentTitle title="Import list">
                <div>
                    <PageHeader title={<FormattedMessage id= "wizard.importWizardList" defaultMessage= "Wizard: Import lists" />}
                                actions={(
                                    <LinkContainer to={Routes.pluginRoute('WIZARD_LISTS')}>
                                        <Button bsStyle="info"><FormattedMessage id="wizard.backlist" defaultMessage= "Back to lists" /></Button>
                                    </LinkContainer>
                                )}>
                        <span>
                            <FormattedMessage id= "wizard.importLists" defaultMessage= "You can import a list." />
                        </span>
                        <span>
                            <FormattedMessage id="wizard.documentationlist"
                                              defaultMessage= "Read more about Wizard lists in the documentation." />
                        </span>
                    </PageHeader>
                    <Row className="content">
                        <Col md={12}>
                            <form onSubmit={onSubmitUploadFile} className="upload" encType="multipart/form-data">
                                <div className="form-group">
                                    <Input type="file" name="bundle" onChange={onSelectUploadFile} />
                                </div>
                                <button type="submit" className="btn btn-success">
                                    <FormattedMessage id="wizard.upload" defaultMessage= "Upload" />
                                </button>
                            </form>
                        </Col>
                    </Row>
                    <Row className="content">
                        <Col md={6}>
                            <form className="form-horizontal build-content-pack" onSubmit={onSubmitApplyAlertLists}>
                                <div className="form-group">
                                    <Col sm={2}>
                                        <label className="control-label" htmlFor="name">
                                            <FormattedMessage id ="wizard.alertsList" defaultMessage="Lists" />
                                        </label>
                                    </Col>
                                    <Col sm={10}>
                                        {isEmpty(alertLists) ?
                                            <span className="help-block help-standalone">
                                                <FormattedMessage id ="wizard.noAlertListsToExport" defaultMessage="There is no list to import." />
                                            </span>
                                            :
                                            <span>
                                              <Button className="btn btn-sm btn-link select-all" onClick={selectAllAlertLists}>
                                                  <FormattedMessage id ="wizard.selectAll" defaultMessage="Select all" />
                                              </Button>
                                                {alertLists.map(formatAlertList)}
                                            </span>
                                        }
                                    </Col>
                                </div>
                                <div className="form-group">
                                    <Col smOffset={2} sm={10}>
                                        <Button bsStyle="success" type="submit">
                                            <FormattedMessage id ="wizard.applyLists" defaultMessage="Apply lists" />
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

export default ImportListPage;
