
USE catalogdb;

INSERT INTO northbound_request_ref_lookup(MACRO_ACTION, ACTION, REQUEST_SCOPE, IS_ALACARTE, MIN_API_VERSION, MAX_API_VERSION) VALUES
('Service-Create', 'createInstance', 'Service', true, '7','7'),
('Service-Delete', 'deleteInstance', 'Service', true, '7','7'),
('Service-Macro-Assign', 'assignInstance', 'Service', false, '7','7'),
('Service-Macro-Activate', 'activateInstance', 'Service', false, '7','7'),
('Service-Macro-Unassign', 'unassignInstance', 'Service', false, '7','7'),
('Service-Macro-Create', 'createInstance', 'Service', false, '7','7'),
('Service-Macro-Delete', 'deleteInstance', 'Service', false, '7','7'),
('Network-Create', 'createInstance', 'Network', true, '7','7'),
('Network-Delete', 'deleteInstance', 'Network', true, '7','7'),
('VNF-Macro-Recreate', 'replaceInstance', 'Vnf', false, '7','7'),
('VNF-Macro-Replace', 'internalReplace', 'Vnf', false, '7','7'),
('VNF-Create', 'createInstance', 'Vnf', true, '7', '7'),
('VNF-Delete', 'deleteInstance', 'Vnf', true, '7', '7'),
('VolumeGroup-Create', 'createInstance', 'VolumeGroup', true, '7','7'),
('VolumeGroup-Delete', 'deleteInstance', 'VolumeGroup', true, '7','7'),
('VFModule-Create', 'createInstance', 'VfModule', true, '7','7'),
('VFModule-Delete', 'deleteInstance', 'VfModule', true, '7','7'),
('NetworkCollection-Macro-Create', 'createInstance', 'NetworkCollection', false, '7','7'),
('NetworkCollection-Macro-Delete', 'deleteInstance', 'NetworkCollection', false, '7','7'),
('AVPNBonding-Macro-Delete', 'deleteInstance', 'BondingService', true, '7','7'),
('AVPNBonding-Macro-Create', 'createInstance', 'BondingService', true, '7','7');

