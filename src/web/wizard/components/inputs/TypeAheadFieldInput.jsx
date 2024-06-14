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
import { useEffect, useState, useRef } from 'react';
import { useTheme } from 'styled-components';
import ReactDOM from 'react-dom';
import $ from 'jquery';
import 'typeahead.js';

import { Input } from 'components/bootstrap';
import UniversalSearch from 'logic/search/UniversalSearch';
import ApiRoutes from 'routing/ApiRoutes';
import { qualifyUrl } from 'util/URLUtils';
import fetch from 'logic/rest/FetchProvider';

// TODO should try to remove this component and rather report the issues with this component to Graylog, so that they fix it
//      this is taken and modified from the graylog TypeAheadFieldInput which has a bug when switching the theme
//      see https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/118
// TODO maybe in more recent versions of Graylog this will be fixed?
const TypeAheadFieldInput = ({defaultValue, onChange}) => {
    const theme = useTheme();
    const fieldInput = useRef(null);

    // TODO The state was added, because otherwise onChange would be called in a context
    //      where it does not have access the the current value of other states (the context of the jquery event)
    //      and then when the value of the typeahead field is changed, the other associated inputs of a field rule are cleared :(
    //      so, instead of directly calling the onChange callback, we set the event value
    //      and then the effect will call the onChange callback in a context where it works...
    //   => this bug should be reported to Graylog
    const [event, setEvent] = useState();

    useEffect(() => {
        if (event === undefined) {
            return;
        }
        onChange(event);
    }, [event]);

    useEffect(() => {
        fetch('GET', qualifyUrl(ApiRoutes.SystemApiController.fields().url))
        .then((data) => {
            $(fieldInput.current.getInputDOMNode()).typeahead({
                hint: true,
                highlight: true,
                minLength: 1,
            }, {
                name: 'fields',
                displayKey: 'value',
                source: UniversalSearch.substringMatcher(data.fields, 'value', 6),
            });
        });

        // eslint-disable-next-line react/no-find-dom-node
        const fieldFormGroup = ReactDOM.findDOMNode(fieldInput.current);

        $(fieldFormGroup).on('typeahead:change typeahead:selected', (event) => {
            setEvent(event);
        });

        return () => {
            $(fieldInput.current.getInputDOMNode()).typeahead('destroy');

            // eslint-disable-next-line react/no-find-dom-node
            const fieldFormGroup = ReactDOM.findDOMNode(fieldInput.current);

            $(fieldFormGroup).off('typeahead:change typeahead:selected');
        }
    }, [theme]);


    return (
        <Input id="field-input" type="text" required name="field"
               ref={fieldInput}
               wrapperClassName="typeahead-wrapper"
               defaultValue={defaultValue} />
    );
}

export default TypeAheadFieldInput;