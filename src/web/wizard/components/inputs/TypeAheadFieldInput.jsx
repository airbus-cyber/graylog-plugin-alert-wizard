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
import { TypeAheadFieldInput } from 'components/common';

// TODO this is a hack, because it seems the callback provided to the TypeAheadFieldInput is called in a context
//      where it does not have access the the current value of other states
//      so, instead of directly calling the onChange callback, we set the event value
//      and then the effect will call the onChange callback in a context where it works...
//   => try to pinpoint the bug and report to Graylog/or try to use some other widget (react-bootstrap-typeahead)/or try to implement own
const TypeAheadFieldInputWrapper = ({id, type, required, name, defaultValue, onChange, autoFocus}) => {
    const [event, setEvent] = useState();

    const _onChangeWrapper = () => {
        if (event === undefined) {
            return;
        }
        onChange(event);
    }

    useEffect(_onChangeWrapper, [event]);

    return (
        <TypeAheadFieldInput id={id} type={type} required={required} name={name} defaultValue={defaultValue} onChange={setEvent} autoFocus={autoFocus} />
    )
}

export default TypeAheadFieldInputWrapper;