/*
 * Copyright (c) 2019 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cbioportal.persistence.util;

import java.io.*;
import java.net.URL;
import java.util.*;
import javax.cache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.Configuration;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.xml.XmlConfiguration;
import org.ehcache.core.config.DefaultConfiguration;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.ehcache.impl.config.persistence.DefaultPersistenceConfiguration;
import org.cbioportal.persistence.CacheEnabledConfig;

public class CustomEhcachingProvider extends EhcacheCachingProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CustomEhcachingProvider.class);

    @Value("${ehcache.xml_configuration:/ehcache.xml}")
    private String xmlConfigurationFile;

    @Value("${persistence.cache_type:no-cache}")
    private String cacheType;

    @Value("${ehcache.general_repository_cache.max_mega_bytes_heap:1024}")
    private Integer generalRepositoryCacheMaxMegaBytes;

    @Value("${ehcache.static_repository_cache_one.max_mega_bytes_heap:30}")
    private Integer staticRepositoryCacheOneMaxMegaBytes;

    @Value("${ehcache.persistence_path:/tmp/}")
    private String persistencePath;

    @Value("${ehcache.general_repository_cache.max_mega_bytes_local_disk:4096}")
    private Integer generalRepositoryCacheMaxMegaBytesLocalDisk;

    @Value("${ehcache.static_repository_cache_one.max_mega_bytes_local_disk:32}")
    private Integer staticRepositoryCacheOneMaxMegaBytesLocalDisk;
    
    @Autowired
    private CacheEnabledConfig cacheEnabledConfig;

    @Override
    public CacheManager getCacheManager() {

        CacheManager toReturn = null;
        try {
            if (cacheEnabledConfig.enableCache(cacheType)) {
                detectCacheConfigurationErrorsAndLog();
                LOG.info("Caching is enabled, using '" + xmlConfigurationFile + "' for configuration");
                XmlConfiguration xmlConfiguration = new XmlConfiguration(getClass().getResource(xmlConfigurationFile));

                // initilize configurations specific to each individual cache (by template)
                // to add new cache - create cache configuration with its own resource pool + template
                ResourcePoolsBuilder generalRepositoryCacheResourcePoolsBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder();
                ResourcePoolsBuilder staticRepositoryCacheOneResourcePoolsBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder();

                // Set up heap resources as long as not disk-only
                if (!cacheType.equalsIgnoreCase(CacheEnabledConfig.EHCACHE_DISK)) {
                    generalRepositoryCacheResourcePoolsBuilder = generalRepositoryCacheResourcePoolsBuilder.heap(generalRepositoryCacheMaxMegaBytes, MemoryUnit.MB);
                    staticRepositoryCacheOneResourcePoolsBuilder = staticRepositoryCacheOneResourcePoolsBuilder.heap(staticRepositoryCacheOneMaxMegaBytes, MemoryUnit.MB);
                }
                // Set up disk resources as long as not heap-only
                // will default to using /tmp -- let Ehcache throw exception if persistence path is invalid (locked or otherwise)
                if (!cacheType.equalsIgnoreCase(CacheEnabledConfig.EHCACHE_HEAP)) {
                    generalRepositoryCacheResourcePoolsBuilder = generalRepositoryCacheResourcePoolsBuilder.disk(generalRepositoryCacheMaxMegaBytesLocalDisk, MemoryUnit.MB);
                    staticRepositoryCacheOneResourcePoolsBuilder = staticRepositoryCacheOneResourcePoolsBuilder.disk(staticRepositoryCacheOneMaxMegaBytesLocalDisk, MemoryUnit.MB);
                }

                CacheConfiguration<Object, Object> generalRepositoryCacheConfiguration = xmlConfiguration.newCacheConfigurationBuilderFromTemplate("RepositoryCacheTemplate",
                        Object.class, Object.class, generalRepositoryCacheResourcePoolsBuilder)
                    .withSizeOfMaxObjectGraph(Long.MAX_VALUE)
                    .withSizeOfMaxObjectSize(Long.MAX_VALUE, MemoryUnit.B)
                    .build();
                CacheConfiguration<Object, Object> staticRepositoryCacheOneConfiguration = xmlConfiguration.newCacheConfigurationBuilderFromTemplate("RepositoryCacheTemplate",
                        Object.class, Object.class, staticRepositoryCacheOneResourcePoolsBuilder)
                    .withSizeOfMaxObjectGraph(Long.MAX_VALUE)
                    .withSizeOfMaxObjectSize(Long.MAX_VALUE, MemoryUnit.B)
                    .build();

                // places caches in a map which will be used to create cache manager
                Map<String, CacheConfiguration<?, ?>> caches = new HashMap<>();
                caches.put("GeneralRepositoryCache", generalRepositoryCacheConfiguration);
                caches.put("StaticRepositoryCacheOne", staticRepositoryCacheOneConfiguration);

                Configuration configuration = null;
                if (cacheType.equalsIgnoreCase(CacheEnabledConfig.EHCACHE_HEAP)) {
                    configuration = new DefaultConfiguration(caches, this.getDefaultClassLoader());
                } else { // add persistence configuration if cacheType is either disk-only or hybrid
                    File persistenceFile = new File(persistencePath);
                    configuration = new DefaultConfiguration(caches, this.getDefaultClassLoader(), new DefaultPersistenceConfiguration(persistenceFile));
                }

                toReturn = this.getCacheManager(this.getDefaultURI(), configuration);
            } else {
                LOG.info("Caching is disabled");
                // we can not really disable caching,
                // we can not make a cache of 0 objects,
                // and we can not make a heap of memory size 0, so make a tiny heap
                CacheConfiguration<Object, Object> generalRepositoryCacheConfiguration = CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class,
                    Object.class,
                    ResourcePoolsBuilder.newResourcePoolsBuilder().heap(1, MemoryUnit.B)).build();
                CacheConfiguration<Object, Object> staticRepositoryCacheOneConfiguration = CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class,
                    Object.class,
                    ResourcePoolsBuilder.newResourcePoolsBuilder().heap(1, MemoryUnit.B)).build();

                Map<String, CacheConfiguration<?, ?>> caches = new HashMap<>();
                caches.put("GeneralRepositoryCache", generalRepositoryCacheConfiguration);
                caches.put("StaticRepositoryCacheOne", staticRepositoryCacheOneConfiguration);

                Configuration configuration = new DefaultConfiguration(caches, this.getDefaultClassLoader());

                toReturn = this.getCacheManager(this.getDefaultURI(), configuration);
            }
        }
        catch (Exception e) {
            LOG.error(e.getClass().getName() + ": " + e.getMessage());
            StringWriter stackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTrace));
            LOG.error(stackTrace.toString());
        }
        return toReturn;
    }

    public void detectCacheConfigurationErrorsAndLog() {
        String MESSAGE_PREFIX = "Errors detected during configuration of Ehcache:";
        StringBuffer messages = new StringBuffer(MESSAGE_PREFIX);
        boolean usesHeap = false;
        boolean usesDisk = false;
        switch (this.cacheType.trim().toLowerCase()) {
            case "no-cache":
                break;
            case "ehcache-heap":
                usesHeap = true;
                break;
            case "ehcache-disk":
                usesDisk = true;
                break;
            case "ehcache-hybrid":
                usesHeap = true;
                usesDisk = true;
                break;
            case "redis":
                break; // we should not be in here in this case
            default:
                messages.append("\n  property persistence.cache_type has value (")
                        .append(cacheType)
                        .append(") which is not a recognized value");
        }
        if (usesDisk || usesHeap) {
            if (xmlConfigurationFile == null || xmlConfigurationFile.trim().length() == 0) {
                messages.append("\n  property ehcache.xml_configuration is required but is unset");
            } else {
                URL configFileURL = getClass().getResource(xmlConfigurationFile);
                if (configFileURL == null) {
                    messages.append("\n  property ehcache.xml_configuration has value (")
                            .append(xmlConfigurationFile)
                            .append(") but this resource is not available to the classloader");
                } else {
                    boolean readable = false;
                    InputStream configFileInputStream = null;
                    try {
                        configFileInputStream = configFileURL.openStream();
                        configFileInputStream.read();
                        configFileInputStream.close();
                        readable = true;
                    } catch (IOException e) {
                    } finally {
                    	try {
							configFileInputStream.close();
						} catch (IOException e) {
							LOG.error("UNABLE TO CLOSE configFileURLInputStream");
						}
                    }
                    
                    if (!readable) {
                        messages.append("\n  property ehcache.xml_configuration has value (")
                                .append(xmlConfigurationFile)
                                .append(") but an attempt to read from this resource failed");
                    }
                }
            }
        }
        if (usesDisk) {
            if (generalRepositoryCacheMaxMegaBytesLocalDisk == null) {
                messages.append("\n  property ehcache.general_repository_cache.max_mega_bytes_local_disk is required to be set, but has no value");
            } else {
                if (generalRepositoryCacheMaxMegaBytesLocalDisk <= 0) {
                    messages.append("\n  property ehcache.general_repository_cache.max_mega_bytes_local_disk must be greater than zero but is not");
                }
            }
            if (staticRepositoryCacheOneMaxMegaBytesLocalDisk == null) {
                messages.append("\n  property ehcache.static_repository_cache_one.max_mega_bytes_local_disk is required to be set, but has no value");
            } else {
                if (staticRepositoryCacheOneMaxMegaBytesLocalDisk <= 0) {
                    messages.append("\n  property ehcache.static_repository_cache_one.max_mega_bytes_local_disk must be greater than zero but is not");
                }
            }
            if (persistencePath == null || persistencePath.trim().length() == 0) {
                messages.append("\n  property ehcache.persistence_path is required when using a disk resource but is unset");
            } else {
                File persistenceDirectory = new File(persistencePath);
                boolean accessible = false;
                try {
                    if (persistenceDirectory.isDirectory() && persistenceDirectory.canWrite()) {
                        accessible = true;
                    }
                } catch (SecurityException e) {
                }
                if (!accessible) {
                    messages.append("\n  property ehcache.persistence_path has value (")
                            .append(persistencePath)
                            .append(") but this path does not exist or is not an accessible directory");
                }
            }
        }
        if (usesHeap) {
            if (generalRepositoryCacheMaxMegaBytes == null) {
                messages.append("\n  property ehcache.general_repository_cache.max_mega_bytes_heap is required to be set, but has no value");
            } else {
                if (generalRepositoryCacheMaxMegaBytes <= 0) {
                    messages.append("\n  property ehcache.general_repository_cache.max_mega_bytes_heap must be greater than zero but is not");
                }
            }
            if (staticRepositoryCacheOneMaxMegaBytes == null) {
                messages.append("\n  property ehcache.static_repository_cache_one.max_mega_bytes_heap is required to be set, but has no value");
            } else {
                if (staticRepositoryCacheOneMaxMegaBytes <= 0) {
                    messages.append("\n  property ehcache.static_repository_cache_one.max_mega_bytes_heap must be greater than zero but is not");
                }
            }
        }
        if (usesHeap && usesDisk) {
            if (generalRepositoryCacheMaxMegaBytesLocalDisk != null
                    && generalRepositoryCacheMaxMegaBytes != null
                    && generalRepositoryCacheMaxMegaBytesLocalDisk <= generalRepositoryCacheMaxMegaBytes) {
                messages.append("\n  property ehcache.general_repository_cache.max_mega_bytes_heap must be set to a value less than the value of ");
                messages.append("property ehcache.general_repository_cache.max_mega_bytes_local_disk, however ");
                messages.append(generalRepositoryCacheMaxMegaBytes);
                messages.append(" is not less than ");
                messages.append(generalRepositoryCacheMaxMegaBytesLocalDisk);
            }
            if (staticRepositoryCacheOneMaxMegaBytesLocalDisk != null
                    && staticRepositoryCacheOneMaxMegaBytes != null
                    && staticRepositoryCacheOneMaxMegaBytesLocalDisk <= staticRepositoryCacheOneMaxMegaBytes) {
                messages.append("\n  property ehcache.static_repository_cache_one.max_mega_bytes_heap must be set to a value less than the value of ");
                messages.append("property ehcache.static_repository_cache_one.max_mega_bytes_local_disk, however ");
                messages.append(staticRepositoryCacheOneMaxMegaBytes);
                messages.append(" is not less than ");
                messages.append(staticRepositoryCacheOneMaxMegaBytesLocalDisk);
            }
        }
        if (messages.length() > MESSAGE_PREFIX.length()) {
            LOG.error(messages.toString());
            LOG.error("because of Ehcache configuration errors, it is likely that an exception will be thrown during startup. Recent observed exceptions contain the string"
                    + " \"Provider org.redisson.jcache.JCachingProvider not a subtype\" even though the problem is in the Ehcache configuration settings.");
        }
    }
}
