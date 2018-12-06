package org.onap.so.bpmn.servicedecomposition.tasks;

import java.util.List;
import java.util.Optional;

import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.client.aai.AAICommonObjectMapperProvider;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.Relationships;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

@Component
public class CloudInfoFromAAI {

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, CloudInfoFromAAI.class);
	@Autowired
	private BBInputSetupUtils bbInputSetupUtils;
	
	public void setBbInputSetupUtils(BBInputSetupUtils bbInputSetupUtils) {
		this.bbInputSetupUtils = bbInputSetupUtils;
	}
	
	protected Optional<CloudRegion> getCloudInfoFromAAI(ServiceInstance serviceInstance) throws JsonProcessingException {
		Optional<Relationships> relationshipsOp = Optional.empty();
		if(!serviceInstance.getVnfs().isEmpty()) {
			GenericVnf vnf = serviceInstance.getVnfs().get(0);
			org.onap.aai.domain.yang.GenericVnf aaiVnf = bbInputSetupUtils.getAAIGenericVnf(vnf.getVnfId());
			AAIResultWrapper vnfWrapper = new AAIResultWrapper(
					new AAICommonObjectMapperProvider().getMapper().writeValueAsString(aaiVnf));
			relationshipsOp = getRelationshipsFromWrapper(vnfWrapper);
		} else if(!serviceInstance.getNetworks().isEmpty()) {
			L3Network network = serviceInstance.getNetworks().get(0);
			org.onap.aai.domain.yang.L3Network aaiL3Network = bbInputSetupUtils.getAAIL3Network(network.getNetworkId());
			AAIResultWrapper networkWrapper = new AAIResultWrapper(
					new AAICommonObjectMapperProvider().getMapper().writeValueAsString(aaiL3Network));
			relationshipsOp = getRelationshipsFromWrapper(networkWrapper);
		} else {
			msoLogger.debug("BBInputSetup could not find a cloud region or tenant, since there are no resources under the SI.");
			return Optional.empty();
		}
		if (relationshipsOp.isPresent()) {
			return getRelatedCloudRegionAndTenant(relationshipsOp.get());
		} else {
			msoLogger.debug("BBInputSetup could not find a cloud region or tenant");
			return Optional.empty();
		}
	}

	protected Optional<Relationships> getRelationshipsFromWrapper(AAIResultWrapper wrapper) {
		Optional<Relationships> relationshipsOp;
		relationshipsOp = wrapper.getRelationships();
		if(relationshipsOp.isPresent()) {
			return relationshipsOp;
		}
		return Optional.empty();
	}

	protected Optional<CloudRegion> getRelatedCloudRegionAndTenant(Relationships relationships) {
		CloudRegion cloudRegion = new CloudRegion();
		List<AAIResultWrapper> cloudRegions = relationships.getByType(AAIObjectType.CLOUD_REGION);
		List<AAIResultWrapper> tenants = relationships.getByType(AAIObjectType.TENANT);
		if(!cloudRegions.isEmpty()) {
			AAIResultWrapper cloudRegionWrapper = cloudRegions.get(0);
			Optional<org.onap.aai.domain.yang.CloudRegion> aaiCloudRegionOp = cloudRegionWrapper
					.asBean(org.onap.aai.domain.yang.CloudRegion.class);
			if(aaiCloudRegionOp.isPresent()) {
				org.onap.aai.domain.yang.CloudRegion aaiCloudRegion = aaiCloudRegionOp.get();
				cloudRegion.setCloudOwner(aaiCloudRegion.getCloudOwner());
				cloudRegion.setCloudRegionVersion(aaiCloudRegion.getCloudRegionVersion());
				cloudRegion.setLcpCloudRegionId(aaiCloudRegion.getCloudRegionId());
				cloudRegion.setComplex(aaiCloudRegion.getComplexName());
			}
		}
		if(!tenants.isEmpty()) {
			AAIResultWrapper tenantWrapper = tenants.get(0);
			Optional<org.onap.aai.domain.yang.Tenant> aaiTenantOp = tenantWrapper
					.asBean(org.onap.aai.domain.yang.Tenant.class);
			if(aaiTenantOp.isPresent()) {
				org.onap.aai.domain.yang.Tenant aaiTenant = aaiTenantOp.get();
				cloudRegion.setTenantId(aaiTenant.getTenantId());
			}
		}
		return Optional.of(cloudRegion);
	}
}
