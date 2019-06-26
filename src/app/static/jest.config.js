module.exports = {
    "testResultsProcessor": "jest-teamcity-reporter",
    "setupFilesAfterEnv": [
        "<rootDir>/src/tests/setup.js"
    ],
    "globals": {
        "appUrl": "http://app"
    },
    "moduleFileExtensions": [
        "js",
        "json",
        "vue"
    ],
    "transform": {
        ".*\\.(vue)$": "vue-jest",
        "^.+\\.js$": "<rootDir>/node_modules/babel-jest"
    },
    "coverageDirectory": "./coverage/",
    "collectCoverage": true
};
