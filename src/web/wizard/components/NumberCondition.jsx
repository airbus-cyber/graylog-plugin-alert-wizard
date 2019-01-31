import PropTypes from 'prop-types';
import React from 'react';
import Reflux from 'reflux';
import {Input} from 'components/bootstrap';
import {Select, Spinner} from 'components/common';
import {Col, Row} from 'react-bootstrap';
import ObjectUtils from 'util/ObjectUtils';
import {FormattedMessage} from 'react-intl';


const NumberCondition = React.createClass({

    propTypes: {
        onUpdate: PropTypes.func,
    },   
    getDefaultProps() {
        return {
            threshold_type:'',
            threshold:'',
        };
    },
    getInitialState() {
        return {
            threshold_type:this.props.threshold_type,
            threshold:this.props.threshold,
        };
    },
    componentWillReceiveProps(nextProps) {
        if(nextProps.threshold !== this.props.threshold){
            this.setState({threshold: nextProps.threshold});
        }
        if(nextProps.threshold_type !== this.props.threshold_type){
            this.setState({threshold_type: nextProps.threshold_type});
        }
    },
    _availableThresholdTypes() {
        return [
            {value: 'MORE', label: <FormattedMessage id= "wizard.more" defaultMessage= "more than" />},
            {value: 'LESS', label: <FormattedMessage id= "wizard.less" defaultMessage= "less than" />},
        ];
    },
    _onThresholdTypeSelect(id) {
        this.setState({threshold_type: id});
        this.props.onUpdate('threshold_type', id)
    },
    _onThresholdChanged() {
        return e => {
            this.setState({threshold: e.target.value});
            this.props.onUpdate('threshold', e.target.value);
        };
    },
    
    render() {

        return (
            <Row>
                <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                    <label className="pull-right"><FormattedMessage id= "wizard.NumberCondition" defaultMessage= "Count Condition" /></label>
                </Col>
                <Col md={10}>
                    <label><FormattedMessage id= "wizard.thereMustBe" defaultMessage= "There must be " /></label>
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
                    <Input ref="threshold" id="threshold" name="threshold" type="number" onChange={this._onThresholdChanged()}
                           value={this.state.threshold}
                           style={{borderTopLeftRadius: '0px', borderBottomLeftRadius: '0px', height:'36px', width:'100px'}} />
                    <label>&nbsp; </label>
                    <label><FormattedMessage id= "wizard.messages" defaultMessage= "messages" /></label>
                    
                </Col>
            </Row>
        );
        
    },
});

export default NumberCondition;
