
build:
	mvn clean package -Dmaven.test.skip=true -f ./mcc/pom.xml $(ARGS);
run:
	@java -jar `find ./mcc/target/ -name "*.jar"`
test:
	mvn test -f ./mcc/pom.xml ;
