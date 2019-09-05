import PropTypes from 'prop-types';
import React from 'react';
import createReactClass from 'create-react-class';
import LinkedStateMixin from 'react-addons-linked-state-mixin';
import BootstrapModalForm from 'components/bootstrap/BootstrapModalForm';
import {Input} from 'components/bootstrap';
import {FormattedMessage} from 'react-intl';

const AlertListForm = createReactClass({
    displayName: 'AlertListForm',

    propTypes: {
        onSubmit: PropTypes.func.isRequired,
    },

    contextTypes: {
        intl: PropTypes.object.isRequired,
    },

    mixins: [LinkedStateMixin],

    componentWillMount(){
        const messages = {
            placeholderTitle: this.context.intl.formatMessage({id: "wizard.placeholderCloneTitle", defaultMessage: "A descriptive name of the new alert list"}),
        };
        this.setState({messages:messages});
    },

    getInitialState() {
        return {
            title: '',
            description: '',
        };
    },

    _resetValues() {
        this.setState({title: ''});
        this.setState({description: ''});
    },

    _onSubmit() {
        this.props.onSubmit(this.state.origTitle, this.state.title, this.state.description);
        this.refs.modal.close();
    },

    open(title) {
        this.setState({origTitle: title});
        this._resetValues();
        this.refs.modal.open();
    },

    close() {
        this.refs.modal.close();
    },

    _onValueChanged(event) {
        this.setState({[event.target.name]: event.target.value});
    },

    render() {
        return (
            <BootstrapModalForm ref="modal"
                                title={<FormattedMessage id= "wizard.cloneAlertList" defaultMessage= 'Cloning Alert List "{title}"' values={{title: this.state.origTitle }} />}
                                onSubmitForm={this._onSubmit}
                                cancelButtonText={<FormattedMessage id= "wizard.cancel" defaultMessage= "Cancel" />}
                                submitButtonText={<FormattedMessage id= "wizard.save" defaultMessage= "Save" />}>
                <Input id="title" type="text" required label={<FormattedMessage id ="wizard.title" defaultMessage="Title" />} name="title"
                       placeholder={this.state.messages.placeholderTitle}
                       onChange={this._onValueChanged} autoFocus/>
                <Input id="description" type="text" label={<FormattedMessage id= "wizard.fieldDescription" defaultMessage= "Description" />} name="description"
                       onChange={this._onValueChanged}/>
            </BootstrapModalForm>
        );
    },
});

export default AlertListForm;
