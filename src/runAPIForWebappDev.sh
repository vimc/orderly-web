#!/usr/bin/env bash

if [ ! -f "app/demo/orderly.sqlite" ]
then
	./gradlew :app:generateTestData
fi

./gradlew :run
