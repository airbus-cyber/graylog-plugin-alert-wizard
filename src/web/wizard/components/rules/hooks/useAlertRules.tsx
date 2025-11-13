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
import { useQuery } from '@tanstack/react-query';

import UserNotification from 'util/UserNotification';
import type { SearchParams } from 'stores/PaginationTypes';
import AlertRuleStore from 'wizard/stores/AlertRuleStore';
import { AlertRule } from 'wizard/model/AlertRule';
import * as React from "react";

type Options = {
    enabled: boolean,
}

export const commonQueryFields = ['id', 'title', 'description'];

export const fieldMap = {
    priority: 'Priority of the alert rule.',
    user: 'User who created the alert rule.'
};

export const queryExample = (
    <>
        <p>
            Find all alert rules with title containing 'log':<br />
            <code>log</code><br />
            <code>title:log</code><br />
        </p>
        <p>
            Find all alert rules with LOW(1) priority:<br />
            <code>priority:1</code><br />
            LOW(1)/NORMAL(2)/HIGH(3)<br />
        </p>
        <p>
            Find all alert rules with a description containing 'security':<br />
            <code>description:security</code><br />
        </p>
        <p>
            Find a alert rule with the id '5f4dfb9c69be46153b9a9a7b':<br />
            <code>id:5f4dfb9c69be46153b9a9a7b</code><br />
        </p>
    </>
);

export const fetchAlertRules = (searchParams: SearchParams) => AlertRuleStore.searchPaginated(
    searchParams.page,
    searchParams.pageSize,
    searchParams.query,
    { sort: searchParams?.sort.attributeId, order: searchParams?.sort.direction },
).then(({ elements, pagination, attributes }) => ({
    list: elements,
    pagination,
    attributes,
}));

export const KEY_PREFIX = ['alertRule', 'overview'];
export const keyFn = (searchParams?: SearchParams | undefined) => ([KEY_PREFIX, ...(searchParams ? [searchParams] : [])]);

const useAlertRules = (searchParams: SearchParams, { enabled }: Options = { enabled: true }): {
    data: {
        list: Array<AlertRule>,
        pagination: { total: number }
        attributes: Array<{ id: string, title: string, sortable: boolean }>
    } | undefined,
    refetch: () => void,
    isInitialLoading: boolean,
} => {
    const { data, refetch, isInitialLoading } = useQuery(
        keyFn(searchParams),
        () => fetchAlertRules(searchParams),
        {
            onError: (errorThrown) => {
                UserNotification.error(`Loading event notifications failed with status: ${errorThrown}`,
                    'Could not load event notifications');
            },
            keepPreviousData: true,
            enabled,
        },
    );

    return ({
        data,
        refetch,
        isInitialLoading,
    });
};

export default useAlertRules;
