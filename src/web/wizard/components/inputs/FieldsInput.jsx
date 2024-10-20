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

    _checkFieldsCondition() {
        if (this.props.message !== undefined) {
            /* Create temporary stream */
            let tempRules = [];
            for (let index = 0; index < this.state.stream.field_rule.length; index++) {
                if (this._isRuleValid(this.state.stream.field_rule[index])) {
                    let inverted = false;
                    let type = this.state.stream.field_rule[index].type;
                    if (type < 0) {
                        inverted = true;
                        type = -this.state.stream.field_rule[index].type;
                    }
                    let rule= {
                        type: type,
                        value: this.state.stream.field_rule[index].value,
                        field: this.state.stream.field_rule[index].field,
                        inverted: inverted
                    };
                    tempRules.push(rule);
                }
            }
            
            let tempStream = {
                title: "WizardTempStream",
                description: "Temporary stream to test wizard rules",
                matching_type: this.state.stream.matching_type,
                rules: tempRules,
                index_set_id: this.state.indexSets[0].id
            };
            
            StreamsStore.save(tempStream, stream => {
                // TODO why do we set this in the state??
                this.setState({tempStreamID: stream.stream_id});
                /* Get the rules */
                // TODO we don't seem to get here... Most probably because it should be stream.id rather than stream.stream_id
                // TODO is it really necessary to get the stream? (don't we already have the information?)
                StreamsStore.get(stream.stream_id, stream => {
                    
                    let newRules = [];
                    for (let index = 0; index < stream.rules.length; index++) {
                        let rule = {
                            value: stream.rules[index].value,
                            field: stream.rules[index].field,
                            id: stream.rules[index].id,
                            type: stream.rules[index].inverted ? -stream.rules[index].type : stream.rules[index].type
                        };
                        newRules.push(rule);
                    }
                    const update = ObjectUtils.clone(this.state.stream);
                    update.field_rule = newRules;
                    this.setState({stream: update}); 
                    
                    /*Check the rules*/
                    // TODO seems the try button is active only in update, why is that?
                    StreamsStore.testMatch(stream.id, {message: this.props.message.fields}, (resultData) => {
                        this.setState({matchData: resultData});

                        /* Remove temporary stream */
                        StreamsStore.remove(this.state.tempStreamID); 
                    });
                });
            });
        } else {
            this.setState({matchData: undefined});
        }
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
                <Button disabled={!this.props.message} onClick={this._checkFieldsCondition} bsStyle="info" title={messages.tryFieldsCondition} style={{marginLeft: '10em'}}>
                    <FormattedMessage id ="wizard.try" defaultMessage="Try" />
                </Button>
                <br/><br/>

                <FieldRuleList fieldRules={this.state.stream.field_rule} matchData={this.state.matchData} onSaveStream={this._onSaveStream} />
            </Col>
        </Row>
        );
    },
});

export default injectIntl(FieldsInput);
