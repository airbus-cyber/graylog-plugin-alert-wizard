import PropTypes from 'prop-types';
import React from 'react';
import createReactClass from 'create-react-class';
import {Input} from 'components/bootstrap';
import {Spinner, MultiSelect} from 'components/common';
import {FormattedMessage} from 'react-intl';
import StoreProvider from 'injection/StoreProvider';
import naturalSort from 'javascript-natural-sort';
import {Col, Row} from 'react-bootstrap';

const FieldsStore = StoreProvider.getStore('Fields');

const GroupByCondition = createReactClass({
    displayName: 'GroupByCondition',

    propTypes: {
        onUpdate: PropTypes.func,
    },   
    getInitialState() {
        return {
            grouping_fields:this.props.grouping_fields,
        };
    },
    componentDidMount() {
        FieldsStore.loadFields().then((fields) => {
            this.setState({fields: fields});
        });
    },
    _formatOption(key, value) {
        return {value: value, label: key};
    },
    _onGroupingFieldsChange(nextValue) {
        const values = (nextValue === '' ? [] : nextValue.split(','));
        this.setState({grouping_fields: values});
        this.props.onUpdate('grouping_fields', values);
    },

    _isLoading() {
        return !(this.state.fields);
    },

    render() {

        if (this._isLoading()) {
            return (
                <div style={{marginLeft: 10}}>
                    <Spinner/>
                </div>
            );
        }

        const formattedOptions = Object.keys(this.state.fields).map(key => this._formatOption(this.state.fields[key], this.state.fields[key]))
        .sort((s1, s2) => naturalSort(s1.label.toLowerCase(), s2.label.toLowerCase()));

        return (
            <Row>
                <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                    <label className="pull-right" ><FormattedMessage id= "wizard.groupBy" defaultMessage= "Group by Condition" /></label>
                </Col>
                <Col md={10}>
                    <label><FormattedMessage id= "wizard.groupByLabel" defaultMessage= "Messages must be grouped by" /></label>
                    <Input ref="grouping_fields" id="grouping_fields" name="grouping_fields">
                        <MultiSelect autoFocus={false} autosize={true} style={{minWidth:'300px'}}
                                 options={formattedOptions}
                                 value={this.state.grouping_fields ? (Array.isArray(this.state.grouping_fields) ? this.state.grouping_fields.join(',') : this.state.grouping_fields) : undefined}
                                 onChange={this._onGroupingFieldsChange}
                                 allowCreate={true}/>
                    </Input>
                </Col>
            </Row>
        );
        
    },
});

export default GroupByCondition;
