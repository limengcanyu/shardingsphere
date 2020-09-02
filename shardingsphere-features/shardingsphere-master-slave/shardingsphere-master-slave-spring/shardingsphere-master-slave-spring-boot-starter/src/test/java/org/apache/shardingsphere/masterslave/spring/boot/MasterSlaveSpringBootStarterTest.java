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

package org.apache.shardingsphere.masterslave.spring.boot;

import org.apache.shardingsphere.masterslave.algorithm.RandomMasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.masterslave.algorithm.config.AlgorithmProvidedMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.api.config.rule.MasterSlaveDataSourceRuleConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MasterSlaveSpringBootStarterTest.class)
@SpringBootApplication
@ActiveProfiles("masterslave")
public class MasterSlaveSpringBootStarterTest {
    
    @Resource
    private RandomMasterSlaveLoadBalanceAlgorithm random;
    
    @Resource
    private AlgorithmProvidedMasterSlaveRuleConfiguration masterSlaveRuleConfiguration;
    
    @Test
    public void assertLoadBalanceAlgorithm() {
        assertTrue(random.getProps().isEmpty());
    }
    
    @Test
    public void assertMasterSlaveRuleConfiguration() {
        assertThat(masterSlaveRuleConfiguration.getDataSources().size(), is(1));
        MasterSlaveDataSourceRuleConfiguration masterSlaveDataSourceRuleConfiguration = masterSlaveRuleConfiguration.getDataSources().stream().findFirst().get();
        assertThat(masterSlaveDataSourceRuleConfiguration.getName(), is("ds_ms"));
        assertThat(masterSlaveDataSourceRuleConfiguration.getMasterDataSourceName(), is("ds_master"));
        assertThat(masterSlaveDataSourceRuleConfiguration.getLoadBalancerName(), is("random"));
        assertThat(masterSlaveDataSourceRuleConfiguration.getSlaveDataSourceNames().size(), is(2));
        assertTrue(masterSlaveRuleConfiguration.getDataSources().contains(masterSlaveDataSourceRuleConfiguration));
        assertThat(masterSlaveRuleConfiguration.getLoadBalanceAlgorithms().size(), is(1));
        assertTrue(masterSlaveRuleConfiguration.getLoadBalanceAlgorithms().containsKey("random"));
    }
}
