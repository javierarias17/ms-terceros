// Recursos de la aplicación dentro del resource group: ACR, Log Analytics (requerido por
// Container Apps), el entorno de Container Apps y la Container App del backend.
param location string
param acrName string
param containerAppsEnvName string
param containerAppName string
param containerImage string
param azureDocumentIntelligenceEndpoint string

@secure()
param azureDocumentIntelligenceKey string

param azureDocumentIntelligenceClassifierModelId string
param azureDocumentIntelligenceCedulaModelId string
param azureDocumentIntelligenceRutModelId string
param rutValidationTipoContribuyenteNatural string
param rutValidationTipoContribuyenteJuridica string

resource acr 'Microsoft.ContainerRegistry/registries@2023-07-01' = {
  name: acrName
  location: location
  sku: {
    name: 'Basic'
  }
  properties: {
    // Los entornos "Express" de Container Apps (los que trae por defecto una suscripción de
    // estudiante) no soportan autenticación al registro por identidad administrada, así que
    // aquí se usa el usuario/clave de administrador del propio ACR en vez de RBAC.
    adminUserEnabled: true
  }
}

resource logAnalytics 'Microsoft.OperationalInsights/workspaces@2023-09-01' = {
  name: '${containerAppsEnvName}-logs'
  location: location
  properties: {
    sku: {
      name: 'PerGB2018'
    }
    retentionInDays: 30
  }
}

resource containerAppsEnv 'Microsoft.App/managedEnvironments@2024-03-01' = {
  name: containerAppsEnvName
  location: location
  properties: {
    appLogsConfiguration: {
      destination: 'log-analytics'
      logAnalyticsConfiguration: {
        customerId: logAnalytics.properties.customerId
        sharedKey: logAnalytics.listKeys().primarySharedKey
      }
    }
  }
}

resource containerApp 'Microsoft.App/containerApps@2024-03-01' = {
  name: containerAppName
  location: location
  identity: {
    type: 'SystemAssigned'
  }
  properties: {
    managedEnvironmentId: containerAppsEnv.id
    configuration: {
      ingress: {
        external: true
        targetPort: 8080
        transport: 'auto'
      }
      registries: [
        {
          server: acr.properties.loginServer
          username: acr.listCredentials().username
          passwordSecretRef: 'acr-password'
        }
      ]
      secrets: [
        {
          name: 'doc-intel-key'
          value: azureDocumentIntelligenceKey
        }
        {
          name: 'acr-password'
          value: acr.listCredentials().passwords[0].value
        }
      ]
    }
    template: {
      containers: [
        {
          name: 'ms-tercero'
          image: containerImage
          resources: {
            cpu: json('0.5')
            memory: '1.0Gi'
          }
          env: [
            { name: 'AZURE_DOCUMENT_INTELLIGENCE_ENDPOINT', value: azureDocumentIntelligenceEndpoint }
            { name: 'AZURE_DOCUMENT_INTELLIGENCE_KEY', secretRef: 'doc-intel-key' }
            { name: 'AZURE_DOCUMENT_INTELLIGENCE_CLASSIFIER_MODEL_ID', value: azureDocumentIntelligenceClassifierModelId }
            { name: 'AZURE_DOCUMENT_INTELLIGENCE_CEDULA_MODEL_ID', value: azureDocumentIntelligenceCedulaModelId }
            { name: 'AZURE_DOCUMENT_INTELLIGENCE_RUT_MODEL_ID', value: azureDocumentIntelligenceRutModelId }
            { name: 'RUT_VALIDATION_TIPO_CONTRIBUYENTE_NATURAL', value: rutValidationTipoContribuyenteNatural }
            { name: 'RUT_VALIDATION_TIPO_CONTRIBUYENTE_JURIDICA', value: rutValidationTipoContribuyenteJuridica }
          ]
        }
      ]
      scale: {
        minReplicas: 0
        maxReplicas: 1
      }
    }
  }
}

output acrLoginServer string = acr.properties.loginServer
output containerAppFqdn string = containerApp.properties.configuration.ingress.fqdn
