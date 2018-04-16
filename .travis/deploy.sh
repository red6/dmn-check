#!/usr/bin/env bash

if [ ! -z "$GPG_SECRET_KEYS" ]; then echo $GPG_SECRET_KEYS | base64 --decode | gpg --import; fi
if [ ! -z "$GPG_OWNERTRUST" ]; then echo $GPG_OWNERTRUST | base64 --decode | gpg --import-ownertrust; fi

if [ ! -z "$TRAVIS_TAG" ]
then
    echo "on a tag -> set pom.xml <version> to $TRAVIS_TAG"
    mvn --settings .travis/settings.xml org.codehaus.mojo:versions-maven-plugin:2.5:set -DnewVersion=$TRAVIS_TAG 1>/dev/null 2>/dev/null
else
    echo "not on a tag -> keep snapshot version in pom.xml"
fi

mvn clean deploy --settings .travis/settings.xml -DskipTests=true -B -U
