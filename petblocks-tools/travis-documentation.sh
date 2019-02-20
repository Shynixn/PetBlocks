#!/bin/bash

echo "Generating documentation..."
apt-get install python3-pip
pip3 install https://github.com/Shynixn/sphinx_rtd_theme/releases/download/C1.0/sphinx_rtd_theme.zip
pip3 install -U sphinx
rm -rf docs/build
rm -rf docs/apidocs
python3 -msphinx -M html docs/source docs/build
gradlew generateJavaDocPages
git config --global user.email "travis@travis-ci.org" && git config --global user.name "Travis CI"
git add docs
git commit --message "Travis build $TRAVIS_BUILD_NUMBER [skip travis-ci]"
git push --quiet https://Shynixn:$GH_TOKEN@github.com/Shynixn/PetBlocks.git HEAD:master
echo "Finished generating documentation"