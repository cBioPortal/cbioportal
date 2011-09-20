# RSync all web assets to local web server
rsync -rvc ../web/* /Library/WebServer/Documents/cbio_portal

# RSync all sandbox pages to local web server
rsync -rvc * /Library/WebServer/Documents/cbio_portal
