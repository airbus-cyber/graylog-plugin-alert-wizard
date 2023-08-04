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

// TODO remove this import?
import PropTypes from 'prop-types';
// TODO remove this import?
import React from 'react';
import { useEffect, useState } from 'react';
import { useIntl, FormattedMessage } from 'react-intl';
import { Input } from 'components/bootstrap';
import { Select, Spinner, TypeAheadFieldInput } from 'components/common';
import ObjectUtils from 'util/ObjectUtils';
import AlertListActions from 'wizard/actions/AlertListActions';
import IconRemove from 'wizard/components/icons/Remove';
import FormsUtils from 'util/FormsUtils';


// TODO transform this into a constant? _AVAILABLE_RULE_TYPE
const _availableRuleType = () => {
    return [
        {value: "1", label: <FormattedMessage id="wizard.matchesExactly" defaultMessage="matches exactly"/>},
        {
            value: "-1",
            label: <FormattedMessage id="wizard.notMatchesExactly" defaultMessage="does not match exactly"/>
        },
        {
            value: "2",
            label: <FormattedMessage id="wizard.matchesRegularExpression"
                                     defaultMessage="matches regular expression"/>
        },
        {
            value: "-2",
            label: <FormattedMessage id="wizard.notMatchRegularExpression"
                                     defaultMessage="does not match regular expression"/>
        },
        {value: "3", label: <FormattedMessage id="wizard.greaterThan" defaultMessage="is greater than"/>},
        {value: "-3", label: <FormattedMessage id="wizard.notGreaterThan" defaultMessage="is not greater than"/>},
        {value: "4", label: <FormattedMessage id="wizard.smallerThan" defaultMessage="is smaller than"/>},
        {value: "-4", label: <FormattedMessage id="wizard.notSmallerThan" defaultMessage="is not smaller than"/>},
        {value: "5", label: <FormattedMessage id="wizard.present" defaultMessage="is present"/>},
        {value: "-5", label: <FormattedMessage id="wizard.notPresent" defaultMessage="is not present"/>},
        {value: "6", label: <FormattedMessage id="wizard.contains" defaultMessage="contains"/>},
        {value: "-6", label: <FormattedMessage id="wizard.notContain" defaultMessage="does not contain"/>},
        {value: "7", label: <FormattedMessage id="wizard.listpresent" defaultMessage="is present in list"/>},
        {
            value: "-7",
            label: <FormattedMessage id="wizard.listnotpresent" defaultMessage="is not present in list"/>
        },
    ];
};

