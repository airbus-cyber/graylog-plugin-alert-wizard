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

// sources of inspiration for this code: views/components/common/EditableTitle.tsx
import React, {useState, useCallback} from 'react';
import { LinkContainer } from 'react-router-bootstrap';
import { useIntl, FormattedMessage } from 'react-intl';

import { Button, Col, Row, Nav, NavItem } from 'components/bootstrap';
import ObjectUtils from 'util/ObjectUtils';

import Navigation from 'wizard/routing/Navigation';
import StatisticsCondition from 'wizard/components/conditions/StatisticsCondition';
import GroupDistinctCondition from 'wizard/components/conditions/GroupDistinctCondition';
import CorrelationCondition from 'wizard/components/conditions/CorrelationCondition';
import OrCondition from 'wizard/components/conditions/OrCondition';
import CountCondition from 'wizard/components/conditions/CountCondition';
import TitlePriority from 'wizard/components/inputs/TitlePriority';
import AlertValidation from 'wizard/logic/AlertValidation';

import styles from './AlertRuleForm.css';

const AlertRuleForm = ({initialAlert, navigationToRuleComponents, onSave, disableNavbar}) => {
    const intl = useIntl();

    const [alert, setAlert] = useState(initialAlert);
    const [isModified, setIsModified] = useState(false);
    const [isValid, setIsValid] = useState(false);
    const [conditionType, setConditionType] = useState(alert.condition_type);

    const _updateAlertField = useCallback((field, value) => {
        const update = ObjectUtils.clone(alert);
        update[field] = value;
        setAlert(update);
        setIsModified(true);
        // TODO why is this check necessary???
        if (value === '') {
            setIsValid(false);
        } else {
            setIsValid(AlertValidation.isAlertValid(alert));
        }
    }, [alert, isModified, isValid]);

    const _updateAlert = useCallback(() => {
        setIsModified(true);
    }, [isModified]);

    const _selectContentComponent = () => {
        switch (conditionType) {
            case 'COUNT':
                return (<CountCondition onUpdate={_updateAlertField} alert={alert} />);
            case 'GROUP_DISTINCT':
                return (<GroupDistinctCondition onUpdate={_updateAlertField} alert={alert} />);
            case 'STATISTICAL':
                return (<StatisticsCondition onUpdate={_updateAlertField} alert={alert} />);
            case 'THEN':
            case 'AND':
                return (<CorrelationCondition onUpdate={_updateAlertField} alert={alert} />);
            case 'OR':
                return (<OrCondition onUpdate={_updateAlertField} onUpdateAlert={_updateAlert} alert={alert} />);
            default:
                return (<div/>);
        }
    };
    const _handleSelect = useCallback((selectedKey) => {
        if (alert.condition_type === selectedKey) {
            return;
        }

        setConditionType(selectedKey);
        _updateAlertField('condition_type', selectedKey);
    }, [conditionType]);

    const messages = {
        titlePopup: intl.formatMessage({id: "wizard.titlePopup", defaultMessage: "Alert rule is saved"}),
        messagePopup: intl.formatMessage({id: "wizard.messagePopup", defaultMessage: "Go to Advanced settings?"}),
        advancedSettings: intl.formatMessage({id: "wizard.advancedSettings", defaultMessage: "Advanced settings"}),
        done: intl.formatMessage({id: "wizard.done", defaultMessage: "I'm done!"}),
        placeholderTitle: intl.formatMessage({id: "wizard.placeholderTitle", defaultMessage: "Title of the alert rule                 "}),
        add: intl.formatMessage({id: "wizard.add", defaultMessage: "Add"}),
        ruleType: intl.formatMessage({id: "wizard.ruleType", defaultMessage: "Rule Type"}),
        tooltipCountCondition: intl.formatMessage({id: "wizard.tooltipCountCondition", defaultMessage: "Count Condition"}),
        tooltipGroupDistinctCondition: intl.formatMessage({id: "wizard.tooltipGroupDistinctCondition", defaultMessage: "Group / Distinct Condition"}),
        tooltipStatisticalCondition: intl.formatMessage({id: "wizard.tooltipStatisticalCondition", defaultMessage: "Statistical Condition"}),
        tooltipThenCondition: intl.formatMessage({id: "wizard.tooltipThenCondition", defaultMessage: "THEN Condition"}),
        tooltipAndCondition: intl.formatMessage({id: "wizard.tooltipAndCondition", defaultMessage: "AND Condition"}),
        tooltipOrCondition: intl.formatMessage({id: "wizard.tooltipOrCondition", defaultMessage: "OR Condition"}),
    };

    return (
        <div>
            <Row>
                <Col md={2} className={styles.subnavigation}>
                    <Nav stacked bsStyle="pills" activeKey={alert.condition_type} onSelect={key => _handleSelect(key)}>
                        <NavItem key="divider" disabled title="Rule Type" className={styles.divider}>
                            {messages.ruleType}
                        </NavItem>
                        <NavItem eventKey={'COUNT'} title={messages.tooltipCountCondition} disabled={disableNavbar}>
                            <FormattedMessage id="wizard.countCondition" defaultMessage="Count" />
                        </NavItem>
                        <NavItem eventKey={'GROUP_DISTINCT'} title={messages.tooltipGroupDistinctCondition} disabled={disableNavbar}>
                            <FormattedMessage id="wizard.groupDistinctCondition" defaultMessage="Group / Distinct" />
                        </NavItem>
                        <NavItem eventKey={'STATISTICAL'} title={messages.tooltipStatisticalCondition} disabled={disableNavbar}>
                            <FormattedMessage id="wizard.StatisticsCondition" defaultMessage="Statistics" />
                        </NavItem>
                        <NavItem eventKey={'THEN'} title={messages.tooltipThenCondition} disabled={disableNavbar}>
                            <FormattedMessage id="wizard.thenCondition" defaultMessage="THEN" />
                        </NavItem>
                        <NavItem eventKey={'AND'} title={messages.tooltipAndCondition} disabled={disableNavbar}>
                            <FormattedMessage id="wizard.andCondition" defaultMessage="AND" />
                        </NavItem>
                        <NavItem eventKey={'OR'} title={messages.tooltipOrCondition} disabled={disableNavbar}>
                            <FormattedMessage id="wizard.orCondition" defaultMessage="OR" />
                        </NavItem>
                    </Nav>
                </Col>
                <Col md={10} className={styles.contentpane}>
                    <h2>
                        <FormattedMessage id="wizard.titleParameters" defaultMessage="Alert rule parameters" />
                    </h2>
                    {navigationToRuleComponents}
                    <p className="description"><FormattedMessage id="wizard.descripionParameters" defaultMessage="Define the parameters of the alert rule." /></p>
                    <form className="form-inline">
                        <TitlePriority onUpdate={_updateAlertField} title={alert.title} priority={alert.priority} />
                        <br/>
                        {_selectContentComponent()}
                    </form>
                    <div className="pull-left">
                        <LinkContainer to={Navigation.getWizardRoute()}>
                            <Button>
                                <FormattedMessage id="wizard.cancel" defaultMessage="Cancel" />
                            </Button>
                        </LinkContainer>{' '}
                        <Button onClick={() => onSave(alert)}
                                disabled={!(isModified && isValid)}
                                bsStyle="primary" className="btn btn-primary">
                            <FormattedMessage id="wizard.save" defaultMessage="Save" />
                        </Button>{' '}
                    </div>
                </Col>
            </Row>
        </div>
    );
};

export default AlertRuleForm;
