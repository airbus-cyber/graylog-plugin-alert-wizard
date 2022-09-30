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
import URI from 'urijs';

import AppConfig from 'util/AppConfig';

// This is the identifier defined in the PluginMetaData (com.airbus_cyber_security.graylog.LoggingAlertMetaData)
const pluginUniqueIdentifier = 'com.airbus-cyber-security.graylog.AlertWizardPlugin';

// The webpack-dev-server serves the assets from "/"
const assetPrefix = AppConfig.gl2DevMode() ? '/' : '/assets/plugin/' + pluginUniqueIdentifier + '/';

// If app prefix was not set, we need to tell webpack to load chunks from root instead of the relative URL path
__webpack_public_path__ = URI.joinPaths(AppConfig.gl2AppPathPrefix(), assetPrefix).path() || assetPrefix;
