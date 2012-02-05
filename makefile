.SUFFIXES: .java, .class

all:
	javac *.java

clean:
	rm *.class