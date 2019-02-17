#!/bin/sh

GENERATE_DOCUMENTATION = $1
TRAVIS_BUILD_NUMBER = $2

if [$GENERATE_DOCUMENTATION == 1]; then
    echo "Generating documentation..."
    sudo apt-get install python3-pip
    sudo pip3 install https://github.com/Shynixn/sphinx_rtd_theme/releases/download/C1.0/sphinx_rtd_theme.zip
    sudo pip3 install -U sphinx
    rm -rf docs/build
    rm -rf docs/apidocs
    python3 -msphinx -M html docs/source docs/build
    gradlew generateJavaDocPages
    git config --global user.email "travis@travis-ci.org" && git config --global user.name "Travis CI"
    git add docs
    git commit --message "Travis build $TRAVIS_BUILD_NUMBER [skip travis-ci]"
    git push --quiet https://Shynixn:$GH_TOKEN@github.com/Shynixn/DiscordWebhook-Ktl.git HEAD:master
    echo "Finished generating documentation"
fi
