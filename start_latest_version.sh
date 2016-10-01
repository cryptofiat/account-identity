pkill -9 java
git pull
./gradlew build
nohup java -jar build/libs/accountmapper-0.0.1-SNAPSHOT.jar >> /opt/logs/accountmapper.log &
tail -f /opt/logs/accountmapper.log