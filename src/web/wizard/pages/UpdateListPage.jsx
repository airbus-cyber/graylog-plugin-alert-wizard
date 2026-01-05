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
import { Button, Col, Row } from 'components/bootstrap';
import { LinkContainer } from 'react-router-bootstrap';
import { DocumentTitle, PageHeader, Spinner } from 'components/common';
import Routes from 'routing/Routes';
import withParams from 'routing/withParams';
import useHistory from 'routing/useHistory';
import { FormattedMessage, IntlProvider } from 'react-intl';
import messages_fr from 'translations/fr.json';
import CreateListFormInput from 'wizard/components/lists/CreateListFormInput';
import AlertListActions from 'wizard/actions/AlertListActions';
import Navigation from 'wizard/routing/Navigation';

const language = navigator.language.split(/[-_]/)[0];

const messages = {
    'fr': messages_fr
};

const UpdateListPage = ({params}) => {

    const history = useHistory();
    const [list, setList] = useState(null);

    useEffect(() => {
        AlertListActions.get(params.alertListTitle).then(list => {
            setList(list);
        });
    }, []);

    const _isLoading = () => {
        return !list;
    };

    const _update = (newList) => {
        AlertListActions.update(list.title, newList).finally(() => {
            history.push(Navigation.getWizardListRoute());
        });
    };

    if (_isLoading()) {
        return <Spinner/>;
    }

    return (
        <IntlProvider locale={language} messages={messages[language]}>
            <DocumentTitle title="Edit list">
                <div>
                    <PageHeader title={<FormattedMessage id="wizard.updateList"
                                                         defaultMessage='Wizard: Editing list "{title}"'
                                                         values={{title: list.title}}/>}
                                actions={(
                                    <LinkContainer to={Routes.pluginRoute('WIZARD_LISTS')}>
                                        <Button bsStyle="info"><FormattedMessage id="wizard.backlist" defaultMessage= "Back to lists" /></Button>
                                    </LinkContainer>
                                )}>
                        <span>
                            <FormattedMessage id="wizard.definelist" defaultMessage="You can define a list."/>
                        </span>
                            <span>
                            <FormattedMessage id="wizard.documentationlist"
                                              defaultMessage="Read more about Wizard lists in the documentation."/>
                        </span>
                    </PageHeader>
                    <Row className="content">
                        <Col md={12}>
                            <CreateListFormInput list={list} onSave={_update}/>
                        </Col>
                    </Row>
                </div>
            </DocumentTitle>
        </IntlProvider>
    );
}

export default withParams(UpdateListPage);
