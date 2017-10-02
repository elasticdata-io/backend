#!/usr/bin/env bash
/usr/local/Cellar/rabbitmq/3.6.9/sbin/rabbitmq-server start &
java -jar /Users/s/Downloads/selenium-server-standalone-3.4.0.jar -role hub -browserTimeout 300 -timeout 300 &
java -jar /Users/s/Downloads/selenium-server-standalone-3.4.0.jar -role node -browser browserName=chrome &
cd /Users/s/sites/scraper-service-ui/app && npm run dev

java -jar /Users/s/Downloads/selenium-server-standalone-3.5.3.jar -role node -browser browserName=chrome -port 4449 -hub http://selenium.bars-parser.com:4444/grid/register -host 37.57.124.190