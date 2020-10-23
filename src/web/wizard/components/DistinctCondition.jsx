import React from 'react';
import PropTypes from 'prop-types';
import createReactClass from 'create-react-class';
import { Input } from 'components/bootstrap';
import { MultiSelect } from 'components/common';
import {FormattedMessage} from 'react-intl';
import { Row, Col } from 'components/graylog';

import withFormattedFields from './withFormattedFields';

const DistinctCondition = createReactClass({
    displayName: 'DistinctCondition',

    propTypes: {
        onUpdate: PropTypes.func,
        formattedFields: PropTypes.array.isRequired,
    },   
    getInitialState() {
        return {
            distinction_fields:this.props.distinction_fields,
        };
    },
    _onDistinctionFieldsChange(nextValue) {
        const values = (nextValue === '' ? [] : nextValue.split(','));
        this.setState({distinction_fields: values});
        this.props.onUpdate('distinction_fields', values);
    },

    render() {
        const { formattedFields } = this.props;

        return (
            <Row>
                <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                    <label className="pull-right" ><FormattedMessage id= "wizard.distinctBy" defaultMessage= "Distinct by Condition" /></label>
                </Col>
                <Col md={10}>
                    <label><FormattedMessage id= "wizard.distinctByLabel" defaultMessage= "Messages must be distincted by" /></label>
                    <Input ref="distinction_fields" id="distinction_fields"
                        name="distinction_fields">
                        <div style={{minWidth:'300px'}}>
                        <MultiSelect autoFocus={false}
                                  options={formattedFields}
                                  value={this.state.distinction_fields ? (Array.isArray(this.state.distinction_fields) ? this.state.distinction_fields.join(',') : this.state.distinction_fields) : undefined}
                                  onChange={this._onDistinctionFieldsChange}
                                  allowCreate={true}/>
                        </div>
                    </Input>
                </Col>
            </Row>
        );
    },
});

export default withFormattedFields(DistinctCondition);
