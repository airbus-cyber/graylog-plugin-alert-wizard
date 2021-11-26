import createReactClass from 'create-react-class';
import React from 'react';
import PropTypes from 'prop-types';
import {LinkContainer} from 'react-router-bootstrap';
import {Button} from 'components/graylog';
import {FormattedMessage} from 'react-intl';
import Routes from 'routing/Routes';

const ButtonToEventDefintion = createReactClass({

    propTypes: {
        target: PropTypes.string.isRequired,
        disabled: PropTypes.bool,
    },

    contextTypes: {
        intl: PropTypes.object.isRequired,
    },

    componentWillMount() {
        const tooltip = this.context.intl.formatMessage({id: "wizard.tooltipEventDefinition", defaultMessage: "Edit event definition for this alert rule"});
        this.setState({tooltip:tooltip});
    },

    render() {
        return (
            <LinkContainer disabled={this.props.disabled} to={Routes.ALERTS.DEFINITIONS.edit(this.props.target)} >
                <Button bsStyle="info" title={this.state.tooltip} >
                    <FormattedMessage id="wizard.eventDefinition" defaultMessage="Event definition" />
                </Button>
            </LinkContainer>
        );
    }
});

export default ButtonToEventDefintion;
