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
import { AlertRule } from "wizard/model/AlertRule";

type Options = {
    enabled: boolean,
}

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
