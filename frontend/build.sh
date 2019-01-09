#!/usr/bin/env bash
echo Backing up config...
cp src/config/Config.js originalConfig.js
echo Munging config for embeddification...
case "$(uname -s)" in
    Darwin)
        sed -i '' -e 's/http:\/\/localhost:38080//' src/config/Config.js
        ;;
    *)
        sed -i -e 's/http:\/\/localhost:38080//' src/config/Config.js
        ;;
esac

echo Installing dependencies...
./yarn.sh install

echo Compiling JavaScript, an interpreted language...
./yarn.sh build

echo Embedding static www into Kotlin solution...
mkdir -p ../backend/src/main/resources/www
rm -fr ../backend/src/main/resources/www
mv dist ../backend/src/main/resources/www

echo Creating test.js for testing...
touch ../backend/src/main/resources/www/test.js
echo $(date) > ../backend/src/main/resources/www/test.js
echo Restoring original config.js...
rm src/config/Config.js
mv originalConfig.js src/config/Config.js
