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
import { useState } from 'react';
import { useIntl } from 'react-intl';

import { Button } from 'components/bootstrap';

import generateIdentifier from 'wizard/logic/IdentifierSequence';
import IconAdd from 'wizard/components/icons/Add';
import FieldRule from './FieldRule';


// see https://react.dev/learn/rendering-lists and https://react.dev/learn/updating-arrays-in-state

const FieldRuleList = ({fieldRules, matchData, onSaveStream}) => {
    // TODO does it work to call useIntl outside of a component?
    const intl = useIntl();
    const messages = {
        add: intl.formatMessage({id: "wizard.add", defaultMessage: "Add"}),
    };
    const [state, setState] = useState(fieldRules);

    // TODO is this really the right way to do it: both changing the state and propagating up (which will come back down)?
    const _updateRules = (rules) => {
        setState(rules);
        onSaveStream(rules);
    }

    const _addFieldRule = () => {
        const newRule = {field: '', type: '', value: '', identifier: generateIdentifier()};
        const nextState = [...state, newRule];
        _updateRules(nextState);
    };

    const _onUpdateFieldRuleSubmit = (index, rule) => {
        const nextState = [...state];
        nextState[index] = {...rule, identifier: state[index].identifier};
        _updateRules(nextState);
    };

    const _onDeleteFieldRuleSubmit = (index) => {
        const nextState = [...state];
        nextState.splice(index, 1);
        _updateRules(nextState);
    };

    const listFieldRule = state.map((rule, index) => {
        // TODO should probably move the delete action out the FieldRule into here???
        // TODO try to remove the index prop from FieldRule!!!
        return (
            <div key={rule.identifier}>
                <FieldRule rule={rule} onUpdate={_onUpdateFieldRuleSubmit} onDelete={_onDeleteFieldRuleSubmit}
                           index={index} matchData={matchData} />
                <br/>
            </div>
        );
    });

    return (
        <>
            {listFieldRule}
            <Button onClick={_addFieldRule} bsStyle="info" title={messages.add}>
                <IconAdd/>
            </Button>
        </>
    );
}

export default FieldRuleList;