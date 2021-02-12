module.exports = {
    "testResultsProcessor": "jest-teamcity-reporter",
    "setupFilesAfterEnv": [
        "<rootDir>/src/tests/setup.js",
        "jest-canvas-mock"
    ],
    "globals": {
        "appUrl": "http://app",
        "ts-jest": {
            tsConfig: 'tsconfig.json',
            "diagnosis": {
                "warmOnly": false
            }
        }
    },
    "transformIgnorePatterns": [
        "node_modules/(?!(vue-typeahead-bootstrap)/)",
    ],
    "moduleFileExtensions": [
        "js",
        "ts",
        "json",
        "vue"
    ],
    "moduleNameMapper": {"tokenize2": "<rootDir>/node_modules/tokenize2/tokenize2.js"},
    "transform": {
        ".*\\.(vue)$": "vue-jest",
        "^.+\\.ts?$": "ts-jest",
        "^.+\\.js$": "<rootDir>/node_modules/babel-jest"
    },
    "coverageDirectory": "./coverage/",
    "collectCoverage": true,
    "coveragePathIgnorePatterns": [
        "/node_modules/"
    ]
};
