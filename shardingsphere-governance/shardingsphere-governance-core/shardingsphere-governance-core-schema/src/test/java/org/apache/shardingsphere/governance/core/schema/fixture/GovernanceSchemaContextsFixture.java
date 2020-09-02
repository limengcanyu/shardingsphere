/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.governance.core.schema.fixture;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.governance.core.facade.GovernanceFacade;
import org.apache.shardingsphere.governance.core.schema.GovernanceSchemaContexts;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.context.SchemaContext;
import org.apache.shardingsphere.infra.context.SchemaContexts;
import org.apache.shardingsphere.infra.context.schema.DataSourceParameter;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public final class GovernanceSchemaContextsFixture extends GovernanceSchemaContexts {
    
    public GovernanceSchemaContextsFixture(final SchemaContexts schemaContexts, final GovernanceFacade governanceFacade) {
        super(schemaContexts, governanceFacade);
    }
    
    @Override
    protected Map<String, DataSource> getAddedDataSources(final SchemaContext oldSchemaContext, final Map<String, DataSourceConfiguration> newDataSourceConfigs) {
        return Collections.singletonMap("ds_2", buildDataSource(getDataSourceParameter()));
    }
    
    @Override
    protected Map<String, DataSource> getModifiedDataSources(final SchemaContext oldSchemaContext, final Map<String, DataSourceConfiguration> newDataSourceConfigs) {
        return Collections.singletonMap("ds_1", buildDataSource(getDataSourceParameter()));
    }
    
    private DataSourceParameter getDataSourceParameter() {
        DataSourceParameter result = new DataSourceParameter();
        result.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        result.setUsername("sa");
        result.setPassword("");
        return result;
    }
    
    private Map<String, DataSource> createDataSources(final Map<String, DataSourceParameter> dataSourceParameters) {
        Map<String, DataSource> result = new LinkedHashMap<>(dataSourceParameters.size(), 1);
        for (Entry<String, DataSourceParameter> entry: dataSourceParameters.entrySet()) {
            result.put(entry.getKey(), buildDataSource(entry.getValue()));
        }
        return result;
    }
    
    private Map<String, Map<String, DataSourceParameter>> createDataSourceParametersMap(final Map<String, Map<String, DataSourceConfiguration>> dataSourcesMap) {
        Map<String, Map<String, DataSourceParameter>> result = new LinkedHashMap<>(dataSourcesMap.size(), 1);
        for (Entry<String, Map<String, DataSourceConfiguration>> entry : dataSourcesMap.entrySet()) {
            result.put(entry.getKey(), getDataSourceParameterMap(entry.getValue()));
        }
        return result;
    }
    
    private DataSource buildDataSource(final DataSourceParameter dataSourceParameter) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dataSourceParameter.getUrl());
        config.setUsername(dataSourceParameter.getUsername());
        config.setPassword(dataSourceParameter.getPassword());
        config.setConnectionTimeout(dataSourceParameter.getConnectionTimeoutMilliseconds());
        config.setIdleTimeout(dataSourceParameter.getIdleTimeoutMilliseconds());
        config.setMaxLifetime(dataSourceParameter.getMaxLifetimeMilliseconds());
        config.setMaximumPoolSize(dataSourceParameter.getMaxPoolSize());
        config.setMinimumIdle(dataSourceParameter.getMinPoolSize());
        config.setReadOnly(dataSourceParameter.isReadOnly());
        return new HikariDataSource(config);
    }
    
    private Map<String, DataSourceParameter> getDataSourceParameterMap(final Map<String, DataSourceConfiguration> dataSourceConfigurationMap) {
        return dataSourceConfigurationMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> createDataSourceParameter(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private DataSourceParameter createDataSourceParameter(final DataSourceConfiguration dataSourceConfig) {
        bindSynonym(dataSourceConfig);
        DataSourceParameter result = new DataSourceParameter();
        for (Field each : result.getClass().getDeclaredFields()) {
            try {
                each.setAccessible(true);
                if (dataSourceConfig.getProps().containsKey(each.getName())) {
                    each.set(result, dataSourceConfig.getProps().get(each.getName()));
                }
            } catch (final ReflectiveOperationException ignored) {
            }
        }
        return result;
    }
    
    private static void bindSynonym(final DataSourceConfiguration dataSourceConfiguration) {
        dataSourceConfiguration.addPropertySynonym("url", "jdbcUrl");
        dataSourceConfiguration.addPropertySynonym("user", "username");
    }
}