INSERT INTO orchestration_flow_reference(COMPOSITE_ACTION, SEQ_NO, FLOW_NAME, FLOW_VERSION, NB_REQ_REF_LOOKUP_ID) VALUES
('Service-Create', '1', 'AssignServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Create')),
('Service-Create', '2', 'ActivateServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Create')),
('Service-Delete', '1', 'DeactivateServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Delete')),
('Service-Delete', '2', 'UnassignServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Delete')),
('Service-Macro-Assign', '1', 'AssignServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Assign')),
('Service-Macro-Assign', '2', 'AssignNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Assign')),
('Service-Macro-Assign', '3', 'AssignVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Assign')),
('Service-Macro-Assign', '4', 'AssignVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Assign')),
('Service-Macro-Assign', '5', 'AssignVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Assign')),
('Service-Macro-Activate', '1', 'CreateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate')),
('Service-Macro-Activate', '2', 'ActivateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate')),
('Service-Macro-Activate', '3', 'CreateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate')),
('Service-Macro-Activate', '4', 'ActivateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate')),
('Service-Macro-Activate', '5', 'CreateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate')),
('Service-Macro-Activate', '6', 'ActivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate')),
('Service-Macro-Activate', '7', 'ActivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate')),
('Service-Macro-Activate', '8', 'ActivateServiceInstance', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate')),
('Service-Macro-Unassign', '1', 'UnassignVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Unassign')),
('Service-Macro-Unassign', '2', 'UnassignVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Unassign')),
('Service-Macro-Unassign', '3', 'UnassignVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Unassign')),
('Service-Macro-Unassign', '4', 'UnassignNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Unassign')),
('Service-Macro-Unassign', '5', 'UnassignServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Unassign')),
('Service-Macro-Create', '1', 'AssignServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '2', 'CreateNetworkCollectionBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '3', 'AssignNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '4', 'AssignVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '5', 'AssignVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '6', 'AssignVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '7', 'CreateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '8', 'ActivateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '9', 'CreateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '10', 'ActivateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '11', 'CreateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '12', 'ActivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '13', 'ActivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '14', 'ActivateNetworkCollectionBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '15', 'ActivateServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Delete', '1', 'DeactivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '2', 'DeleteVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '3', 'DeactivateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '4', 'DeleteVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '5', 'DeactivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '6', 'DeactivateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '7', 'DeleteNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '8', 'DeactivateNetworkCollectionBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '9', 'DeleteNetworkCollectionBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '10', 'DeactivateServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '11', 'UnassignVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '12', 'UnassignVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '13', 'UnassignVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '14', 'UnassignNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '15', 'UnassignServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Network-Create', '1', 'AssignNetwork1802BB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Network-Create')),
('Network-Create', '2', 'CreateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Network-Create')),
('Network-Create', '3', 'ActivateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Network-Create')),
('Network-Delete', '1', 'DeactivateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Network-Delete')),
('Network-Delete', '2', 'DeleteNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Network-Delete')),
('Network-Delete', '3', 'UnassignNetwork1802BB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Network-Delete')),
('VNF-Create', '1', 'AssignVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Create')),
('VNF-Create', '2', 'ActivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Create')),
('VNF-Delete', '1', 'DeactivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Delete')),
('VNF-Delete', '2', 'UnassignVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Delete')),
('VNF-Macro-Recreate', '1', 'AAICheckVnfInMaintBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Recreate')),
('VNF-Macro-Recreate', '2', 'AAISetVnfInMaintBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Recreate')),
('VNF-Macro-Recreate', '3', 'VNF-Macro-Replace', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Recreate')),
('VNF-Macro-Recreate', '4', 'SDNOVnfHealthCheckBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Recreate')),
('VNF-Macro-Recreate', '5', 'AAIUnsetVnfInMaintBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Recreate')),
('VNF-Macro-Replace', '1', 'DeactivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace')),
('VNF-Macro-Replace', '2', 'DeleteVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace')),
('VNF-Macro-Replace', '3', 'DeactivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace')),
('VNF-Macro-Replace', '4', 'CreateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace')),
('VNF-Macro-Replace', '5', 'ActivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace')),
('VNF-Macro-Replace', '6', 'ActivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace')),
('VolumeGroup-Create', '1', 'AssignVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VolumeGroup-Create')),
('VolumeGroup-Create', '2', 'CreateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VolumeGroup-Create')),
('VolumeGroup-Create', '3', 'ActivateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VolumeGroup-Create')),
('VolumeGroup-Delete', '1', 'DeactivateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VolumeGroup-Delete')),
('VolumeGroup-Delete', '2', 'DeleteVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VolumeGroup-Delete')),
('VolumeGroup-Delete', '3', 'UnassignVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VolumeGroup-Delete')),
('VFModule-Create', '1', 'AssignVfModuleBB',  1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Create')),
('VFModule-Create', '2', 'CreateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Create')),
('VFModule-Create', '3', 'ActivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Create')),
('VFModule-Delete', '1', 'DeactivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Delete')),
('VFModule-Delete', '2', 'DeleteVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Delete')),
('VFModule-Delete', '3', 'UnassignVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Delete')),
('NetworkCollection-Macro-Create', '1', 'CreateNetworkCollectionBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Create')),
('NetworkCollection-Macro-Create', '2', 'AssignNetworkBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Create')),
('NetworkCollection-Macro-Create', '3', 'CreateNetworkBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Create')),
('NetworkCollection-Macro-Create', '4', 'ActivateNetworkBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Create')),
('NetworkCollection-Macro-Create', '5', 'ActivateNetworkCollectionBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Create')),
('NetworkCollection-Macro-Delete', '1', 'DeactivateNetworkBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Delete')),
('NetworkCollection-Macro-Delete', '2', 'DeleteNetworkBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Delete')),
('NetworkCollection-Macro-Delete', '3', 'UnassignNetworkBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Delete')),
('NetworkCollection-Macro-Delete', '4', 'DeactivateNetworkCollectionBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Delete')),
('NetworkCollection-Macro-Delete', '5', 'DeleteNetworkCollectionBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Delete')),
('AVPNBonding-Macro-Delete','1','DeactivateServiceInstanceBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'AVPNBonding-Macro-Delete')),
('AVPNBonding-Macro-Delete','2','DeactivateAndUnassignVpnBondingLinksBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'AVPNBonding-Macro-Delete')),
('AVPNBonding-Macro-Delete','3','UnassignServiceInstanceBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'AVPNBonding-Macro-Delete')),
('AVPNBonding-Macro-Create','1','SniroHoming',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'AVPNBonding-Macro-Create')),
('AVPNBonding-Macro-Create','2','CreateCustomerVpnBindingBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'AVPNBonding-Macro-Create')),
('AVPNBonding-Macro-Create','3','AvpnAssignServiceInstanceBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'AVPNBonding-Macro-Create')),
('AVPNBonding-Macro-Create','4','AssignAndActivateVpnBondingLinksBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'AVPNBonding-Macro-Create')),
('AVPNBonding-Macro-Create','5','ActivateServiceInstanceBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'AVPNBonding-Macro-Create'));