const FieldRule = ({matchData, rule, onUpdate, onDelete, index}) => {
    // TODO why the ObjectUtils.clone here?
    // TODO split this state in several states with only one value
    const [state, setState] = useState({
        rule: ObjectUtils.clone(rule),
        isModified: false,
        isValid: false
    });
    const [lists, setLists] = useState(null);

    useEffect(() => {
        AlertListActions.list().then(setLists);
    }, []);

    const _getMatchDataColor = () => {
        return (matchData.rules[rule.id] ? '#dff0d8' : '#f2dede');
    };

    const _checkForm = () => {
        if (state.rule.field !== '' &&
            (state.rule.type === 5 || state.rule.type === -5) ||
            state.rule.field !== '' && state.rule.value !== '' &&
            (state.rule.type === 1 || state.rule.type === -1 ||
                state.rule.type === 2 || state.rule.type === -2 ||
                state.rule.type === 3 || state.rule.type === -3 ||
                state.rule.type === 4 || state.rule.type === -4 ||
                state.rule.type === 6 || state.rule.type === -6 ||
                state.rule.type === 7 || state.rule.type === -7)) {
            setState({ ...state, isValid: true });
        }
        if (value === '') {
            setState({ ...state, isValid: false });
        }
    };

    const _updateAlertField = (field, value) => {
        // TODO why the clone here?
        const update = ObjectUtils.clone(state.rule);
        update[field] = value;
        setState({
            ...state,
            rule: update,
            isModified: true
        });
        _checkForm();
    };

    const _onValueChanged = (field) => {
        return e => {
            _updateAlertField(field, e.target.value);
        };
    };

    const _onListTypeSelect = (value) => {
        _updateAlertField('value', value)
    };

    const _onRuleFieldSelect = (event) => {
        const value = FormsUtils.getValueFromInput(event.target);
        _updateAlertField('field', value);
    };

    const _onRuleTypeSelect = (value) => {
        // TODO parseInt('') returns NaN which is a problem later on (will turn into null after a call to ObjectUtils.clone
        _updateAlertField('type', parseInt(value));
    };

    const _createSelectItemsListTitle = (list) => {
        let items = [];

        if (list !== null) {
            for (let i = 0; i < list.length; i++) {
                items.push({
                    value: list[i].title,
                    label: <span title={list[i].lists}><FormattedMessage id={list[i].title}
                                                                         defaultMessage={list[i].title}/></span>
                });
            }
        }
        return items;
    };

    const _update = () => {
        onUpdate(index, state.rule, state.isValid);
    };

    const _delete = () => {
        onDelete(index);
    };

    const intl = useIntl();
    const messages = {
        delete: intl.formatMessage({id: "wizard.delete", defaultMessage: "Delete"}),
        select: intl.formatMessage({id: "wizard.select", defaultMessage: "Select..."})
    };

    const isMatchDataPesent = (matchData && matchData.rules.hasOwnProperty(rule.id));
    const color = (isMatchDataPesent ? _getMatchDataColor() : '');

    const valueBox = ((state.rule.type !== 5 && state.rule.type !== -5 && state.rule.type !== 7 && state.rule.type !== -7) ?
        <Input style={{
                backgroundColor: color,
                borderTopLeftRadius: '0px',
                borderBottomLeftRadius: '0px',
                height: '36px'
            }}
               id="value" name="value" type="text"
               onChange={_onValueChanged("value")} value={state.rule.value}/>
        : (state.rule.type === 7 || state.rule.type === -7) ?
            <Input id="alertLists" name="alertLists">
                <div style={{width: '150px'}}>
                    <Select style={{backgroundColor: color, borderRadius: '0px'}}
                            autosize={false}
                            required
                            clearable={false}
                            value={state.rule.value}
                            options={_createSelectItemsListTitle(lists)}
                            matchProp="value"
                            onChange={_onListTypeSelect}
                            placeholder={messages.select}/>
                </div>
            </Input>
            : <span style={{marginRight: 199}}/>);

    const deleteAction = (
        <button id="delete-alert" type="button" className="btn btn-md btn-primary"
                title={messages.delete} style={{marginRight: '0.5em'}}
                onClick={_delete}>
            <IconRemove/>
        </button>
    );

    if (state.isModified) {
        setState({ ...state, isModified: false });
        _update();
    }

    if (lists) {
        return (
            <div className="form-inline">
                {deleteAction}
                <Input id="field" name="field">
                    <div style={{width: '200px'}}>
                        <TypeAheadFieldInput id="field-input" type="text" required name="field" defaultValue={state.rule.field} onChange={_onRuleFieldSelect} autoFocus />
                    </div>
                </Input>
                <Input id="type" name="type">
                    <div style={{width: '200px'}}>
                        <Select style={{backgroundColor: color}}
                                required
                                clearable={false}
                                value={state.rule.type.toString()}
                                options={_availableRuleType()}
                                matchProp="value"
                                onChange={_onRuleTypeSelect}
                                placeholder={messages.select}
                        />
                    </div>
                </Input>
                {valueBox}
            </div>
        );
    }
    return <Spinner/>
}

FieldRule.propTypes = {
    rule: PropTypes.object.isRequired,
    onUpdate: PropTypes.func.isRequired,
    onDelete: PropTypes.func.isRequired,
    // TODO isRequired?
    matchData: PropTypes.array,
    // TODO isRequired?
    index: PropTypes.number
};

export default FieldRule;
