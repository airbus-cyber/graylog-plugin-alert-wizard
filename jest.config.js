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
  moduleDirectories: [].concat([
    'src',
    'src/test',
    'node_modules'
  ], [`${webSrcPrefix}/src`, `${webSrcPrefix}/test`]),
  moduleNameMapper: {
    '^react$': `${webSrcPrefix}/node_modules/react/index.js`,
    '^react-dom$': `${webSrcPrefix}/node_modules/react-dom/index.js`,
    '^styled-components$': `${webSrcPrefix}/node_modules/styled-components`,
  },
};
module.exports = jestConfig;
