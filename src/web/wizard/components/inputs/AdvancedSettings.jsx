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
import { FormattedMessage } from 'react-intl';

import { Col, Input, Row } from 'components/bootstrap';

// conditionParameters: Record<string, any>
// onUpdate: (field: string, value: any) => void
const AdvancedSettings = ({alert, onUpdate}) => {

    const _onGraceChanged = (e) => {
        onUpdate('grace', e.target.value);
    };

    const _onBacklogChanged = (e) => {
        onUpdate('backlog', e.target.value);
    };

    const _onAggregationTimeChanged = (e) => {
        onUpdate('aggregation_time', e.target.value);
    };

  const [showControls, setShowControls] = React.useState(false)

    return (
        <div style={{paddingBottom: '1em',}}>
            {/* <button class="btn btn-primary" type="button" data-toggle="collapse" data-target="#advancedSettings" aria-expanded="true" aria-controls="advancedSettings"> */}
            <button class="btn btn-primary" type="button" onClick={() => setShowControls(!showControls)}>
                <FormattedMessage id="wizard.advancedSettings" defaultMessage="Advanced settings" />
            </button>
                { showControls ? (
                    <div class="panel panel-default" id="advancedSettings">
                        <Row style={{ marginTop: '.25em', marginBottom: '.25em'}}>
                            <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                                <label className="pull-right" ><FormattedMessage id="wizard.advancedSettingsGrace" defaultMessage="Execute search every (minutes)" /></label>
                            </Col>
                            <Col md={10}>
                                <Input  id="grace" name="grace" type="number" min="0" onChange={_onGraceChanged}
                                        value={alert?.condition_parameters?.grace}
                                        style={{borderTopLeftRadius: '0px', borderBottomLeftRadius: '0px', height:'36px', width:'150px'}} />
                            </Col>
                        </Row>
                        <Row style={{ marginTop: '.25em', marginBottom: '.25em'}}>
                            <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                                <label className="pull-right" ><FormattedMessage id="wizard.advancedSettingsBacklog" defaultMessage="Message backlog" /></label>
                            </Col>
                            <Col md={10}>
                                <Input  id="backlog" name="backlog" type="number" min="0" onChange={_onBacklogChanged}
                                        value={alert?.backlog}
                                        style={{borderTopLeftRadius: '0px', borderBottomLeftRadius: '0px', height:'36px', width:'150px'}} />
                            </Col>
                        </Row>
                        <Row style={{ marginTop: '.25em', marginBottom: '.25em'}}>
                            <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                                <label className="pull-right" ><FormattedMessage id="wizard.advancedSettingsAggregationTime" defaultMessage="Notification aggregation time range (minutes)" /></label>
                            </Col>
                            <Col md={10}>
                                <Input  id="aggregation_time" name="aggregation_time" type="number" min="0" onChange={_onAggregationTimeChanged}
                                        value={alert?.aggregation_time}
                                        style={{borderTopLeftRadius: '0px', borderBottomLeftRadius: '0px', height:'36px', width:'150px'}} />
                            </Col>
                        </Row>
                    </div>
                ) : (
                    <div/>
                )}
        </div>
    );
};

export default AdvancedSettings;
