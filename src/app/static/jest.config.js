module.exports = {
    "testResultsProcessor": "jest-teamcity-reporter",
    "setupFilesAfterEnv": [
        "<rootDir>/src/tests/setup.js",
        "jest-canvas-mock"
    ],
    "testEnvironment": "jsdom",
    "globals": {
        "appUrl": "http://app"
    },
    "preset": 'ts-jest',
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
    "transformIgnorePatterns": ["node_modules/(?!bootstrap-vue)"],
    "coverageDirectory": "./coverage/",
    "collectCoverage": true,
    "coveragePathIgnorePatterns": [
        "/node_modules/"
    ]
};
