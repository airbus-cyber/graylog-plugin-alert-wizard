const fs = require('fs');
const buildConfig = require('./build.config');

const webSrcPrefix = buildConfig.web_src_path;

const jestConfig = {
  preset: 'jest-preset-graylog',
  setupFiles: [],
  roots: [
    'src'
  ],
  transform: {
    '^.+\\.[tj]sx?$': 'babel-jest'
  },
  moduleDirectories: [
    'src',
    'src/test',
    'node_modules',
    `${webSrcPrefix}/src`, `${webSrcPrefix}/test`
  ],
  moduleNameMapper: {
    '^react$': `${webSrcPrefix}/node_modules/react/index.js`,
    '^react-dom$': `${webSrcPrefix}/node_modules/react-dom/index.js`,
    '^styled-components$': `${webSrcPrefix}/node_modules/styled-components`,
    '^translations/(.+)$': 'web/translations/$1',
    '^wizard/(.+)$': 'web/wizard/$1',
  }
};
module.exports = jestConfig;
