mv *.class crew.log IamServer Client tmp/
javac -source 1.6 -target 1.6 *.java
rmic Hello 
