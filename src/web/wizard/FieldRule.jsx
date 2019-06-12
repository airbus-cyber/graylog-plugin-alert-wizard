import PropTypes from 'prop-types';
import React from 'react';
import Reflux from 'reflux';
import {Button, Col, Row} from 'react-bootstrap';
import {Input} from 'components/bootstrap';
import {IfPermitted, Select, Spinner} from 'components/common';
import ObjectUtils from 'util/ObjectUtils';
import StoreProvider from 'injection/StoreProvider';
import naturalSort from 'javascript-natural-sort';
import {FormattedMessage} from 'react-intl';

const FieldsStore = StoreProvider.getStore('Fields');

const FieldRule = React.createClass({
    propTypes: {
        rule: PropTypes.object,
        onUpdate: PropTypes.func,
        onDelete: PropTypes.func,
        matchData: PropTypes.array,
    },
    contextTypes: {
        intl: React.PropTypes.object.isRequired
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
            fields: null,
        };
    },

    componentDidMount() {
       FieldsStore.loadFields().then((fields) => {
            //add value to list fields if not present
            if (this.state.rule.field && this.state.rule.field !== '' && fields.indexOf(this.state.rule.field) < 0) {
                fields.push(this.state.rule.field);
            }
            this.setState({fields: fields});
        });
    },
    componentDidUpdate(nextProps) {
        if (this.props.rule !== nextProps.rule) {
            this.setState({rule: this.props.rule})
        }
    },
    componentWillMount(){
        const messages = {
                delete: this.context.intl.formatMessage({id: "wizard.delete", defaultMessage: "Delete"}),
            };
        this.setState({messages:messages});
    },

    _availableRuleType() {
        return [
            {value: 1, label: <FormattedMessage id= "wizard.matchesExactly" defaultMessage= "matches exactly" />},
            {value: -1, label: <FormattedMessage id= "wizard.notMatchesExactly" defaultMessage= "does not match exactly" />},
            {value: 2, label: <FormattedMessage id= "wizard.matchesRegularExpression" defaultMessage= "matches regular expression" />},
            {value: -2, label: <FormattedMessage id= "wizard.notMatchRegularExpression" defaultMessage= "does not match regular expression" />},
            {value: 3, label: <FormattedMessage id= "wizard.greaterThan" defaultMessage= "is greater than" />},
            {value: -3, label: <FormattedMessage id= "wizard.notGreaterThan" defaultMessage= "is not greater than" />},
            {value: 4, label: <FormattedMessage id= "wizard.smallerThan" defaultMessage= "is smaller than" />},
            {value: -4, label: <FormattedMessage id= "wizard.notSmallerThan" defaultMessage= "is not smaller than" />},
            {value: 5, label: <FormattedMessage id= "wizard.present" defaultMessage= "is present" />},
            {value: -5, label: <FormattedMessage id= "wizard.notPresent" defaultMessage= "is not present" />},
            {value: 6, label: <FormattedMessage id= "wizard.contains" defaultMessage= "contains" />},
            {value: -6, label: <FormattedMessage id= "wizard.notContain" defaultMessage= "does not contain" />},
        ];
    },

    _onRuleTypeSelect(value) {
        this._updateAlertField('type', value);
    },

    _onRuleFieldSelect(value) {
        this._updateAlertField('field', value);

        //add value to list fields if not present
        if (value !== '' && this.state.fields.indexOf(value) < 0) {
            const update = ObjectUtils.clone(this.state.fields);
            update.push(value);
            this.setState({fields: update});
        }
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
                this.state.rule.type === 6 || this.state.rule.type === -6)) {
            this.setState({isValid: true});
        }
    },
    _formatOption(key, value) {
        return {value: value, label: key};
    },
    _getMatchDataColor(){
        return (this.props.matchData.rules[this.props.rule.id] ? '#dff0d8' : '#f2dede');
    },

    render() {
        if (this._isLoading()) {
            return (
                <div style={{marginLeft: 10}}>
                    <Spinner/>
                </div>
            );
        }
        const isMatchDataPesent = (this.props.matchData && this.props.matchData.rules.hasOwnProperty(this.props.rule.id));
        const color = (isMatchDataPesent ? this._getMatchDataColor() : '#FFFFFF');

        let formattedOptions = null;
        if(this.state.fields) {
            formattedOptions = Object.keys(this.state.fields).map(key => this._formatOption(this.state.fields[key], this.state.fields[key]))
                .sort((s1, s2) => naturalSort(s1.label.toLowerCase(), s2.label.toLowerCase()));
        }

        const valueBox = (this.state.rule.type !== 5 && this.state.rule.type !== -5 ?
            <Input style={{backgroundColor: color, borderTopLeftRadius: '0px', borderBottomLeftRadius: '0px', height:'36px'}} 
                ref="value" id="value" name="value" type="text"
                   onChange={this._onValueChanged("value")} value={this.state.rule.value}/> :
            <span style={{marginRight: 199}}/>);

        const deleteAction = (

                <button id="delete-alert" type="button" className="btn btn-md btn-primary" title={this.state.messages.delete} style={{marginRight: '0.5em'}}
                    onClick={this._delete}>
                    <i className="fa fa-trash-o"/>
                </button>
        );

        if (!this.state.isValid) {
            this._checkForm();
        }

        if (this.state.isModified && !this.props.create) {
            this.setState({isModified: false});
            this._update();
        }

        return (
                <form className="form-inline">
                    {deleteAction}
                    <Input ref="field" id="field" name="field">
                        <Select style={{backgroundColor: color, borderTopRightRadius: '0px', borderBottomRightRadius: '0px'}}
                            autosize={false}
                            required
                            value={this.state.rule.field}
                            options={formattedOptions}
                            matchProp="value"
                            onChange={this._onRuleFieldSelect}
                            allowCreate={true}
                            placeholder={<FormattedMessage id= "wizard.select" defaultMessage= "Select..." />}
                        />
                    </Input>
                    <Input ref="type" id="type" name="type">
                        <Select  style={{backgroundColor: color, borderRadius: '0px'}}
                            autosize={false}
                            required
                            value={this.state.rule.type}
                            options={this._availableRuleType()}
                            matchProp="value"
                            onChange={this._onRuleTypeSelect}
                            placeholder={<FormattedMessage id= "wizard.select" defaultMessage= "Select..." />}
                        />
                    </Input>
                    {valueBox}
                </form>
        );
    },
});

export default FieldRule;
