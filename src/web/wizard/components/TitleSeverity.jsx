import PropTypes from 'prop-types';
import React from 'react';
import createReactClass from 'create-react-class';
import {Input} from 'components/bootstrap';
import {Select, Spinner} from 'components/common';
import {Col, Row} from 'react-bootstrap';
import {FormattedMessage} from 'react-intl';

const TitleSeverity = createReactClass({
    displayName: 'TitleSeverity',

    propTypes: {
        onUpdate: PropTypes.func,
    },   
    getDefaultProps() {
        return {
            title:'',
            severity:'',
            isPluginLoggingAlertPresent:true,
        };
    },
    getInitialState() {
        return {
            title: this.props.title,
            severity: this.props.severity,
        };
    },
    componentWillReceiveProps(nextProps) {
        if(nextProps.isPluginLoggingAlertPresent !== this.props.isPluginLoggingAlertPresent){
            this._onSeverityTypeSelect(nextProps.isPluginLoggingAlertPresent? nextProps.severity: '');
        }
    },
    _onTitleChanged() {
        return e => {
            this.setState({title: e.target.value});
            this.props.onUpdate('title', e.target.value);
        };
    },
   
    _availableSeverityTypes() {
        return [
            {value: 'info', label: <FormattedMessage id= "wizard.info" defaultMessage= "Info" />},
            {value: 'low', label: <FormattedMessage id= "wizard.low" defaultMessage= "Low" />},
            {value: 'medium', label: <FormattedMessage id= "wizard.medium" defaultMessage= "Medium" />},
            {value: 'high', label: <FormattedMessage id= "wizard.high" defaultMessage= "High" />},
        ];
    },
    _onSeverityTypeSelect(id) {
        this.setState({severity: id});
        this.props.onUpdate('severity', id)
    },
    
    render() {

        return (
            <Row>
                <Col md={2} style={{ marginTop: 10, marginBottom: 0 }}>
                    <label className="pull-right"><FormattedMessage id= "wizard.titleSeverity" defaultMessage= "Alert Title and Severity" /></label>
                </Col>
                <Col md={10}>
                    <Input style={{borderTopRightRadius: '0px', borderBottomRightRadius: '0px', height:'36px', width:'450px'}}
                           ref="title" id="title" name="title" type="text"
                           onChange={this._onTitleChanged()}
                           defaultValue={this.state.title} minWidth={true}/>
                    <Input ref="severity" id="severity" name="severity">
                        <Select style={{borderTopLeftRadius: '0px', borderBottomLeftRadius: '0px'}}
                            autosize={false}
                            value={this.state.severity}
                            options={this._availableSeverityTypes()}
                            matchProp="value"
                            onChange={this._onSeverityTypeSelect}
                            disabled={!this.props.isPluginLoggingAlertPresent}
                            placeholder={<FormattedMessage id= "wizard.select" defaultMessage= "Select..." />}
                        />
                    </Input>
                </Col>
            </Row>
        );
        
    },
});

export default TitleSeverity;
