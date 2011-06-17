# Deploy Main Portal on Buri
ant clean
cp config/all_gene_sets.txt config/gene_sets.txt
cp build.properties.ALL build.properties
ant war
echo "Deploying Main Portal to buri"
scp build/war/cgx.war cerami@buri.cbio.mskcc.org:/srv/www/sander-tomcat/tomcat6/webapps/
