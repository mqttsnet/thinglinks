export default {
  table: { title: 'ProductService list' },
  id: 'Id',
  productId: 'ProductId',
  serviceCode: 'ServiceCode',
  serviceName: 'ServiceName',
  serviceType: 'ServiceType',
  serviceStatus: 'ServiceStatus',
  description: 'Description',
  remark: 'Remark',
  createdTime: 'CreatedTime',
  createdBy: 'CreatedBy',
  updatedTime: 'UpdatedTime',
  updatedBy: 'UpdatedBy',
  createdOrgId: 'CreatedOrgId',
  serviceList: 'Services',
  attributeList: 'Properties',
  commandList: 'Commands',
  emptyService: 'No services yet, click + to add',
  pickServiceHint: 'Pick a service from the left panel',
  helpMessage: {
    serviceCode:
      'Supports lowercase English letters, numbers and underscores. All names should be in lowercase. Uppercase English letters are prohibited. Multiple words should be separated by underscores. Length [2,50]',
    description:
      'The text description does not affect the actual function and can be configured as an empty string””',
  },
};
