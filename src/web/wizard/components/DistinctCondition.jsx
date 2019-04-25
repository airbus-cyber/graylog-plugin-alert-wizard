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

const DistinctCondition = createReactClass({
    displayName: 'DistinctCondition',

    propTypes: {
        onUpdate: PropTypes.func,
    },   
    getInitialState() {
        return {
            distinction_fields:this.props.distinction_fields,
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
    _onDistinctionFieldsChange(nextValue) {
        const values = (nextValue === '' ? [] : nextValue.split(','));
        this.setState({distinction_fields: values});
        this.props.onUpdate('distinction_fields', values);
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
                    <label className="pull-right" ><FormattedMessage id= "wizard.distinctBy" defaultMessage= "Distinct by Condition" /></label>
                </Col>
                <Col md={10}>
                    <label><FormattedMessage id= "wizard.distinctByLabel" defaultMessage= "Messages must be distincted by" /></label>
                    <Input ref="distinction_fields" id="distinction_fields"
                        name="distinction_fields">
                        <MultiSelect autoFocus={false} autosize={true} style={{minWidth:'300px'}}
                                  options={formattedOptions}
                                  value={this.state.distinction_fields ? (Array.isArray(this.state.distinction_fields) ? this.state.distinction_fields.join(',') : this.state.distinction_fields) : undefined}
                                  onChange={this._onDistinctionFieldsChange}
                                  allowCreate={true}/>
                    </Input>
                </Col>
            </Row>
        );
        
    },
});

export default DistinctCondition;
