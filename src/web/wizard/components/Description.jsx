import PropTypes from 'prop-types';
import React from 'react';
import Reflux from 'reflux';
import {Input} from 'components/bootstrap';
import {Select, Spinner} from 'components/common';
import {Col, Row} from 'react-bootstrap';
import ObjectUtils from 'util/ObjectUtils';
import {FormattedMessage} from 'react-intl';


const Description = React.createClass({

    propTypes: {
        onUpdate: PropTypes.func,
    },   
    getDefaultProps() {
        return {
            description:'',
        };
    },
    getInitialState() {
        return {
            description:this.props.description,
        };
    },
    _onValueChanged(field) {
        return e => {
            this.props.onUpdate(field, e.target.value);
        };
    },
    
    render() {
        return (
            <Row>               
                <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                    <label className="pull-right" ><FormattedMessage id= "wizard.titleDescription" defaultMessage= "Description (optional)" /></label>
                </Col>
                <Col md={10}>    
                    <Input style={{minWidth: 600}} ref="description" id="description" name="description" type="textarea"
                                   onChange={this._onValueChanged("description")} defaultValue={this.state.description}/>
                </Col>
            </Row>
        );
    },
});

export default Description;
