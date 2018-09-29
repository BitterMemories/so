/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.aai.tasks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.client.exception.BBObjectNotFoundException;


public class AAIDeleteTasksTest extends BaseTaskTest {
	
	@InjectMocks
	private AAIDeleteTasks aaiDeleteTasks = new AAIDeleteTasks();
	
	private L3Network network;
	private ServiceInstance serviceInstance;
	private GenericVnf genericVnf;
	private VfModule vfModule;
	private VolumeGroup volumeGroup;
	private CloudRegion cloudRegion;
	private Configuration configuration;
	
	@Before
	public void before() throws BBObjectNotFoundException {
		serviceInstance = setServiceInstance();
		genericVnf = setGenericVnf();
		vfModule = setVfModule();
		network = setL3Network();
		volumeGroup = setVolumeGroup();
		cloudRegion = setCloudRegion();
		configuration = setConfiguration();
		
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.GENERIC_VNF_ID), any())).thenReturn(genericVnf);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID), any())).thenReturn(vfModule);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.NETWORK_ID), any())).thenReturn(network);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.VOLUME_GROUP_ID), any())).thenReturn(volumeGroup);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID), any())).thenReturn(serviceInstance);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.CONFIGURATION_ID), any())).thenReturn(configuration);
		

		doThrow(new BpmnError("BPMN Error")).when(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(Exception.class));
	}
	
	@Test
	public void deleteVfModuleTest() throws Exception {
		
		doNothing().when(aaiVfModuleResources).deleteVfModule(vfModule, genericVnf);
		
		aaiDeleteTasks.deleteVfModule(execution);
		verify(aaiVfModuleResources, times(1)).deleteVfModule(vfModule, genericVnf);
	}
	
	@Test
	public void deleteVfModuleExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		doThrow(RuntimeException.class).when(aaiVfModuleResources).deleteVfModule(vfModule, genericVnf);
		aaiDeleteTasks.deleteVfModule(execution);
	}
	
	@Test
	public void deleteServiceInstanceTest() throws Exception {
		doNothing().when(aaiServiceInstanceResources).deleteServiceInstance(serviceInstance);
		
		aaiDeleteTasks.deleteServiceInstance(execution);
		
		verify(aaiServiceInstanceResources, times(1)).deleteServiceInstance(serviceInstance);
	}
	
	@Test 
	public void deleteServiceInstanceExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		doThrow(RuntimeException.class).when(aaiServiceInstanceResources).deleteServiceInstance(serviceInstance);
		
		aaiDeleteTasks.deleteServiceInstance(execution);
	}	
	
	@Test
	public void deleteVnfTest() throws Exception {
		doNothing().when(aaiVnfResources).deleteVnf(genericVnf);
		aaiDeleteTasks.deleteVnf(execution);
		verify(aaiVnfResources, times(1)).deleteVnf(genericVnf);
	}
	
	@Test
	public void deleteVnfTestException() throws Exception {
		expectedException.expect(BpmnError.class);
		doThrow(RuntimeException.class).when(aaiVnfResources).deleteVnf(genericVnf);
		
		aaiDeleteTasks.deleteVnf(execution);
		verify(aaiVnfResources, times(1)).deleteVnf(genericVnf);
	}
	
	@Test 
	public void deleteNetworkTest() throws Exception {
		doNothing().when(aaiNetworkResources).deleteNetwork(network);
		aaiDeleteTasks.deleteNetwork(execution);
		verify(aaiNetworkResources, times(1)).deleteNetwork(network);
	}
	
	@Test 
	public void deleteCollectionTest() throws Exception {
		doNothing().when(aaiNetworkResources).deleteCollection(serviceInstance.getCollection());
		aaiDeleteTasks.deleteCollection(execution);
		verify(aaiNetworkResources, times(1)).deleteCollection(serviceInstance.getCollection());
	}
	
	@Test 
	public void deleteInstanceGroupTest() throws Exception {
		doNothing().when(aaiNetworkResources).deleteNetworkInstanceGroup(serviceInstance.getCollection().getInstanceGroup());
		aaiDeleteTasks.deleteInstanceGroup(execution);
		verify(aaiNetworkResources, times(1)).deleteNetworkInstanceGroup(serviceInstance.getCollection().getInstanceGroup());
	}

	@Test
	public void deleteVolumeGroupTest() {
		doNothing().when(aaiVolumeGroupResources).deleteVolumeGroup(volumeGroup, cloudRegion);
		
		aaiDeleteTasks.deleteVolumeGroup(execution);
		
		verify(aaiVolumeGroupResources, times(1)).deleteVolumeGroup(volumeGroup, cloudRegion);
	}
	
	@Test
	public void deleteVolumeGroupExceptionTest() {
		expectedException.expect(BpmnError.class);
		
		doThrow(RuntimeException.class).when(aaiVolumeGroupResources).deleteVolumeGroup(volumeGroup, cloudRegion);
		
		aaiDeleteTasks.deleteVolumeGroup(execution);
	}
	
	@Test
	public void deleteConfigurationTest() throws Exception {
		gBBInput = execution.getGeneralBuildingBlock();
		doNothing().when(aaiConfigurationResources).deleteConfiguration(configuration);
		aaiDeleteTasks.deleteConfiguration(execution);
		verify(aaiConfigurationResources, times(1)).deleteConfiguration(configuration);
	}
}
