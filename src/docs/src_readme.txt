src_readme.txt

Antelope @@build.num@@

Antelope home page: http://antelope.sourceforge.net


Prerequisites: 
1. A working version of Ant 1.5 or later.
2. Java 1.4.0 or later.


To build Antelope:

(Note: steps 4 and 5 may be skipped if your environment matches a standard
jEdit build environment)

1. Download AntelopeSrc_@@build.num@@.zip.
2. jar xf AntelopeSrc_@@build.num@@.zip
3. cd AntelopeSrc_@@build.num@@
4. Copy build.props.example to build.props
5. Adjust build.props for your machine
6. To build all distribution files: ant all
   To build just the application distribution file: ant app
   To build just the plugin distribution file: ant
   To build just the tasks distribution file: ant tasks
   To build just the source distribution file: ant source
The distribution files will be placed in the "dist" directory.


To run Antelope once it's been built:
1. ant run


To create the documentation, xalan and docbook must be installed:
(skip steps 1 and 2 if you are using Ant 1.6. Ant 1.6 comes with the Trax
xslt processor built it.)
1. Download xalan from xml.apache.org.
2. Copy xalan.jar to your ${ant.home}/lib directory.
3. Download docbook from www.docbook.org. Follow the installation
instructions. Most Linux distributions already have docbook installed. 
4. Edit src/docs/users-guide.xsl to reference the location of your copy of 
chunkfast.xsl.
5. ant transform-docs


To run the unit tests, junit must be installed:
1. Download junit from www.junit.org.
2. Copy junit.jar to the "lib" directory.
3. ant test



