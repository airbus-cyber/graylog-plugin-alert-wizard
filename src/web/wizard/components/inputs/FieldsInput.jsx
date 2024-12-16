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
// * pages/ShowMessagePage.tsx
// * pages/IndexSetPage.tsx
// * components/lookup-tables/DataAdapterForm.jsx (isEqual)
// * components/streamrules/StreamRulesEditor.tsx
import PropTypes from 'prop-types';
import React from 'react';
import createReactClass from 'create-react-class';
import Reflux from 'reflux';
import { injectIntl, FormattedMessage } from 'react-intl';

import isEqual from 'lodash/isEqual';

import { Button } from 'components/bootstrap';
import { Input, Row, Col } from 'components/bootstrap';
import { Select, Spinner } from 'components/common';
import StreamsStore from 'stores/streams/StreamsStore';
import { IndexSetsActions, IndexSetsStore } from 'stores/indices/IndexSetsStore';
import ObjectUtils from 'util/ObjectUtils';

import FieldRuleList from './FieldRuleList';


// TODO should convert to a functional component
const FieldsInput = createReactClass({
    displayName: 'FieldsInput',

    mixins: [Reflux.connect(IndexSetsStore)],

    propTypes: {
        stream: PropTypes.object.isRequired,
        onSaveStream: PropTypes.func.isRequired,
        message: PropTypes.object,
        matchData: PropTypes.object,
    },

    componentDidMount() {
        IndexSetsActions.list(false);
    },

    componentDidUpdate(prevProps) {
        if (!isEqual(this.props.matchData, prevProps.matchData)) {
            this.setState({matchData: this.props.matchData});
        }
        if (!isEqual(this.props.stream, prevProps.stream) && this.props.stream !== null) {
            this.setState({stream: this.props.stream});
            this.setState({matchData: undefined});
        }
    },

    getInitialState() {
        return {
            stream: ObjectUtils.clone(this.props.stream),
        };
    },

    _availableMatchingType() {
        return [
            {value: 'AND', label: <FormattedMessage id="wizard.all" defaultMessage="all" />},
            {value: 'OR', label: <FormattedMessage id="wizard.atLeastOne" defaultMessage="at least one" />},
        ];
    },

    _onMatchingTypeSelect(id) {
        this._updateStreamField('matching_type', id)
    },
    
    _updateStreamField(field, value) {
        const update = ObjectUtils.clone(this.state.stream);
        update[field] = value;
        this.setState({stream: update});
        this.props.onSaveStream(field, value);
    },

    _onSaveStream(newFieldRules) {
        const update = {
            ...this.state.stream,
            field_rule: newFieldRules
        };
        this.setState({stream: update});
        this.props.onSaveStream('field_rule', newFieldRules);
    },

    _isRuleValid(rule) {
        if (!(rule.field !== '' &&
                (rule.type === 5 || rule.type === -5) ||
                rule.field !== '' && rule.value !== '' &&
                (rule.type === 1 || rule.type === -1 ||
                    rule.type === 2 || rule.type === -2 ||
                    rule.type === 3 || rule.type === -3 ||
                    rule.type === 4 || rule.type === -4 ||
                    rule.type === 6 || rule.type === -6))) {
                return false;
            }
        return true;
    },

    _isFieldRulesValid() {
        for (let i = 0; i < this.state.stream.field_rule.length; i++) {
            if (!this._isRuleValid(this.state.stream.field_rule[i])) {
                return false;
            }
        }
        return true;
    },

    // TODO factor this copy-pasted code (see FieldRule)
    _getMatchDataColor() {
        return (this.state.matchData.matches ? '#dff0d8' : '#f2dede');
    },

    _isLoading() {
        return (!this.state.stream);
    },
    
    render() {
        if (this._isLoading()) {
            return (
                <div style={{marginLeft: 10}}>
                    <Spinner/>
                </div>
            );
        }
        const color = (this.state.matchData ? this._getMatchDataColor() : '#FFFFFF');

        const { intl } = this.props;
        const messages = {
            tryFieldsCondition: intl.formatMessage({ id: "wizard.tryFieldsCondition", defaultMessage: "Try the fields condition" }),
            select: intl.formatMessage({id: "wizard.select", defaultMessage: "Select..."})
        };

        // TODO: if possible try to isolate the Test Button as a special component TestButton... (functional one)
        //       emulate the code of the StreamRulesEditor (even though we don't have a stream at creation time)...
        return (
        <Row>
            <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                <label className="pull-right" ><FormattedMessage id="wizard.fieldsCondition" defaultMessage="Fields Condition" /></label>
            </Col>
            <Col md={10}>
                <label><FormattedMessage id="wizard.messagesMatch" defaultMessage="Messages must match" /></label>
                <Input ref="matching_type" id="matching_type" name="matching_type" required>
                    <div style={{width:'150px'}}>
                    <Select style={{backgroundColor: color}}
                        autosize={false}
                        required
                        clearable={false}
                        value={this.state.stream.matching_type}
                        options={this._availableMatchingType()}
                        matchProp="value"
                        onChange={this._onMatchingTypeSelect}
                        placeholder={messages.select}
                    />
                    </div>
                </Input>
                <label>&nbsp; </label>
                <label><FormattedMessage id="wizard.followingRules" defaultMessage="of the following rules:" /></label>
                {' '}
                <br/><br/>

                <FieldRuleList fieldRules={this.state.stream.field_rule} matchData={this.state.matchData} onSaveStream={this._onSaveStream} />
            </Col>
        </Row>
        );
    },
});

export default injectIntl(FieldsInput);
