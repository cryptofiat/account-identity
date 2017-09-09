echo "======== STEP 1/5 KILLING JAVA ==================================="
pkill -9 java
echo "======== STEP 2/5 PULLING LATEST MASTER =========================="
git pull
echo "======== STEP 3/5 BUILDING APPLICATION ==========================="
./gradlew build
echo "======== STEP 4/5 STARTING APPLICATION ==========================="
nohup java -javaagent:/home/ubuntu/newrelic/newrelic.jar -jar build/libs/accountmapper-0.0.1-SNAPSHOT.jar >> /opt/logs/accountmapper.log &
echo "======== STEP 5/5 READY! TAILING LOG (You can Ctrl+C now) ========"
tail -f /opt/logs/accountmapper.log
