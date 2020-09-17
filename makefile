
build:
	mvn clean package -Dmaven.test.skip=true -f ./mcc/pom.xml ;
run: build
	java -ea -jar `find ./mcc/target/ -name "*.jar"` $(IN) $(OUT)
test:
	mvn test -f ./mcc/pom.xml ;
watch:
	while inotifywait -e close_write test.txt; do make run IN="$(IN)" OUT="$(OUT)" ; done
