
build:
	mvn clean package -Dmaven.test.skip=true -f ./mcc/pom.xml ;
run: build
	java -jar `find ./mcc/target/ -name "*.jar"` $(ARGS)
test:
	mvn test -f ./mcc/pom.xml ;
watch:
	while inotifywait -e close_write test.txt; do make run ARGS="$(ARGS)" ; done