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

// TODO move this class somewhere down... Used only in ./conditions/FieldsCondition

import PropTypes from 'prop-types';
import React from 'react';
import {FormattedMessage} from 'react-intl';
import Reflux from 'reflux';
import createReactClass from 'create-react-class';
import {Input} from 'components/bootstrap';
import {Select, Spinner, TypeAheadFieldInput} from 'components/common';
import ObjectUtils from 'util/ObjectUtils';
import StoreProvider from 'injection/StoreProvider';
import AlertListActions from 'wizard/actions/AlertListActions';
import AlertListStore from 'wizard/stores/AlertListStore';
import IconRemove from './icons/Remove';
import FormsUtils from 'util/FormsUtils';


const CurrentUserStore = StoreProvider.getStore('CurrentUser');

const FieldRule = createReactClass({
    displayName: 'FieldRule',

    mixins: [Reflux.connect(CurrentUserStore), Reflux.connect(AlertListStore)],

    propTypes: {
        rule: PropTypes.object,
        onUpdate: PropTypes.func,
        onDelete: PropTypes.func,
        matchData: PropTypes.array
    },
    contextTypes: {
        intl: PropTypes.object.isRequired,
    },

    getDefaultProps() {
        return {
            rule: {field: '', type: '', value: ''},
        };
    },

    getInitialState() {
        return {
            rule: ObjectUtils.clone(this.props.rule),
            isModified: false,
            isValid: false,
            hover: false,
            lists: null,
        };
    },

    componentDidUpdate(nextProps) {
        if (this.props.rule !== nextProps.rule) {
            this.setState({rule: this.props.rule})
        }
    },
    componentWillMount() {
        const messages = {
            delete: this.context.intl.formatMessage({id: "wizard.delete", defaultMessage: "Delete"}),
        };
        this.setState({messages: messages});
        this.list();
    },

    list() {
        AlertListActions.list().then(lists => {
            this.setState({lists: lists});
        });
    },

    _availableRuleType() {
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
    },

    _createSelectItemsListTitle(list) {
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
    },

    _onListTypeSelect(value) {
        this._updateAlertField('value', value)
    },

    _onRuleTypeSelect(value) {
        // TODO parseInt('') returns NaN which is a problem later on (will turn into null after a call to ObjectUtils.clone
        this._updateAlertField('type', parseInt(value));
    },

    _onRuleFieldSelect(event) {
        const value = FormsUtils.getValueFromInput(event.target);
        this._updateAlertField('field', value);
    },

    _updateAlertField(field, value) {
        const update = ObjectUtils.clone(this.state.rule);
        update[field] = value;
        this.setState({rule: update});
        this.setState({isModified: true});
        if (value === '') {
            this.setState({isValid: false});
        }
    },

    _onValueChanged(field) {
        return e => {
            this._updateAlertField(field, e.target.value);
        };
    },

    _update() {
        this.props.onUpdate(this.props.index, this.state.rule, this.state.isValid);
    },
    _delete() {
        this.props.onDelete(this.props.index);
    },

    _isLoading() {
        return (!this.state.rule);
    },

    _checkForm() {
        if (this.state.rule.field !== '' &&
            (this.state.rule.type === 5 || this.state.rule.type === -5) ||
            this.state.rule.field !== '' && this.state.rule.value !== '' &&
            (this.state.rule.type === 1 || this.state.rule.type === -1 ||
                this.state.rule.type === 2 || this.state.rule.type === -2 ||
                this.state.rule.type === 3 || this.state.rule.type === -3 ||
                this.state.rule.type === 4 || this.state.rule.type === -4 ||
                this.state.rule.type === 6 || this.state.rule.type === -6 ||
                this.state.rule.type === 7 || this.state.rule.type === -7)) {
            this.setState({isValid: true});
        }
    },
    _getMatchDataColor() {
        return (this.props.matchData.rules[this.props.rule.id] ? '#dff0d8' : '#f2dede');
    },

    render() {
        const isMatchDataPesent = (this.props.matchData && this.props.matchData.rules.hasOwnProperty(this.props.rule.id));
        const color = (isMatchDataPesent ? this._getMatchDataColor() : '#FFFFFF');

        const valueBox = ((this.state.rule.type !== 5 && this.state.rule.type !== -5 && this.state.rule.type !== 7 && this.state.rule.type !== -7) ?
            <Input style={{
                backgroundColor: color,
                borderTopLeftRadius: '0px',
                borderBottomLeftRadius: '0px',
                height: '36px'
            }}
                   id="value" name="value" type="text"
                   onChange={this._onValueChanged("value")} value={this.state.rule.value}/>
            : (this.state.rule.type === 7 || this.state.rule.type === -7) ?
                <Input id="alertLists" name="alertLists">
                    <div style={{width: '150px'}}>
                        <Select style={{backgroundColor: color, borderRadius: '0px'}}
                                autosize={false}
                                required
                                clearable={false}
                                value={this.state.rule.value}
                                options={this._createSelectItemsListTitle(this.state.lists)}
                                matchProp="value"
                                onChange={this._onListTypeSelect}
                                placeholder={<FormattedMessage id="wizard.select" defaultMessage="Select..."/>}/>
                    </div>
                </Input>
                : <span style={{marginRight: 199}}/>);

        const deleteAction = (
            <button id="delete-alert" type="button" className="btn btn-md btn-primary"
                    title={this.state.messages.delete} style={{marginRight: '0.5em'}}
                    onClick={this._delete}>
                <IconRemove/>
            </button>
        );

        if (!this.state.isValid) {
            this._checkForm();
        }

        if (this.state.isModified) {
            this.setState({isModified: false});
            this._update();
        }

        if (this.state.lists) {
            return (
                <form className="form-inline">
                    {deleteAction}
                    <Input id="field" name="field">
                        <div style={{width: '200px'}}>
                            <TypeAheadFieldInput id="field-input" type="text" required name="field" defaultValue={this.state.rule.field} onChange={this._onRuleFieldSelect} autoFocus />
                        </div>
                    </Input>
                    <Input id="type" name="type">
                        <div style={{width: '200px'}}>
                            <Select style={{backgroundColor: color}}
                                    required
                                    clearable={false}
                                    value={this.state.rule.type.toString()}
                                    options={this._availableRuleType()}
                                    matchProp="value"
                                    onChange={this._onRuleTypeSelect}
                                    placeholder={<FormattedMessage id="wizard.select" defaultMessage="Select..."/>}
                            />
                        </div>
                    </Input>
                    {valueBox}
                </form>
            );
        }
        return <Spinner/>
    },
});

export default FieldRule;
