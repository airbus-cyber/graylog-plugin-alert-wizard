import createReactClass from 'create-react-class';
import React from 'react';
import PropTypes from 'prop-types';
import {LinkContainer} from 'react-router-bootstrap';
import {Button} from 'components/graylog';
import {FormattedMessage} from 'react-intl';
import Routes from 'routing/Routes';

const ButtonToNotification = createReactClass({

    propTypes: {
        target: PropTypes.string.isRequired,
        disabled: PropTypes.bool,
    },

    contextTypes: {
        intl: PropTypes.object.isRequired,
    },

    componentWillMount() {
        const tooltip = this.context.intl.formatMessage({id: "wizard.tooltipNotification", defaultMessage: "Edit notification for this alert rule"});
        this.setState({tooltip:tooltip});
    },

    render() {
        // TODO could look for all the LinkContainer/Button/FormattedMessage and maybe factor
        return (
            <LinkContainer disabled={this.props.disabled} to={Routes.ALERTS.NOTIFICATIONS.edit(this.props.target)} >
                <Button bsStyle="info" title={this.state.tooltip} >
                    <FormattedMessage id="wizard.notification" defaultMessage="Notification" />
                </Button>
            </LinkContainer>
        );
    }
});

export default ButtonToNotification;
