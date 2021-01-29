module.exports = {
    "testResultsProcessor": "jest-teamcity-reporter",
    "setupFilesAfterEnv": [
        "<rootDir>/src/tests/setup.js",
        "jest-canvas-mock"
    ],
    "globals": {
        "appUrl": "http://app"
    },
    "transformIgnorePatterns": [
        "node_modules/(?!(vue-typeahead-bootstrap)/)",
    ],
    "moduleFileExtensions": [
        "js",
        "json",
        "vue"
    ],
    "moduleNameMapper": {"tokenize2": "<rootDir>/node_modules/tokenize2/tokenize2.js"},
    "transform": {
        ".*\\.(vue)$": "vue-jest",
        "^.+\\.js$": "<rootDir>/node_modules/babel-jest"
    },
    "coverageDirectory": "./coverage/",
    "collectCoverage": true
};
