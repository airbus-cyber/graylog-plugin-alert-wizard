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

import PropTypes from 'prop-types';
import React from 'react';
import createReactClass from 'create-react-class';
import { Input, Row, Col } from 'components/bootstrap';
import { Select } from 'components/common';
import { injectIntl, FormattedMessage } from 'react-intl';

// TODO convert to a functional component
const TitleSeverity = createReactClass({
    displayName: 'TitleSeverity',

    propTypes: {
        onUpdate: PropTypes.func,
    },   
    getDefaultProps() {
        return {
            title: '',
            severity: '',
        };
    },
    getInitialState() {
        return {
            title: this.props.title,
            severity: this.props.severity,
        };
    },
    _onTitleChanged() {
        return e => {
            this.setState({title: e.target.value});
            this.props.onUpdate('title', e.target.value);
        };
    },
   
    _availableSeverityTypes() {
        return [
            {value: 'info', label: <FormattedMessage id="wizard.info" defaultMessage="Info" />},
            {value: 'low', label: <FormattedMessage id="wizard.low" defaultMessage="Low" />},
            {value: 'medium', label: <FormattedMessage id="wizard.medium" defaultMessage="Medium" />},
            {value: 'high', label: <FormattedMessage id="wizard.high" defaultMessage="High" />},
        ];
    },
    _onSeverityTypeSelect(id) {
        this.setState({severity: id});
        this.props.onUpdate('severity', id)
    },
    
    render() {
        const { intl } = this.props;

        return (
            <Row>
                <Col md={2} style={{ marginTop: 10, marginBottom: 0 }}>
                    <label className="pull-right"><FormattedMessage id="wizard.titleSeverity" defaultMessage="Alert Title and Severity" /></label>
                </Col>
                <Col md={10}>
                    <Input style={{borderTopRightRadius: '0px', borderBottomRightRadius: '0px', height:'36px', width:'450px'}}
                           ref="title" id="title" name="title" type="text"
                           onChange={this._onTitleChanged()}
                           defaultValue={this.state.title}/>
                    <Input ref="severity" id="severity" name="severity">
                        <div style={{width:'150px'}}>
                        <Select
                            value={this.state.severity}
                            options={this._availableSeverityTypes()}
                            matchProp="value"
                            onChange={this._onSeverityTypeSelect}
                            clearable={false}
                            placeholder={intl.formatMessage({id: "wizard.select", defaultMessage: "Select..."})}
                        />
                        </div>
                    </Input>
                </Col>
            </Row>
        );
        
    },
});

export default injectIntl(TitleSeverity);
