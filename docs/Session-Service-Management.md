# Session Service Management
## Clear Study View User settings
### **Using kubernetes:**
Go inside session service pod (replace `CBIOPORTAL_SESSION_SERVICE_MONGO_POD_ID` with your session service pod id):
```shell
kubectl exec  -it CBIOPORTAL_SESSION_SERVICE_MONGO_POD_ID  -- /bin/sh -c 'mongo admin -u root -p ${MONGODB_ROOT_PASSWORD}'
```
Choose database:
```shell
use session_service;
```
(Optional) Make sure there are matching records to remove (replace `PORTAL_SOURCE` with correct text, one can find it in `session.service.url`, e.g. `public_portal`) 
```shell
db.settings.find({ "source": "PORTAL_SOURCE", "data.page": "study_view" }).count();
```
Remove user settings in study view:
```shell
db.settings.remove({ "source": "PORTAL_SOURCE", "data.page": "study_view" });
```

### **Using docker-compose:**
Go inside session service container (replace `CBIOPORTAL_SESSION_SERVICE_MONGO_CONTAINER_ID` with your session service database container id):
```shell
docker exec  -it CBIOPORTAL_SESSION_SERVICE_MONGO_CONTAINER_ID  bash -c 'mongo'
```
Then follow the same procedure described in the `Using kubernetes` section.