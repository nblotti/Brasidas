#/bin/bash

java -jar jwtbuilder-0.0.1-SNAPSHOT.jar | grep Bear | tr -d '\n' | xclip  -selection c

echo "JWT token copied in clipboard"
