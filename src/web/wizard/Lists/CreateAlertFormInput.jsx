import PropTypes from 'prop-types';
import React from 'react';
import createReactClass from 'create-react-class';
import Reflux from 'reflux';
import {Button, Col, Row, Nav, NavItem} from 'react-bootstrap';
import {Spinner} from 'components/common';
import ObjectUtils from 'util/ObjectUtils';
import Routes from 'routing/Routes';
import {LinkContainer} from 'react-router-bootstrap';
import {confirmAlert} from 'react-confirm-alert';
import 'react-confirm-alert/src/react-confirm-alert.css';
import {FormattedMessage} from 'react-intl';
import StoreProvider from 'injection/StoreProvider';
import {Input} from 'components/bootstrap';
import LoaderTabs from 'components/messageloaders/LoaderTabs';
import StatisticsCondition from 'wizard/ruletype/StatisticsCondition'
import GroupDistinctCondition from 'wizard/ruletype/GroupDistinctCondition'
import CorrelationCondition from 'wizard/ruletype/CorrelationCondition'
import OrCondition from 'wizard/ruletype/OrCondition'
import CountCondition from 'wizard/ruletype/CountCondition'
import history from 'util/History';
import AlertListActions from "./AlertListActions";

const StreamsStore = StoreProvider.getStore('Streams');
const PluginsStore = StoreProvider.getStore('Plugins');

const CreateAlertFormInput = createReactClass({
        displayName: 'CreateAlertFormInput',

    propTypes: {
        list: PropTypes.object,
        create: PropTypes.bool.isRequired,
        nodes: PropTypes.object,
    },
    contextTypes: {
        intl: PropTypes.object.isRequired,
    },

    _save() {
        // AlertListActions.create.triggerPromise(this.state.list).then((response) => {
        //     if (response === true) {
        //         AlertListActions.getData(this.state.list.title).then(list => {
        //             this.setState({list: list});
        //             this._advancedSettings();
        //         });
        //     }
        // });
        // this.setState({isModified: false});
    },

    onSubmitUploadFile(submitEvent) {
        submitEvent.preventDefault();
        if (!this.refs.uploadedFile.files || !this.refs.uploadedFile.files[0]) {
            return;
        }

        const reader = new FileReader();
        reader.onload = (evt) => {
            this.setState({alertRules: JSON.parse(evt.target.result)});
        };

        reader.readAsText(this.refs.uploadedFile.files[0]);
    },

    render: function() {

        let actions;
        const buttonCancel = (
            <LinkContainer to={Routes.pluginRoute('WIZARD_LISTS')}>
                <Button><FormattedMessage id= "wizard.cancel" defaultMessage= "Cancel" /></Button>
            </LinkContainer>
        );

        let buttonSave;
            buttonSave = (
                <Button onClick={this._save} className="btn btn-md btn-primary">
                    <FormattedMessage id= "wizard.save" defaultMessage= "Save" />
                </Button>
            );

        actions = (
            <div className="alert-actions pull-left">
                {buttonCancel}{' '}
                {buttonSave}{' '}
            </div>);

        const style = { display: 'flex', alignItems: 'center' };

        return (
            <div>
                <Row>
                    <Col md={4}>
                        <Input id="title" type="text" required label={<FormattedMessage id ="wizard.title" defaultMessage="Title" />} name="title"/>
                        <Input id="description" type="text" label={<FormattedMessage id= "wizard.fieldDescription" defaultMessage= "Description" />} name="description"/>
                    </Col>
                </Row>
                <Row style={style}>
                    <Col md={5}>
                    <Input style={{minWidth: 600}} ref="list" id="wizard.fieldList" name="list" type="textarea" rows="10"
                           label={<FormattedMessage id ="wizard.fieldList" defaultMessage="List" />} />
                        {actions}
                    </Col>
                    <Col md={3}>
                        <form onSubmit={this.onSubmitUploadFile} className="upload" encType="multipart/form-data">
                                <input ref="uploadedFile" type="file" name="bundle" />
                        </form>
                    </Col>
                </Row>
            </div>
        );
    },
});

export default CreateAlertFormInput;