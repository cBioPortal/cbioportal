#### Notes for non-Linux systems

##### Docker for Mac/Windows (newer versions)
Make sure to assign enough memory to Docker when using Docker for Windows (Windows 10 Pro 64-bit) or Docker for Mac (Mac OS X Yosemite 10.10.3 or above). In Mac OS X this can be set when clicked on the Docker icon -> Preferences... -> Adjust the Memory slider. By default it's set to 2 GB, which is too low and causes problems when loading multiple studies.

##### Docker-machine (older versions)
Because the Docker Engine daemon uses Linux-specific kernel features, you can’t run Docker Engine natively in Windows or Mac OS X. In versions of these systems that do not support the newer lightweight virtualisation technologies mentioned above, you must instead use the Docker Machine command, `docker-machine`. This creates and attaches to a small Linux VM on your machine, which hosts Docker Engine.

The Docker Quickstart Terminal in the Docker Toolbox will automatically create a default VM for you (`docker-machine create`), boot it up (`docker-machine start`) and set environment variables in the running shell to transparently forward the docker commands to the VM (`eval $(docker-machine env)`). Do note however, that forwarded ports in the docker commands will pertain to the VM and not your Windows/OS X system. The local cBioPortal and MySQL servers will not be available on `localhost` or `127.0.0.1`, but on the address printed by the command `docker-machine ip`, unless you configure VirtualBox to further forward the port to the host system.
