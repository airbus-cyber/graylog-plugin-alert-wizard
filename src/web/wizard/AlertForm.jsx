import PropTypes from 'prop-types';
import React from 'react';
import createReactClass from 'create-react-class';
import LinkedStateMixin from 'react-addons-linked-state-mixin';
import BootstrapModalForm from 'components/bootstrap/BootstrapModalForm';
import {Input} from 'components/bootstrap';
import {FormattedMessage} from 'react-intl';

const AlertForm = createReactClass({
    displayName: 'AlertForm',

    propTypes: {
        onSubmit: PropTypes.func.isRequired,
    },

    contextTypes: {
        intl: PropTypes.object.isRequired,
    },

    mixins: [LinkedStateMixin],

    componentWillMount(){
        const messages = {
                placeholderTitle: this.context.intl.formatMessage({id: "wizard.placeholderCloneTitle", defaultMessage: "A descriptive name of the new alert rule"}),
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

    render() {
        return (
            <BootstrapModalForm ref="modal"
                                title={<FormattedMessage id= "wizard.cloneAlertRule" defaultMessage= 'Cloning Alert Rule "{title}"' values={{title: this.state.origTitle }} />}
                                onSubmitForm={this._onSubmit}
                                cancelButtonText={<FormattedMessage id= "wizard.cancel" defaultMessage= "Cancel" />}
                                submitButtonText={<FormattedMessage id= "wizard.save" defaultMessage= "Save" />}>
                <Input id="Title" type="text" required label={<FormattedMessage id ="wizard.title" defaultMessage="Title" />} name="Title"
                       placeholder={this.state.messages.placeholderTitle}
                       valueLink={this.linkState('title')} autoFocus/>
                <Input id="Description" type="text" label={<FormattedMessage id= "wizard.fieldDescription" defaultMessage= "Description" />} name="Description"
                       valueLink={this.linkState('description')}/>
            </BootstrapModalForm>
        );
    },
});

export default AlertForm;
