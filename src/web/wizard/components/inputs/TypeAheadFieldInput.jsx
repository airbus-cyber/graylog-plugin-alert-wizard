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

import React from 'react';
import { useEffect, useState } from 'react';

import { withTheme } from 'styled-components';
import { themePropTypes } from 'theme';
import isEqual from 'lodash/isEqual';

import PropTypes from 'prop-types';
import ReactDOM from 'react-dom';
import $ from 'jquery';
import 'typeahead.js';

import { Input } from 'components/bootstrap';
import UniversalSearch from 'logic/search/UniversalSearch';
import ApiRoutes from 'routing/ApiRoutes';
import { qualifyUrl } from 'util/URLUtils';
import fetch from 'logic/rest/FetchProvider';

// TODO should try to remove this component.
//      this is taken and modified from the graylog TypeAheadFieldInput which has a bug when switching the theme
//      see https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/118
class TypeAheadFieldInput extends React.Component {
    static propTypes = {
        onChange: PropTypes.func,
        theme: themePropTypes.isRequired,
    };

    static defaultProps = {
        onChange: () => {},
    };

    startTypeahead() {
        const fieldInput = $(this.fieldInput.getInputDOMNode());

        fieldInput.typeahead({
            hint: true,
            highlight: true,
            minLength: 1,
        }, {
            name: 'fields',
            displayKey: 'value',
            source: this.source,
        });
    }

    stopTypeahead() {
        const fieldInput = $(this.fieldInput.getInputDOMNode());

        fieldInput.typeahead('destroy');
    }

    componentDidMount() {
        if (this.fieldInput) {
            const { onChange } = this.props;

            fetch('GET', qualifyUrl(ApiRoutes.SystemApiController.fields().url))
            .then((data) => {
                this.source = UniversalSearch.substringMatcher(data.fields, 'value', 6)
                this.startTypeahead();
            });

            const fieldInput = $(this.fieldInput.getInputDOMNode());
            // eslint-disable-next-line react/no-find-dom-node
            const fieldFormGroup = ReactDOM.findDOMNode(this.fieldInput);

            $(fieldFormGroup).on('typeahead:change typeahead:selected', (event) => {
                if (onChange) {
                    onChange(event);
                }
            });
        }
    }

    componentDidUpdate(prevProps) {
        if (!isEqual(this.props.theme, prevProps.theme)) {
            const fieldInput = $(this.fieldInput.getInputDOMNode());

            fieldInput.typeahead('destroy');
            fieldInput.typeahead({
                  hint: true,
                  highlight: true,
                  minLength: 1,
              }, {
                  name: 'fields',
                  displayKey: 'value',
                  source: this.source,
              });
        }
    }

    componentWillUnmount() {
        if (this.fieldInput) {
            this.stopTypeahead();

            const fieldInput = $(this.fieldInput.getInputDOMNode());
            // eslint-disable-next-line react/no-find-dom-node
            const fieldFormGroup = ReactDOM.findDOMNode(this.fieldInput);

            $(fieldFormGroup).off('typeahead:change typeahead:selected');
        }
    }

    render() {
        const { defaultValue } = this.props;

        return (
            <Input id="field-input" type="text" required name="field"
                   ref={(fieldInput) => { this.fieldInput = fieldInput; }}
                   wrapperClassName="typeahead-wrapper"
                   defaultValue={defaultValue} />
        );
    }
}

const TypeAheadFieldInputWithTheme = withTheme(TypeAheadFieldInput);

// TODO this is a hack, because it seems the callback provided to the TypeAheadFieldInput is called in a context
//      where it does not have access the the current value of other states
//      so, instead of directly calling the onChange callback, we set the event value
//      and then the effect will call the onChange callback in a context where it works...
//   => try to pinpoint the bug and report to Graylog/or try to use some other widget (react-bootstrap-typeahead)/or try to implement own
const TypeAheadFieldInputWrapper = ({ defaultValue, onChange }) => {
    const [event, setEvent] = useState();

    const _onChangeWrapper = () => {
        if (event === undefined) {
            return;
        }
        onChange(event);
    }

    useEffect(_onChangeWrapper, [event]);

    return (
        <TypeAheadFieldInputWithTheme defaultValue={defaultValue} onChange={setEvent} />
    )
}

export default TypeAheadFieldInputWrapper;