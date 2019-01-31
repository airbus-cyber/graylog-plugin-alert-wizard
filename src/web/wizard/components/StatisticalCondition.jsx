import PropTypes from 'prop-types';
import React from 'react';
import {Col, Row} from 'react-bootstrap';
import {Input} from 'components/bootstrap';
import {DocumentTitle, Select, Spinner} from 'components/common';
import ObjectUtils from 'util/ObjectUtils';
import {FormattedMessage} from 'react-intl';
import StoreProvider from 'injection/StoreProvider';
import naturalSort from 'javascript-natural-sort';

const FieldsStore = StoreProvider.getStore('Fields');

const StatisticalCondition = React.createClass({

    propTypes: {
        onUpdate: PropTypes.func.isRequired,
    },

    getInitialState() {
        return {
            type: this.props.type,
            field: this.props.field,
            threshold: this.props.threshold,
            threshold_type: this.props.threshold_type,
        };
    },
    componentDidMount() {
        FieldsStore.loadFields().then((fields) => {
            //add value to list fields if not present
            if (this.state.field && this.state.field !== '' && fields.indexOf(this.state.field) < 0) {
                fields.push(this.state.field);
            }
            this.setState({fields: fields});
        });
    },
    _availableAggregationTypes() {
        return [
            {value: 'MEAN', label: <FormattedMessage id= "wizard.meanValue" defaultMessage= "mean value" />},
            {value: 'STDDEV', label: <FormattedMessage id= "wizard.standardDeviation" defaultMessage= "standard deviation" />},
            {value: 'MIN', label: <FormattedMessage id= "wizard.minValue" defaultMessage= "min value" />},
            {value: 'MAX', label: <FormattedMessage id= "wizard.maxValue" defaultMessage= "max value" />},
            {value: 'SUM', label: <FormattedMessage id= "wizard.sum" defaultMessage= "sum" />},
        ];
    },
    _onAggregationTypeSelect(value) {
        this.setState({type: value});
        this.props.onUpdate('type', value);
    },
    _availableThresholdTypes() {
        return [
            {value: 'HIGHER', label: <FormattedMessage id= "wizard.higher" defaultMessage= "higher than" />},
            {value: 'LOWER', label: <FormattedMessage id= "wizard.lower" defaultMessage= "lower than" />},
        ];
    },
    _onThresholdTypeSelect(value) {
        this.setState({threshold_type: value});
        this.props.onUpdate('threshold_type', value);
    },
    
    _updateAlertField(field, value) {
        const update = ObjectUtils.clone(this.state);
        update[field] = value;
        this.setState({parameters: update});

        this.props.onUpdate(field, value);
    },
    _onThresholdChanged() {
        return e => {
            this.setState({threshold: e.target.value});
            this.props.onUpdate('threshold', e.target.value);
        };
    },
    _onParameterFieldSelect(value) {
        this.setState({field: value});
        this.props.onUpdate('field', value);

        //add value to list fields if not present
        if (value !== '' && this.state.fields.indexOf(value) < 0) {
            const update = ObjectUtils.clone(this.state.fields);
            update.push(value);
            this.setState({fields: update});
        }
    },
    _formatOption(key, value) {
        return {value: value, label: key};
    },
    _isLoading() {
        return (!this.state.fields);
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
                            <label className="pull-right"><FormattedMessage id= "wizard.statisticalCondition" defaultMessage= "Statistical Condition" /></label>
                        </Col>
                        <Col md={10}>
                            <label><FormattedMessage id= "wizard.the" defaultMessage= "The" /></label>
                            <Input ref="type" id="type" name="type" required>
                                <Select
                                    autosize={false}
                                    required
                                    value={this.state.type}
                                    options={this._availableAggregationTypes()}
                                    matchProp="value"
                                    onChange={this._onAggregationTypeSelect}
                                    placeholder={<FormattedMessage id= "wizard.select" defaultMessage= "Select..." />}
                                />
                            </Input>
                            <label>&nbsp; </label>
                            <label><FormattedMessage id= "wizard.of" defaultMessage= "of" /></label> 
                            <Input ref="field" id="field" name="field">
                                <Select
                                    autosize={false}
                                    required
                                    value={this.state.field}
                                    options={formattedOptions}
                                    matchProp="value"
                                    onChange={this._onParameterFieldSelect}
                                    allowCreate={true}
                                    placeholder={<FormattedMessage id= "wizard.select" defaultMessage= "Select..." />}
                                />
                            </Input>
                            <label>&nbsp;</label>
                            <label><FormattedMessage id= "wizard.mustBe" defaultMessage= "must be" /></label>
                            <Input ref="threshold_type" id="threshold_type" name="threshold_type" required>
                                <Select style={{borderTopRightRadius: '0px', borderBottomRightRadius: '0px'}}
                                    autosize={false}
                                    required
                                    value={this.state.threshold_type}
                                    options={this._availableThresholdTypes()}
                                    matchProp="value"
                                    onChange={this._onThresholdTypeSelect}
                                    placeholder={<FormattedMessage id= "wizard.select" defaultMessage= "Select..." />}
                                />
                            </Input>
                            <Input ref="threshold" id="threshold" name="threshold" type="number"
                                   onChange={this._onThresholdChanged()}
                                   value={this.state.threshold}
                                   style={{borderTopLeftRadius: '0px', borderBottomLeftRadius: '0px', height:'36px', width:'100px'}}/>
                        </Col>
                    </Row>
        );
    },
});

export default StatisticalCondition;
