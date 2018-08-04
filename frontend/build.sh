#!/usr/bin/env bash
echo Backing up config...
cp src/config/Config.js originalConfig.js
echo Munging config for embeddification...
case "$(uname -s)" in
    Darwin)
        sed -i '' -e 's/http:\/\/localhost:9000//' src/config/Config.js
        ;;
    *)
        sed -i -e 's/http:\/\/localhost:9000//' src/config/Config.js
        ;;
esac
echo Installing dependencies...
npm install
echo Compiling JavaScript, an interpreted language...
npm run-script build
echo Embedding static www into Kotlin solution...
mkdir -p ../src/main/resources/www
rm -fr ../src/main/resources/www
mv build ../src/main/resources/www
echo Creating test.js for testing...
touch ../src/main/resources/www/static/test.js
echo $(date) > ../src/main/resources/www/static/test.js
echo Restoring original config.js...
rm src/config/Config.js
mv originalConfig.js src/config/Config.js
