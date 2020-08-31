
build:
	mvn clean package -Dmaven.test.skip=true -f ./mcc/pom.xml ;
run: build
	java -jar `find ./mcc/target/ -name "*.jar"` $(ARGS)
test:
	mvn test -f ./mcc/pom.xml ;
