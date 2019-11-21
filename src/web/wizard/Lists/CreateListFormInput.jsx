import PropTypes from 'prop-types';
import React from 'react';
import createReactClass from 'create-react-class';
import Reflux from 'reflux';
import {Button, Col, Row} from 'react-bootstrap';
import {Spinner} from 'components/common';
import ObjectUtils from 'util/ObjectUtils';
import Routes from 'routing/Routes';
import {LinkContainer} from 'react-router-bootstrap';
import 'react-confirm-alert/src/react-confirm-alert.css';
import {FormattedMessage} from 'react-intl';
import {Input} from 'components/bootstrap';
import AlertListActions from "./AlertListActions";
import AlertListStore from "./AlertListStore";

const INIT_LIST = {
    title: '',
    description: '',
    lists: '',
    };

const CreateListFormInput = createReactClass({
        displayName: 'CreateListFormInput',

    mixins: [Reflux.connect(AlertListStore)],

    propTypes: {
        list: PropTypes.object,
        create: PropTypes.bool.isRequired,
    },
    contextTypes: {
        intl: PropTypes.object.isRequired,
    },


    getDefaultProps() {
        return {
            list: INIT_LIST,
            default_values: {
                title: '',
                description: '',
                lists: '',
            },
        };
    },

    getInitialState() {
        let list = ObjectUtils.clone(this.props.list);

        if (this.props.create) {
            list.title = this.props.default_values.title;
            list.description = this.props.default_values.description;
            list.lists = this.props.default_values.lists;
        }

        return {
            list: list,
            isModified: false,
            contentComponent: <Spinner/>,
        };
    },

    _save() {
        AlertListActions.create.triggerPromise(this.state.list).then((response) => {
            if (response === true) {
                    this.setState({list: list});
            }
        });
        this.setState({isModified: false});
    },

    _update() {
        AlertListActions.update.triggerPromise(this.props.list.title, this.state.list).then((response) => {
            if (response === true) {
                    this.setState({list: list});
            }
        });
        this.setState({isModified: false});
    },

    // TODO add le bouton pour importer un fichier de listes dans le champ liste
    /*onSubmitUploadFile(submitEvent) {
        submitEvent.preventDefault();
        if (!this.refs.uploadedFile.files || !this.refs.uploadedFile.files[0]) {
            return;
        }

        const reader = new FileReader();
        reader.onload = (evt) => {
            this.setState({alertLists: JSON.parse(evt.target.result)});
        };

        reader.readAsText(this.refs.uploadedFile.files[0]);
    },*/

    _updateConfigField(field, value) {
        const update = ObjectUtils.clone(this.state.list);
        update[field] = value;
        this.setState({ list: update });
    },

    _onUpdate(field) {
        return e => {
            this._updateConfigField(field, e.target.value);
        };
    },

    render: function() {

        let actions;

        const buttonCancel = (
            <LinkContainer to={Routes.pluginRoute('WIZARD_LISTS')}>
                <Button><FormattedMessage id= "wizard.cancel" defaultMessage= "Cancel" /></Button>
            </LinkContainer>
        );

        let buttonSave;
        if (this.props.create) {
            buttonSave = (
                <LinkContainer to={Routes.pluginRoute('WIZARD_LISTS')}>
                    <Button onClick={this._save} disabled={this.state.list.title === '' || this.state.list.lists === ''} className="btn btn-md btn-primary">
                        <FormattedMessage id="wizard.save" defaultMessage="Save"/>
                    </Button>
                </LinkContainer>
            );
        }
        else {
            buttonSave = (
                <LinkContainer to={Routes.pluginRoute('WIZARD_LISTS')}>
                    <Button onClick={this._update}
                            className="btn btn-md btn-primary">
                        <FormattedMessage id= "wizard.save" defaultMessage= "Save" />
                    </Button>
                </LinkContainer>
            );
        }

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
                        <Input id="title" type="text" required label={<FormattedMessage id ="wizard.title" defaultMessage="Title" />}
                               onChange={this._onUpdate('title')} defaultValue={this.state.list.title} name="title" />
                        <Input id="description" type="text" label={<FormattedMessage id= "wizard.fieldOptionalDescription" defaultMessage= "Description (optional)" />}
                               onChange={this._onUpdate('description')}
                               defaultValue = {this.state.list.description}
                               name="description"/>
                    </Col>
                </Row>
                <Row style={style}>
                    <Col md={5}>
                    <Input style={{minWidth: 600}} ref="lists" id="lists" name="lists" type="textarea" rows="10"
                           label={<FormattedMessage id ="wizard.fieldListwithexemple" defaultMessage="List (example : 172.10.0.1; 192.168.1.4; ...)" />}
                           onChange={this._onUpdate('lists')} defaultValue = {this.state.list.lists}/>
                        {actions}
                    </Col>
                </Row>
            </div>
        );
    },
});

export default CreateListFormInput;