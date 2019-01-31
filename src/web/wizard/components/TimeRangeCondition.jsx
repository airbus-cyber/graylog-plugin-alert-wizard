import PropTypes from 'prop-types';
import React from 'react';
import Reflux from 'reflux';
import {Input} from 'components/bootstrap';
import {Select, Spinner} from 'components/common';
import {Col, Row} from 'react-bootstrap';
import ObjectUtils from 'util/ObjectUtils';
import {FormattedMessage} from 'react-intl';


const TimeRangeCondition = React.createClass({

    propTypes: {
        onUpdate: PropTypes.func,
    },   
    getDefaultProps() {
        return {
            time:0,
            time_type:1,
        };
    },
    getInitialState() {       
        return {
            time:this.props.time,
            time_type:this.props.time_type,
        };
    },
    _availableTimeTypes() {
        return [
            {value: 1, label: <FormattedMessage id= "wizard.minutes" defaultMessage= "minutes" />},
            {value: 60, label: <FormattedMessage id= "wizard.hours" defaultMessage= "hours" />},
            {value: 1440, label: <FormattedMessage id= "wizard.days" defaultMessage= "days" />},
        ];
    },
    _onTimeSelect(value) {
        this.setState({time_type: value});
        this.props.onUpdate("time", value * this.state.time);
    },
    _onTimeChanged() {
        return e => {
            this.setState({time: e.target.value});
            this.props.onUpdate("time", e.target.value * this.state.time_type);
        };
    },

    render() {
        
        return (
            <Row>
                <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                    <label className="pull-right"><FormattedMessage id= "wizard.timeRangeCondition" defaultMessage= "Time Range Condition" /></label>
                </Col>
                <Col md={10}>
                    <label><FormattedMessage id= "wizard.messagesCome" defaultMessage= "Messages must come in the last" /></label>
                    <Input ref="time" id="time" name="time" type="number" onChange={this._onTimeChanged()} value={this.state.time}
                           style={{borderTopRightRadius: '0px', borderBottomRightRadius: '0px', height:'36px' , width:'100px'}} />
                    <Input ref="time_type" id="time_type" name="time_type" required className="form-control">
                        <Select style={{borderTopLeftRadius: '0px', borderBottomLeftRadius: '0px'}} 
                            autosize={false}
                            required
                            value={this.state.time_type}
                            options={this._availableTimeTypes()}
                            matchProp="value"
                            onChange={this._onTimeSelect}
                            placeholder={<FormattedMessage id= "wizard.select" defaultMessage= "Select..." />}
                        />
                    </Input>
                </Col>
            </Row>
        );
        
    },
});

export default TimeRangeCondition;
