mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file \
                        -Dfile=lib/viewer.jar \
                        -DgroupId=org.systemsbiology \
                        -DartifactId=msInspect \
                        -Dversion=1.0\
                        -Dpackaging=jar \
                        -DlocalRepositoryPath=.local-mvn-repo
